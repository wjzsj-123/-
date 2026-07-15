package com.example.demo.demos.web.service.impl;

import com.example.demo.demos.web.mapper.PaperMapper;
import com.example.demo.demos.web.mapper.UserAnswerMapper;
import com.example.demo.demos.web.pojo.*;
import com.example.demo.demos.web.service.FillAnswerService;
import com.example.demo.demos.web.service.PaperQuestionService;
import com.example.demo.demos.web.service.PaperService;
import com.example.demo.demos.web.service.QuestionOptionService;
import com.example.demo.demos.web.service.UserMessageService;
import com.example.demo.demos.web.redis.AnswerDraftRedisService;
import com.example.demo.demos.web.redis.HotRankService;
import com.example.demo.demos.web.redis.RedisCacheHelper;
import com.example.demo.demos.web.redis.RedisKeyConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PaperServiceImpl implements PaperService {

    @Resource
    private PaperMapper paperMapper;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Resource
    private UserAnswerMapper userAnswerMapper;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private FillAnswerService fillAnswerService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private AnswerDraftRedisService answerDraftRedisService;

    @Resource
    private RedisCacheHelper redisCacheHelper;

    @Resource
    private HotRankService hotRankService;

    private void evictPublicPaperCache() {
        redisCacheHelper.evictByPrefix(RedisKeyConstants.PUBLIC_PAPER_LIST_PREFIX);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addPaper(Paper paper) {
        if (paper.getTitle() == null || paper.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("试卷标题不能为空");
        }
        if (paper.getUserId() == null) {
            throw new IllegalArgumentException("创建者ID不能为空");
        }
        if (paper.getIsShared() == null) {
            paper.setIsShared(false);
        }
        paper.setCreateTime(LocalDateTime.now()); // 自动设置创建时间
        return paperMapper.insert(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removePaper(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        // 先删除关联的题目关系
        paperQuestionService.removePaperQuestionsByPaperId(id);
        // 再删除试卷本身
        return paperMapper.deleteById(id);
    }

    @Override
    public int modifyPaper(Paper paper) {
        if (paper.getId() == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperMapper.updateById(paper);
    }

    @Override
    public Paper getPaperById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        return paperMapper.selectById(id);
    }

    @Override
    public Paper getPaperWithQuestionsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        Paper paper = paperMapper.selectWithQuestionsById(id);
        if (paper != null) {
            // 补充查询关联的题目列表
            List<PaperQuestion> paperQuestions = paperQuestionService.getPaperQuestionsByPaperId(id);
            paper.setPaperQuestions(paperQuestions);
        }
        return paper;
    }

    @Override
    public List<Paper> getPapersByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return paperMapper.selectByUserId(userId);
    }

    @Override
    public List<Paper> getAllPapers() {
        return paperMapper.selectAll();
    }

    @Override
    public Paper getPaperWithQuestions(Long paperId) {
        if (paperId == null) {
            throw new IllegalArgumentException("试卷ID不能为空");
        }
        // 获取包含题目的试卷
        Paper paper = paperMapper.selectWithQuestionsById(paperId);
        if (paper == null) {
            return null;
        }
        // 补充题目选项（选择题）或空答案（填空题）
        for (Question question : paper.getQuestions()) {
            if (question.getType() == 1) { // 假设1是选择题
                List<QuestionOption> options = questionOptionService.getOptionsByQuestionId(question.getId());
                question.setOptions(options);
            } else if (question.getType() == 2) { // 假设2是填空题
                List<FillAnswer> fillAnswers = fillAnswerService.getFillAnswersByQuestionId(question.getId());
                question.setFillAnswers(fillAnswers);
            }
        }
        return paper;
    }

    @Transactional
    @Override
    public void submitUserAnswers(Long paperId, Long userId, List<UserAnswer> answers) {
        if (paperId == null || userId == null || answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("参数不完整");
        }
        // 1. 删除该用户之前的提交记录（避免重复）
        userAnswerMapper.deleteByPaperIdAndUserId(paperId, userId);

        // 2. 批量保存新答案并判分
        for (UserAnswer answer : answers) {
            answer.setPaperId(paperId);
            answer.setUserId(userId);
            answer.setSubmitTime(LocalDateTime.now());
            answer.setUpdateTime(LocalDateTime.now());

            // 3. 判分逻辑
            judgeAnswer(answer);

            userAnswerMapper.insert(answer);
        }
        answerDraftRedisService.clearDraft(paperId, userId);
    }

    @Override
    public List<UserAnswer> getUserAnswers(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        return userAnswerMapper.selectByPaperIdAndUserId(paperId, userId);
    }

    @Transactional
    @Override
    public void saveAnswerDraft(Long paperId, Long userId, List<UserAnswer> answers) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        answerDraftRedisService.saveDraft(paperId, userId, answers);
        List<UserAnswer> cached = answerDraftRedisService.getDraft(paperId, userId);
        if (cached.isEmpty() && answers != null && !answers.isEmpty()) {
            saveAnswerDraftToDb(paperId, userId, answers);
        }
    }

    private void saveAnswerDraftToDb(Long paperId, Long userId, List<UserAnswer> answers) {
        userAnswerMapper.deleteDraftByPaperIdAndUserId(paperId, userId);
        for (UserAnswer answer : answers) {
            answer.setPaperId(paperId);
            answer.setUserId(userId);
            answer.setIsCorrect(null);
            answer.setUpdateTime(LocalDateTime.now());
            userAnswerMapper.insert(answer);
        }
    }

    @Override
    public List<UserAnswer> getAnswerDraft(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("试卷ID和用户ID不能为空");
        }
        List<UserAnswer> cached = answerDraftRedisService.getDraft(paperId, userId);
        if (!cached.isEmpty()) {
            return cached;
        }
        return userAnswerMapper.selectDraftByPaperIdAndUserId(paperId, userId);
    }

    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return paperMapper.countByUserId(userId); // 需要在PaperMapper中添加对应方法
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Paper sharePaper(Long paperId, Long userId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }
        if (!paper.getUserId().equals(userId)) {
            throw new IllegalArgumentException("只能分享自己的试卷");
        }
        if (paper.getShareCode() == null || paper.getShareCode().trim().isEmpty()) {
            paper.setShareCode(UUID.randomUUID().toString().replace("-", ""));
        }
        boolean wasShared = Boolean.TRUE.equals(paper.getIsShared());
        paper.setIsShared(true);
        paperMapper.updateById(paper);
        if (!wasShared) {
            evictPublicPaperCache();
            hotRankService.setPaperHotScore(paperId, 0);
            userMessageService.notifyFollowersNewSharedPaper(userId, paperId, paper.getTitle());
        }
        return paperMapper.selectById(paperId);
    }

    @Override
    public List<Paper> getSharedPapers() {
        List<Paper> papers = paperMapper.selectShared();
        enrichQuestionTags(papers);
        return papers;
    }

    @Override
    public Map<String, Object> getPublicPapers(String name, String sortBy, Long currentUserId, Integer page, Integer size) {
        if (name != null && !name.trim().isEmpty()) {
            name = "%" + name.trim() + "%";
        } else {
            name = null;
        }
        if (!"hot".equals(sortBy)) {
            sortBy = "publishTime";
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 10;
        }
        final int pageFinal = page;
        final int sizeFinal = size;
        final String nameFinal = name;
        final String sortByFinal = sortBy;
        String cacheKey = RedisKeyConstants.publicPaperList(nameFinal, sortByFinal, pageFinal, sizeFinal, currentUserId);
        return redisCacheHelper.getOrLoad(cacheKey, () -> loadPublicPapers(nameFinal, sortByFinal,
                currentUserId, pageFinal, sizeFinal), 5, TimeUnit.MINUTES);
    }

    private Map<String, Object> loadPublicPapers(String name, String sortBy, Long currentUserId, int page, int size) {
        if ("hot".equals(sortBy) && name == null && hotRankService.countPaperRank() > 0) {
            Map<String, Object> hotResult = loadPublicPapersFromHotRank(currentUserId, page, size);
            if (hotResult != null) {
                return hotResult;
            }
        }
        int offset = (page - 1) * size;
        List<Paper> papers = paperMapper.selectPublicPapers(name, sortBy, currentUserId, offset, size);
        enrichQuestionTags(papers);
        int total = paperMapper.countPublicPapers(name);
        Map<String, Object> result = new HashMap<>();
        result.put("list", papers);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    private Map<String, Object> loadPublicPapersFromHotRank(Long currentUserId, int page, int size) {
        int offset = (page - 1) * size;
        List<Long> hotIds = hotRankService.getTopPaperIds(offset, size);
        if (hotIds.isEmpty()) {
            return null;
        }
        List<Paper> papers = paperMapper.selectPublicPapersByIds(hotIds, currentUserId);
        if (papers.isEmpty()) {
            return null;
        }
        Map<Long, Paper> byId = new LinkedHashMap<>();
        for (Paper paper : papers) {
            byId.put(paper.getId(), paper);
        }
        List<Paper> ordered = new ArrayList<>();
        for (Long id : hotIds) {
            Paper paper = byId.get(id);
            if (paper != null) {
                ordered.add(paper);
            }
        }
        enrichQuestionTags(ordered);
        int total = paperMapper.countPublicPapers(null);
        Map<String, Object> result = new HashMap<>();
        result.put("list", ordered);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Paper copySharedPaper(Long paperId, Long userId) {
        Paper source = paperMapper.selectById(paperId);
        if (source == null || source.getIsShared() == null || !source.getIsShared()) {
            throw new IllegalArgumentException("在线试卷不存在或未分享");
        }

        Paper target = new Paper();
        target.setUserId(userId);
        target.setTitle(source.getTitle() + "（副本）");
        target.setDescription(source.getDescription());
        target.setCategory(source.getCategory());
        target.setTotalQuestions(source.getTotalQuestions());
        target.setCreateTime(LocalDateTime.now());
        target.setIsShared(false);
        target.setSourcePaperId(source.getId());
        addPaper(target);

        List<Long> sourceQuestionIds = paperQuestionService.getQuestionIdsByPaperId(source.getId());
        List<PaperQuestion> relations = new ArrayList<>();
        for (int i = 0; i < sourceQuestionIds.size(); i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(target.getId());
            pq.setQuestionId(sourceQuestionIds.get(i));
            pq.setSortOrder(i + 1);
            relations.add(pq);
        }
        if (!relations.isEmpty()) {
            paperQuestionService.batchAdd(relations);
        }
        hotRankService.incrementPaperHot(source.getId(), 3);
        evictPublicPaperCache();
        return paperMapper.selectById(target.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Paper updatePaperPublicStatus(Long paperId, Long userId, Boolean isPublic) {
        if (paperId == null || userId == null || isPublic == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }
        if (!userId.equals(paper.getUserId())) {
            throw new IllegalArgumentException("只能修改自己的试卷可见性");
        }
        boolean wasShared = Boolean.TRUE.equals(paper.getIsShared());
        paper.setIsShared(isPublic);
        if (isPublic && (paper.getShareCode() == null || paper.getShareCode().trim().isEmpty())) {
            paper.setShareCode(UUID.randomUUID().toString().replace("-", ""));
        }
        if (!isPublic) {
            paper.setShareCode(null);
        }
        paperMapper.updateById(paper);
        if (Boolean.TRUE.equals(isPublic) && !wasShared) {
            evictPublicPaperCache();
            hotRankService.setPaperHotScore(paperId, 0);
            userMessageService.notifyFollowersNewSharedPaper(userId, paperId, paper.getTitle());
        }
        return paperMapper.selectById(paperId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPaperForRetry(Long paperId, Long userId) {
        if (paperId == null || userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new IllegalArgumentException("试卷不存在");
        }
        if (!userId.equals(paper.getUserId())) {
            throw new IllegalArgumentException("只能重做自己的试卷");
        }
        userAnswerMapper.deleteByPaperIdAndUserId(paperId, userId);
        answerDraftRedisService.clearDraft(paperId, userId);
        paper.setIsAnswered(false);
        paper.setLastAnswerTime(null);
        paperMapper.updateById(paper);
    }

    private void enrichQuestionTags(List<Paper> papers) {
        for (Paper paper : papers) {
            Paper detail = paperMapper.selectWithQuestionsById(paper.getId());
            if (detail == null) {
                paper.setQuestionTags(new ArrayList<>());
                continue;
            }
            List<String> rawTags = detail.getQuestions().stream()
                    .map(Question::getTag)
                    .collect(Collectors.toList());
            paper.setQuestionTags(mergeTags(rawTags));
        }
    }

    private List<String> mergeTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String rawTag : rawTags) {
            if (rawTag == null || rawTag.trim().isEmpty()) {
                continue;
            }
            Arrays.stream(rawTag.split("[,，]"))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .forEach(unique::add);
        }
        return new ArrayList<>(unique);
    }

    // 辅助方法：判分逻辑
    private void judgeAnswer(UserAnswer answer) {
        if (answer.getQuestionType() == 1) { // 选择题（兼容单选/多选）
            List<QuestionOption> correctOptions = questionOptionService.getCorrectOptionsByQuestionId(answer.getQuestionId());
            if (correctOptions.isEmpty()) {
                answer.setIsCorrect(0); // 无正确选项配置，视为错误
                return;
            }
            // 多选题需匹配所有正确选项ID（假设用户答案存储为逗号分隔的选项ID字符串）
            // 此处需根据实际存储格式调整（当前UserAnswer用Long存储单个选项ID，可能需要扩展为List<Long>）
            boolean isAllCorrect = correctOptions.stream()
                    .allMatch(option -> option.getId().equals(answer.getChoiceOptionId()));
            answer.setIsCorrect(isAllCorrect ? 1 : 0);
        } else if (answer.getQuestionType() == 2) { // 填空题
            if (answer.getSortOrder() == null) {
                answer.setIsCorrect(0); // 缺少排序序号，视为错误
                return;
            }
            FillAnswer correctAnswer = fillAnswerService.getFillAnswerByQuestionIdAndSortOrder(
                    answer.getQuestionId(), answer.getSortOrder());
            // 处理用户答案为空的情况
            String userAnswer = answer.getFillContent();
            if (correctAnswer != null && correctAnswer.getAnswer() != null
                    && correctAnswer.getAnswer().equals(userAnswer)) {
                answer.setIsCorrect(1);
            } else {
                answer.setIsCorrect(0);
            }
        }
    }
}
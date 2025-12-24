<template>
  <div class="question-set-form">
    <h2>{{ isEdit ? '编辑题库' : '新增题库' }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="name">题库名称 <span class="required">*</span></label>
        <input
            type="text"
            id="name"
            v-model="form.name"
            required
            placeholder="请输入题库名称"
        >
      </div>

      <div class="form-group">
        <label for="category">分类 <span class="required">*</span></label>
        <select id="category" v-model="form.category" required>
          <option value="">请选择分类</option>
          <option v-for="category in categories" :key="category" :value="category">
            {{ category }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="description">描述</label>
        <textarea
            id="description"
            v-model="form.description"
            rows="4"
            placeholder="请输入题库描述（可选）"
        ></textarea>
      </div>

      <div class="form-actions">
        <button type="button" class="cancel-btn" @click="handleCancel">取消</button>
        <button type="submit" class="submit-btn">保存</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const isEdit = ref(!!route.params.id); // 根据是否有ID判断是编辑还是新增
const categories = ref(['编程语言', '数据库', '操作系统', '计算机网络']);

// 表单数据
const form = ref({
  id: null,
  name: '',
  category: '',
  description: ''
});

// 获取题库详情（编辑时）
const fetchQuestionSetDetail = async () => {
  try {
    const response = await fetch(`/api/question-set/${route.params.id}`);
    const result = await response.json();

    if (result.code === 0) {
      form.value = result.data;
    } else {
      alert(result.message || '获取题库详情失败');
      router.back();
    }
  } catch (err) {
    console.error('获取题库详情失败:', err);
    alert('网络错误，请稍后重试');
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    // 添加用户ID（从localStorage获取）
    const userInfo = JSON.parse(localStorage.getItem('userInfo'));
    const submitData = { ...form.value, userId: userInfo.id };

    let url = '/api/question-set';
    let method = 'POST';

    // 编辑模式
    if (isEdit.value) {
      method = 'PUT';
    }

    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(submitData)
    });

    const result = await response.json();

    if (result.code === 0) {
      alert(isEdit.value ? '更新成功' : '创建成功');
      router.push('/home/question-set');
    } else {
      alert(result.message || (isEdit.value ? '更新失败' : '创建失败'));
    }
  } catch (err) {
    console.error('提交表单失败:', err);
    alert('网络错误，请稍后重试');
  }
};

// 取消操作
const handleCancel = () => {
  router.back();
};

// 编辑模式下加载数据
onMounted(() => {
  if (isEdit.value) {
    fetchQuestionSetDetail();
  }
});
</script>

<style scoped>
.question-set-form {
  max-width: 600px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #666;
}

.required {
  color: #ea4335;
}

input, select, textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn {
  padding: 8px 16px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
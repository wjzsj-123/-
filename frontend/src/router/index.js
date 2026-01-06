import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Home from '@/views/Home.vue'
import HomeContent from '@/views/HomeContent.vue'
import QuestionSet from '@/views/QuestionSet.vue'
import Paper from '@/views/Paper.vue'
import UserProfile from '@/views/UserProfile.vue'
import GeneratePaper from "@/views/GeneratePaper.vue";
import PaperDetail from "@/views/PaperDetail.vue";
import PaperResult from "@/views/PaperResult.vue";

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    {
        path: '/home',  // 父路由路径
        component: Home,
        children: [
            { path: '', name: 'Home', component: HomeContent },
            // 题库路由
            {
                path: 'question-set',  // 对应完整路径 /home/question-set
                component: () => import('@/views/QuestionSetList.vue'),
                meta: { requiresAuth: true }
            },
            // 添加题库路由
            {
                path: 'question-set/add',
                component: () => import('@/views/QuestionSetForm.vue'),
                meta: { requiresAuth: true }
            },
            // 题库详情页（含题目管理）
            {
                path: 'question-set/:id',
                component: () => import('@/views/QuestionSet.vue'),
                meta: { requiresAuth: true }
            },
            { path: 'paper', component: Paper },
            // 添加生成试卷路由
            {
                path: 'paper/generate',
                component: GeneratePaper,
                meta: { requiresAuth: true }
            },
            // 答题页面路由
            {
                path: 'paper/answer/:paperId',
                component: () => import('@/views/PaperDetail.vue'),
                name: 'PaperDetail',
                meta: { requiresAuth: true }
            },
            // 查看答案页面路由
            {
                path: 'paper/result/:paperId',
                component: () => import('@/views/PaperResult.vue'),
                name: 'PaperResult',
                meta: { requiresAuth: true }
            },
            // 在线题库
            {
                path: 'online-bank', // home 子路径：/home/online-bank
                component: () => import('@/views/OnlineQuestionBank.vue'),
                name: 'OnlineQuestionBank',
                meta: { requiresAuth: true }
            },
            { path: 'user/profile', component: UserProfile }
        ]
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
    const userInfo = localStorage.getItem('userInfo');
    const requireAuthPaths = [
        '/home',
        '/home/question-set',
        '/home/paper',
        '/home/paper/generate',
        '/home/paper/answer/:paperId',
        '/home/paper/result/:paperId',
        '/home/user/profile',
        '/home/online-bank',
    ];

    if (requireAuthPaths.includes(to.path)) {
        userInfo ? next() : next('/login');
    } else {
        if ((to.path === '/login' || to.path === '/register') && userInfo) {
            next('/home');
        } else {
            next();
        }
    }
});

export default router
import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Home from '@/views/Home.vue'
import HomeContent from '@/views/HomeContent.vue'
import QuestionSet from '@/views/QuestionSet.vue'
import Paper from '@/views/Paper.vue'
import UserProfile from '@/views/UserProfile.vue'
import GeneratePaper from "@/views/GeneratePaper.vue";

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    {
        path: '/home',  // 父路由路径
        component: Home,
        children: [
            { path: '', name: 'Home', component: HomeContent },
            // 子路由路径不需要带 /home 前缀（相对路径）
            {
                path: 'question-set',  // 对应完整路径 /home/question-set
                component: () => import('@/views/QuestionSetList.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'question-set/add',  // 对应完整路径 /home/question-set/add
                component: () => import('@/views/QuestionSetForm.vue'),
                meta: { requiresAuth: true }
            },
            {
                path: 'question-set/:id', // 题库详情页（含题目管理）
                component: () => import('@/views/QuestionSet.vue'),
                meta: { requiresAuth: true }
            },
            { path: 'paper', component: Paper },
            // 添加生成试卷路由（作为home的子路由，保持路径一致性）
            {
                path: 'paper/generate',
                component: GeneratePaper,
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
        '/home/paper/generate',  // 新增路径
        '/home/user/profile'
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
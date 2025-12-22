import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Home from '@/views/Home.vue'
import HomeContent from '@/views/HomeContent.vue'
import QuestionSet from '@/views/QuestionSet.vue'
import Paper from '@/views/Paper.vue'
import UserProfile from '@/views/UserProfile.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    {
        path: '/home',
        component: Home,
        children: [
            { path: '', name: 'Home', component: HomeContent },
            { path: 'question-set', component: QuestionSet },
            { path: 'paper', component: Paper },
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
    const requireAuthPaths = ['/home', '/home/question-set', '/home/paper', '/home/user/profile'];

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
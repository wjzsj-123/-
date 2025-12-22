// static/router/index.js
// 适配 CDN 全局变量：从 VueRouter 全局对象中获取方法
const { createRouter, createWebHistory } = VueRouter;

// 导入组件：使用相对路径（基于 static 目录，./views/ 对应 static/views/）
const Login = () => import('../views/Login.vue');
const Register = () => import('../views/Register.vue');
const Home = () => import('../views/Home.vue');
const HomeContent = () => import('../views/HomeContent.vue');
// 其他组件同理
const QuestionSet = () => import('../views/QuestionSet.vue');
const Paper = () => import('../views/Paper.vue');
const UserProfile = () => import('../views/UserProfile.vue');

// 路由配置（修正后，兼容 Spring Boot 静态资源）
const routes = [
    {
        path: '/',
        redirect: '/login' // 默认访问根路径跳转到登录页
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        // 嵌套路由：子路径使用相对路径
        children: [
            {
                path: '', // 匹配 /home
                component: HomeContent
            },
            {
                path: 'question-set', // 匹配 /home/question-set
                component: QuestionSet
            },
            {
                path: 'paper', // 匹配 /home/paper
                component: Paper
            },
            {
                path: 'user/profile', // 匹配 /home/user/profile
                component: UserProfile
            }
        ]
    }
];

// 创建路由实例：使用 createWebHistory，base 适配 Spring Boot 根路径
const router = createRouter({
    history: createWebHistory(), // 无需配置 base，Spring Boot 已映射 / 到 static
    routes
});

// 全局路由守卫：统一校验登录状态
router.beforeEach((to, from, next) => {
    const userInfo = localStorage.getItem('userInfo');
    // 需要登录的路径（适配嵌套路由路径）
    const requireAuthPaths = ['/home', '/home/question-set', '/home/paper', '/home/user/profile'];

    if (requireAuthPaths.includes(to.path)) {
        // 需要登录：有用户信息放行，否则跳登录页
        userInfo ? next() : next('/login');
    } else {
        // 无需登录：已登录用户访问登录/注册页，跳首页
        if ((to.path === '/login' || to.path === '/register') && userInfo) {
            next('/home');
        } else {
            next();
        }
    }
});

// 导出路由实例（供 main.js 导入）
export default router;
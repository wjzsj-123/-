// router/index.js
const { createRouter, createWebHashHistory } = VueRouter; // 改为 Hash 模式

// 登录/注册组件（保持不变，无需修改）
const Login = {
    template: `
    <div class="login-container">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-item">
          <label>用户名：</label>
          <input type="text" v-model="username" placeholder="请输入用户名" required>
        </div>
        <div class="form-item">
          <label>密码：</label>
          <input type="password" v-model="password" placeholder="请输入密码" required>
        </div>
        <button type="submit" class="login-btn">登录</button>
        <p class="switch-btn">还没有账号？<a href="#/register">去注册</a></p>
      </form>
    </div>
  `,
    data() {
        return {
            username: '',
            password: ''
        };
    },
    methods: {
        async handleLogin() {
            try {
                const res = await axios.post('/api/user/login', {
                    username: this.username,
                    password: this.password
                });
                if (res.data.code === 0) {
                    alert('登录成功！');
                    localStorage.setItem('userInfo', JSON.stringify(res.data.data));
                } else {
                    alert(res.data.message);
                }
            } catch (err) {
                alert('登录失败：' + err.message);
            }
        }
    }
};

const Register = {
    template: `
    <div class="login-container">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-item">
          <label>用户名：</label>
          <input type="text" v-model="username" placeholder="请设置用户名" required>
        </div>
        <div class="form-item">
          <label>密码：</label>
          <input type="password" v-model="password" placeholder="请设置密码" required>
        </div>
        <div class="form-item">
          <label>昵称：</label>
          <input type="text" v-model="nickname" placeholder="请设置昵称（选填）">
        </div>
        <button type="submit" class="login-btn">注册</button>
        <p class="switch-btn">已有账号？<a href="#/login">去登录</a></p>
      </form>
    </div>
  `,
    data() {
        return {
            username: '',
            password: '',
            nickname: ''
        };
    },
    methods: {
        async handleRegister() {
            try {
                if (this.password.length < 6) {
                    alert('密码长度不能少于6位！');
                    return;
                }
                const res = await axios.post('/api/user/register', {
                    username: this.username,
                    password: this.password,
                    nickname: this.nickname
                });
                if (res.data.code === 0) {
                    alert('注册成功！请登录');
                    window.location.href = '#/login'; // 锚点跳转兼容 Hash 模式
                } else {
                    alert(res.data.message);
                }
            } catch (err) {
                alert('注册失败：' + err.message);
            }
        }
    }
};

// 路由配置（保持不变）
const routes = [
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/', redirect: '/login' }
];

// 创建路由实例（使用 Hash 模式）
window.appRouter = createRouter({
    history: createWebHashHistory(), // 关键：改为 Hash 模式
    routes
});

// 路由守卫（保持不变）
window.appRouter.beforeEach((to, from, next) => {
    if (to.matched.some(record => record.meta.requiresAuth)) {
        const userInfo = localStorage.getItem('userInfo');
        if (!userInfo) {
            next({ path: '/login', query: { redirect: to.fullPath } });
        } else {
            next();
        }
    } else {
        next();
    }
});
import Login from '@/views/login'
import Home from '@/views/home'
import Index from '@/views/index/index'
import ErrorPage404 from '@/views/error-page/404.vue'
import ErrorPage403 from '@/views/error-page/403.vue'
import ErrorPage500 from '@/views/error-page/500.vue'
import Customer from '@/views/customer/customer-manager.vue'

export const customer = {
  path:'/customer/:userId?',
  name:'customer',
  meta:{
     title:'customer - customer'
  },
  component:Customer
}



// 不作为Main组件的子页面展示的页面单独写，如下
export const loginRouter = {
  path: '/login',
  name: 'login',
  meta: {
    title: 'Login - 登录'
  },
  component: Login
}

export const page404 = {
  path: '/404',
  name: 'error-404',
  meta: {
    title: '404-页面不存在'
  },
  component: ErrorPage404
}

export const page403 = {
  path: '/403',
  meta: {
    title: '403-权限不足'
  },
  name: 'error-403',
  component: ErrorPage403
}

export const page500 = {
  path: '/500',
  meta: {
    title: '500-服务端错误'
  },
  name: 'error-500',
  component: ErrorPage500
}



// 作为Main组件的子页面展示但是不在左侧菜单显示的路由写在otherRouter里
export const otherRouter = {
  path: '/',
  name: 'otherRouter',
  title: {i18n: 'message.home.title'},
  redirect: '/home',
  component: Home,
  children: [
    {path: 'home', title: {i18n: 'message.home.title'}, name: 'home_index', component: Index}
  ]
}
// 所有上面定义的路由都要写在下面的routers里
export const routers = [
  loginRouter,
  otherRouter,
  page500,
  page403,
  page404,
  customer
]

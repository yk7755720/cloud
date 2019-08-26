import Vue from 'vue'
import Router from 'vue-router'
import util from '@/libs/util'
import { routers, otherRouter } from './router'
import iView from 'iview'
import Cookies from 'js-cookie'
import store from '@/store'

Vue.use(Router)

// 路由配置
const RouterConfig = {
  routes: routers
}

export const router = new Router(RouterConfig)

router.beforeEach((to, from, next) => {
    iView.LoadingBar.start()
    if (store.getters.appRouter.length == 0 && sessionStorage.getItem('appRouter')) {
      let appRouter = JSON.parse(sessionStorage.getItem('appRouter'))
      let myRouter = util.generateAppRouter(appRouter)  
      store.commit('setAppRouter', myRouter)
      router.addRoutes(myRouter)
      store.commit('updateMenulist')
      let tagsList = []
      myRouter.map((item) => {
        if (item.children.length <= 1) {
          tagsList.push(item.children[0])
        } else {
          tagsList.push(...item.children)
        }
      })
      store.commit('setTagsList', tagsList)
    }
    util.title(to.meta.title)


    if (Cookies.get('locking') === '1' && to.name !== 'locking') { // 判断当前是否是锁定状态
      next({
        replace: true,
        name: 'locking'
      })
    } else if (Cookies.get('locking') === '0' && to.name === 'locking') {
      next(false)
    } else {
      if (!Cookies.get('user') && to.name !== 'login' && to.name !== 'signUp') { // 判断是否已经登录且前往的页面不是登录页
        next({
          name: 'login'        
        })
      } else if (Cookies.get('user') && to.name === 'login') { // 判断是否已经登录且前往的是登录页
        util.title()  
        next({
          name: 'home_index'
        })
      } else {
        const curRouterObj = util.getRouterObjByName(store.state.app.routers, to.name)
        if (curRouterObj && curRouterObj.meta && curRouterObj.meta.roles !== undefined) { // 需要判断权限的路由
          if (util.hasPermission(curRouterObj)) {
            util.toDefaultPage(store.state.app.routers, to.path, router, next) // 如果在地址栏输入的是一级菜单则默认打开其第一个二级菜单的页面
          } else {
            next({
              replace: true,
              name: 'error-403'
            })
          }
        } else { // 没有配置权限的路由, 直接通过
          if (to.name)
            next()
          else
            util.toDefaultPage(store.state.app.routers, to.path, router, next)
        }
      }
   }
})

router.afterEach((to) => {
  iView.LoadingBar.finish()
  window.scrollTo(0, 0)

})

export default router

/**
 * Created by Administrator on 2018/3/7.
 */
import axios from 'axios'
import Cookies from 'js-cookie'
import store from '@/store'
import router from '@/router'
import env from '../config/env'

const ajaxUrl = env === 'test' ? '//192.168.0.175:16666/web-service' : env === 'prd' ? '//47.92.223.201:16666/web-service' : '//127.0.0.1:16666/web-service'

let util = {}

const SUPER_ADMIN = ['admin']

util.getBaseUrl = function () {
  return ajaxUrl
}

util.ajax = axios.create({
  baseURL: ajaxUrl,
  timeout: 30000
})
util.title = function (title) {
  title = title || '可视化医疗'
  window.document.title = title
}
util.inOf = function (arr, targetArr) {
  let res = true
  arr.forEach(item => {
    if (targetArr.indexOf(item) < 0) {
      res = false
    }
  })
  return res
}

util.oneOf = function (ele, targetArr) {
  if (targetArr.indexOf(ele) >= 0) {
    return true
  } else {
    return false
  }
}

util.validToken = function () {
  return util.ajax({
    method: 'get',
    url: '/web/validUser',
    data: {}
  })
}
util.getRouterObjByName = function (routers, name) {
  if (!name || !routers || !routers.length) {
    return null
  }
  let routerObj = null
  for (let item of routers) {
    if (item.name === name) {
      return item
    }
    routerObj = util.getRouterObjByName(item.children, name)
    if (routerObj) {
      return routerObj
    }
  }
  return null
}

util.handleTitle = function (vm, item) {
  if (typeof item.title === 'object') {
    return vm.$t(item.title.i18n)
  } else {
    return item.title
  }
}

util.setCurrentPath = function (vm, name) {
  let title = ''
  let isOtherRouter = false
  vm.$store.state.app.routers.forEach(item => {
    if (item.children.length === 1) {
      if (item.children[0].name === name) {
        title = util.handleTitle(vm, item)
        if (item.name === 'otherRouter') {
          isOtherRouter = true
        }
      }
    } else {
      item.children.forEach(child => {
        if (child.name === name) {
          title = util.handleTitle(vm, child)
          if (item.name === 'otherRouter') {
            isOtherRouter = true
          }
        }
      })
    }
  })
  let currentPathArr = []
  if (name === 'home_index') {
    currentPathArr = [
      {
        title: util.handleTitle(vm, util.getRouterObjByName(vm.$store.state.app.routers, 'home_index')),
        path: '',
        name: 'home_index'
      }
    ]
  } else if ((name.indexOf('_index') >= 0 || isOtherRouter) && name !== 'home_index') {
    currentPathArr = [
      {
        title: util.handleTitle(vm, util.getRouterObjByName(vm.$store.state.app.routers, 'home_index')),
        path: '/home',
        name: 'home_index'
      },
      {
        title: title,
        path: '',
        name: name
      }
    ]
  } else {
    let currentPathObj = vm.$store.state.app.routers.filter(item => {
      if (item.children.length == 0) {
        return false;
      }
      if (item.children.length == 1) {
        return item.children[0].name === name
      } else {
        let i = 0
        let childArr = item.children
        let len = childArr.length
        while (i < len) {
          if (childArr[i].name === name) {
            return true
          }
          i++
        }
        return false
      }
    })[0]
    if (!currentPathObj) {
      currentPathArr = [
        {
          title: '首页',
          path: '',
          name: 'home_index'
        }
      ]
    } else if (currentPathObj.children.length <= 1 && currentPathObj.name === 'home') {
      currentPathArr = [
        {
          title: '首页',
          path: '',
          name: 'home_index'
        }
      ]
    } else if (currentPathObj.children.length <= 1 && currentPathObj.name !== 'home') {
      currentPathArr = [
        {
          title: '首页',
          path: '/home',
          name: 'home_index'
        },
        {
          title: currentPathObj.title,
          path: '',
          name: name
        }
      ]
    } else {
      let childObj = currentPathObj.children.filter((child) => {
        return child.name === name
      })[0]
      currentPathArr = [
        {
          title: '首页',
          path: '/home',
          name: 'home_index'
        },
        {
          title: currentPathObj.title,
          path: '',
          name: currentPathObj.name
        },
        {
          title: childObj.title,
          path: currentPathObj.path + '/' + childObj.path,
          name: name
        }
      ]
    }
  }
  vm.$store.commit('setCurrentPath', currentPathArr)

  return currentPathArr
}

util.openNewPage = function (vm, name, argu, query) {
  let pageOpenedList = vm.$store.state.app.pageOpenedList
  let openedPageLen = pageOpenedList.length
  let i = 0
  let tagHasOpened = false
  while (i < openedPageLen) {
    if (name === pageOpenedList[i].name) { // 页面已经打开
      vm.$store.commit('pageOpenedList', {
        index: i,
        argu: argu,
        query: query
      })
      tagHasOpened = true
      break
    }
    i++
  }
  if (!tagHasOpened) {
    let tag = vm.$store.state.app.tagsList.filter((item) => {
      if (!item) {
        return false;
      }
      if (item.children) {
        return name === item.children[0].name
      } else {
        return name === item.name
      }
    })
    tag = tag[0]
    if (tag) {
      tag = tag.children ? tag.children[0] : tag
      if (argu) {
        tag.argu = argu
      }
      if (query) {
        tag.query = query
      }
      vm.$store.commit('increateTag', tag)
    }
  }
  vm.$store.commit('setCurrentPageName', name)
}

util.getRouteNameByPath = function (router, path) {
  if (!Array.isArray(router) || !path)
    return ''
  let len = router.length
  let i = 0
  while (i < len) {
    let routerItem = router[i]
    if (!!routerItem.path && routerItem.path == path) {
      return routerItem.name
    }
    if (!!routerItem.children && Array.isArray(routerItem.children)) {
      let childLen = routerItem.children.length
      for (let j = 0; j < childLen; j++) {
        let routerPath = '/' + routerItem.path + '/' + routerItem.children[j]['path']
        routerPath = routerPath.replace(/(\/)\/*\1/g, '$1')
        if (routerPath == path) {
          return routerItem.children[j].name
        }
      }
    }
    i++
  }
  return ''

}
util.toDefaultPage = function (routers, path, route, next) {
  let isRouterPath = false
  let notHandle = true
  let len = routers.length
  let i = 0
  while (i < len) {
    let routerItem = routers[i]
    if (!!routerItem.path && routerItem.path == path) {
      route.replace({
        name: routerItem.name
      })
      isRouterPath = true
      notHandle = false
      next()
      break
    }
    if (!!routerItem.children && Array.isArray(routerItem.children)) {
      let childLen = routerItem.children.length
      for (let j = 0; j < childLen; j++) {
        let routerPath = '/' + routerItem.path + '/' + routerItem.children[j]['path']
        routerPath = routerPath.replace(/(\/)\/*\1/g, '$1')
        if (routerPath == path) {
          route.replace({
            name: routerItem.children[j].name
          })
          isRouterPath = true
          notHandle = false
          next()
          break
        }
      }
    }
    i++
  }
  if (!isRouterPath) {
    route.replace({
      name: 'error-404'
    })
    next()
  }
  if (notHandle) {
    next()
  }
}

util.dateToFormat = function (time) {
  if(time == null){
    return null;
  }
  var date = new Date(time);
  var Y = date.getFullYear() + '-';
  var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
  var D = date.getDate() + ' ';
  var h = (date.getHours() + 1 < 10 ? '0' + date.getHours() : date.getHours());
  var m = (date.getMinutes() + 1 < 10 ? '0' + date.getMinutes() : date.getMinutes()) + ':';
  var s = (date.getSeconds() + 1 < 10 ? '0' + date.getSeconds() : date.getSeconds());

  if(D < 10) {
    D = "0" + D;
  }
  if(h<10){
    h="0"+h
  }

  return Y + M + D + h+ ':' +m +s
}

util.fullscreenEvent = function (vm) {
  vm.$store.commit('initCachepage')
  // 权限菜单过滤相关
  vm.$store.commit('updateMenulist')
  // 全屏相关
}

util.checkUpdate = function (vm) {
  axios.get('https://api.github.com/repos/iview/iview-admin/releases/latest').then(res => {
  })
}
/**
 *
 * @param route
 * @returns {boolean}
 */
util.hasPermission = function (route) {
  let roles = Cookies.get('roles') ? JSON.parse(Cookies.get('roles')) : []
  if (route && route.meta && route.meta.roles) {
    if (Array.isArray(route.meta.roles) && roles && Array.isArray(roles)) {
      return roles.some(role => SUPER_ADMIN.includes(role) ? true : route.meta.roles.includes(role))
    }
    return false
  } else {
    return true
  }
}
/**
 * 依据后台
 * @param appRouter
 * @returns {Array}
 */
util.generateAppRouter = function (appRouter = []) {
  let roleRouter = []
  appRouter.forEach((router) => {
    util.resolveComponent(router)
    roleRouter.push(router)
  })
  return roleRouter
}
util.resolveComponent = function (router) {
  console.log(router)
  let newRouter = {}
  if (router.component) {
    let componentName = router.component
    router.component = resolve => require([`@/views` + componentName + `.vue`], resolve)
    router.title = router.name
    if (router.children) {
      router.children.forEach((child) => util.resolveComponent(child))
    }
  }
  return router
}
/*
 拦截器设置
 */
util.ajax.interceptors.request.use(
  config => {
    const token = Cookies.get('token') // 获取Cookie
    if (token) {
      config.headers.token = token
    }
    return config
  },
  err => {
    return Promise.reject(err)
  }
)

// http response 拦截器
util.ajax.interceptors.response.use(
  response => {
    let token = Cookies.get('token')
    if (response.data) {
      if (response.data.token && token !== response.data.token) {
        Cookies.set('token', response.data.token)
      }
    }
    return response
  },
  error => {
    if (error.response.status == 401) {
      store.commit('loginOut')
      router.push('/login')
    }
    return error
  })

export default util

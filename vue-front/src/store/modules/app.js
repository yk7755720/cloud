import { otherRouter } from '@/router/router'
import util from '@/libs/util'
import Vue from 'vue'

const app = {
  state: {
    cachePage: [],
    lang: 'zh-CN',
    isFullScreen: false,
    openedSubmenuArr: [], // 要展开的菜单数组
    menuTheme: 'dark', // 主题
    themeColor: '',
    appRouter: [],//后台获取所有菜单
    pageOpenedList: [{
      path: '',
      name: 'home_index'
    }],
    currentPageName: '',
    currentPath: [
      {
        path: '',
        name: 'home_index'
      }
    ], // 面包屑数组
    menuList: [],
    routers: [
      otherRouter
    ],
    tagsList: [...otherRouter.children],
    messageCount: 0,
    dontCache: ['text-editor', 'artical-publish'] // 在这里定义你不想要缓存的页面的name属性值(参见路由配置router.js)
  },
  mutations: {
    setAppRouter (state, router) {
      state.appRouter = router
      state.routers = [otherRouter, ...router]

    },
    clearAppRouter (state) {
      state.appRouter = []
      state.routers = [otherRouter]
    },
    setTagsList (state, list) {
      state.tagsList.push(...list)
    },
    updateMenulist (state) {
      let menuList = []
      state.appRouter.forEach((item, index) => {
        if (item.meta && item.meta.roles !== undefined) {
          if (util.hasPermission(item)) {
            if (item.children.length === 1) {
              menuList.push(item)
            } else {
              let len = menuList.push(item)
              let childrenArr = []
              childrenArr = item.children.filter(child => {
                if (child.meta && child.meta.roles !== undefined) {
                  if (util.hasPermission(child)) {
                    return child
                  }
                } else {
                  return child
                }
              })
              menuList[len - 1].children = childrenArr
            }
          }
        } else {
          if (item.children.length === 1) {
            menuList.push(item)
          } else {
            let len = menuList.push(item)
            let childrenArr = []
            childrenArr = item.children.filter(child => {
              if (child.meta && child.meta.roles !== undefined) {
                if (util.hasPermission(child)) {
                  return child
                }
              } else {
                return child
              }
            })
            if (childrenArr === undefined || childrenArr.length === 0) {
              menuList.splice(len - 1, 1)
            } else {
              let handledItem = JSON.parse(JSON.stringify(menuList[len - 1]))
              handledItem.children = childrenArr
              menuList.splice(len - 1, 1, handledItem)
            }
          }
        }
      })
      state.menuList = menuList
    },
    changeMenuTheme (state, theme) {
      state.menuTheme = theme
    },
    changeMainTheme (state, mainTheme) {
      state.themeColor = mainTheme
    },
    addOpenSubmenu (state, name) {
      let hasThisName = false
      let isEmpty = false
      if (name.length === 0) {
        isEmpty = true
      }
      if (state.openedSubmenuArr.includes(name)) {
        hasThisName = true
      }
      if (!hasThisName && !isEmpty) {
        state.openedSubmenuArr.push(name)
      }
    },
    closePage (state, name) {
      state.cachePage.forEach((item, index) => {
        if (item === name) {
          state.cachePage.splice(index, 1)
        }
      })
    },
    initCachepage (state) {
      if (localStorage.cachePage) {
        state.cachePage = JSON.parse(localStorage.cachePage)
      }
    },
    removeTag (state, name) {
      state.pageOpenedList.map((item, index) => {
        if (item.name === name) {
          state.pageOpenedList.splice(index, 1)
        }
      })
    },
    pageOpenedList (state, get) {
      let openedPage = state.pageOpenedList[get.index]
      if (get.argu) {
        openedPage.argu = get.argu
      }
      if (get.query) {
        openedPage.query = get.query
      }
      state.pageOpenedList.splice(get.index, 1, openedPage)
      localStorage.pageOpenedList = JSON.stringify(state.pageOpenedList)
    },
    clearAllTags (state) {
      state.pageOpenedList.splice(1)
      state.cachePage.length = 0
      localStorage.pageOpenedList = JSON.stringify(state.pageOpenedList)
    },
    clearOtherTags (state, vm) {
      let currentName = vm.$route.name
      let currentIndex = 0
      state.pageOpenedList.forEach((item, index) => {
        if (item.name === currentName) {
          currentIndex = index
        }
      })
      if (currentIndex === 0) {
        state.pageOpenedList.splice(1)
      } else {
        state.pageOpenedList.splice(currentIndex + 1)
        state.pageOpenedList.splice(1, currentIndex - 1)
      }
      let newCachepage = state.cachePage.filter(item => {
        return item === currentName
      })
      state.cachePage = newCachepage
      localStorage.pageOpenedList = JSON.stringify(state.pageOpenedList)
    },
    setOpenedList (state) {
      state.pageOpenedList = localStorage.pageOpenedList ? JSON.parse(localStorage.pageOpenedList) : [otherRouter.children[0]]
    },
    setCurrentPath (state, pathArr) {
      state.currentPath = pathArr
    },
    setCurrentPageName (state, name) {
      state.currentPageName = name
    },
    setAvator (state, path) {
      localStorage.avatorImgPath = path
    },
    switchLang (state, lang) {
      state.lang = lang
      Vue.config.lang = lang
    },
    clearOpenedSubmenu (state) {
      state.openedSubmenuArr.length = 0
    },
    setMessageCount (state, count) {
      state.messageCount = count
    },
    increateTag (state, tagObj) {
      if (!util.oneOf(tagObj.name, state.dontCache)) {
        state.cachePage.push(tagObj.name)
        localStorage.cachePage = JSON.stringify(state.cachePage)
      }
      state.pageOpenedList.push(tagObj)
      localStorage.pageOpenedList = JSON.stringify(state.pageOpenedList)
    }
  }
}

export default app

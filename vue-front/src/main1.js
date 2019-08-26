import Vue from 'vue'
import iView from 'iview'
import 'babel-polyfill'
import App from '@/app'
import util from '@/libs/util'
import { router } from '@/router/index1'
import '@/i18n'
import store from '@/store'
import VueI18n from 'vue-i18n'

Vue.use(VueI18n)
Vue.use(iView)

Vue.prototype.$axios = util

new Vue({
  el: '#app',
  router,
  store,
  components: {App},
  template: '<App/>',
  data: {
    currentPageName: ''
  },
  mounted () {
  //  this.currentPageName = this.$route.name
  //   显示打开的页面的列表
  //   this.$store.commit('setOpenedList')
  //   this.$store.commit('initCachepage')
  //   // 权限菜单过滤相关
  //   this.$store.commit('updateMenulist')
  },
  created () {
  }
})

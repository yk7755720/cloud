import Cookies from 'js-cookie'

const user = {
  state: {},
  mutations: {
    loginOut (state) {
      Cookies.remove('user')
      Cookies.remove('roles')
      Cookies.remove('password')
      Cookies.remove('token')
      sessionStorage.clear()
      localStorage.clear()
    }
  },
  actions: {}
}

export default user

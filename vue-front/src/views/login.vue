<template>
  <div class="login" @keydown.enter="doLogin">
    <div class="login-con">
      <i-card :bordered="false">
        <p slot="title">
          <i-icon type="log-in"></i-icon>
          欢迎登录
        </p>
        <div class="form-con">
          <i-form ref="loginForm" :model="formInline" :rules="ruleInline">
            <i-form-item prop="language">
              <i-select v-model="language" @on-change="switchLang">
                <Option value="en-US">English</Option>
                <Option value="zh-CN">中文</Option>
              </i-select>
            </i-form-item>
            <i-form-item prop="user">
              <i-input type="text" v-model="formInline.user"
                       :placeholder="$t('message.login.username')">
                <i-icon type="ios-person-outline" slot="prepend"></i-icon>
              </i-input>
            </i-form-item>
            <i-form-item prop="password">
              <i-input type="password" v-model="formInline.password"
                       :placeholder="$t('message.login.password')">
                <i-icon type="ios-locked-outline" slot="prepend"></i-icon>
              </i-input>
            </i-form-item>
            <i-form-item>
              <i-button type="primary" style="width: 100%"
                        @click="doLogin">
                {{$t('message.sign_in')}}
              </i-button>
            </i-form-item>
          </i-form>
          <p class="login-tip">输入正确的用户名与密码</p>
        </div>
      </i-card>
    </div>
  </div>
</template>
<script>
  import Cookies from 'js-cookie'
  import util from '@/libs/util'

  export default {
    data () {
      return {
        language: this.$store.state.app.lang,
        value2: 0,
        formInline: {
          user: '',
          password: ''
        },
        ruleInline: {
          user: [
            {required: true, message: this.$t('message.login.username_error'), trigger: 'blur'}
          ],
          password: [
            {required: true, message: this.$t('message.login.password_error'), trigger: 'blur'}
          ]
        }
      }
    },
    methods: {
      switchLang: function (lang) {
        this.$store.commit('switchLang', lang)
        this.ruleInline.user[0].message = this.$t('message.login.username_error')
        this.ruleInline.password[0].message = this.$t('message.login.password_error')
      },
      doLogin: function () {
        this.$refs['loginForm'].validate((valid) => {
          if (valid) {
            this.$axios.ajax({
              method: 'post',
              url: '/web/doLogin',
              data: {
                userCode: this.formInline.user,
                password: this.formInline.password
              }
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                let data = res.data.data
                Cookies.set('user', data['userInfo']['username'])
                Cookies.set('password', data['userInfo']['password'])
                Cookies.set('token', data['token'])
                Cookies.set('roles', [])
                Cookies.set('userInfo', JSON.stringify(data['userInfo']))
                sessionStorage.setItem('appRouter', JSON.stringify(data['userMenus']))
                this.$store.commit('setAvator', 'https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=3448484253,3685836170&fm=27&gp=0.jpg')
                if (data['token']) {
                  this.$router.push('/home') //跳转用户中心页
                }
              } else {
                this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '登录失败')
              }
            })
          }
        })
      }
    }
  }
</script>

<style lang="less">
  @import "./login.less";
</style>

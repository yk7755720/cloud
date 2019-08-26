<template>
  <div class="main" :class="shrink">
    <div class="sidebar-menu-con" :style="{width: shrink?'60px':'200px', overflow:shrink ? 'visible' : 'auto'}">
      <shrinkable-menu
        :shrink="shrink"
        @on-change="handleSubmenuChange"
        :theme="menuTheme"
        :before-push="beforePush"
        :open-names="openedSubmenuArr"
        :menu-list="menuList">
        <div slot="top" class="logo-con">
          <img v-show="!shrink" src="../assets/img/logo.jpg" key="max-logo"/>
          <img v-show="shrink" src="../assets/img/logo-min.jpg" key="min-logo"/>
        </div>
      </shrinkable-menu>
    </div>
    <div class="main-header-con" :style="{paddingLeft: shrink?'60px':'200px'}">
      <div class="main-header">
        <div class="navicon-con">
          <i-button :style="{transform: 'rotateZ(' + (this.shrink ? '-90' : '0') + 'deg)'}" type="text"
                    @click="toggleClick">
            <i-icon type="navicon" size="32"></i-icon>
          </i-button>
        </div>
        <div class="header-middle-con">
          <div class="main-breadcrumb">
            <breadcrumb-nav :currentPath="currentPath"></breadcrumb-nav>
          </div>
        </div>
        <div class="header-avator-con">
          <div class="user-dropdown-menu-con">
            <i-row type="flex" justify="end" align="middle" class="user-dropdown-innercon">
              <i-dropdown transfer trigger="click" @on-click="handleClickUserDropdown">
                <a href="javascript:void(0)">
                  <span class="main-user-name">{{ userName }}</span>
                  <i-icon type="arrow-down-b"></i-icon>
                </a>
                <i-dropdown-menu slot="list">
                  <i-dropdown-item name="ownSpace">{{$t('message.home.ownSpace')}}</i-dropdown-item>
                  <i-dropdown-item name="switchLang" divided>{{$t('message.home.switchLang')}}

                  </i-dropdown-item>
                  <i-dropdown-item name="loginOut" divided>{{$t('message.home.loginOut')}}

                  </i-dropdown-item>
                </i-dropdown-menu>
              </i-dropdown>
              <i-avatar :src="avatorPath" style="background: #619fe7;margin-left: 10px;"></i-avatar>
            </i-row>
          </div>
        </div>
      </div>
      <div class="tags-con">
        <tags-page-opened :pageTagsList="pageTagsList"></tags-page-opened>
      </div>
    </div>
    <div class="single-page-con" :style="{left: shrink?'60px':'200px'}">
      <div class="single-page">
        <keep-alive :include="cachePage">
          <router-view></router-view>
        </keep-alive>
      </div>
    </div>
		<i-modal width="700px"
						:title="this.maskTitle"
						v-model="maskModal"
						:mask-closable="false"
						:closable="false" class-name="vertical-center-modal">
			<i-form ref="editFormItem" :model="editFormItem" :label-width="80" :rules="validateRules">
				<i-row>
						<i-form-item label="原密码" prop="oldPassword">
							<i-input type="password" placeholder="原密码" v-model="editFormItem.oldPassword" style="width:550px"></i-input>
						</i-form-item>
						<i-form-item label="输入新密码" prop="password">
							<i-input type="password" placeholder="输入新密码" v-model="editFormItem.password" style="width:550px"></i-input>
						</i-form-item>
						<i-form-item label="再次输入新密码" prop="pwdCheck">
							<i-input type="password" placeholder="再次输入新密码" v-model="editFormItem.newPassword" style="width:550px"></i-input>
						</i-form-item>
				</i-row>
			</i-form>
			<div slot="footer">
				<i-button type="primary" size="large" @click="updatePassword">修改</i-button>
				<i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
			</div>
		</i-modal>
  </div>
</template>
<script>
  import shrinkableMenu from '@components/shrinkable-menu/shrinkable-menu.vue'
  import tagsPageOpened from '@components/tags-page-opened.vue'
  import breadcrumbNav from '@components/breadcrumb-nav.vue'
  import Cookies from 'js-cookie'
  import util from '@/libs/util.js'

  export default {
    components: {
      shrinkableMenu,
      tagsPageOpened,
      breadcrumbNav
    },
    data () {
			const pwdValidate = (rule, value, callback) => {
				  if (!this.editFormItem.password) {
						  callback(new Error('密码不能为空'));
					}
          this.$refs['editFormItem'].validateField('pwdCheck');
          callback();
      };
      const pwdCheckValidate = (rule, value, callback) => {
          if (this.editFormItem.password != '' && this.editFormItem.newPassword == '') {
              callback(new Error('确认密码不能为空'));
          } else if (this.editFormItem.password != this.editFormItem.newPassword) {
              callback(new Error('新密码和确认密码应相同'));
          } else {
              callback();
          }
      };
      return {
        shrink: false,
        userName: '',
        openedSubmenuArr: this.$store.state.app.openedSubmenuArr,
				maskModal: false,
				maskTitle: '修改密码',
				editFormItem: {},
				validateRules: {
					  oldPassword: [
							{required: true, message: '密码不能为空', trigger: 'blur' }
            ],
            password: [
              {validator: pwdValidate, trigger: 'blur'}
            ],
            pwdCheck: [
              {validator: pwdCheckValidate, trigger: 'blur'}
          ]
        }
      }
    },
    computed: {
      menuList () {
        return this.$store.state.app.menuList
      },
      pageTagsList () {
        return this.$store.state.app.pageOpenedList // 打开的页面的页面对象
      },
      currentPath () {
        return this.$store.state.app.currentPath // 当前面包屑数组
      },
      avatorPath () {
        return localStorage.avatorImgPath
      },
      cachePage () {
        return this.$store.state.app.cachePage
      },
      lang () {
        return this.$store.state.app.lang
      },
      menuTheme () {
        return this.$store.state.app.menuTheme
      },
      mesCount () {
        return this.$store.state.app.messageCount
      }
    },
    methods: {
      init () {
        let userInfo = JSON.parse(Cookies.get('userInfo'))
        let pathArr = util.setCurrentPath(this, this.$route.name)
        this.$store.commit('updateMenulist')
        if (pathArr.length >= 2) {
          this.$store.commit('addOpenSubmenu', pathArr[1].name)
        }
        this.userName = Cookies.get('user') + ' | 管理员 '
        let messageCount = 3
        this.messageCount = messageCount.toString()
        this.checkTag(this.$route.name)
        this.$store.commit('setMessageCount', 3)
      },
      toggleClick () {
        this.shrink = !this.shrink
      },
      handleClickUserDropdown (name) {
        if (name === 'ownSpace') {
					this.maskModal = true;
        } else if (name === 'switchLang') {
          this.$store.commit('switchLang', this.$store.state.app.lang === 'zh-CN' ? 'en-US' : 'zh-CN')
        } else if (name === 'loginOut') {
          this.$axios.ajax({
            method: 'post',
            url: '/web/doLogout',
            data: {}
          }).then((res) => {
            // 退出登录
            this.$store.commit('loginOut')
            this.$store.commit('clearOpenedSubmenu')
            this.$router.push({
              name: 'login'
            })
          })
        }
      },
      checkTag (name) {
        let openpageHasTag = this.pageTagsList.some(item => {
          if (item.name === name) {
            return true
          }
        })
        if (!openpageHasTag) { //  解决关闭当前标签后再点击回退按钮会退到当前页时没有标签的问题
          util.openNewPage(this, name, this.$route.params || {}, this.$route.query || {})
        }
      },
      handleSubmenuChange (val) {
        val
      },
      beforePush (name) {
        name
        return true
      },
			closeModal () {
				this.maskModal = false;
				this.$refs['editFormItem'].resetFields();
				this.editFormItem = [];
			},
			updatePassword() {
				this.$refs['editFormItem'].validate((valid) => {
					if (valid) {
						this.$axios.ajax({
							method: 'post',
							url: '/web/updatePassword',
							data: this.editFormItem
						}).then((res) => {
							if (!!res.data.code && 10000 == res.data.code) {
								this.$Message.success('修改密码成功')
								this.maskModal = false
							} else {
								this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '修改密码失败')
							}
						})
				  }
				})
			}
    },
    watch: {
      '$route' (to) {
        this.$store.commit('setCurrentPageName', to.name)
        let pathArr = util.setCurrentPath(this, to.name)
        if (pathArr.length > 2) {
          this.$store.commit('addOpenSubmenu', pathArr[1].name)
        }
        this.checkTag(to.name)
        localStorage.currentPageName = to.name    
      },
      lang () {
        util.setCurrentPath(this, this.$route.name) // 在切换语言时用于刷新面包屑
      }
    },
    mounted () {
      this.init()
    },
    created () {
      // 显示打开的页面的列表
      this.$store.commit('setOpenedList')
    }
  }
</script>
<style lang="less">
  @import "./home.less";
</style>

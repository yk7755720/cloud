<template>
  <i-tabs size="small">
    <i-tab-pane label="菜单维护">
      <i-row>
        <i-col :xs="10" :sm="8" :md="5" :lg="3" class-name="menu_tree">
          <div class="menu_button">
            <i-button size="small" style="float: right" @click="delMenu">删除</i-button>
            <i-button size="small" type="primary" @click="addMenu" style="float: right">新增</i-button>
          </div>
          <div style="" class="op_tree">
            <i-tree :data="menuArray" show-checkbox @on-select-change="selectTreeNode"
                    @on-check-change="selectRowChange"></i-tree>
          </div>
        </i-col>
        <i-col :xs="14" :sm="16" :md="19" :lg="21" class-name="menu_edit">
          <i-form ref="editFormItem" :model="editMenuItem" :label-width="80" :rules="ruleValidate">
            <i-form-item label="菜单名称"  prop="name">
              <i-input :autofocus=nameFocus placeholder="请输入菜单名称" v-model="editMenuItem.name"></i-input>
            </i-form-item>
            <i-form-item label="菜单编码">
              <i-input placeholder="请输入菜单编码" v-model="editMenuItem.menuCode"></i-input>
            </i-form-item>
            <i-form-item label="节点类型">
              <i-radio-group v-model="editMenuItem.level">
                <i-radio label="0">树节点</i-radio>
                <i-radio label="1">子节点</i-radio>
              </i-radio-group>
            </i-form-item>
            <i-form-item v-show="editMenuItem.level==='1'" label="上级菜单">
              <i-select v-model="editMenuItem.parentCode"
                        placeholder="请选择上级菜单">
                <i-option v-for="item in parentMenuArray" :value="item.code" :key="item.code">{{ item.name }}
                </i-option>
              </i-select>
            </i-form-item>
            <i-form-item label="菜单路径">
              <i-input placeholder="请输入菜单路径" v-model="editMenuItem.path"></i-input>
            </i-form-item>
            <i-form-item label="访问路径">
              <i-input placeholder="请输入访问路径" v-model="editMenuItem.url"></i-input>
            </i-form-item>
						<i-form-item label="排序">
							<i-input placeholder="请输入序号" v-model="editMenuItem.sn"></i-input>
						</i-form-item>
            <i-form-item label="菜单状态">
              <i-radio-group v-model="editMenuItem.state">
                <i-radio label="0">失效</i-radio>
                <i-radio label="1">正常</i-radio>
              </i-radio-group>
            </i-form-item>
						<template v-if="editMenuItem.name != '系统菜单目录'" >
							<i-form-item>
								<i-button type="primary" size="large" @click="saveOrUpdateMenu">保存</i-button>
							</i-form-item>
						</template>
          </i-form>
        </i-col>
      </i-row>
    </i-tab-pane>
    <i-tab-pane label="角色维护">
      <i-row>
        <i-col :xs="10" :sm="8" :md="5" :lg="3" class-name="menu_tree">
          <div class="menu_button">
            <i-button size="small" style="float: right" @click="delRole">删除</i-button>
            <i-button size="small" type="primary" @click="addRole" style="float: right">新增</i-button>
          </div>
          <div style="" class="op_tree">
            <i-tree :data="roleArray" show-checkbox @on-select-change="selectRoleTreeNode"
                    @on-check-change="selectRoleRowChange"></i-tree>
          </div>
        </i-col>
        <i-col :xs="14" :sm="16" :md="19" :lg="21" class-name="menu_edit">
          <i-form :model="editRoleItem" :label-width="80">
            <i-form-item label="角色名称">
              <i-input :autofocus=nameFocus placeholder="请输入角色名称" v-model="editRoleItem.roleName"></i-input>
            </i-form-item>
            <i-form-item label="是否启用">
              <i-radio-group v-model="editRoleItem.enabled">
                <i-radio label="0">禁用</i-radio>
                <i-radio label="1">启用</i-radio>
              </i-radio-group>
            </i-form-item>
            <i-form-item label="权限描述" prop="desc">
              <i-input v-model="editRoleItem.comment" type="textarea" :autosize="{minRows: 2,maxRows: 5}"
                       placeholder="Enter something..."></i-input>
            </i-form-item>
						<i-form-item>
							<i-button type="primary" size="large" @click="saveOrUpdateRole">保存</i-button>
						</i-form-item>
          </i-form>
        </i-col>
      </i-row>
    </i-tab-pane>
    <i-tab-pane label="菜单绑定">
      <i-row>
        <i-col :xs="10" :sm="8" :md="5" :lg="3" class-name="menu_tree">
          <div style="height: 100%">
            <i-tree :data="roleArray" ref="roleTree" @on-select-change="getMenuCodeByRoleId"></i-tree>
          </div>
        </i-col>
        <i-col :xs="14" :sm="16" :md="19" :lg="21" class-name="menu_edit">
          <i-scroll>
            <div class="edit_menu_tree">
              <i-tree :data="roleMenuArray" ref="roleMenuTree" show-checkbox @on-select-change="selectTreeNode"
                      @on-check-change="selectRowChange"></i-tree>
            </div>
          </i-scroll>

          <div style="margin-left: 80px;margin-top: 20px">
            <i-button type="primary" size="large" @click="saveOrUpdateRoleMenu">绑定</i-button>
          </div>

        </i-col>
      </i-row>
    </i-tab-pane>
  </i-tabs>
</template>

<script>
  import util from '@/libs/util.js'
  import { router } from '@/router'

  export default {
    name: 'menu-edit',
    data () {
      return {
        //菜单相关
        selectedMenuRows: [],
        nameFocus: true,
        editMenuItem: {},
        menuArray: [],
        parentMenuArray: [],
        //角色相关
        selectedRoleRows: [],
        editRoleItem: {},
        roleArray: [],
        //配置相关
        roleMenuArray: [],
				showButton: false,
				ruleValidate: {
					name: [
						{ required: true, message: '菜单名称不能为空', trigger: 'blur' }
					]
				}
      }
    },
    methods: {
      getAllRoleList () {
        this.$axios.ajax({
          method: 'post',
          url: '/menu/getRoleList',
          data: {}
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let menu = []
            if (Array.isArray(res.data.data) && res.data.data.length > 0) {
              res.data.data.forEach((item) => {
                menu.push({
                  item: item,
                  title: item.roleName,
                  expand: true
                })
              })

              this.roleArray = [{
                item: {},
                title: '系统角色目录',
                expand: true,
                children: menu
              }]
            }
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取角色信息失败')
          }
        })
      },
      getAllMenuList () {
        this.$axios.ajax({
          method: 'post',
          url: '/menu/getMenuList',
          data: {}
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let menu = []
            if (Array.isArray(res.data.data) && res.data.data.length > 0) {
              res.data.data.forEach((item) => {
                if (!!item.children && item.children.length > 0) {
                  let childrenArr = item.children
                  let childArr = []
                  childrenArr.forEach((child) => {
                    childArr.push({
                      item: child,
                      title: child.name,
                      expand: true
                    })
                  })
                  menu.push({
                    item: item,
                    title: item.name,
                    expand: true,
                    children: childArr
                  })
                } else {
                  menu.push({
                    item: item,
                    title: item.name,
                    expand: true,
                    children: []
                  })
                }
              })

              this.menuArray = [{
                item: {},
                title: '系统菜单目录',
                expand: true,
                children: menu
              }]
              this.roleMenuArray = JSON.parse(JSON.stringify(menu))
              this.parentMenuArray = []
              menu.forEach((menuChild) => {
                this.parentMenuArray.push({
                  name: menuChild['item'].name,
                  code: menuChild['item'].menuCode
                })
              })
            }
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取菜单信息失败')
          }
        })
      },
      selectTreeNode (treeNode) {
      	debugger
        if (!!treeNode && !!treeNode[0] && !!treeNode[0].item) {
					this.editMenuItem = treeNode[0].item
					if (!this.editMenuItem.name) {
						this.editMenuItem.name = '系统菜单目录';
						this.showButton = false;
					}
					else {
						this.showButton = true;
					}
        }
      },
      selectRoleTreeNode (treeNode) {
        if (!!treeNode && !!treeNode[0] && !!treeNode[0].item) {
          this.editRoleItem = treeNode[0].item
        }
      },
      getMenuCodeByRoleId (treeNode) {
        if (!treeNode || treeNode.length === 0) {
          return false
        }
        let roleId = ''
        if (!!treeNode && !!treeNode[0] && !!treeNode[0].item) {
          roleId = treeNode[0].item.id
        }
        if (roleId === '') {
          this.$Message.error('角色信息有误')
          return false
        }
        this.$axios.ajax({
          method: 'get',
          url: '/menu/getMenuCodeByRoleId?roleId=' + roleId,
          data: {}
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let _array = res.data.data
            if (Array.isArray(_array)) {
              this.clearAndSelectMenu(_array)//渲染选中
            }
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取菜单信息失败')
          }
        })
        if (!!treeNode && !!treeNode[0] && !!treeNode[0].item) {
          this.editRoleItem = treeNode[0].item
        }
      },
      setAppRouter (userMenus) {
        sessionStorage.setItem('appRouter', JSON.stringify(userMenus))
        let myRouter = util.generateAppRouter(userMenus)
        this.$store.commit('clearAppRouter')
        this.$store.commit('setAppRouter', myRouter)
        this.$store.commit('updateMenulist')
        router.addRoutes(myRouter)
        let tagsList = []
        myRouter.map((item) => {
          if (item.children.length <= 1) {
            tagsList.push(item.children[0])
          } else {
            tagsList.push(...item.children)
          }
        })
        this.$store.commit('setTagsList', tagsList)
      },
      clearAndSelectMenu (keyArr) {
        let _data = JSON.parse(JSON.stringify(this.roleMenuArray))
        _data.forEach((item) => {
          item['checked'] = false
          if (!!item.children) {
            item.children.forEach((child) => {
              child.checked = false
              if (!!child['item'] && keyArr.includes(child['item']['menuCode'])) {
                child.checked = true
              }
            })
          }
        })
        this.roleMenuArray = _data
      },
      saveOrUpdateMenu () {
				this.$refs['editFormItem'].validate((valid) => {
					if (valid) {
						if (this.editMenuItem.level == '0') {
							this.editMenuItem.parentCode = ''
						}
						if (isNaN(this.editMenuItem.sn)) {
							this.editMenuItem.sn = 0;
						}
						this.$axios.ajax({
							method: 'post',
							url: '/menu/saveOrUpdateMenu',
							data: this.editMenuItem
						}).then((res) => {
							if (!!res.data.code && 10000 == res.data.code) {
								this.$Message.success('保存菜单信息成功')
								this.getAllMenuList()
							} else {
								this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取菜单信息失败')
							}
						})
					}
				})
      },
      saveOrUpdateRoleMenu () {
        let roleMenuArr = []
        let roleNode = this.$refs.roleTree.getSelectedNodes()
        if (!roleNode[0] || !roleNode[0].item || !roleNode[0].item.id) {
          this.$Message.error('请选择角色节点')
          return false
        }
        let roleId = roleNode[0].item.id
        let choicesAll = this.$refs.roleMenuTree.getCheckedNodes() //方法的运用 getSelectedNodes也是如此用法
        if (!choicesAll) {
          roleMenuArr.push({
            id: '',
            roleId: roleId,
            menuCode: ''
          })
        } else {
          choicesAll.forEach((menu) => {
            roleMenuArr.push({
              id: '',
              roleId: roleId,
              menuCode: menu['item']['menuCode']
            })
          })
        }
        this.$axios.ajax({
          method: 'post',
          url: '/menu/saveOrUpdateRoleMenus',
          data: roleMenuArr
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            this.$Message.success('保存角色关联菜单信息成功')
            this.getMenuCodeByRoleId(roleNode)
            this.getUserMenuByRoleId(roleId)
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存角色关联菜单信息失败')
          }
        })
      },
      getUserMenuByRoleId (roleId) {
        if (!roleId)
          return false
        this.$axios.ajax({
          method: 'get',
          url: '/web/getUserMenuByRoleId?roleId=' + roleId,
          data: {}
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            this.setAppRouter(res.data.data)
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取角色关联菜单信息失败')
          }
        })
      },
      saveOrUpdateRole () {
        this.$axios.ajax({
          method: 'post',
          url: '/menu/saveOrUpdateRole',
          data: this.editRoleItem
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            this.$Message.success('保存角色信息成功')
            this.getAllRoleList()
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存角色信息失败')
          }
        })
      },
      selectRowChange (selection) {
        this.selectedMenuRows = []
        selection.forEach((select) => {
          let item = select['item']
          if (!!item && !!item.id) {
            this.selectedMenuRows.push(item.id)
          }
        })
      },
      selectRoleRowChange (selection) {
        this.selectedRoleRows = []
        selection.forEach((select) => {
          let item = select['item']
          if (!!item && !!item.id) {
            this.selectedRoleRows.push(item.id)
          }
        })
      },
      addMenu () {
        this.nameFocus = true
        this.editMenuItem = {}
      },
      addRole () {
        this.editRoleItem = {}
      },
      delMenu () {
        if (this.selectedMenuRows.length == 0) {
          this.$Message.info('请选择待删除菜单记录')
          return false
        }
        this.$Modal.confirm({
          title: '提示',
          content: '是否删除菜单记录?',
          okText: '是',
          cancelText: '否',
          onOk: function () {
            this.$axios.ajax({
              method: 'post',
              url: '/menu/delMenus',
              data: this.selectedMenuRows
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('记录删除成功')
                this.getAllMenuList()
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '删除记录失败')
              }
            })
          }.bind(this)
        })

      },
      delRole () {
        if (this.selectedRoleRows.length == 0) {
          this.$Message.info('请选择待删除角色记录')
          return false
        }
        this.$Modal.confirm({
          title: '提示',
          content: '是否删除角色记录?',
          okText: '是',
          cancelText: '否',
          onOk: function () {
            this.$axios.ajax({
              method: 'post',
              url: '/menu/delRoles',
              data: this.selectedRoleRows
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('角色删除成功')
                this.getAllRoleList()
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '删除角色失败')
              }
            })
          }.bind(this)
        })
      }
    },
    created () {
      this.getAllMenuList()//获取资源列表
      this.getAllRoleList()
    }
  }
</script>

<style scoped>
  .menu_tree {
    height: calc(100vh - 180px);
    border-width: 1px;
    border-right-style: double;
    border-right-color: #1d8ab0;
  }

  .edit_menu_tree {
    height: calc(100vh - 100px);
    margin-left: 10px;
  }

  .menu_button {
    padding-right: 5px;
    padding-top: 5px;
  }

  .op_tree {
    margin-top: 40px;
    padding-left: 5px;
    padding-right: 5px;
  }

  .menu_edit {
    padding-top: 5px;
    padding-left: 5px;
    padding-right: 20px;
  }
</style>

<template>
  <div class="user-content">
    <i-form inline :label-width="80">
      <i-row>
        <i-form-item prop="name" inline label="姓名">
          <i-input type="text" v-model="condition.userName" placeholder="请输入检索姓名">
          </i-input>
        </i-form-item>

        <i-form-item prop="userCode" inline label="账号">
          <i-input type="text" v-model="condition.userCode" placeholder="请输入检索账号">
          </i-input>
        </i-form-item>

        <i-form-item prop="sex" label="性别">
          <i-select clearable v-model="condition.sex" style="width:160px" placeholder="请选择待筛选性别">
            <i-option value="1">男</i-option>
            <i-option value="2">女</i-option>
          </i-select>
        </i-form-item>
      </i-row>
      <i-row>
        <i-form-item prop="status" label="状态">
          <i-select clearable v-model="condition.status" style="width:160px" placeholder="请选择待筛选状态">
            <i-option value="0">正常</i-option>
            <i-option value="1">冻结</i-option>
          </i-select>
        </i-form-item>
        <i-form-item prop="startDate" inline label="创建时间">
          <i-date-picker format="yyyy-MM-dd HH:mm:ss" @on-change="getStartTime" type="datetime" placeholder="请选择开始时间" style="width: 160px"></i-date-picker>
        </i-form-item>
        <i-form-item prop="endDate" :label-width="5">
          <i-date-picker  format="yyyy-MM-dd HH:mm:ss" @on-change="getEndTime" type="datetime" placeholder="请选择结束时间" style="width: 160px"></i-date-picker>
        </i-form-item>
        <i-form-item :label-width="20">
          <i-button type="primary" @click="getUserInfoList()" icon="ios-search">查询</i-button>
          <i-button type="primary" @click="showMask()"  icon="plus">新增</i-button>
        </i-form-item>
      </i-row>
    </i-form>
    <div>
      <i-table border stripe :loading="loading" :columns="userInfoColumns" :data="userInfoArray" class="user_grid" @on-sort-change='changeSort'></i-table>
    </div>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <i-page :total="dataCount" :page-size="pageSize" :current.sync="currentPage" show-total
                @on-change="changePage"></i-page>
      </div>
    </div>

    <i-modal width="800px"
             :title="this.maskTitle"
             v-model="maskModal"
             :mask-closable="false"
             :closable="false" class-name="vertical-center-modal">
      <i-form ref="editFormItem" :model="editFormItem" :label-width="80" :rules="ruleValidate">
        <i-row>
          <i-col span="12">
            <i-form-item label="姓名" prop="name">
              <i-input placeholder="请输入姓名" v-model="editFormItem.name" style="width:160px"></i-input>
            </i-form-item>

            <i-form-item  label="账号" prop="userCode">
              <i-input v-model="editFormItem.userCode" placeholder="请输入检索账号" style="width:160px">
              </i-input>
            </i-form-item>

            <i-form-item prop="roleId" label="角色">
              <i-select v-model="editFormItem.roleId"  style="width:160px" placeholder="请选择待筛选角色">
                <i-option v-for="item in roleInfoList" :value="item.roleId" :key="item.roleId">{{ item.roleName }}</i-option>
              </i-select>
            </i-form-item>

          </i-col>
          <i-col span="12">
            <i-form-item label="性别" prop="sex" >
              <i-select v-model="editFormItem.sex" placeholder="请选择性别" style="width:160px">
                <i-option value="1">男</i-option>
                <i-option value="2">女</i-option>
              </i-select>
            </i-form-item>

            <i-form-item  label="密码" prop="password" >
              <i-input v-model="editFormItem.password"  style="width:160px">
              </i-input>
            </i-form-item>

            <i-form-item  label="电话" prop="phone">
              <i-input v-model="editFormItem.phone" style="width:160px" placeholder="请输入电话号码">
              </i-input>
            </i-form-item>

          </i-col>
        </i-row>
      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveUserInfo" v-show="editFormItem.operatorType!=='2'">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>
    </i-modal>


  </div>
</template>

<script>
  import Vue from 'vue';
  import Cookies from 'js-cookie';
  import VueI18n from 'vue-i18n';
  Vue.use(VueI18n);

  export default {
    name: 'user-manager',
    data () {
      return {
        condition: {
          userName: '',//过滤条件用户名称
          userCode:'',//过滤工号
          deptCode:'',//过滤科室编码
          sex:'',//性别
          roleId :'',//角色编码
          status:'',//状态
          startDate: '',//开始时间
          endDate:''//结束时间
        },
        deptArray:[],
        loading: false,
        roleInfoList:[],//角色信息列表
        userInfoArray: [],//用户信息数据
        currentPage: 1,
        sortName:'',
        sortOrder:'',
        partList:[],
        emptyMask: {
          id: '',
          name: '',
          userCode: '',
          password:'888888',
          roleId:'',
          state:0,
          sex:'',
          deptCode:'',
          hosCode:JSON.parse(Cookies.get('userInfo')).hosCode,
          createUser:'',
          modifyUser:'',
          userPhoto:'',
          username:'',
          entryTime:'',
          operatorType:'0'
        },
        headers: {
          token: Cookies.get('token')
        },
        maskTitle: '用户信息维护',
        editFormItem: {},
        editSetMealItem:{},
        maskModal: false,
        setMealModal: false,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 12,
        userInfoColumns: [
          {
            title: '姓名',
            key: 'name'
          },
          {
            title: '性别',
            key: 'sexVal',
          },
          {
            title: '账号',
            key: 'userCode'
          },
          {
            title: '角色',
            key: 'roleName'
          },
          {
            title: '创建时间',
            key: 'createTime',
            sortname:'rs.createTime',
            sortable:'custom'
          },
          {
            title: '操作',
            key: 'action',
            width: 360,
            align: 'center',
            render: (h, params) => {
              let btns = []
              btns.push(
                h('i-button', {
                  props: {
                    type: 'primary',
                    size: 'small',
                    icon:'edit'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      params.row.operatorType = '1'
                      this.showMask(params.row)
                    }
                  }
                }, '编辑')
              )
              params.row.state==='0' && btns.push(
                h('i-button', {
                  props: {
                    type: 'primary',
                    size: 'small',
                    icon:'ios-close-outline'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      this.changeState(params.row)
                    }
                  }
                }, '冻结')
              )
              params.row.state==='1' && btns.push(
                h('i-button', {
                  props: {
                    type: 'warning',
                    size: 'small',
                    icon:'ios-checkmark-outline'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      this.changeState(params.row)
                    }
                  }
                }, '解冻')
              )

              btns.push(
                h('i-button', {
                  props: {
                    type: 'primary',
                    size: 'small',
                    icon:'ios-loop-strong'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      this.resetPassword(params.row)
                    }
                  }
                }, '重置密码')
              )
              return h("div",btns)
            }
          }
        ],
        ruleValidate: {
          name: [
            { required: true, message: this.$t('validation.user.nameNotEmpty'), trigger: 'blur' }
          ],
          sex: [
            { required: true, message: '请选择性别', trigger: 'blur' }
          ],
          userCode: [
            { required: true, message: '账号号不能为空', trigger: 'blur' }
          ],
          roleId: [
            { required: true, message: '请选择角色', trigger: 'blur' }
          ],
          state: [
            { required: true, message: '请选择状态', trigger: 'blur' }
          ]
        },
        ruleValidate2:{
          part: [
            { required: true,type: 'array', message: '请选择基本功能', trigger: 'blur' }
          ]
        },
        options3: {
          disabledDate (date) {
            return date && date.valueOf() < Date.now() - 86400000;
          }
        }
      }
    },

    methods: {
      changeSort(column){
        this.sortName=column.column.sortname;
        this.sortOrder=column.order;
        this.getUserInfoList()
      },
      getStartTime(starTime) {
        this.condition.startDate = starTime
      },
      getEndTime(endTime) {
        this.condition.endDate = endTime
      },
      getEntryTime(entryTime) {
        this.editFormItem.entryTime = entryTime
      },
      // 获取历史记录信息
      changePage (index) {
        this.getUserInfoList(index)
      },
      //弹出用户辑窗口
      showMask (rows) {
        this.editFormItem = JSON.parse(JSON.stringify(this.emptyMask))
        if (!!rows) {
          this.editFormItem = JSON.parse(JSON.stringify(rows))
          this.editFormItem.password=""
        }
        this.maskModal = true
      },


      //获取用户信息
      getUserInfoList (pageIndex) {
        this.userInfoArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/user/getUserInfoByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'sortName':this.sortName,
            'sortOrder':this.sortOrder,
            'userInfo': {
              'name': this.condition.userName,
              'userCode': this.condition.userCode,
              'deptCode':this.condition.deptCode,
              'sex':this.condition.sex,
              'roleId':this.condition.roleId,
              'state':this.condition.status,
              'startDate':this.condition.startDate,
              'endDate':this.condition.endDate
            }
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.userInfoArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取用户列表数据失败')
          }
        })
      },

      //获取初始化科室和角色列表信息
      getDeptAndRoleInfo () {
        let hosCode = JSON.parse(Cookies.get('userInfo')).hosCode
        let userInfo = JSON.parse(Cookies.get('userInfo'))
        this.$axios.ajax({
          method: 'get',
          url: '/user/getRoleInfo',
          data: {}
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            //this.roleInfoList= data['roleInfo']
            let roleList  = data['roleInfo']
            for(let i = 0; i < roleList.length; i++) {
              if(roleList[i].comment=='3'){
                let role=[]
                let roleInfo=roleList[i]
                role.push(roleInfo)
                this.roleInfoList=role
              }
            }

          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取科室和角色信息失败')
          }
        })
      },
      closeModal () {
        this.maskModal = false
      },
      //保存用户信息
      saveUserInfo () {
        this.$refs['editFormItem'].validate((valid) => {
          if (valid) {
            if(this.editFormItem.roleName=='主任'){
              this.editFormItem.type=1
            }else if(this.editFormItem.roleName=='医生'){
              this.editFormItem.type=2
            }
            if(!!this.editFormItem.createUser)
              this.editFormItem.modifyUser=JSON.parse(Cookies.get('userInfo')).name
            else
              this.editFormItem.createUser = JSON.parse(Cookies.get('userInfo')).name

            this.$axios.ajax({
              method: 'post',
              url: '/user/saveOrUpdateUserInfo',
              data: this.editFormItem
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('保存用户信息成功')
                this.getUserInfoList()
                this.maskModal = false
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存用户信息失败')
              }
            })
          }
        })
      },

      changeState(rows){
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p>确认修改当前状态？</p>',
          onOk: () => {
            this.$axios.ajax({
              method: 'get',
              url: '/user/changUserStateById?userId=' + JSON.parse(JSON.stringify(rows)).id,
              data: {}
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('状态更改成功')
                this.getUserInfoList()
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '状态更改失败')
              }
            })
          },
          onCancel: () => {
            return;
          }
        });
      },

      resetPassword(rows) {
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p>确认重置密码？</p>',
          onOk: () => {
            this.$axios.ajax({
              method: 'get',
              url: '/user/resetPassword?userId=' + JSON.parse(JSON.stringify(rows)).id,
              data: {}
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('密码重置成功')
                this.getUserInfoList()
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '密码重置失败')
              }
            })
          },
          onCancel: () => {
            return;
          }
        });
      },
      getPartList() {
        this.$axios.ajax({
          method: 'post',
          url: '/bodyPart/getPartList'
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.partList = !!data ? data : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取部位数据失败')
          }
        })
      }
    },
    created () {
      this.getPartList()
      this.getUserInfoList()//获取用户信息列表
      this.getDeptAndRoleInfo()//获取初始化科室和角色列表信息
    }
  }
</script>

<style>
  .user-content {
    width: 100%;
    height: 100%;
  }

  .user_grid {
    height: calc(10% - 300px);
    overflow: auto;
  }

  .vertical-center-modal {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .vertical-center-modal .ivu-modal {
    top: 0;
  }
</style>

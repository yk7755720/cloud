<style >
  .cusChange-content {
    width: 100%;
    height: 100%;
  }
</style>
<template>
  <div class="cusChange-content">
    <i-form inline :label-width="80">
      <i-form-item inline label="客户">
        <i-input type="text" v-model="cusChange.talkerCode" placeholder="请输入客户">
        </i-input>
      </i-form-item>
      <i-form-item inline label="设计师">
        <i-input type="text" v-model="cusChange.userCode" placeholder="请输入设计师">
        </i-input>
      </i-form-item>
      <i-form-item :label-width="20">
        <i-button type="primary" @click="getCusChangeList()">查询</i-button>
      </i-form-item>
    </i-form>
    <div>
      <i-table border :columns="cusChangeColumns"
               :data="cusChangeArray" ></i-table>
    </div>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <i-page :total="dataCount" :page-size="pageSize" :current.sync="currentPage" show-total
                @on-change="changePage"></i-page>
      </div>
    </div>

    <i-modal width="400px"
             title="选择设计师"
             v-model="maskModal"
             :mask-closable="false"
             @on-ok="addSimpleUser"
             :closable="false" >
      <i-row>
        <i-col span="9">
          <i-col span="12">
            <i-radio-group v-model="userCode"  >
              <i-radio v-for="(list,index) in simpleUserList" :label="list.id" :key="list.id">{{list.userName}}</i-radio>
            </i-radio-group>
          </i-col>
        </i-col>
      </i-row>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveCusChangeInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>
    </i-modal>
  </div>
</template>

<script>
  import util from '@/libs/util'

  export default {
    name: 'cusChange-manage',
    data () {
      return {
        baseUrl: util.getBaseUrl(),
        cusChange: {talkerCode:"",userCode:""},
        editFormItem: {},
        currentPage: 1,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 10,
        maskModal:false,
        optIndex:0,
        userCode:"",
        simpleUserList:[],
        cusChangeArray: [],
        recordId:"",
        cusChangeColumns: [
          {
            title: '客户名称',
            key: 'talkerName'
          },
          {
            title: '客户电话',
            key: 'talkerPhone'
          },
          {
            title: '设计师名称',
            key: 'userName'
          },
          {
            title: '设计师账号',
            key: 'userCode'
          },
          {
            title: '操作',
            key: 'action',
            width: 300,
            align: 'center',
            render: (h, params) => {
              return h('div', [
                h('i-button', {
                  props: {
                    type: 'primary',
                    size: 'small'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      this.addSimpleUser(params.row)
                    }
                  }
                }, '变更所属设计师')])
            }
          }
        ]
      }
    },
    methods: {
      changePage (index) {
        this.getCusChangeList(index)
      },
      getCusChangeList(pageIndex) {
        this.cusChangeArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/visitor/getVisitorByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'talkerCode':this.cusChange.talkerCode,
            'userCode':this.cusChange.userCode
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.cusChangeArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取记录列表数据失败')
          }
        })
      },
      closeModal () {
        this.userCode=""
        this.maskModal = false
      },
      saveCusChangeInfo(){
        this.$axios.ajax({
          method: 'get',
          url: '/visitor/updateToOtherUser?userId='+this.userCode+"&id="+this.recordId
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            this.$Message.success('保存信息成功')
            this.getCusChangeList()
            this.maskModal = false
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存信息失败')
          }
        })
      },
      getSimpleUserList() {
        this.$axios.ajax({
          method: 'get',
          url: '/visitor/getSimpleUserByRole'
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.simpleUserList = !!data ? data : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取科室列表数据失败')
          }
        })
      },
      addSimpleUser(rows){
        this.recordId=rows.id
        this.maskModal = true
      }
    },
    created () {
      this.getCusChangeList()
      this.getSimpleUserList()
    }
  }
</script>



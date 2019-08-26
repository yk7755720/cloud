<template>
  <div class="template-content">
    <i-form inline :label-width="80">
      <i-form-item inline label="模板名称">
        <i-input type="text" v-model="template.tempName" placeholder="请输入名称">
        </i-input>
      </i-form-item>
      <i-form-item :label-width="20">
        <i-button type="primary" @click="getTemplateList()">查询</i-button>
        <i-button type="primary" @click="addTempInfo()"  icon="plus">新增</i-button>
      </i-form-item>
    </i-form>
    <div>
      <i-table border :columns="templateColumns"
               :data="templateArray" class="template_grid"></i-table>
    </div>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <i-page :total="dataCount" :page-size="pageSize" :current.sync="currentPage" show-total
                @on-change="changePage"></i-page>
      </div>
    </div>

    <i-modal width="700px"
             title="模板设置"
             v-model="maskModal"
             :mask-closable="false"
             :closable="false" class-name="vertical-center-modal">
      <i-form ref="editFormItem" :model="editFormItem" :label-width="80" :rules="ruleValidate" >

        <i-row>
          <i-col span="9">
            <i-form-item label="模板名称"  prop="tempName">
              <i-col span="12">
                <i-input  v-model="editFormItem.tempName"  style="width:180px" ></i-input>
              </i-col>
            </i-form-item>
          </i-col>
        </i-row>

        <i-row>
          <i-col span="9">
            <i-form-item label="变形参数"  >
              <i-col span="12">
                <i-button v-on:click="addTrans">新增参数</i-button>
              </i-col>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <div v-for="(list,index) in transList">
            <i-col span="12">
              <span>名称</span><i-input  v-model="list.transName"  style="width:80px" ></i-input>
              <span>数值</span><i-input  v-model="list.transValue"  style="width:80px" ></i-input>
              <i-button v-on:click="delTrans(index)">删除</i-button>
            </i-col>
          </div>
        </i-row>

      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveTempInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>

    </i-modal>
  </div>
</template>

<script>
  import util from '@/libs/util'
  import Cookies from 'js-cookie'

  export default {
    name: 'template-manage',
    data () {
      return {
        baseUrl: util.getBaseUrl(),
        template: {tempName:""},
        editFormItem: {},
        transList:[],
        maskModal:false,
        currentPage: 1,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 10,
        templateArray: [],
        templateColumns: [
          {
            title: '模板名称',
            key: 'tempName'
          },
          {
            title: '创建时间',
            key: 'createTime',
             render: (h, params) => {
              return h('div', [
                h('strong', util.dateToFormat(params.row.createTime))
              ]);
            }
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
                      this.addTempInfo(params.row)
                    }
                  }
                }, '编辑'),
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
                      this.delTempInfo(params.row)
                    }
                  }
                }, '删除')])
            }
          }
        ],
        ruleValidate: {
          tempName: [
            { required: true, message: '名称不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      changePage (index) {
        this.getTemplateList(index)
      },

      //获取资源信息
      getTemplateList (pageIndex) {
        this.templateArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/template/getTemplateInfoByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'tempName':this.template.tempName
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.templateArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取数据失败')
          }
        })
      },
      saveTempInfo(){
        this.$axios.ajax({
          method: 'post',
          url: '/template/saveOrUpdateTemplate',
          data: {
            'id':this.editFormItem.id,
            'tempName':this.editFormItem.tempName,
            'transList':this.transList
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            this.$Message.success('保存成功')
            this.getTemplateList()
            this.maskModal = false
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存失败')
          }
        })
      },
      closeModal () {
        this.transList=[]
        this.editFormItem={}
        this.maskModal = false
        this.$refs['editFormItem'].resetFields()
      },
      addTrans(){
        let cope = {
          transName:"",
          transValue:""
        }
        this.transList.push(cope);
      },
      delTrans(index){
        this.transList.splice(index,1);
      },
      delTempInfo(rows){
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p1>确认删除该模板？</p1>',
          onOk: () => {
            this.$axios.ajax({
              url: '/template/delTemplateById?id='+rows.id
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('删除成功')
                this.getTemplateList()
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '删除失败')
              }
            })
          },
          onCancel: () => {
            return;
          }
        })
      },
      addTempInfo(rows){
        if (!!rows) {
          this.editFormItem.tempName=rows.tempName
          this.editFormItem.id=rows.id
          this.$axios.ajax({
            method: 'post',
            url: '/template/getTransList',
            data: {
              'id':rows.id
            }
          }).then((res) => {
            if (!!res.data.code && 10000 == res.data.code) {
              let data = res.data.data.transList
              this.transList=data
            } else {
              this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取列表数据失败')
            }
          })
        }
        this.maskModal = true
      }
    },

    created () {
      this.getTemplateList()//获取资源列表
    }
  }
</script>

<style scoped>
  .template-content {
    width: 100%;
    height: 100%;
  }

  .template_grid {
    height: calc(10% - 240px);
    overflow: auto;
  }

</style>

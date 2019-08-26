<template>
  <div class="disease-content">
    <i-form inline :label-width="80">
      <i-row>

        <i-form-item  inline label="编号">
          <i-input type="text" v-model="parameter.paramName" placeholder="请输入检索编号名称">
          </i-input>
        </i-form-item>

        <i-form-item prop="optId" inline label="部位">
          <i-input type="text" v-model="parameter.partName" placeholder="请输入检索部位名称">
          </i-input>
        </i-form-item>

        <i-form-item prop="optId" inline label="术式">
          <i-input type="text" v-model="parameter.optName" placeholder="请输入检索术式名称">
          </i-input>
        </i-form-item>

        <i-form-item :label-width="20">
          <i-button type="primary" @click="getParamsInfoList()" icon="ios-search">查询</i-button>
          <i-button type="primary" @click="addParamsInfo()"  icon="plus">新增</i-button>
        </i-form-item>
      </i-row>
      </i-form>
    <div>
      <i-table border stripe :loading="false" :columns="paramInfoColumns" :data="paramInfoArray" class="disease_grid" ></i-table>
    </div>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <i-page :total="dataCount" :page-size="pageSize" :current.sync="currentPage" show-total
                @on-change="changePage"></i-page>
      </div>
    </div>

    <i-modal width="700px"
             :title="this.maskTitle"
             v-model="maskModal"
             :mask-closable="false"
             :closable="false" class-name="vertical-center-modal">
      <i-form ref="editFormItem" :model="editFormItem" :label-width="80" :rules="ruleValidate" >
        <i-row>
          <i-col span="12">
            <i-form-item label="编号" >
              <i-input  v-model="editFormItem.id"  style="width:180px" disabled></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="9">
            <i-form-item label="画像位置"  >
              <i-radio-group v-model="editFormItem.ptype" >
                <i-radio label="1">客户画像</i-radio>
                <i-radio label="2">方案画像</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
          	<i-form-item label="部位" prop="partId">
              <i-radio-group v-model="editFormItem.partId"  @on-change="changePart">
                <i-radio v-for="(list,index) in partList" :label="list.partId" :key="index">{{list.partName}}</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="9">
            <i-form-item label="美容术式" prop="optId" >
              <i-radio-group v-model="editFormItem.optId">
                <i-radio v-for="(list) in optList" :label="list.optId" :key="list.optId">{{list.optName}}</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
          	<i-form-item label="名称" prop="paramName">
              <i-input placeholder="请输入名称" v-model="editFormItem.paramName"  style="width:180px"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="排序" >
              <i-input placeholder="请输入名称" v-model="editFormItem.sortNum"  style="width:180px"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-button v-on:click="addParam">新增参数</i-button>
          </i-col>
        </i-row>
        <i-row>
          <div v-for="(list,index) in editFormItem.childParamsList" :key="index">
            <i-col span="12">
              <span>名称</span><i-input  v-model="list.paramName"  style="width:80px" ></i-input>
              <span>价格</span><i-input  v-model="list.price"  style="width:80px" ></i-input>
              <span v-show="list.paramType!=0">单位</span><i-input v-show="list.paramType!=0" v-model="list.unit"  style="width:80px" ></i-input>
              <span v-show="list.paramType==2">最小值</span><i-input v-show="list.paramType==2" v-model="list.paramOne"  style="width:80px" ></i-input>
              <span v-show="list.paramType==2">最大值</span><i-input v-show="list.paramType==2" v-model="list.paramTwo"  style="width:80px" ></i-input>
            </i-col>
            <i-col span="12">
              <span>类型</span>
              <i-select  v-model="list.paramType"  style="width:100px" >
                <Option value="0">普通参数</Option>
                <Option value="1">单位参数</Option>
                <Option value="2">进度条</Option>
              </i-select>
              <span>排序</span><i-input  v-model="list.sortNum"  style="width:50px" ></i-input>
            </i-col>
            <i-button v-on:click="delParam(index)">删除</i-button>
          </div>
        </i-row>
      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveParamsInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>
    </i-modal>
  </div>
</template>

<script>
  import Cookies from 'js-cookie'

  export default {
    name: 'param-manager',
    data () {
      return {
        parameter:{
          paramName:'',
          partName:'',
          optName:''
        },
        partList:[],
        optList:[],
        paramInfoArray:[],
        currentPage: 1,
        loading:false,
        headers: {
          token: Cookies.get('token')
        },
        maskTitle: '参数信息维护',
        editFormItem: {

        },
        maskModal: false,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 13,
        paramInfoColumns: [
          {
            title: '参数编号',
            key: 'id'
          },
          {
            title: '参数名称',
            key: 'paramName',
          },
          {
            title: '美容术式',
            key: 'optName'
          },
          {
            title: '部位',
            key: 'partName'
          },
          {
            title: '画像位置',
            key: 'ptype',
            render: (h, params) => {
              return h('div', [
                h('strong', this.getLabelBypType(params.row.ptype))
              ]);
            }
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
                      this.addParamsInfo(params.row)
                    }
                  }
                }, '编辑'),h('i-button', {
                  props: {
                    type: 'primary',
                    size: 'small',
                    icon:'eye'
                  },
                  style: {
                    marginRight: '5px'
                  },
                  on: {
                    click: () => {
                      params.row.operatorType = '2'
                      this.delParamsInfo(params.row)
                    }
                  }
                }, '删除')
              )
              return h("div",btns)
            }
          }
        ],
        ruleValidate: {
          partId: [
            { required: true, message: '部位不能为空', trigger: 'blur' }
          ],
          optId: [
            { required: true, message: '美容术式不能为空', trigger: 'blur' }
          ],
          paramName: [
            { required: true, message: '参数不能为空', trigger: 'blur' }
          ]
        }
      }
    },

    methods: {
      getLabelBypType(code) {
        let label = ''
        switch(code) {
          case '1':
            label = '客户画像'
            break
          case '2':
            label = '方案画像'
            break
          default:
        }
        return label
      },

      // 获取历史记录信息
      changePage (index) {
        this.getParamsInfoList(index)
      },
      //弹出用户辑窗口
      addParamsInfo(rows) {
        var obj={}
        obj.childParamsList=[]
        this.editFormItem=obj
        if (!!rows) {
          let dataRow = JSON.parse(JSON.stringify(rows))
          //dataRow.ptype=dataRow.ptype.split(",")
          this.editFormItem = dataRow
          for(let i = 0; i < this.partList.length; i++) {
            if(this.partList[i].partId==this.editFormItem.partId){
              this.optList=this.partList[i].optList
            }
          }
        }
        this.maskModal = true
      },

      //获取参数信息
      getParamsInfoList (pageIndex) {
        this.optInfoArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/params/getParamsInfoByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'paramName': this.parameter.paramName,
            'partName':this.parameter.partName,
            'optName':this.parameter.optName
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.paramInfoArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取科室列表数据失败')
          }
        })
      },
      getPartList () {
        this.$axios.ajax({
          method: 'post',
          url: '/bodyPart/getPartList'
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.partList = !!data ? data : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取科室列表数据失败')
          }
        })
      },
      changePart(index){
        for(let i = 0; i < this.partList.length; i++) {
          if(this.partList[i].partId==index){
            this.optList=this.partList[i].optList
          }
        }
      },
      closeModal () {
        this.editFormItem={}
        this.maskModal = false
        this.$refs['editFormItem'].resetFields()
      },
       //删除参数信息
	   delParamsInfo(rows) {
	       this.$Modal.confirm({
	          title: '确认信息',
	          content: '<p1>确认删除该参数？</p1>',
	          onOk: () => {
	            let optData = JSON.parse(JSON.stringify(rows))
	            this.$axios.ajax({
	              method: 'post',
	              url: '/params/delParamsInfo',
	              data:{
	                'id':optData.id
                }
	            }).then((res) => {
	              if (!!res.data.code && 10000 == res.data.code) {
	                this.$Message.success('删除成功')
	                this.getParamsInfoList()
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
      addParam(){
        let cope = {
          paramName:"",
          price:"",
          paramType:"0",
          unit:"",
          sortNum:0,
          paramOne:"0",
          paramTwo:"100"
        }
        this.editFormItem.childParamsList.push(cope);
      },
      delParam(index){
        this.editFormItem.childParamsList.splice(index,1);
      },
      //保存参数信息
      saveParamsInfo () {
        this.$refs['editFormItem'].validate((valid) => {
          if (valid) {
            this.$axios.ajax({
              method: 'post',
              url: '/params/saveOrUpdateParamsInfo',
              data:{
                'id':this.editFormItem.id,
                'paramName':this.editFormItem.paramName,
                'optName':this.editFormItem.optName,
                'partName':this.editFormItem.partName,
                'partId':this.editFormItem.partId,
                'optId':this.editFormItem.optId,
                'ptype':this.editFormItem.ptype,
                'sortNum':this.editFormItem.sortNum,
                'childParamsList':this.editFormItem.childParamsList
              }
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('保存术式信息成功')
                this.getParamsInfoList(this.currentPage)
                this.maskModal = false
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存疾病信息失败')
              }
            })
          }
        })
      },
    },
    created () {
      this.getParamsInfoList()
      this.getPartList()
    }
  }
</script>

<style>
  .disease-content {
    width: 100%;
    height: 100%;
  }

  .disease_grid {
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

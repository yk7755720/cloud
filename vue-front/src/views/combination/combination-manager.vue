<template>
  <div class="disease-content">
    <i-form inline :label-width="80">
      <i-row>

        <i-form-item  inline label="参数名">
          <i-input type="text" v-model="comb.paramName" placeholder="请输入参数组名称">
          </i-input>
        </i-form-item>

        <i-form-item  inline label="术式名">
          <i-input type="text" v-model="comb.optName" placeholder="请输入术式名称">
          </i-input>
        </i-form-item>

        <i-form-item  inline label="部位">
          <i-input type="text" v-model="comb.partName" placeholder="请输入部位名称">
          </i-input>
        </i-form-item>

        <i-form-item  label="所属画像">
          <i-select clearable v-model="comb.ptype" style="width:160px" placeholder="请选择待筛选画像">
            <i-option value="1">客户画像</i-option>
            <i-option value="2">方案画像</i-option>
          </i-select>
        </i-form-item>



        <i-form-item :label-width="20">
          <i-button type="primary" @click="getCombInfoList()" icon="ios-search">查询</i-button>
          <i-button type="primary" @click="addCombInfo()"  icon="plus">新增</i-button>
        </i-form-item>
      </i-row>
    </i-form>
    <div>
      <i-table border stripe :loading="false" :columns="combInfoColumns" :data="combInfoArray" class="disease_grid" ></i-table>
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
              <i-radio-group v-model="editFormItem.ptype" @on-change="changePtype">
                <i-radio label="1" key="1">客户画像</i-radio>
                <i-radio label="2" key="2">方案画像</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="部位" prop="partId">
              <i-radio-group v-model="editFormItem.partId"  @on-change="changePart">
                <i-radio v-for="(list,index) in partList" :label="list.partId" :key="list.partId">{{list.partName}}</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="9">
            <i-form-item label="美容术式" prop="optId" >
              <i-radio-group v-model="editFormItem.optId" @on-change="changeOpt">
                <i-radio v-for="(list,index) in optList" :label="list.optId" :key="list.optId">{{list.optName}}</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="14">
            <i-form-item label="选取子参数"  >
              <CheckboxGroup v-model="editFormItem.params" >
                <Checkbox v-for="(list,index) in childParamList" :label="list.id" :key="list.id">{{list.paramName}}</Checkbox>
              </CheckboxGroup>
            </i-form-item>
          </i-col>
        </i-row>

        <i-row>
          <i-col span="12">
            <i-form-item label="选择变形参数模板" >
              <i-radio-group v-model="tempId"  @on-change="changeTemp">
                <i-radio  :label="0" :key="0">无</i-radio>
                <i-radio v-for="(list,index) in tempList" :label="list.id" :key="list.id">{{list.tempName}}</i-radio>
              </i-radio-group>
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
        <div v-for="(list,index) in editFormItem.transList">
          <i-col span="12">
            <span>名称</span><i-input  v-model="list.transName"  style="width:80px" ></i-input>
            <span>数值</span><i-input  v-model="list.transValue"  style="width:80px" ></i-input>
            <i-button v-on:click="delTrans(index)">删除</i-button>
          </i-col>
        </div>
        </i-row>

      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveCombInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>

    </i-modal>

  </div>

</template>

<script>
  import Cookies from 'js-cookie'
  import util from '@/libs/util'

  export default {
    name: 'combination-manager',
    data () {
      return {
        comb:{
          optName:'',
          partName:'',
          ptype:'',
          paramName:''
        },
        tempId:'0',
        baseUrl: util.getBaseUrl(),
        partList:[],
        optList:[],
        paramList:[],
        tempList:[],
        childParamList:[],
        successCode: 10000,
        editFormPosItem: {},
        notShowUploadList: false,
        combList:[],
        combInfoArray:[],
        currentPage: 1,
        loading:false,
        headers: {
          token: Cookies.get('token')
        },
        maskTitle: '参数组',
        editFormItem: {

        },
        maskModal: false,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 13,
        combInfoColumns: [
          {
            title: '编号',
            key: 'id'
          },
          {
            title: '参数组',
            key: 'params',
            render: (h, params) => {
              return h('div', [
                h('strong', this.getParamsById(params.row))
              ]);
            }
          },
          {
            title: '所属画像',
            key: 'ptype',
            render: (h, params) => {
              return h('div', [
                h('strong', this.getLabelByType(params.row.ptype))
              ]);
            }
          },
          {
            title: '术式',
            key: 'optName'
          },
          {
            title: '部位',
            key: 'partName'
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
                      this.addCombInfo(params.row)
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
                      this.delCombInfo(params.row)
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
          combName: [
            { required: true, message: '名称不能为空', trigger: 'blur' }
          ]
        }
      }
    },

    methods: {

      // 获取历史记录信息
      changePage (index) {
        this.getCombInfoList(index)
      },

      //弹出用户辑窗口
      addCombInfo(rows) {
        var obj={}
        obj.transList=[]
        this.editFormItem=obj
        this.childParamList=[]
        if (!!rows) {
          let dataRow = JSON.parse(JSON.stringify(rows));
          this.editFormItem = dataRow;
          for(let i = 0; i < this.partList.length; i++) {
            if(this.partList[i].partId==this.editFormItem.partId){
              this.optList=this.partList[i].optList
            }
          }
          let params=[]
          for(let i = 0; i < this.paramList.length; i++) {
            if(this.editFormItem.ptype==1){
              //客户画像没有术式，所以关联所有术式
              for(let p = 0; p < this.optList.length; p++){
                if(this.optList[p].optId==this.paramList[i].optId){
                  for(let j = 0; j < this.paramList[i].childList.length; j++){
                    //进度条排除
                    //if(this.paramList[i].childList[j].paramType!='2'){
                      let ptype = this.paramList[i].ptype.split(",")
                      if(ptype.length>1){
                        let param={}
                        param.id=this.paramList[i].childList[j].id+""
                        param.paramName=this.paramList[i].childList[j].paramName
                        params.push(param)
                      }else{
                        if(this.editFormItem.ptype==ptype[0]){
                          let param={}
                          param.id=this.paramList[i].childList[j].id+""
                          param.paramName=this.paramList[i].childList[j].paramName
                          params.push(param)
                        }
                      }
                    //}
                  }
                }
              }
            }else{
              //方案画像和术士关联
              if(this.editFormItem.optId==this.paramList[i].optId){
                for(let j = 0; j < this.paramList[i].childList.length; j++){
                  //if(this.paramList[i].childList[j].paramType!='2'){
                    let ptype = this.paramList[i].ptype.split(",")
                    if(ptype.length>1){
                      let param={}
                      param.id=this.paramList[i].childList[j].id+""
                      param.paramName=this.paramList[i].childList[j].paramName
                      params.push(param)
                    }else{
                      if(this.editFormItem.ptype==ptype[0]){
                        let param={}
                        param.id=this.paramList[i].childList[j].id+""
                        param.paramName=this.paramList[i].childList[j].paramName
                        params.push(param)
                      }
                    }
                  //}
                }
              }
            }

          }
          this.childParamList=params
          this.editFormItem.params=this.editFormItem.params.split(",")
        }
        this.maskModal = true
      },
      changePtype(index){
        let params=[]

        for(let i = 0; i < this.paramList.length; i++) {
          if(this.editFormItem.ptype==1){
            //客户画像没有术式，所以关联所有术式
            for(let p = 0; p < this.optList.length; p++){
              if(this.optList[p].optId==this.paramList[i].optId){
                for(let j = 0; j < this.paramList[i].childList.length; j++){
                  //if(this.paramList[i].childList[j].paramType!='2'){
                    let ptype = this.paramList[i].ptype.split(",")
                    if(ptype.length>1){
                      let param={}
                      param.id=this.paramList[i].childList[j].id+""
                      param.paramName=this.paramList[i].childList[j].paramName
                      params.push(param)
                    }else{
                      if(this.editFormItem.ptype==ptype[0]){
                        let param={}
                        param.id=this.paramList[i].childList[j].id+""
                        param.paramName=this.paramList[i].childList[j].paramName
                        params.push(param)
                      }
                    }
                  //}

                }
              }
            }
          }else{
            if(this.editFormItem.optId==this.paramList[i].optId){
              for(let j = 0; j < this.paramList[i].childList.length; j++){
                //if(this.paramList[i].childList[j].paramType!='2'){
                  let ptype = this.paramList[i].ptype.split(",")
                  if(ptype.length>1){
                    let param={}
                    param.id=this.paramList[i].childList[j].id+""
                    param.paramName=this.paramList[i].childList[j].paramName
                    params.push(param)
                  }else{
                    if(this.editFormItem.ptype==ptype[0]){
                      let param={}
                      param.id=this.paramList[i].childList[j].id+""
                      param.paramName=this.paramList[i].childList[j].paramName
                      params.push(param)
                    }
                  }
                //}

              }
            }
          }
        }
        this.childParamList=params
      },
      getCombInfoList (pageIndex) {
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/combination/getCombInfoByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'optName': this.comb.optName,
            'partName':this.comb.partName,
            'paramName':this.comb.paramName,
            'ptype':this.comb.ptype
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.combInfoArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取参数组失败')
          }
        })
      },

      closeModal () {
        for(let i = 0; i < this.combList.length; i++) {
          this.combList[i].paramKey=0
        }
        this.editFormItem={}
        this.maskModal = false
        this.tempId="0"
        this.$refs['editFormItem'].resetFields()
      },
      //删除参数信息
      delCombInfo(rows) {
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p1>确认删除该组合？</p1>',
          onOk: () => {
            this.$axios.ajax({
              url: '/combination/delCombById?id='+rows.id
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('删除成功')
                this.getCombInfoList()
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

      //保存参数信息
      saveCombInfo(){

        this.$refs['editFormItem'].validate((valid) => {
          if (valid) {
            this.$axios.ajax({
              method: 'post',
              url: '/combination/saveOrUpdateCombInfo',
              data:{
                'id':this.editFormItem.id,
                'ptype':this.editFormItem.ptype,
                'optId':this.editFormItem.optId,
                'childParams':this.editFormItem.params,
                'transParams':this.editFormItem.transList
              }
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('保存参数组成功')
                this.getCombInfoList()
                this.maskModal = false
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存参数组失败')
              }
            })
          }
        })
      },
      getParamList() {
        this.$axios.ajax({
          method: 'get',
          url: '/params/getParamsListByType'
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.paramList = !!data ? data : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取部位数据失败')
          }
        })
      },

      getTempList() {
        this.$axios.ajax({
          method: 'post',
          url: '/template/getTemplateAndTrans'
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.tempList = !!data ? data : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取数据失败')
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
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取部位数据失败')
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
      getLabelByType(code){
        let label = ''
        switch (code){
          case '1':
            label='客户画像'
            break
          case '2':
            label='方案画像'
            break
          default :
            label='客户画像,方案画像'
        }
        return label
      },
      getParamsById(row){
        let param=row.params.split(",")
        let label =""
        for(let i = 0; i < this.paramList.length; i++) {
          if(row.optId==this.paramList[i].optId){
            for(let j = 0; j < this.paramList[i].childList.length; j++) {
              for(let p = 0; p < param.length; p++) {
                if(this.paramList[i].childList[j].id==param[p]){
                  if(label==""){
                    label=this.paramList[i].childList[j].paramName
                  }else{
                    label+=","+this.paramList[i].childList[j].paramName
                  }
                }
              }
            }
          }
        }
        return label
      },
      changeOpt(index){
        let params=[]
        for(let i = 0; i < this.paramList.length; i++) {
          for(let j = 0; j < this.paramList[i].childList.length; j++){
            //if(this.paramList[i].childList[j].paramType!='2'){
              if(this.editFormItem.ptype==1){
                for(let p = 0; p < this.optList.length; p++){
                  if(this.optList[p].optId==this.paramList[i].optId){
                    let ptype = this.paramList[i].ptype.split(",")
                    if(ptype.length>1){
                      let param={}
                      param.id=this.paramList[i].childList[j].id+""
                      param.paramName=this.paramList[i].childList[j].paramName
                      params.push(param)
                    }else{
                      if(this.editFormItem.ptype==ptype[0]){
                        let param={}
                        param.id=this.paramList[i].childList[j].id+""
                        param.paramName=this.paramList[i].childList[j].paramName
                        params.push(param)
                      }
                    }
                  }
                }
              }else{
                if(index==this.paramList[i].optId){
                  let ptype = this.paramList[i].ptype.split(",")
                  if(ptype.length>1){
                    let param={}
                    param.id=this.paramList[i].childList[j].id+""
                    param.paramName=this.paramList[i].childList[j].paramName
                    params.push(param)
                  }else{
                    if(this.editFormItem.ptype==ptype[0]){
                      let param={}
                      param.id=this.paramList[i].childList[j].id+""
                      param.paramName=this.paramList[i].childList[j].paramName
                      params.push(param)
                    }
                  }
                }
              }
            //}

          }
        }
        this.childParamList=params
      },
      addTrans(){
        let cope = {
          transName:"",
          transValue:""
        }
        this.editFormItem.transList.push(cope);
      },
      delTrans(index){
        this.editFormItem.transList.splice(index,1);
      },
      changeTemp(index){
        this.editFormItem.transList=[]
        if(index==0){
          this.editFormItem.transList=[]
        }else{
          for(let i = 0; i < this.tempList.length; i++) {
            if(this.tempList[i].id==index){
              for(let j = 0; j < this.tempList[i].transList.length; j++) {
                let cope = {
                  transName:this.tempList[i].transList[j].transName,
                  transValue:this.tempList[i].transList[j].transValue
                }
                this.editFormItem.transList.push(cope);
              }
            }
          }
        }
      }

    },
    created () {
      this.getCombInfoList()
      this.getPartList()
      this.getParamList()
      this.getTempList()
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

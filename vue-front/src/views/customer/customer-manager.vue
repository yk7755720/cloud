<style >
  .customer-content {
    width: 100%;
    height: 100%;
  }

  .ivu-table td.demo-table-info-column{
    font-size: 15px;
  }
</style>
<template>
  <div class="customer-content">
    <i-form inline :label-width="80">
      <i-form-item inline label="接待客户">
        <i-input type="text" v-model="customer.talkName" placeholder="请输入接待客户">
        </i-input>
      </i-form-item>
      <i-form-item :label-width="20">
        <i-button type="primary" @click="getCustomerList()">查询</i-button>
      </i-form-item>
    </i-form>
    <div>
      <i-table border :columns="customerColumns"
               :data="customerArray" ></i-table>
    </div>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <i-page :total="dataCount" :page-size="pageSize" :current.sync="currentPage" show-total
                @on-change="changePage"></i-page>
      </div>
    </div>


    <i-modal width="700px"
             title="谈话记录"
             v-model="maskModal"
             :mask-closable="false"
             :closable="false" >
      <i-form ref="editFormItem" :model="editFormItem" :label-width="80" :rules="ruleValidate" >
        <i-row style="border-bottom: #cccccc 1px solid;padding-bottom: 5px">
          <span style="font-size: 14px">基础信息</span>
        </i-row>
        <i-row style="margin-top: 20px">
          <i-col span="6">
            <i-form-item label="姓名"  prop="talkName">
              <i-col span="9">
                <i-input  v-model="editFormItem.talkName"  style="width:90px" ></i-input>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="年龄"  prop="age">
              <i-col span="9">
                <i-input   v-model="editFormItem.age"  style="width:90px" number></i-input>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="电话"  prop="phone">
              <i-col span="9">
                <i-input  v-model="editFormItem.phone"  style="width:90px" ></i-input>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="来访记录"  >
              <i-col span="9">
                <span>{{editFormItem.times}}次</span>
                <!--<i-input  v-model="editFormItem.times"  style="width:50px" ></i-input>-->
              </i-col>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="备注"  >
              <i-col span="12">
                <i-input type="textarea"  v-model="editFormItem.remark" :rows="4" style="width:500px" ></i-input>
              </i-col>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row style="border-bottom: #cccccc 1px solid;padding-bottom: 5px">
          <span style="font-size: 14px">信息完善</span>
        </i-row>
        <i-row style="margin-top: 20px">
          <i-col span="6">
            <i-form-item label="性别"  prop="sex">
              <i-col span="9">
                <i-select  v-model="editFormItem.sex" style="width:60px" placeholder="请选择待筛选性别">
                  <i-option value="1">男</i-option>
                  <i-option value="2">女</i-option>
                </i-select>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="婚否"  prop="marriage">
              <i-col span="9">
                <i-select  v-model="editFormItem.marriage" style="width:60px" placeholder="请选择">
                  <i-option value="2">未婚</i-option>
                  <i-option value="1">已婚</i-option>
                </i-select>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="职业"  prop="career">
              <i-col span="9">
                <i-input  v-model="editFormItem.career"  style="width:90px" ></i-input>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="过敏史"  prop="allergy">
              <i-col span="9">
                <i-input  v-model="editFormItem.allergy"  style="width:90px" ></i-input>
              </i-col>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="6">
            <i-form-item label="年收入"  prop="annualIncome">
              <i-col span="9">
                <i-select  v-model="editFormItem.annualIncome" style="width:90px" placeholder="请选择">
                  <i-option value="0-10万">0-10万</i-option>
                  <i-option value="10-20万">10-20万</i-option>
                  <i-option value="20-50万">20-50万</i-option>
                  <i-option value="50-100万">50-100万</i-option>
                  <i-option value="100-500万">100-500万</i-option>
                  <i-option value="500-1000万">500-1000万</i-option>
                </i-select>
              </i-col>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="医美史"  >
              <i-col span="9">
                <i-button type="primary" @click="changeMedicalHistory()"  icon="plus">请选择</i-button>
              </i-col>
            </i-form-item>
          </i-col>
          <div v-for="(list,index) in medicalHistoryList">
            <i-col span="6">
              <span>{{list.name}}</span><i-input  v-model="list.value"  style="width:80px" ></i-input>
              <i-button v-on:click="delMedical(index)">删除</i-button>
            </i-col>
          </div>
        </i-row>
        <i-row style="border-bottom: #cccccc 1px solid;padding-bottom: 5px">
          <span style="font-size: 14px">来访记录</span>
        </i-row>

        <div v-for="(list,index) in editFormItem.customerParam" style="padding-bottom: 20px;border-bottom: #aaaaaa 1px dashed">
          <i-row style="margin-top: 20px">
            <i-col span="7">
              来访时间:<span style="margin-left: 10px">{{list.createTime}}</span>
            </i-col>
            <i-col span="7">
              来访部位:<span style="margin-left: 10px">{{list.partName}}</span>
            </i-col>
            <i-col span="7">
              消费项目:<span style="margin-left: 10px">{{list.consumer}}</span><a @click="changeConsumer(index)">请选择</a>
            </i-col>
          </i-row>
          <i-row style="margin-top: 20px">
            <i-col span="7">
              消费金额:<input  type="number"  v-model="list.price"  style="width:60px;margin-left: 10px;" >￥<span v-show="list.price!=list.bjdPrice" style="color: #e13c13;font-size: 18px">*</span>
            </i-col>
            <i-col span="7">
              报价单:<a style="margin-left: 10px" v-show="!!list.fileUrl" @click="openResourceFile(list.fileUrl)">查看</a>
              <span v-show="!list.fileUrl">空</span>
            </i-col>
            <i-col span="7">
              接待人:<span style="margin-left: 10px">{{list.userName}}</span>
            </i-col>
          </i-row>

        </div>
      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveCustomerInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>

    </i-modal>

    <i-modal width="400px"
             title="医美史选择"
             v-model="maskModaltwo"
             :mask-closable="false"
             @on-ok="medicalOk"
             :closable="false" >
      <i-row>
        <i-col span="9">
          <i-col span="12">
            <i-radio-group v-model="medicalHistory"  >
              <i-radio  label="眼部" >眼部</i-radio>
              <i-radio  label="鼻部" >鼻部</i-radio>
              <i-radio  label="脸型">脸型</i-radio>
              <i-radio  label="唇部">唇部</i-radio>
              <i-radio  label="胸部">胸部</i-radio>
              <i-radio  label="塑性">塑性</i-radio>
            </i-radio-group>
          </i-col>
        </i-col>
      </i-row>
    </i-modal>

    <i-modal width="500px"
             title="消费项目选择"
             v-model="maskModalthree"
             :mask-closable="false"
             @on-ok="consumerOk"
             :closable="false" >
      <i-row>
        <i-col span="12">
          <i-radio-group v-model="partId"  @on-change="changePart">
            <i-radio v-for="(list,index) in partList" :label="list.partId" :key="index">{{list.partName}}</i-radio>
          </i-radio-group>
        </i-col>
      </i-row>
      <i-row style="border-bottom: #cccccc 1px solid;padding-bottom: 5px">
      </i-row>
      <i-row style="margin-top: 20px">
        <i-col span="12">
          <CheckboxGroup v-model="optId">
            <Checkbox v-for="(list) in optList" :label="list.optName" >{{list.optName}}</Checkbox>
          </CheckboxGroup>
        </i-col>
      </i-row>
    </i-modal>
  </div>
</template>

<script>
  import util from '@/libs/util'

  export default {
    name: 'customer-manage',
    data () {
      return {
        baseUrl: util.getBaseUrl(),
        customer: {talkName:""},
        editFormItem: {},
        currentPage: 1,
        medicalHistory:"",
        medicalHistoryList:[],
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 10,
        maskModal:false,
        maskModaltwo:false,
        maskModalthree:false,
        optIndex:0,
        partList:[],
        optList:[],
        partId:"",
        optId:[],
        customerArray: [],
        customerColumns: [
          {
            title: '账号',
            className: 'demo-table-info-column',
            key: 'userName'
          },
          {
            title: '接待客户',
            className: 'demo-table-info-column',
            key: 'talkName'
          },
          {
            title: '客户电话',
            className: 'demo-table-info-column',
            key: 'phone'
          },
          {
            title: '首次接待时间',
            key: 'firstTime',
            className: 'demo-table-info-column',
            render: (h, params) => {
              return h('div', [
                h('normal', util.dateToFormat(params.row.firstTime))
              ]);
            }
          },

          {
            title: '最近接待时间',
            key: 'lastTime',
            className: 'demo-table-info-column',
            render: (h, params) => {
              return h('div', [
                h('normal', util.dateToFormat(params.row.lastTime))
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
                      this.addCustomer(params.row)
                    }
                  }
                }, '接待记录')])
            }
          }
        ],
        ruleValidate: {
          talkName: [
            { required: true, message: '名称不能为空', trigger: 'blur' }
          ],
          age: [
            { required: true,type: 'number', message: '年龄只能是数字', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {


      changePage (index) {
        this.getCustomerList(index)
      },
      medicalOk(){
        if(!this.medicalHistoryList){
          this.medicalHistoryList=[]
        }
        let cope = {
          name:this.medicalHistory,
          value:""
        }
        this.medicalHistoryList.push(cope);
      },
      consumerOk(){
        this.editFormItem.customerParam[this.optIndex].consumer=this.optId+""
      },
      delMedical(index){
        this.medicalHistoryList.splice(index,1);
      },
      changeMedicalHistory(){
        this.maskModaltwo=true
      },
      changeConsumer(index){
        this.optIndex=index
        this.partId=""
        this.optId=[]
        this.maskModalthree=true
      },
      changePart(index){
        for(let i = 0; i < this.partList.length; i++) {
          if(this.partList[i].partId==index){
            this.optList=this.partList[i].optList
          }
        }
      },
      openResourceFile(fileUrl){
        let url = this.baseUrl + '/model/downResourceFile?filePath=' + fileUrl;
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', "bjd")
        document.body.appendChild(link)
        link.click()
      },
      //获取资源信息


      getCustomerList (pageIndex) {
        this.customerArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/customer/getCustomerListByPage',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'userId':this.$route.params.userId,
            'talkName':this.customer.talkName
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.customerArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取记录列表数据失败')
          }
        })
      },
      closeModal () {
        this.medicalHistoryList=[]
        this.medicalHistory=""
        this.editFormItem={}
        this.maskModal = false
        this.$refs['editFormItem'].resetFields()
      },
      saveCustomerInfo(){
        this.$axios.ajax({
          method: 'post',
          url: '/customer/saveCustomer',
          data: {
            'talkName': this.editFormItem.talkName,
            'id': this.editFormItem.id,
            'phone':this.editFormItem.phone,
            'remark':this.editFormItem.remark,
            'age':this.editFormItem.age,
            'sex':this.editFormItem.sex,
            'marriage':this.editFormItem.marriage,
            'career':this.editFormItem.career,
            'allergy':this.editFormItem.allergy,
            'annualIncome':this.editFormItem.annualIncome,
            'medicalHistory':JSON.stringify(this.medicalHistoryList),
            'customerParam': this.editFormItem.customerParam
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            this.$Message.success('保存信息成功')
            this.getCustomerList()
            this.maskModal = false
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存疾病信息失败')
          }
        })
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
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取科室列表数据失败')
          }
        })
      },
      addCustomer(rows){
        if (!!rows) {
          this.$axios.ajax({
            method: 'get',
            url: '/customer/getCustomerParam?talkId='+rows.talkId+'&time='+new Date().getTime()
          }).then((res) => {
            if (!!res.data.code && 10000 == res.data.code) {
              let data = res.data.data
              this.editFormItem=data
              this.medicalHistoryList=JSON.parse(data.medicalHistory);
            } else {
              this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取列表数据失败')
            }
            this.maskModal = true
          })
        }

      }
    },

    created () {
      this.getCustomerList()//获取资源列表
      this.getPartList()
    }
  }
</script>



<template>
  <div class="disease-content">
    <i-form inline :label-width="80">
      <i-row>

        <i-form-item  inline label="方案名称">
          <i-input type="text" v-model="tcase.caseName" placeholder="请输入方案名称">
          </i-input>
        </i-form-item>

        <i-form-item prop="optId" inline label="术式">
          <i-input type="text" v-model="tcase.optName" placeholder="请输入术式名称">
          </i-input>
        </i-form-item>

        <i-form-item :label-width="20">
          <i-button type="primary" @click="getCaseInfoList()" icon="ios-search">查询</i-button>
          <i-button type="primary" @click="addCaseInfo()"  icon="plus">新增</i-button>
        </i-form-item>
      </i-row>
    </i-form>
    <div>
      <i-table border stripe :loading="false" :columns="caseInfoColumns" :data="caseInfoArray" class="disease_grid" ></i-table>
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
                <i-radio v-for="(list,index) in optList" :label="list.optId" :key="index">{{list.optName}}</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="名称" prop="caseName">
              <i-input placeholder="请输入名称" v-model="editFormItem.caseName"  style="width:180px"></i-input>
            </i-form-item>
          </i-col>
        </i-row>

        <i-row>

          <i-form-item label="资源附件">
            <a @click="downloadResourceFile">{{editFormItem.videoUrl}}</a>
            <i-upload :show-upload-list="notShowUploadList" multiple :action="uploadAction" :on-success="uploadModels"
                      :before-upload="beforeLoad"
                      :on-error="uploadFailed"
                      name="upfile" :headers=headers
                      style="float: right;width: 60px;margin-right: 20px"
                      :data=editFormItem>
              <i-button type="ghost" icon="ios-cloud-upload-outline">上传</i-button>
            </i-upload>
            <a v-show="!!editFormItem.videoUrl" style="float: right;color: #ed3f14;padding-right: 30px"> *
              已存在附件【{{editFormItem.videoUrl }}】，继续上传操作将覆盖原有文件
            </a>
          </i-form-item>

          <i-form-item  label="封面图片">
            <a @click="downloadPicFile">{{editFormItem.coverUrl}}</a>
            <i-upload :show-upload-list="notShowUploadList" multiple :action="uploadOrAction" :on-success="uploadModelsCover"
                      :before-upload="beforeLoad"
                      :on-error="uploadFailed"
                      name="upfiles" :headers=headers
                      style="float: right;width: 60px;margin-right: 20px"
                      :data=editFormItem>
              <i-button type="ghost" icon="ios-cloud-upload-outline">上传</i-button>
            </i-upload>
            <a v-show="!!editFormItem.coverUrl" style="float: right;color: #ed3f14;padding-right: 30px"> *
              已存在附件【{{editFormItem.coverUrl }}】，继续上传操作将覆盖原有文件
            </a>
          </i-form-item>

        </i-row>
      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveCaseInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>
      <i-spin size="large" fix v-if="loadingModal">
        <i-icon type="load-c" size=25 class="demo-spin-icon-load"></i-icon>
        <div style="font-size: large">上传资源中,请耐心等待...</div>
      </i-spin>
    </i-modal>

  </div>

</template>

<script>
  import Cookies from 'js-cookie'
  import util from '@/libs/util'
  var edit = true;
  var jiaob= 0;

  export default {
    name: 'case-manager',
    data () {
      return {
        tcase:{
          caseName:'',
          optName:''
        },
        baseUrl: util.getBaseUrl(),
        uploadAction: util.getBaseUrl() + '/model/fileUpload',
        uploadOrAction: util.getBaseUrl() + '/model/fileOrUpload',
        maskPosTitle: '添加位置信息参数',
        loadingModal: false,
        maskPosModal: false,
        partList:[],
        optList:[],
        successCode: 10000,
        editFormPosItem: {},
        notShowUploadList: false,
        caseList:[],
        caseInfoArray:[],
        currentPage: 1,
        loading:false,
        headers: {
          token: Cookies.get('token')
        },
        maskTitle: '手术方案',
        editFormItem: {

        },
        maskModal: false,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 13,
        caseInfoColumns: [
          {
            title: '编号',
            key: 'id'
          },
          {
            title: '手术方案名称',
            key: 'caseName',
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
                this.addCaseInfo(params.row)
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
          this.delCaseInfo(params.row)
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
          caseName: [
          { required: true, message: '名称不能为空', trigger: 'blur' }
        ]
      }
    }
    },

    methods: {

      // 获取历史记录信息
      changePage (index) {
        this.getCaseInfoList(index)
      },

      //弹出用户辑窗口
      addCaseInfo(rows) {

        this.editFormItem.path="";
        if (!!rows) {
          let dataRow = JSON.parse(JSON.stringify(rows));
          this.editFormItem = dataRow;
          this.editFormItem.path=dataRow.videoUrl;
          for(let i = 0; i < this.partList.length; i++) {
            if(this.partList[i].partId==this.editFormItem.partId){
              this.optList=this.partList[i].optList
            }
          }
        }
        this.editFormItem.type=2;
        this.maskModal = true
      },

      getCaseInfoList (pageIndex) {
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/case/getCaseInfoByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'caseName': this.tcase.caseName,
            'optName':this.tcase.optName
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
          let data = res.data.data
          this.dataCount = !!data['total'] ? data['total'] : 0
          this.caseInfoArray = !!data['list'] ? data['list'] : []
        } else {
          this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取手术方案失败')
        }
      })
      },

      closeModal () {
        for(let i = 0; i < this.caseList.length; i++) {
          this.caseList[i].paramKey=0
        }
        this.editFormItem={}
        this.maskModal = false
        this.$refs['editFormItem'].resetFields()
      },
      //删除参数信息
      delCaseInfo(rows) {
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p1>确认删除该方案？</p1>',
          onOk: () => {
          this.$axios.ajax({
          url: '/case/delCaseInfo?id='+rows.id
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
          this.$Message.success('删除成功')
          this.getCaseInfoList()
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
      saveCaseInfo () {

        this.$refs['editFormItem'].validate((valid) => {
          if (valid) {
            this.$axios.ajax({
              method: 'post',
              url: '/case/saveOrUpdateCaseInfo',
              data:{
                'id':this.editFormItem.id,
                'caseName':this.editFormItem.caseName,
                'videoUrl':this.editFormItem.videoUrl,
                'coverUrl':this.editFormItem.coverUrl,
                'version':this.editFormItem.version,
                'optId':this.editFormItem.optId
              }
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
              this.$Message.success('保存方案信息成功')
              this.getCaseInfoList()
              this.maskModal = false
            } else {
              this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存疾病信息失败')
            }
          })
          }
        })
      },
      downloadResourceFile () {
        let url = this.baseUrl + '/model/downResourceFile?filePath=' + this.editFormItem.videoUrl;
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', this.editFormItem.fileName)
        document.body.appendChild(link)
        link.click()
      },
      downloadPicFile () {
        let url = this.baseUrl + '/model/downResourceFile?filePath=' + this.editFormItem.coverUrl;
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', this.editFormItem.picName)
        document.body.appendChild(link)
        link.click()
      },
      //上传文件
      uploadModels (response, file, fileList) {
        if (!!response.code && this.successCode == response.code) {
          if (Array.isArray(response.data) && response.data.length > 0) {
            this.editFormItem.videoUrl = response.data[0]['path'];
            this.editFormItem.path = response.data[0]['path'];
          }
        } else {
          this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
        }
        this.loadingModal = false
      },
      beforeLoad (e) {
        this.loadingModal = true
      },

      uploadFailed (response, file, fileList) {
        this.loadingModal = false
        this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
      },

      closePos () {
        this.maskPosModal = false
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
      //上传视频封面
      uploadModelsCover(response, file, fileList){

        if (!!response.code && this.successCode == response.code) {
          if (Array.isArray(response.data) && response.data.length > 0) {
            this.editFormItem.coverUrl = response.data[0]['coverPath']
          }
        } else {
          this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
        }
        this.loadingModal = false
      },
    },
    created () {
      this.getCaseInfoList()
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

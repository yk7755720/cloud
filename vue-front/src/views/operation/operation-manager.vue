<template>
  <div class="disease-content">
    <i-form inline :label-width="80">
      <i-row>

        <i-form-item  inline label="编号">
          <i-input type="text" v-model="bodyPart.id" placeholder="请输入编号">
          </i-input>
        </i-form-item>

        <i-form-item prop="optId" inline label="术式">
          <i-input type="text" v-model="bodyPart.optName" placeholder="请输入部位名称">
          </i-input>
        </i-form-item>

        <i-form-item :label-width="20">
          <i-button type="primary" @click="getOptInfoList()" icon="ios-search">查询</i-button>
          <i-button type="primary" @click="addPartInfo()"  icon="plus">新增</i-button>
        </i-form-item>
      </i-row>
    </i-form>
    <div>
      <i-table border stripe :loading="false" :columns="partInfoColumns" :data="partInfoArray" class="disease_grid" ></i-table>
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
              <i-select v-model="editFormItem.partId" @on-change="changePart">
                <i-option v-for="(list,index) in partList" :value="list.id"  :key="index">{{list.partName}}</i-option>
              </i-select>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="术式名称" prop="optName">
              <i-input placeholder="请输入名称" v-model="editFormItem.optName"  style="width:180px"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="9">
            <i-form-item label="画像位置"  >
              <i-radio-group v-model="editFormItem.flag" >
                <i-radio label="1">客户画像</i-radio>
                <i-radio label="2">方案画像</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="18">
            <i-form-item label="选取子参数"  >
              <CheckboxGroup v-model="editFormItem.defParams" >
                <Checkbox v-for="(list) in childParamList"  :label="list.id" :key="list.id">{{list.paramName}}</Checkbox>
              </CheckboxGroup>
            </i-form-item>
          </i-col>
        </i-row>

        <i-row>

          <i-form-item label="资源附件">
            <a @click="downloadResourceFile">{{editFormItem.fileUrl}}</a>
            <i-upload :show-upload-list="notShowUploadList" multiple :action="uploadAction" :on-success="uploadModels"
                      :before-upload="beforeLoad"
                      :on-error="uploadFailed"
                      name="upfile" :headers=headers
                      style="float: right;width: 60px;margin-right: 20px"
                      :data=editFormItem>
              <i-button type="ghost" icon="ios-cloud-upload-outline">上传</i-button>
            </i-upload>
            <a v-show="!!editFormItem.fileUrl" style="float: right;color: #ed3f14;padding-right: 30px"> *
              已存在附件【{{editFormItem.fileUrl }}】，继续上传操作将覆盖原有文件
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
        <i-button type="primary" size="large" @click="saveOptInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>
    </i-modal>

  </div>
</template>

<script>

  import Cookies from 'js-cookie'
  import util from '@/libs/util'
  var edit = true;
  var jiaob= 0;

    export default {
      name: "bodyPart-manager",
      data () {
        return {
          bodyPart:{
            id:'',
            partName:''
          },
          baseUrl: util.getBaseUrl(),
          uploadAction: util.getBaseUrl() + '/model/fileUpload',
          uploadOrAction: util.getBaseUrl() + '/model/fileOrUpload',
          loadingModal: false,
          successCode: 10000,
          notShowUploadList: false,
          maskPosTitle: '添加位置信息参数',
          maskPosModal: false,
          partList:[],
          optList:[],
          childParamList:[],
          paramList:[],
          editFormPosItem: {},
          partInfoArray:[],
          currentPage: 1,
          loading:false,
          headers: {
            token: Cookies.get('token')
          },
          maskTitle: '美容部位',
          editFormItem: {

          },
          maskModal: false,
          // 初始化信息总条数
          dataCount: 0,
          // 每页显示多少条
          pageSize: 13,
          partInfoColumns: [
            {
              title: '编号',
              key: 'id'
            },
            {
              title: '术式名称',
              key: 'optName',
            },
            {
              title: '所属部位',
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
                        this.addPartInfo(params.row)
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
                        this.delOptInfo(params.row)
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
              { required: true, message: '术式不能为空', trigger: 'blur' }
            ],
            historyName: [
              { required: true, message: '名称不能为空', trigger: 'blur' }
            ]
          }
        }
      },

      methods: {

        // 获取历史记录信息
        changePage (index) {
          this.getPartInfoList(index)
        },
        changePart(index){
          this.childParamList=[]
          for(let i = 0; i < this.paramList.length; i++) {
            if(index==this.paramList[i].partId){
              for(let j = 0; j < this.paramList[i].childList.length; j++){
                let param={}
                param.id = this.paramList[i].childList[j].id
                param.paramName = this.paramList[i].childList[j].paramName
                this.childParamList.push(param)
              }
            }
          }
        },
        //弹出用户辑窗口
        addPartInfo(rows) {
          this.editFormItem.path="";
          if (!!rows) {
            let dataRow = JSON.parse(JSON.stringify(rows));
            this.editFormItem = dataRow;
            for(let i = 0; i < this.partList.length; i++) {
              if(this.partList[i].partId==this.editFormItem.partId){
                this.optList=this.partList[i].optList
              }
            }
            this.childParamList=[]
            let params=[]
            for(let i = 0; i < this.paramList.length; i++) {
              if(this.editFormItem.partId==this.paramList[i].partId){
                for(let j = 0; j < this.paramList[i].childList.length; j++){
                  let param={}
                  param.id = this.paramList[i].childList[j].id+""
                  param.paramName = this.paramList[i].childList[j].paramName
                  params.push(param)
                }
              }
            }
            this.childParamList=params
            this.editFormItem.defParams=this.editFormItem.defParams.split(",")
            this.editFormItem.path=(dataRow.coverUrl==undefined?"":dataRow.coverUrl);
          }
          this.editFormItem.type=8;
          this.maskModal = true
        },

        getOptInfoList (pageIndex) {
          if (isNaN(pageIndex)) {
            this.currentPage = 1
          }
          this.$axios.ajax({
            method: 'post',
            url: '/operation/getOperationInfoList',
            data: {
              'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
              'pageSize': this.pageSize,
              'id': this.bodyPart.id,
              'optName':this.bodyPart.optName
            }
          }).then((res) => {
            if (!!res.data.code && 10000 == res.data.code) {
              let data = res.data.data
              this.dataCount = !!data['total'] ? data['total'] : 0
              this.partInfoArray = !!data['list'] ? data['list'] : []
              this.partList=this.partInfoArray[0].partList
            } else {
              this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取手术方案失败')
            }
          })
        },

        closeModal () {
          this.editFormItem={}
          this.maskModal = false
          this.$refs['editFormItem'].resetFields()
        },
        //删除信息
        delOptInfo(rows) {
          this.$Modal.confirm({
            title: '确认信息',
            content: '<p1>确认删除该术式？</p1>',
            onOk: () => {
              this.$axios.ajax({
                url: '/operation/delOperationInfo?id='+rows.id
              }).then((res) => {
                if (!!res.data.code && 10000 == res.data.code) {
                  this.$Message.success('删除成功')
                  this.getOptInfoList()
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
        saveOptInfo () {
          this.$refs['editFormItem'].validate((valid) => {
            if (valid) {
              var defParams=this.editFormItem.defParams
              if(this.editFormItem.defParams.length==0){
                defParams=""
              }else{
                var tempList = this.childParamList
                var resultList = []

                for(let i = 0; i < tempList.length; i++){
                  for(let j = 0; j < this.editFormItem.defParams.length; j++){
                    if(tempList[i].id==this.editFormItem.defParams[j]){
                      resultList.push(tempList[i].id)
                    }
                  }
                }
                defParams=resultList+""
              }

              this.$axios.ajax({
                method: 'post',
                url: '/operation/saveOrUpdateOperationInfo',
                data:{
                  'id':this.editFormItem.id,
                  'optName':this.editFormItem.optName,
                  'partId':this.editFormItem.partId,
                  'fileUrl':this.editFormItem.fileUrl,
                  'coverUrl':this.editFormItem.coverUrl,
                  'version':this.editFormItem.version,
                  'flag':this.editFormItem.flag,
                  'defParams':defParams
                }
              }).then((res) => {
                if (!!res.data.code && 10000 == res.data.code) {
                  this.$Message.success('保存成功')
                  this.getOptInfoList()
                  this.maskModal = false
                } else {
                  this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存疾病信息失败')
                }
              })
            }
          })
        },

        getParamList() {
          this.$axios.ajax({
            method: 'get',
            url: '/params/getParamsListByType?type=2'
          }).then((res) => {
            if (!!res.data.code && 10000 == res.data.code) {
              let data = res.data.data
              this.paramList = !!data ? data : []
            } else {
              this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取部位数据失败')
            }
          })
        },
        closePos () {
          this.maskPosModal = false
        },
        downloadResourceFile () {
          let url = this.baseUrl + '/model/downResourceFile?filePath=' + this.editFormItem.fileUrl;
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
              this.editFormItem.fileUrl = response.data[0]['path'];
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
        this.getOptInfoList()
        this.getParamList()
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

  .vertical-center-modal .ivu-modal {
    top: 0;
  }

</style>

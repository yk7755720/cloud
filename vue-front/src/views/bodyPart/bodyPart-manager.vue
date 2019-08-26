<template>
  <div class="disease-content">
    <i-form inline :label-width="80">
      <i-row>

        <i-form-item  inline label="编号">
          <i-input type="text" v-model="bodyPart.id" placeholder="请输入编号">
          </i-input>
        </i-form-item>

        <i-form-item prop="optId" inline label="部位">
          <i-input type="text" v-model="bodyPart.partName" placeholder="请输入部位名称">
          </i-input>
        </i-form-item>

        <i-form-item :label-width="20">
          <i-button type="primary" @click="getPartInfoList()" icon="ios-search">查询</i-button>
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
            <i-form-item label="名称" prop="partName">
              <i-input placeholder="请输入名称" v-model="editFormItem.partName"  style="width:180px"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="备注" prop="partFlag">
              <i-input placeholder="请输入备注" v-model="editFormItem.partFlag"  style="width:180px"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>

          <i-form-item label="模型">
            <a @click="downloadResourceFile">{{editFormItem.pathUrl}}</a>
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
          <i-form-item v-show="!!displayParamList&&displayParamList.length>0" label="位置信息">
            <i-row>
              <i-col span="2">名称</i-col>
              <i-col span="3">屏幕长宽比</i-col>
              <i-col span="3">缩放比例</i-col>
              <i-col span="2">X</i-col>
              <i-col span="2">Y</i-col>
              <i-col span="2">Z</i-col>
              <i-col span="3">描述</i-col>
              <i-col span="4">操作</i-col>
            </i-row>
            <i-row v-for="(item,index) in displayParamList" key="item">
              <i-col span="3">{{item.name}}</i-col>
              <i-col span="2">{{item.dispayRate}}</i-col>
              <i-col span="3">{{item.scale}}</i-col>
              <i-col span="2">{{item.x}}</i-col>
              <i-col span="2">{{item.y}}</i-col>
              <i-col span="2">{{item.z}}</i-col>
              <i-col span="3">{{item.description}}</i-col>
              <i-col span="3"><i-button type="ghost" @click="deleteInfo(index)">删除</i-button></i-col>
              <i-col span="3"><i-button type="ghost" @click="showPosMask1(item,index)">编辑</i-button></i-col>
            </i-row>
          </i-form-item>
        </i-row>
      </i-form>
      <div slot="footer">
        <i-button type="success" size="large" @click="showPosMask">添加位置信息</i-button>
        <i-button type="primary" size="large" @click="savePartInfo">保存</i-button>
        <i-button type="ghost" @click="closeModal" size="large">关闭</i-button>
      </div>
      <i-spin size="large" fix v-if="loadingModal">
        <i-icon type="load-c" size=25 class="demo-spin-icon-load"></i-icon>
        <div style="font-size: large">上传资源中,请耐心等待...</div>
      </i-spin>
    </i-modal>

    <i-modal width="800px"
             :title="this.maskPosTitle"DD:D
             v-model="maskPosModal"
             :mask-closable="false"
             :closable="false" class-name="vertical-center-modal">
      <i-form ref="editFormPosItem" :model="editFormPosItem" :label-width="80" :rules="ruleValidate2">
        <i-row>
          <i-col span="7">
            <i-form-item label="名称" prop="name">
              <i-input v-model="editFormPosItem.name"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="7">
            <i-form-item label="屏幕长宽比" prop="dispayRate">
              <i-input v-model="editFormPosItem.dispayRate"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="7">
            <i-form-item label="缩放比例" prop="scale">
              <i-input v-model="editFormPosItem.scale"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="7">
            <i-form-item label="X" prop="x">
              <i-input v-model="editFormPosItem.x"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="7">
            <i-form-item label="Y" prop="y">
              <i-input v-model="editFormPosItem.y"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="7">
            <i-form-item label="Z" prop="z">
              <i-input v-model="editFormPosItem.z"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="7">
            <i-form-item label="ROTATIONX" prop="rotationx">
              <i-input v-model="editFormPosItem.rotationx"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="7">
            <i-form-item label="ROTATIONY" prop="rotationy">
              <i-input v-model="editFormPosItem.rotationy"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="7">
            <i-form-item label="ROTATIONZ" prop="rotationz">
              <i-input v-model="editFormPosItem.rotationz"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="描述" prop="description">
              <i-input v-model="editFormPosItem.description"></i-input>
            </i-form-item>
          </i-col>
        </i-row>
      </i-form>
      <div slot="footer">
        <i-button type="primary" size="large" @click="saveDisplayParam(index)">保存</i-button>
        <i-button type="ghost" @click="closePos()" size="large">关闭</i-button>
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
        maskPosTitle: '添加位置信息参数',
        maskPosModal: false,
        uploadAction: util.getBaseUrl() + '/model/fileUpload',
        baseUrl: util.getBaseUrl(),
        notShowUploadList: false,
        loadingModal: false,
        successCode: 10000,
        editFormPosItem: {},
        partInfoArray:[],
        emptyParaMask: {
          id: '',
          name: '',
          dispayRate: '',
          x: '',
          y: '',
          z: '',
          rotationx: '',
          rotationy: '',
          rotationz: '',
          scale: '',
          description: '',
        },
        displayParamList: [],
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
            title: '部位名称',
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
                      this.delPartInfo(params.row)
                    }
                  }
                }, '删除')
              )
              return h("div",btns)
            }
          }
        ],
        ruleValidate: {
          partName: [
            { required: true, message: '术式不能为空', trigger: 'blur' }
          ]
        },
        ruleValidate2:{
          name: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          dispayRate: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          scale: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          x: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          y: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          z: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          rotationx: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          rotationy: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ],
          rotationz: [
            { required: true, message: '不能为空', trigger: 'blur' }
          ]
        }
      }
    },

    methods: {

      // 获取历史记录信息
      changePage (index) {
        this.getPartInfoList(index)
      },

      //弹出用户辑窗口
      addPartInfo(rows) {
        this.displayParamList=[]
        if (!!rows) {
          this.getPartInfoList(this.currentPage)
          let dataRow = JSON.parse(JSON.stringify(rows));
          this.editFormItem = dataRow;
          this.editFormItem.pathUrl=this.editFormItem.partUrl
          this.displayParamList=this.editFormItem.modelDisplayParamList
        }
        this.editFormItem.path=""
        this.editFormItem.type=6;
        this.maskModal = true
      },

      getPartInfoList (pageIndex) {
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/bodyPart/getPartInfoList',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'id': this.bodyPart.id,
            'partName':this.bodyPart.partName
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.partInfoArray = !!data['list'] ? data['list'] : []
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
      //删除参数信息
      delPartInfo(rows) {
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p1>确认删除该部位？</p1>',
          onOk: () => {
            this.$axios.ajax({
              url: '/bodyPart/delPartInfo?id='+rows.id
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('删除成功')
                this.getPartInfoList()
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

      //保存信息
      savePartInfo () {
        this.$refs['editFormItem'].validate((valid) => {
          if (valid) {
            this.$axios.ajax({
              method: 'post',
              url: '/bodyPart/savePartInfoList',
              data:{
                'id':this.editFormItem.id,
                'partName':this.editFormItem.partName,
                'partUrl':this.editFormItem.pathUrl,
                'version':this.editFormItem.version,
                'modelDisplayParamList':this.displayParamList,
                'partFlag':this.editFormItem.partFlag
              }
            }).then((res) => {
              if (!!res.data.code && 10000 == res.data.code) {
                this.$Message.success('保存成功')
                this.getPartInfoList()
                this.maskModal = false
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存信息失败')
              }
            })
          }
        })
      },
      downloadResourceFile () {
        let url = this.baseUrl + '/model/downResourceFile?filePath=' + this.editFormItem.pathUrl;
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', this.editFormItem.fileName)
        document.body.appendChild(link)
        link.click()
      },

      beforeLoad (e) {
        this.loadingModal = true
      },

      uploadFailed (response, file, fileList) {
        this.loadingModal = false
        this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
      },
      //上传文件
      uploadModels (response, file, fileList) {
        if (!!response.code && this.successCode == response.code) {
          if (Array.isArray(response.data) && response.data.length > 0) {
            this.editFormItem.pathUrl = response.data[0]['path'];
            this.editFormItem.path = response.data[0]['path'];
          }
        } else {
          this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
        }
        this.loadingModal = false
      },
      closePos () {
        this.maskPosModal = false
      },
      deleteInfo(index){
        this.displayParamList.splice(index,1);
      },
      //弹出资源编辑窗口（获取坐标）
      showPosMask(){
        edit=true
        this.maskPosModal = true
      },
      saveDisplayParam(index){
        this.$refs['editFormPosItem'].validate((valid) => {
          if (valid) {
            var obj={};
            obj.name = this.editFormPosItem.name;
            obj.dispayRate = this.editFormPosItem.dispayRate;
            obj.x = this.editFormPosItem.x;
            obj.y = this.editFormPosItem.y;
            obj.z = this.editFormPosItem.z;
            obj.rotationx = this.editFormPosItem.rotationx;
            obj.rotationy = this.editFormPosItem.rotationy;
            obj.rotationz = this.editFormPosItem.rotationz;
            obj.scale = this.editFormPosItem.scale;
            obj.description = this.editFormPosItem.description;
            if(edit){
              this.displayParamList.push(obj);
            }else{
              this.displayParamList.splice(jiaob,1,obj);
            }
            this.maskPosModal = false;
          }
        })
      },
      showPosMask1(item,index){
        this.editFormPosItem = JSON.parse(JSON.stringify(this.emptyParaMask))
        this.editFormPosItem.name = item.name;
        this.editFormPosItem.dispayRate = item.dispayRate;
        this.editFormPosItem.x = item.x+"";
        this.editFormPosItem.y = item.y+"";
        this.editFormPosItem.z = item.z+"";
        this.editFormPosItem.rotationx = item.rotationx+"";
        this.editFormPosItem.rotationy = item.rotationy+"";
        this.editFormPosItem.rotationz = item.rotationz+"";
        this.editFormPosItem.scale = item.scale+"";
        this.editFormPosItem.description = item.description;
        jiaob = index;
        edit = false ;
        this.maskPosModal = true
      }
    },
    created () {
      this.getPartInfoList()
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

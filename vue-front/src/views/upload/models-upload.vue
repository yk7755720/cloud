<template>
  <div class="models-content">
    <i-form inline :label-width="80">
      <i-form-item prop="name" inline label="资源名称">
        <i-input type="text" v-model="condition.resoName" placeholder="请输入资源名称">
        </i-input>
      </i-form-item>
      <i-form-item prop="disease" label="疾病名称">
        <i-select clearable filterable v-model="condition.diseaseCode" style="width:260px"
                  placeholder="请选择待筛选疾病"
                  remote
                  :remote-method="getConditionDiseaseList">
          <i-option v-for="(item,index) in condition.diseaseArray" :value="item.diseaseCode" :key="index">{{
            item.diseaseName }}
          </i-option>
        </i-select>
      </i-form-item>
			<i-form-item prop="bussinessType" label="业务类型">
			  <i-select clearable v-model="condition.bussinessType" style="width:160px" placeholder="请选择待筛选业务类型">
          <i-option value="0">广告</i-option>
			    <i-option value="1">生理</i-option>
			    <i-option value="2">病理</i-option>
			  </i-select>
			</i-form-item>
      <i-form-item :label-width="20">
        <i-button type="primary" @click="getResourceList()" icon="ios-search">查询</i-button>
        <i-button type="primary" @click="showMask()" icon="plus">新增</i-button>
        <i-button type="primary" @click="delSelectedRow()">删除</i-button>
      </i-form-item>
    </i-form>
    <div>
      <i-table @on-selection-change="selectTableChange" ref="selection" border :columns="resourceColumns"
               :data="resourceArray" class="models_grid" @on-sort-change="sortChange"></i-table>
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
      <i-form :model="editFormItem" :label-width="80">
        <i-row>
          <i-col span="12">
            <i-form-item label="资源名称">
              <i-input placeholder="请输入资源名称" v-model="editFormItem.resoName"></i-input>
            </i-form-item>
            <i-form-item label="业务类型" v-show="editFormItem.type<'3'">
              <i-radio-group v-model="editFormItem.bussinessType" >
                <i-radio label="1">生理</i-radio>
                <i-radio label="2">病理</i-radio>
              </i-radio-group>
            </i-form-item>
          </i-col>
          <i-col span="12">
            <i-form-item label="资源类型">
              <i-radio-group v-model="editFormItem.type">
                <i-radio label="0">3D</i-radio>
                <i-radio label="1">图片</i-radio>
                <i-radio label="2">动画</i-radio>
                <i-radio label="3">广告</i-radio>
              </i-radio-group>
            </i-form-item>
            <!--<i-form-item label="是否绑定">
              <i-radio-group v-model="editFormItem.binding">
                <i-radio label="0">绑定</i-radio>
                <i-radio v-show="editFormItem.bussinessType==='1'" label="1">不绑定</i-radio>
              </i-radio-group>
            </i-form-item>
            <i-form-item v-show="editFormItem.binding==='0'" label="绑定疾病">
              <i-select clearable filterable
                        v-model="editFormItem.diseaseCode"
                        placeholder="请选择待筛选疾病"
                        remote
                        :remote-method="getMaskDiseaseList">
                <i-option v-for="(item,index) in maskDiseaseArray" :value="item.diseaseCode"
                          :key="index">{{
                  item.diseaseName }}
                </i-option>
              </i-select>
            </i-form-item>-->
          </i-col>
        </i-row>
        <i-row>
          <i-col span="12">
            <i-form-item label="文件名称">
              <i-input readonly :value="editFormItem.fileName"></i-input>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item label="文件大小">
              <i-input readonly :value="editFormItem.size">
                <span slot="append">KB</span>
              </i-input>
            </i-form-item>
          </i-col>
          <i-col span="6">
            <i-form-item v-show="editFormItem.type==='2'" label="文件时长">
              <i-input readonly :value="editFormItem.duration">
                <span slot="append">秒</span>
              </i-input>
            </i-form-item>
          </i-col>
        </i-row>

        <i-form-item label="文件描述" prop="desc">
          <i-input v-model="editFormItem.comment" type="textarea" :autosize="{minRows: 2,maxRows: 5}"
                   placeholder="Enter something..."></i-input>
        </i-form-item>
        <i-form-item label="资源附件">
          <a @click="downloadResourceFile">{{editFormItem.fileName}}</a>
          <i-upload :show-upload-list="notShowUploadList" multiple :action="uploadAction" :on-success="uploadModels"
                    :before-upload="beforeLoad"
                    :on-error="uploadFailed"
                    name="upfile" :headers=headers
                    style="float: right;width: 60px;margin-right: 20px"
                    :data=editFormItem>
            <i-button type="ghost" icon="ios-cloud-upload-outline">上传</i-button>
          </i-upload>
          <a v-show="!!editFormItem.fileName" style="float: right;color: #ed3f14;padding-right: 30px"> *
            已存在附件【{{editFormItem.fileName }}】，继续上传操作将覆盖原有文件
          </a>
        </i-form-item>

		   <i-form-item v-show="editFormItem.type>'1'" label="封面图片">
          <a @click="downloadPicFile">{{editFormItem.picName}}</a>
          <i-upload :show-upload-list="notShowUploadList" multiple :action="uploadOrAction" :on-success="uploadModelsCover"
                    :before-upload="beforeLoad"
                    :on-error="uploadFailed"
                    name="upfiles" :headers=headers
                    style="float: right;width: 60px;margin-right: 20px"
                    :data=editFormItem>
            <i-button type="ghost" icon="ios-cloud-upload-outline">上传</i-button>
          </i-upload>
          <a v-show="!!editFormItem.picName" style="float: right;color: #ed3f14;padding-right: 30px"> *
            已存在附件【{{editFormItem.picName }}】，继续上传操作将覆盖原有文件
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
      </i-form>
      <div slot="footer">
      	<i-button type="success" v-show="editFormItem.type==='0'" size="large" @click="showPosMask">添加位置信息</i-button>
        <i-button type="primary" size="large" @click="submitResource()">保存</i-button>
        <i-button type="ghost" @click="close" size="large">关闭</i-button>
      </div>
      <i-spin size="large" fix v-if="loadingModal">
        <i-icon type="load-c" size=25 class="demo-spin-icon-load"></i-icon>
        <div style="font-size: large">上传资源中,请耐心等待...</div>
      </i-spin>
    </i-modal>

    <i-modal width="800px"
             :title="this.maskPosTitle"
             v-model="maskPosModal"
             :mask-closable="false"
             :closable="false" class-name="vertical-center-modal">
      <i-form ref="editFormPosItem" :model="editFormPosItem" :label-width="80" :rules="ruleValidate">
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
        <i-button type="ghost" @click="closePos" size="large">关闭</i-button>
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
    name: 'models-upload',
    data () {
      return {
				sortKey: '',
				sortOrder: '',
        successCode: 10000,
        baseUrl: util.getBaseUrl(),
        selectedRows: [],
        uploadAction: util.getBaseUrl() + '/model/fileUpload',
		    uploadOrAction: util.getBaseUrl() + '/model/fileOrUpload',
        notShowUploadList: false,
        condition: {
          resoName: '',//过滤条件资源名称
          diseaseCode: '',//过滤条件疾病编码
          diseaseArray: []
        },
        maskDiseaseArray: [],//疾病列表数据
        maskOptArray: [],//资源术式数据
        maskRelOrginArray: [],//关联生理资源
        maskBoneTypeArray: [],//骨治疗数据
        resourceArray: [],//资源数据
        currentPage: 1,
        emptyMask: {
          id: '',
          resoName: '',
          type: '0',
          path: '',
          size: '',
          fileName: '',
          comment: '',
          duration: '',
          diseaseCode: '',
          binding: '',
          bussinessType: '1',
          sn: '',
          optCode: '',
          relId: '',
          boneType: ''
        },
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
        headers: {
          token: Cookies.get('token')
        },
        loadingModal: false,
        maskTitle: '资源信息维护',
        maskPosTitle: '添加位置信息参数',
        editFormItem: {},
        editFormPosItem: {},
        displayParamList: [],
        maskModal: false,
        maskPosModal: false,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 10,
        resourceColumns: [
          {
            type: 'selection',
            width: 60,
            align: 'center'
          },
          {
            title: '资源id',
            key: 'id'
          },
          {
            title: '资源名称',
            key: 'resoName',
						sortable: 'custom'
          },
           {
            title: '资源类型',
            key: 'type',
            render: (h, params) => {
              return h('div', [
                h('strong', this.getLabelByType(params.row.type))
              ]);
            }
          },
          {
            title: '业务类型',
            key: 'bussinessType',
            render: (h, params) => {
              return h('div', [
                h('strong', this.getLabelByBussinessType(params.row.bussinessType))
              ]);
            }
          },
          {

						title: '文件名',
            key: 'fileName'
          },
					{
						title: '疾病名称',
						key: 'diseaseName',
						sortable: 'custom'
					},
          {
            title: '操作',
            key: 'action',
            width: 150,
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
                      this.showMask(params.row)
                    }
                  }
                }, '编辑')])
            }
          }
        ],
        ruleValidate: {
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
    }, methods: {
      downloadResourceFile () {
        let url = this.baseUrl + '/model/downResourceFile?filePath=' + this.editFormItem.path
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', this.editFormItem.fileName)
        document.body.appendChild(link)
        link.click()
      },
	  downloadPicFile () {
        let url = this.baseUrl + '/model/downResourceFile?filePath=' + this.editFormItem.coverPath
        let link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', this.editFormItem.picName)
        document.body.appendChild(link)
        link.click()
      },
      selectTableChange (selection) {
        this.selectedRows = selection
      },
			sortChange: function(column) {
				this.sortKey = column.key;
			  this.sortOrder = column.order;
				this.getResourceList(1);
			},
      getLabelByType(code){
       let label = ''
        switch (code){
          case '0':
            label='3D'
            break
          case '1':
            label='图片'
            break
          case '2':
            label='动画'
            break
          case '3':
            label='广告'
            break
          default:
        }
        return label
      },
      getLabelByBussinessType(code){
       let label = ''
        switch (code){
          case '0':
            label='广告'
            break
          case '1':
            label='生理'
            break
          case '2':
            label='病理'
            break
          default:
        }
        return label
      },
      delSelectedRow () {
        if (this.selectedRows.length == 0) {
          this.$Message.info('请选择待删除模型记录')
          return false
        }
        this.$Modal.confirm({
          title: '确认信息',
          content: '<p1>确认删除该数据？</p1>',
          onOk: () => {
            this.$axios.ajax({
              method: 'post',
              url: '/model/delResourceList',
              data: this.selectedRows
            }).then((res) => {
              if (!!res.data.code && this.successCode == res.data.code) {
                this.$Message.success('记录删除成功')
                this.getResourceList()
              } else {
                this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '删除记录失败')
              }
            })
          },
          onCancel: () => {
            return;
          }
        })
      },
      close () {
        this.maskModal = false;
        this.displayParamList=[];
      },
      closePos () {
        this.maskPosModal = false
      },
      //保存资源信息
      submitResource () {
        if(this.editFormItem.type=="3"){
          //广告
          this.editFormItem.bussinessType="0";
        }
        this.$axios.ajax({
          method: 'post',
          url: '/model/saveOrUpdateResourceInfo',
          data: {'resourceInfo':this.editFormItem,'modelDisplayParamList':this.displayParamList}
        }).then((res) => {
          if (!!res.data.code && this.successCode == res.data.code) {
            this.$Message.success('保存资源成功')
            this.getResourceList()
            this.maskModal = false
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '保存资源信息失败')
          }
        })
      },
      //获取关联资源
      changeRelResource (code) {
        this.getMaskDictData(this.editFormItem)
      },
      //
      beforeLoad (e) {
        this.loadingModal = true
      },
      //上传文件
      uploadModels (response, file, fileList) {
        if (!!response.code && this.successCode == response.code) {
          if (Array.isArray(response.data) && response.data.length > 0) {
            this.editFormItem.oldFileName = this.editFormItem.fileName;
            this.editFormItem.fileName = response.data[0]['fileName']
            this.editFormItem.path = response.data[0]['path']
            this.editFormItem.size = response.data[0]['size']
            this.editFormItem.duration = response.data[0]['duration']
          }
        } else {
          this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
        }
        this.loadingModal = false
      },
	  //上传视频封面
      uploadModelsCover (response, file, fileList) {
        if (!!response.code && this.successCode == response.code) {
          if (Array.isArray(response.data) && response.data.length > 0) {
            this.editFormItem.picName = response.data[0]['picName']
            this.editFormItem.coverPath = response.data[0]['coverPath']
          }
        } else {
          this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
        }
        this.loadingModal = false
      },
      uploadFailed (response, file, fileList) {
        this.loadingModal = false
        this.$Message.error(!!response.msg ? response.msg : '上传资源列表数据失败')
      },
      // 获取历史记录信息
      changePage (index) {
        this.getResourceList(index)
      },
      //弹出资源编辑窗口
      showMask (rows) {
        this.editFormItem = JSON.parse(JSON.stringify(this.emptyMask))
        if (!!rows) {
          let dataRow = JSON.parse(JSON.stringify(rows))
          this.getMaskDictData(dataRow).then(function (res) {
            this.editFormItem = dataRow;
          }.bind(this))
        }
        this.maskModal = true
      },

       //弹出资源编辑窗口（获取坐标）
      showPosMask () {
      	edit = true ;
        this.maskPosModal = true
      },
      showPosMask1 (item,index) {
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
      },
      deleteInfo(index){
         this.displayParamList.splice(index,1);
      },

      //获取生理资源
      getRelResource () {

      },
      //获取资源信息
      getResourceList (pageIndex) {
        this.resourceArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/model/getModelResourceByCondition',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'resourceInfo': {
              'diseaseCode': this.condition.diseaseCode,
              'resoName': this.condition.resoName,
							'bussinessType': this.condition.bussinessType,
							'sortKey':this.sortKey,
							'sortOrder':this.sortOrder
            }
          }
        }).then((res) => {
          if (!!res.data.code && this.successCode == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.resourceArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取资源列表数据失败')
          }
        })
      }
      ,
      //获取主界面疾病筛选信息
      getConditionDiseaseList (query) {
        this.getDiseaseList(query).then((res) => {
          if (!!res.data.code && this.successCode == res.data.code) {
            this.condition.diseaseArray = JSON.parse(JSON.stringify(res.data.data))
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取疾病信息失败')
          }
        })
      }
      ,
      //获取资源维护页面疾病信息
      getMaskDiseaseList (query) {
        this.getDiseaseList(query).then((res) => {
          if (!!res.data.code && this.successCode == res.data.code) {
            this.maskDiseaseArray = JSON.parse(JSON.stringify(res.data.data))
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取疾病信息失败')
          }
        })
      }
      ,
      //获取疾病列表
      getDiseaseList (key) {
        return this.$axios.ajax({
          method: 'get',
          url: '/model/getDiseaseList?key=' + key,
          data: {}
        })
      }
      ,
      //获取术式数据
      getOptList (query) {
        this.$axios.ajax({
          method: 'get',
          url: '/model/getOptList?key=' + query,
          data: {}
        }).then((res) => {
          if (!!res.data.code && this.successCode == res.data.code) {
            this.maskOptArray = JSON.parse(JSON.stringify(res.data.data))
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取术式信息失败')
          }
        })
      },
      //获取mask基础字典数据
      getMaskDictData (item) {
        return new Promise(function (resolve, reject) {
          this.$axios.ajax({
            method: 'post',
            url: '/model/getResourceRelation',
            data: item
          }).then((res) => {
            if (!!res.data.code && this.successCode == res.data.code) {
              if(!!res.data.data['modelDisplayParamList']){
              	this.displayParamList = res.data.data['modelDisplayParamList']
              }
            } else {
              this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取数据失败')
            }
            resolve(!!res ? res : {})
          })
        }.bind(this))
      },


      saveDisplayParam (index) {
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
      }

    },
    created () {
      this.getResourceList()//获取资源列表
    }
  }
</script>

<style>
  .models-content {
    width: 100%;
    height: 100%;
  }

  .models_grid {
    height: calc(10% - 240px);
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

  .demo-spin-icon-load {
    animation: ani-demo-spin 1s linear infinite;
  }

  .hide_columns {
    /*width: 0px;*/
    /*display: none;*/
  }
</style>

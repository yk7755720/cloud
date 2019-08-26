<template>
  <div class="record-content">
    <i-form inline :label-width="80">
      <i-form-item inline label="姓名">
        <i-input type="text" v-model="record.name" placeholder="请输入编号">
        </i-input>
      </i-form-item>
      <i-form-item inline label="电话">
        <i-input type="text" v-model="record.phone" placeholder="请输入编号">
        </i-input>
      </i-form-item>
      <i-form-item :label-width="20">
        <i-button type="primary" @click="getRecordList()">查询</i-button>
      </i-form-item>
    </i-form>
    <div>
      <i-table border :columns="recordColumns"
               :data="recordArray" class="record_grid"></i-table>
    </div>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <i-page :total="dataCount" :page-size="pageSize" :current.sync="currentPage" show-total
                @on-change="changePage"></i-page>
      </div>
    </div>
  </div>
</template>

<script>
  import util from '@/libs/util'
  import Cookies from 'js-cookie'

  export default {
    name: 'record-manage',
    data () {
      return {
        baseUrl: util.getBaseUrl(),
        videoName: '.mp4',//
        agreeFileName: 'quotation.pdf',
        record: {name:"",phone:""},
        currentPage: 1,
        // 初始化信息总条数
        dataCount: 0,
        // 每页显示多少条
        pageSize: 10,
        recordArray: [],
        recordColumns: [
          {
            title: '求美者姓名',
            key: 'talkName'
          },
          {
            title: '电话',
            key: 'phone'
          },
          {
            title: '方案报价上传时间',
            key: 'uploadtimeBjd',
             render: (h, params) => {
              return h('div', [
                h('strong', util.dateToFormat(params.row.uploadtimeBjd))
              ]);
            }
          },

          {
            title: '录屏上传时间',
            key: 'uploadtimeThsp',
			render: (h, params) => {
              return h('div', [
                h('strong', util.dateToFormat(params.row.uploadtimeThsp))
              ]);
            }
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
            title: '下载',
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
                      this.downloadRecordFile(params.row, this.agreeFileName)
                    }
                  }
                }, '方案报价'),
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
                      this.downloadVideoFile(params.row)
                    }
                  }
                }, '谈话录屏')])
            }
          }
        ]
      }
    },
    methods: {
      downloadRecordFile (rows, name) {
        if (!rows || !rows.fileUrl) {
          this.$Message.error('文件路径不存在，无法下载')
          return false
        }
        let filePath = rows.fileUrl;
        this.$axios.ajax({
          method: 'get',
          url: '/record/validRecordFile?filePath=' + filePath,
          data: {}
        }).then((res) => {
          console.log(res.data)
          if (!!res.data.data && true == res.data.data) {
            let url = this.baseUrl + '/model/downResourceFile?filePath=' + filePath
            let link = document.createElement('a')
            link.style.display = 'none'
            link.href = url
            link.setAttribute('download', name)
            document.body.appendChild(link)
            link.click()
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '下载资源文件失败')
          }
        })
      },
      downloadVideoFile (rows) {
        if (!rows || !rows.videoUrl) {
          this.$Message.error('文件路径不存在，无法下载')
          return false
        }
        this.$axios.ajax({
          method: 'get',
          url: '/record/validRecordFile?filePath=' + rows.videoUrl,
          data: {}
        }).then((res) => {
          if (!!res.data.data && true == res.data.data) {
            let url = this.baseUrl + '/model/downResourceFile?filePath=' + rows.videoUrl
            let link = document.createElement('a')
            link.style.display = 'none'
            link.href = url
            link.setAttribute('download', name)
            document.body.appendChild(link)
            link.click()
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '下载资源文件失败')
          }
        })
      },

      changePage (index) {
        this.getRecordList(index)
      },

      //获取资源信息
      getRecordList (pageIndex) {
        this.recordArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/record/getRecordList',
          data: {
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,
            'pageSize': this.pageSize,
            'phone':this.record.phone,
            'talker':this.record.name
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.recordArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.msg ? res.data.msg : '获取记录列表数据失败')
          }
        })
      }
    },

    created () {
      this.getRecordList()//获取资源列表
    }
  }
</script>

<style scoped>
  .record-content {
    width: 100%;
    height: 100%;
  }

  .record_grid {
    height: calc(10% - 240px);
    overflow: auto;
  }

</style>

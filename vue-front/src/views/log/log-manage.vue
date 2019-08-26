<template>
  <div class="log-content">
    <i-form inline :label-width="80">
      <i-row>
        <i-form-item prop="userId" inline label="用户ID">
          <i-input type="text" v-model="condition.userId" placeholder="用户ID">
          </i-input>
        </i-form-item>

        <i-form-item prop="startDate" inline label="登录时间">
          <i-date-picker format="yyyy-MM-dd HH:mm:ss" @on-change="getStartTime" type="datetime" placeholder="请选择开始时间" style="width: 160px"></i-date-picker>
        </i-form-item>
        <i-form-item prop="endDate" :label-width="5">
          <i-date-picker  format="yyyy-MM-dd HH:mm:ss" @on-change="getEndTime" type="datetime" placeholder="请选择结束时间" style="width: 160px"></i-date-picker>
        </i-form-item>
        
        <i-form-item :label-width="20">
          <i-button type="primary" @click="getLogInfoList()" icon="ios-search">查询</i-button>  
        </i-form-item>
      </i-row>
      </i-form>
    <div>
      <i-table border stripe :columns="logInfoColumns" :data="logInfoArray" class="logInfo_grid"></i-table>
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
  import Cookies from 'js-cookie'
  import util from '@/libs/util'

  export default {
    name: 'log-manager',
    data () {
      return {
        condition: {
          userId:'',//用户id
          startDate: '',//开始时间
          endDate:''//结束时间 
        },
        logInfoArray:[],//日志列表数据
        currentPage: 1,       
        headers: {
          token: Cookies.get('token')
        },
        
        dataCount: 0,
        // 每页显示多少条
        pageSize: 13,
        logInfoColumns: [
          {
            title: '用户ID',
            key: 'userId'
          },
          {
            title: '登录时间',
            key: 'userLoginTime',
            render: (h, params) => {
              return h('div', [
                h('strong', util.dateToFormat(params.row.userLoginTime))
              ]);
            }
          },
          {
            title: '登录IP',
            key: 'userLoginIp'
          }
          
        ],
      }
    },

    methods: {
      getStartTime(starTime) {
        this.condition.startDate = starTime
      },
      getEndTime(endTime) {
        this.condition.endDate = endTime
      },

      // 获取历史记录信息
      changePage (index) {
        this.getLogInfoList(index)
      },
      //获取日志信息
      getLogInfoList (pageIndex) {
        this.logInfoArray = []
        if (isNaN(pageIndex)) {
          this.currentPage = 1
        }
        this.$axios.ajax({
          method: 'post',
          url: '/user/getUserLoginInfoByCondition',
          data: {
          	'userId': this.condition.userId,
          	'pageSize': this.pageSize,
            'pageNum': !isNaN(pageIndex) ? pageIndex : 1,                     
            'startDate': this.condition.startDate, 
            'endDate': this.condition.endDate
          }
        }).then((res) => {
          if (!!res.data.code && 10000 == res.data.code) {
            let data = res.data.data
            this.dataCount = !!data['total'] ? data['total'] : 0
            this.logInfoArray = !!data['list'] ? data['list'] : []
          } else {
            this.$Message.error(!!res.data && !!res.data.message ? res.data.message : '获取日志列表数据失败')
          }
        })
      },         
    },
    created () {
      this.getLogInfoList()//获取用户登录日志信息列表
    }
  }
</script>

<style>
  .log-content {
    width: 100%;
    height: 100%;
  }

  .log_grid {
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

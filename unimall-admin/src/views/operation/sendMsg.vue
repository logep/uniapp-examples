<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <!--<el-input v-model="listQuery.userId" clearable class="filter-item" style="width: 200px;" placeholder="请输入用户ID"/>-->
      <el-input
        v-model="msg"
        type="textarea"
        placeholder="请输入内容"

        maxlength="15"
        resize="none"
        show-word-limit
      />
      <el-input
        v-model="phone"
        placeholder="请输入手机号码"
        maxlength="15"
      />
      <el-button
        v-permission="['operation:freight:sendMsg']"
        :loading="verifyLoading"
        clearable
        class="filter-item"
        type="primary"
        icon="el-icon-chat-line-round"
        @click.native.prevent="sendMsg"
        @click="handleFilter"
      >发送短信</el-button>
    </div>
  </div>
</template>

<style>
</style>

<script>
import { deleteAppraise } from '@/api/appraise'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination
import { sendPhoneMsg } from '@/api/login.js'
export default {
  name: 'Order',
  components: { Pagination },
  filters: {
  },
  data() {
    return {
      loading: false,
      verifyLoading: false,
      show: true,
      phone: undefined,
      count: '',
      timer: null,
      list: undefined,
      total: 0,
      msg: undefined,
      listLoading: true,
      listQuery: {
        pageNo: 1,
        limit: 20,
        id: undefined,
        spuName: undefined,
        content: undefined,
        userName: undefined
      }
    }
  },
  created() {
  },
  methods: {
    sendMsg() {
      const TIME_COUNT = 60
      if (!this.timer) {
        this.count = TIME_COUNT
        this.show = false
        this.timer = setInterval(() => {
          if (this.count > 0 && this.count <= TIME_COUNT) {
            this.count--
          } else {
            this.show = true
            clearInterval(this.timer)
            this.timer = null
          }
        }, 1000)
      }
      if (this.msg == null || this.msg === '') {
        this.$notify.error({
          title: '失败',
          message: '请先填写需要发送的信息'
        })
        return false
      }
      if (this.phone == null || this.phone === '') {
        this.$notify.error({
          title: '失败',
          message: '请先填写手机号码'
        })
        return false
      }
      this.verifyLoading = true
      sendPhoneMsg({ message: this.msg, phone: this.phone }).then(response => {
        console.log(response)
        console.log('response')
        this.verifyLoading = false
        this.$notify.success({
          title: '成功',
          message: '信息发送成功'
        })
      })
        .catch(response => {
          console.log(response)
          console.log('response1')
          // this.verifyLoading = false
          this.$notify.error({
            title: '失败',
            message: response.data.errmsg
          })
          this.show = true
          clearInterval(this.timer)
          this.timer = null
          setTimeout(() => { this.verifyLoading = false }, 3000)
        })
    },
    handleDelete(row) {
      this.$confirm('此操作将永久删除该评论' + row.id + ', 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAppraise(row)
          .then(response => {
            this.$notify.success({
              title: '成功',
              message: '删除优惠券成功'
            })
            const index = this.list.indexOf(row)
            this.list.splice(index, 1)
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
      }).catch(() => {
        return false
      })
    }
  }
}
</script>

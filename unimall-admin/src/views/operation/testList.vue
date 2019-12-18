<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.title"
        clearable
        class="filter-item"
        style="width: 200px;"
        placeholder="请输入商品名称"
      />
      <!--v-permission="['operation:goods:list']"-->
      <el-button  class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查找</el-button>
      <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="downExcelBtn">导出</el-button>
      <!--<el-button v-permission="['operation:goods:create']" class="filter-item" type="primary" icon="el-icon-edit" @click="handleCreate">添加</el-button>-->
    </div>

    <!-- 查询结果 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      size="small"
      element-loading-text="正在查询中。。。"
      border
      fit
      highlight-current-row
    >
      <el-table-column type="expand">
        <template slot-scope="props">
          <el-form label-position="left" class="table-expand">
            <el-form-item label="商品单位">
              <span>{{ props.row.unit }}</span>
            </el-form-item>
            <el-form-item label="类目ID">
              <span>{{ props.row.categoryId }}</span>
            </el-form-item>
          </el-form>
        </template>
      </el-table-column>

      <el-table-column align="center" label="商品编号" prop="id" />

      <el-table-column align="center" min-width="100" label="名称" prop="title" />

      <el-table-column align="center" property="img" label="图片">
        <template slot-scope="scope">
          <img :src="scope.row.img" width="40" >
        </template>
      </el-table-column>

      <el-table-column align="center" label="详情" prop="detail">
        <template slot-scope="scope">
          <el-dialog :visible.sync="detailDialogVisible" title="商品详情">
            <div v-html="goodsDetail" />
          </el-dialog>
          <el-button type="primary" size="mini" @click="showDetail(scope.row.detail)">查看</el-button>
        </template>
      </el-table-column>

      <el-table-column align="center" label="原价" prop="originalPrice" />

      <el-table-column align="center" label="现价" prop="price" />

      <el-table-column align="center" label="VIP价格" prop="vipPrice" />

      <el-table-column align="center" label="是否在售" prop="status">
        <template slot-scope="scope">
          <el-tag
            :type="scope.row.status == 1 ? 'success' : 'error' "
          >{{ scope.row.status == 1 ? '在售' : '未售' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        max-width="300"
        min-width="300"
        label="描述"
        prop="description"
      />

      <el-table-column align="center" label="操作" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-permission="['operation:goods:edit']" type="primary" size="mini" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button v-permission="['operation:goods:delete']" type="danger" size="mini" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />
  </div>
</template>

<style>
</style>

<script>
import { listOrderTest, shipOrder, refundOrder, detailOrder, getExcelInfo } from '@/api/order'
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination
import checkPermission from '@/utils/permission' // 权限判断函数

const statusMap = {
  10: '未付款',
  20: '待出库',
  30: '待收货',
  40: '待评价',
  50: '已完成',
  60: '退款中',
  70: '已退款',
  80: '已取消',
  90: '已取消(系统)'
}

const shipCodeMap = {
  'NONE': '无需物流公司',
  'SF': '顺丰速运',
  'HTKY': '百世快递',
  'ZTO': '中通快递',
  'STO': '申通快递',
  'YTO': '圆通速递',
  'YD': '韵达速递',
  'YZPY': '邮政快递包裹',
  'EMS': 'EMS',
  'HHTT': '天天快递',
  'JD': '京东快递',
  'UC': '优速快递',
  'DBL': '德邦快递',
  'ZJS': '宅急送',
  'TNT': 'TNT快递'
}

const payChannelMap = {
  WX: '微信支付',
  ALI: '支付宝',
  OFFLINE: '线下支付'
}

export default {
  name: 'Order',
  components: { Pagination },
  filters: {
    orderStatusFilter(status) {
      return statusMap[status]
    },
    payChannelFilter(channel) {
      const str = payChannelMap[channel]
      if (str) {
        return str
      }
      return '未支付'
    }
  },
  data() {
    return {
      goodsDetail: '',
      detailDialogVisible: false,
      excelData: {
        barcode: undefined,
        name: undefined,
        specifications: undefined,
        unit: undefined,
        num: undefined,
        address: undefined
      },
      excelDataList: [],
      downData: {
        status: '',
        gmtStart: undefined,
        gmtEnd: undefined
      },
      shipCodeMap,
      list1: {
        'data': {
          'code': 0,
          'count': 332,
          'items': [
            {
            'actualPrice': 7200,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568812880000,
            'gmtUpdate': 1568812897000,
            'id': 338,
            'mono': '',
            'orderNo': '1012019092121201134',
            'payChannel': 'OFFLINE',
            'skuOriginalTotalPrice': 8640,
            'skuTotalPrice': 7200,
            'status': 20,
            'userId': 452
          }, {
            'actualPrice': 28900,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568795779000,
            'gmtUpdate': 1568795782000,
            'id': 337,
            'mono': '',
            'orderNo': '1012019091636181133',
            'payChannel': 'OFFLINE',
            'skuOriginalTotalPrice': 34680,
            'skuTotalPrice': 28900,
            'status': 20,
            'userId': 449
          }, {
            'actualPrice': 53200,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568794267000,
            'gmtUpdate': 1568795220000,
            'id': 336,
            'mono': '',
            'orderNo': '1012019091611061132',
            'skuOriginalTotalPrice': 54900,
            'skuTotalPrice': 53200,
            'status': 90,
            'userId': 449
          }, {
            'actualPrice': 53200,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568789381000,
            'gmtUpdate': 1568789392000,
            'id': 335,
            'mono': '',
            'orderNo': '1012019091449401131',
            'payChannel': 'OFFLINE',
            'skuOriginalTotalPrice': 54900,
            'skuTotalPrice': 53200,
            'status': 20,
            'userId': 445
          }, {
            'actualPrice': 46000,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568789007000,
            'gmtUpdate': 1568789940000,
            'id': 334,
            'mono': '',
            'orderNo': '1012019091443261130',
            'skuOriginalTotalPrice': 57600,
            'skuTotalPrice': 46000,
            'status': 90,
            'userId': 444
          }, {
            'actualPrice': 1600,
            'channel': 'ios',
            'freightPrice': 0,
            'gmtCreate': 1568786293000,
            'gmtUpdate': 1568786359000,
            'id': 333,
            'mono': '',
            'orderNo': '1012019091358131129',
            'skuOriginalTotalPrice': 1920,
            'skuTotalPrice': 1600,
            'status': 80,
            'userId': 442
          }, {
            'actualPrice': 1380,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568786039000,
            'gmtUpdate': 1568786940000,
            'id': 332,
            'mono': '',
            'orderNo': '1012019091353581128',
            'skuOriginalTotalPrice': 1800,
            'skuTotalPrice': 1380,
            'status': 90,
            'userId': 441
          }, {
            'actualPrice': 1,
            'address': '香溪路2号',
            'channel': 'ios',
            'city': '重庆市',
            'consignee': '魏朝正',
            'county': '南岸区',
            'freightPrice': 0,
            'gmtCreate': 1568780431000,
            'gmtPay': 1568780442000,
            'gmtUpdate': 1568780431000,
            'id': 331,
            'mono': '',
            'orderNo': '1012019091220301127',
            'payChannel': 'WX',
            'payId': '4200000421201909185749675281',
            'payPrice': 1,
            'phone': '18584669549',
            'province': '重庆',
            'skuOriginalTotalPrice': 100,
            'skuTotalPrice': 1,
            'status': 20,
            'userId': 21
          }, {
            'actualPrice': 53200,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568776343000,
            'gmtUpdate': 1568777280000,
            'id': 330,
            'mono': '',
            'orderNo': '1012019091112221126',
            'skuOriginalTotalPrice': 54900,
            'skuTotalPrice': 53200,
            'status': 90,
            'userId': 439
          }, {
            'actualPrice': 1,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568723471000,
            'gmtPay': 1568723480000,
            'gmtUpdate': 1568723471000,
            'id': 329,
            'mono': '',
            'orderNo': '1012019092031101125',
            'payChannel': 'WX',
            'payId': '4200000417201909176103940938',
            'payPrice': 1,
            'skuOriginalTotalPrice': 100,
            'skuTotalPrice': 1,
            'status': 20,
            'userId': 413
          }, {
            'actualPrice': 28900,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568717101000,
            'gmtUpdate': 1568718060000,
            'id': 328,
            'mono': '',
            'orderNo': '1012019091845001124',
            'skuOriginalTotalPrice': 34680,
            'skuTotalPrice': 28900,
            'status': 90,
            'userId': 438
          }, {
            'actualPrice': 28900,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568717095000,
            'gmtUpdate': 1568718000000,
            'id': 327,
            'mono': '',
            'orderNo': '1012019091844541123',
            'skuOriginalTotalPrice': 34680,
            'skuTotalPrice': 28900,
            'status': 90,
            'userId': 438
          }, {
            'actualPrice': 53200,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568713851000,
            'gmtUpdate': 1568713867000,
            'id': 326,
            'mono': '',
            'orderNo': '1012019091750501122',
            'payChannel': 'OFFLINE',
            'skuOriginalTotalPrice': 54900,
            'skuTotalPrice': 53200,
            'status': 20,
            'userId': 437
          }, {
            'actualPrice': 68800,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568710912000,
            'gmtUpdate': 1568711820000,
            'id': 325,
            'mono': '',
            'orderNo': '1012019091701511121',
            'skuOriginalTotalPrice': 99900,
            'skuTotalPrice': 68800,
            'status': 90,
            'userId': 436
          }, {
            'actualPrice': 68800,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568710667000,
            'gmtUpdate': 1568711580000,
            'id': 324,
            'mono': '',
            'orderNo': '1012019091657461120',
            'skuOriginalTotalPrice': 99900,
            'skuTotalPrice': 68800,
            'status': 90,
            'userId': 436
          }, {
            'actualPrice': 1380,
            'channel': 'ios',
            'freightPrice': 0,
            'gmtCreate': 1568692935000,
            'gmtUpdate': 1568692947000,
            'id': 323,
            'mono': '',
            'orderNo': '1012019091202141119',
            'payChannel': 'OFFLINE',
            'skuOriginalTotalPrice': 1800,
            'skuTotalPrice': 1380,
            'status': 20,
            'userId': 433
          }, {
            'actualPrice': 1,
            'address': '香溪路2号',
            'channel': 'ios',
            'city': '重庆市',
            'consignee': '魏朝正',
            'county': '南岸区',
            'freightPrice': 0,
            'gmtCreate': 1568676987000,
            'gmtPay': 1568676993000,
            'gmtUpdate': 1568676987000,
            'id': 322,
            'mono': '',
            'orderNo': '1012019090736271118',
            'payChannel': 'WX',
            'payId': '4200000420201909177778069659',
            'payPrice': 1,
            'phone': '18584669549',
            'province': '重庆',
            'skuOriginalTotalPrice': 100,
            'skuTotalPrice': 1,
            'status': 20,
            'userId': 21
          }, {
            'actualPrice': 28900,
            'address': '香溪路2号',
            'channel': 'ios',
            'city': '重庆市',
            'consignee': '魏朝正',
            'county': '南岸区',
            'freightPrice': 0,
            'gmtCreate': 1568640746000,
            'gmtUpdate': 1568641680000,
            'id': 321,
            'mono': '',
            'orderNo': '1012019092132261117',
            'phone': '18584669549',
            'province': '重庆',
            'skuOriginalTotalPrice': 34680,
            'skuTotalPrice': 28900,
            'status': 90,
            'userId': 21
          }, {
            'actualPrice': 206400,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568626029000,
            'gmtUpdate': 1568626980000,
            'id': 320,
            'mono': '',
            'orderNo': '1012019091727081116',
            'skuOriginalTotalPrice': 99900,
            'skuTotalPrice': 206400,
            'status': 90,
            'userId': 431
          }, {
            'actualPrice': 1380,
            'channel': 'android',
            'freightPrice': 0,
            'gmtCreate': 1568619700000,
            'gmtUpdate': 1568620620000,
            'id': 319,
            'mono': '',
            'orderNo': '1012019091541401115',
            'skuOriginalTotalPrice': 1800,
            'skuTotalPrice': 1380,
            'status': 90,
            'userId': 138
          }],
          'msg': '第1页,共332条',
          'pageNo': 1,
          'pageSize': 20,
          'total': 332,
          'totalPageNo': 17
        }, 'errmsg': '成功', 'errno': 200, 'timestamp': 1576140689132
      },
      list2: [
        {
          'actualPrice': 7200,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568812880000,
          'gmtUpdate': 1568812897000,
          'id': 338,
          'mono': '',
          'orderNo': '1012019092121201134',
          'payChannel': 'OFFLINE',
          'skuOriginalTotalPrice': 8640,
          'skuTotalPrice': 7200,
          'status': 20,
          'userId': 452
        }, {
          'actualPrice': 28900,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568795779000,
          'gmtUpdate': 1568795782000,
          'id': 337,
          'mono': '',
          'orderNo': '1012019091636181133',
          'payChannel': 'OFFLINE',
          'skuOriginalTotalPrice': 34680,
          'skuTotalPrice': 28900,
          'status': 20,
          'userId': 449
        }, {
          'actualPrice': 53200,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568794267000,
          'gmtUpdate': 1568795220000,
          'id': 336,
          'mono': '',
          'orderNo': '1012019091611061132',
          'skuOriginalTotalPrice': 54900,
          'skuTotalPrice': 53200,
          'status': 90,
          'userId': 449
        }, {
          'actualPrice': 53200,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568789381000,
          'gmtUpdate': 1568789392000,
          'id': 335,
          'mono': '',
          'orderNo': '1012019091449401131',
          'payChannel': 'OFFLINE',
          'skuOriginalTotalPrice': 54900,
          'skuTotalPrice': 53200,
          'status': 20,
          'userId': 445
        }, {
          'actualPrice': 46000,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568789007000,
          'gmtUpdate': 1568789940000,
          'id': 334,
          'mono': '',
          'orderNo': '1012019091443261130',
          'skuOriginalTotalPrice': 57600,
          'skuTotalPrice': 46000,
          'status': 90,
          'userId': 444
        }, {
          'actualPrice': 1600,
          'channel': 'ios',
          'freightPrice': 0,
          'gmtCreate': 1568786293000,
          'gmtUpdate': 1568786359000,
          'id': 333,
          'mono': '',
          'orderNo': '1012019091358131129',
          'skuOriginalTotalPrice': 1920,
          'skuTotalPrice': 1600,
          'status': 80,
          'userId': 442
        }, {
          'actualPrice': 1380,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568786039000,
          'gmtUpdate': 1568786940000,
          'id': 332,
          'mono': '',
          'orderNo': '1012019091353581128',
          'skuOriginalTotalPrice': 1800,
          'skuTotalPrice': 1380,
          'status': 90,
          'userId': 441
        }, {
          'actualPrice': 1,
          'address': '香溪路2号',
          'channel': 'ios',
          'city': '重庆市',
          'consignee': '魏朝正',
          'county': '南岸区',
          'freightPrice': 0,
          'gmtCreate': 1568780431000,
          'gmtPay': 1568780442000,
          'gmtUpdate': 1568780431000,
          'id': 331,
          'mono': '',
          'orderNo': '1012019091220301127',
          'payChannel': 'WX',
          'payId': '4200000421201909185749675281',
          'payPrice': 1,
          'phone': '18584669549',
          'province': '重庆',
          'skuOriginalTotalPrice': 100,
          'skuTotalPrice': 1,
          'status': 20,
          'userId': 21
        }, {
          'actualPrice': 53200,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568776343000,
          'gmtUpdate': 1568777280000,
          'id': 330,
          'mono': '',
          'orderNo': '1012019091112221126',
          'skuOriginalTotalPrice': 54900,
          'skuTotalPrice': 53200,
          'status': 90,
          'userId': 439
        }, {
          'actualPrice': 1,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568723471000,
          'gmtPay': 1568723480000,
          'gmtUpdate': 1568723471000,
          'id': 329,
          'mono': '',
          'orderNo': '1012019092031101125',
          'payChannel': 'WX',
          'payId': '4200000417201909176103940938',
          'payPrice': 1,
          'skuOriginalTotalPrice': 100,
          'skuTotalPrice': 1,
          'status': 20,
          'userId': 413
        }, {
          'actualPrice': 28900,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568717101000,
          'gmtUpdate': 1568718060000,
          'id': 328,
          'mono': '',
          'orderNo': '1012019091845001124',
          'skuOriginalTotalPrice': 34680,
          'skuTotalPrice': 28900,
          'status': 90,
          'userId': 438
        }, {
          'actualPrice': 28900,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568717095000,
          'gmtUpdate': 1568718000000,
          'id': 327,
          'mono': '',
          'orderNo': '1012019091844541123',
          'skuOriginalTotalPrice': 34680,
          'skuTotalPrice': 28900,
          'status': 90,
          'userId': 438
        }, {
          'actualPrice': 53200,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568713851000,
          'gmtUpdate': 1568713867000,
          'id': 326,
          'mono': '',
          'orderNo': '1012019091750501122',
          'payChannel': 'OFFLINE',
          'skuOriginalTotalPrice': 54900,
          'skuTotalPrice': 53200,
          'status': 20,
          'userId': 437
        }, {
          'actualPrice': 68800,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568710912000,
          'gmtUpdate': 1568711820000,
          'id': 325,
          'mono': '',
          'orderNo': '1012019091701511121',
          'skuOriginalTotalPrice': 99900,
          'skuTotalPrice': 68800,
          'status': 90,
          'userId': 436
        }, {
          'actualPrice': 68800,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568710667000,
          'gmtUpdate': 1568711580000,
          'id': 324,
          'mono': '',
          'orderNo': '1012019091657461120',
          'skuOriginalTotalPrice': 99900,
          'skuTotalPrice': 68800,
          'status': 90,
          'userId': 436
        }, {
          'actualPrice': 1380,
          'channel': 'ios',
          'freightPrice': 0,
          'gmtCreate': 1568692935000,
          'gmtUpdate': 1568692947000,
          'id': 323,
          'mono': '',
          'orderNo': '1012019091202141119',
          'payChannel': 'OFFLINE',
          'skuOriginalTotalPrice': 1800,
          'skuTotalPrice': 1380,
          'status': 20,
          'userId': 433
        }, {
          'actualPrice': 1,
          'address': '香溪路2号',
          'channel': 'ios',
          'city': '重庆市',
          'consignee': '魏朝正',
          'county': '南岸区',
          'freightPrice': 0,
          'gmtCreate': 1568676987000,
          'gmtPay': 1568676993000,
          'gmtUpdate': 1568676987000,
          'id': 322,
          'mono': '',
          'orderNo': '1012019090736271118',
          'payChannel': 'WX',
          'payId': '4200000420201909177778069659',
          'payPrice': 1,
          'phone': '18584669549',
          'province': '重庆',
          'skuOriginalTotalPrice': 100,
          'skuTotalPrice': 1,
          'status': 20,
          'userId': 21
        }, {
          'actualPrice': 28900,
          'address': '香溪路2号',
          'channel': 'ios',
          'city': '重庆市',
          'consignee': '魏朝正',
          'county': '南岸区',
          'freightPrice': 0,
          'gmtCreate': 1568640746000,
          'gmtUpdate': 1568641680000,
          'id': 321,
          'mono': '',
          'orderNo': '1012019092132261117',
          'phone': '18584669549',
          'province': '重庆',
          'skuOriginalTotalPrice': 34680,
          'skuTotalPrice': 28900,
          'status': 90,
          'userId': 21
        }, {
          'actualPrice': 206400,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568626029000,
          'gmtUpdate': 1568626980000,
          'id': 320,
          'mono': '',
          'orderNo': '1012019091727081116',
          'skuOriginalTotalPrice': 99900,
          'skuTotalPrice': 206400,
          'status': 90,
          'userId': 431
        }, {
          'actualPrice': 1380,
          'channel': 'android',
          'freightPrice': 0,
          'gmtCreate': 1568619700000,
          'gmtUpdate': 1568620620000,
          'id': 319,
          'mono': '',
          'orderNo': '1012019091541401115',
          'skuOriginalTotalPrice': 1800,
          'skuTotalPrice': 1380,
          'status': 90,
          'userId': 138
        }],
      list:[],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        id: undefined,
        name: undefined,
        orderStatusArray: [],
        sort: 'id',
        order: 'desc'
      },
      statusMap,
      orderDialogVisible: false,
      orderDetail: {},
      refundForm: {
        orderNo: undefined,
        type: '0'
      },
      refundDialogVisible: false,
      refundSubmiting: false,
      downloadLoading: false,
      shipForm: {
        orderNo: undefined,
        shipCode: 'NONE',
        shipNo: undefined
      },
      shipDialogVisible: false,
      shipSubmiting: false,
      refundRules: {
        orderNo: [
          { required: true, message: '请使用非IE浏览器重试', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择退货类型', trigger: 'blur' }
        ]
      },
      shipRules: {
        shipCode: [
          { required: true, message: '请选择物流类型', trigger: 'blur' }
        ],
        orderNo: [
          { required: true, message: '请使用非IE浏览器重试', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    checkPermission,
    showDetail(detail) {
      this.goodsDetail = detail
      this.detailDialogVisible = true
    },
    getList() {
      this.listLoading = true
      listOrderTest(this.listQuery)
        .then(response => {
          console.log(response)
          console.log(response)
          this.list = response.data.data.items
          this.total = response.data.data.total
          this.listLoading = false
        })
        .catch(() => {
          this.list = []
          this.total = 0
          this.listLoading = false
        })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleDetail(row) {
      detailOrder(row.id).then(response => {
        this.orderDetail = response.data.data
      })
      this.orderDialogVisible = true
    },
    handleShip(row) {
      this.shipDialogVisible = true
      this.shipForm.orderNo = row.orderNo
    },
    confirmShip() {
      this.$refs['shipForm'].validate(valid => {
        if (valid) {
          this.shipSubmiting = true
          if (this.shipForm.shipCode !== 'NONE' && !this.shipForm.shipNo) {
            this.$notify.error({
              title: '失败',
              message: '请填写运单号'
            })
          } else {
            shipOrder(this.shipForm)
              .then(response => {
                this.shipSubmiting = false
                this.shipDialogVisible = false
                this.$notify.success({
                  title: '成功',
                  message: '确认发货成功！'
                })
                this.getList()
              })
              .catch(response => {
                this.shipSubmiting = false
                this.$notify.error({
                  title: '失败',
                  message: response.data.errmsg
                })
              })
          }
        }
      })
    },
    handleRefund(row) {
      this.refundForm.orderNo = row.orderNo
      this.refundDialogVisible = true
      this.shipForm.shipCode = 'NONE'
    },
    confirmRefund() {
      this.$refs['refundForm'].validate(valid => {
        if (valid) {
          this.refundSubmiting = true
          refundOrder(this.refundForm)
            .then(response => {
              this.refundSubmiting = false
              this.refundDialogVisible = false
              this.$notify.success({
                title: '成功',
                message: '确认退款成功！'
              })
              this.getList()
            })
            .catch(response => {
              this.refundSubmiting = false
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
            })
        }
      })
    },
    // 订单表格中下载
    downOrderExcelBtn(row) {
      detailOrder(row.id).then(response => {
        var temp = response.data.data
        this.downData.address = temp.province + temp.city + temp.county + temp.address
        for (var j = 0; j < temp.skuList.length; j++) {
          var sku = temp.skuList[j]
          this.downData.unit = sku.unit
          this.downData.num = sku.num
          this.downData.specifications = sku.title
          this.downData.barcode = sku.barCode
          this.downData.name = sku.spuTitle
          var copy = Object.assign({}, this.downData)
          this.excelDataList.push(copy)
        }
        this.handleDownload(this.excelDataList)
        this.excelDataList = []
      })
    },
    // 选择条件下载
    downExcelBtn() {
      this.downloadLoading = true
      new Promise((resolve, reject) => {resolve(this.list)}).then(response => {
      // getExcelInfo(this.list).then(response => {
        console.log(response)
        console.log("response")
        if (response == null) {
          this.$notify.error({
            title: '失败',
            message: '没有信息可以打印'
          })
        }

        var data = response
        console.log(data)
        console.log(data.length)
        console.log("response")
        // for (var i = 0; i < data.length; i++) {
        //   var temp = data[i]
        //   this.downData.address = temp.province + temp.city + temp.county + temp.address
        //   for (var j = 0; j < temp.skuList.length; j++) {
        //     var sku = temp.skuList[j]
        //     this.downData.unit = sku.unit
        //     this.downData.num = sku.num
        //     this.downData.specifications = sku.title
        //     this.downData.barcode = sku.barCode
        //     this.downData.name = sku.spuTitle
        //     var copy = Object.assign({}, this.downData)
        //     this.excelDataList.push(copy)
        //   }
        // }
        this.handleDownload(response)
        this.excelDataList = []
        this.downloadLoading = false
      })
        .catch(response => {
          console.log(response)
          this.downloadLoading = false
          this.$notify.error({
            title: '失败',
            message: response.data.errmsg
          })
        })
    },
    handleDownload(data) {
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = [
          '原价',
          '现价',
          'VIP价格',
        ]
        const filterVal = [
          'originalPrice',
          'price',
          'vipPrice',
        ]
        excel.export_json_to_excel2(tHeader, data, filterVal, '订单信息')
      })
    }
  }
}
</script>

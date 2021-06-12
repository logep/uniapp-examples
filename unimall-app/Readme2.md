#  第一次
只能HbuilderX 编译 构建 运行   没有package.json 文件
有应用声生命周期 和页面生命周期
1. app 用户登陆注册的页面 用户名和手机号
2. 此app 原来只支持小程序登陆（userID 方法里cache 的key值设置出错导致h5网页登陆不成功）
3.怎么控制某些页面不显示tabbar （只有tab页面有tabbar非tab页面没有）
4. 怎么设置 badge（https://uniapp.dcloud.io/api/ui/tabbar?id=settabbarbadge）
uni.setTabBarBadge({
  index: 0,
  text: '1'
})

# 需要做的  todo
1.对接 微信公众号   通过某个网站 给公众号做模板发送消息
2.钉钉机器人 对接模板信息
3.企业微信的微工作台，个人微信注册就可以用（注册的时候团队名称随便一个），不用证件不用审核，HTTP API 推送，
只用 token 不用签名，开发特别简单，支持文本消息、图片消息、模板消息、按钮消息（按钮回调）。
4.走语音网关 电话播报语音通知
5.server 酱在用

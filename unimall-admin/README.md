# manager-web

#### 介绍
vue工程  管理后台 
# 有几点已经实现的：
国际化 针对标题  菜单名  一些固定的标题  内容是用户自己传上，已过



修复问题 ：
1.点击验证 如果是不需要验证用户,倒计时不开始，只是4s内disable
#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request


####  新增功能
1.怎么控制不需要登陆 进去页面 后台不通也可以启动( 主要是用户接口获取权限 再把白名单route 加入constantRouterMap 即可直接访问)
2.增加一个 发送短信页面 一个用户独有的功能 测试菜单权限  只需要登陆 就可以看到的页面  不需要经过查询权限验证
3. 写了两个一样的router  第一个生效 第二个不会生效 （如sendmsg 会在新窗口打开   本来设置第二个在右侧窗口区域打开的） 或者 直接把sendmsg 组件放置在 layout children下
4.         @HttpMethod(description = "发送短信" ,permission = "operation:message:send") 给后台短信增加了权限  如果此登陆人 有此权限可以发送


### 怎么配置到ngnix
1. 注意 static 文件夹的文件做代理 或者处理路径 生成/static/ 和 ./static/ 两种  参考第6点
2. centos 安装好后 启动、停止nginx
  cd /usr/local/nginx/sbin/
  ./nginx 
  ./nginx -s stop
  ./nginx -s quit
  ./nginx -s reload
  可能会发生异常
  3. 配置nginx.conf  做location 处理
  4. 把静态文件总的处理放置在某一个地方，在location里做处理
  5.做好端口号的安全处理
  6.
      // Paths
      assetsRoot: path.resolve(__dirname, '../dist'),
      assetsSubDirectory: 'static',
     // assetsPublicPath: '/',
      assetsPublicPath: './',
    7. 字体路径问题 修改  element-ui 字体路径问题 （解决方案  把element limit 修改下 base64打包）
  
  
  ###  需要做的 todostatic/fonts
 1.支持http2 先绑定域名 （已完成  9ping域名）
 2.  支持缓存 和gzip( 前端productionGzip开启 记得compression-webpack-plugin降级版本-高版本api对不上
   后端配置nginx 开启gzip
   location 配置 expires 30d;  做的配置是浏览器缓存  浏览器没有勾选 Disable cache  就在有效期内不会去请求
   )
 3.  支持 自动构建部署
 4. 支持cdn和构建时直接引用第三方域名
 5. 配置域名 访问
 6. 用nginx 配置多个端口号 开启负载均衡（
 ）
  6.1  安装php提供服务
  6.2 搞一个论坛 有注册和登陆服务 改造
  6.3 和手机app 匹配配套
  6.4 pc端抽奖  app抽奖  嵌入第三方app 抽奖
  6.5 访问指定phpmyadmin 数据库
  
  7. 加上百度的流量统计
  8. 封装引导页 插件
  9. 用composer 引用打包传递
  Docker-Compose将所管理的容器分为三层，分别是工程（project），服务（service）以及容器（container）。Docker-Compose运行目录下的所有文件（docker-compose.yml，extends文件或环境变量文件等）组成一个工程，若无特殊指定工程名即为当前目录名。一个工程当中可包含多个服务，
  每个服务中定义了容器运行的镜像，参数，依赖。一个服务当中可包括多个容器实例，Docker-Compose并没有解决负载均衡的问题，因此需要借助其它工具实现服务发现及负载均衡。
  10. 已增加 频繁提交防刷限制
  
  
  todo
  
  1. 增加在线视频播放通过摄像头
  2. 增加 七牛云上传
  3. 增加微信支付和支付宝支付
  4. IP定位  计算商家距离和当前距离
  5. 腾讯地图位置
  6. 微信小程序appid 和密钥
  7. 融合node api
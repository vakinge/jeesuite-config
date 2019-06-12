**交流群(提供开发视频教程)**：230192763🈵 ，61859839🈵 ，19829337

### 分布式配置中心(Distributed configuration center)
#### 为什么要造轮子？
之前试用过几个开源的配置中心，各有弊端(或者是不满足我们的需求)、譬如部署复杂、不支持全局配置、不支持敏感配置加密，对springboot支持不友好等等。所以从2015年开始就有了这个项目。
目前已经在生产环境多个公司生产环境稳定运行，欢迎点赞。

#### 实现功能
   1. 支持多环境、多版本配置管理
   1. 支持全局配置、多应用共享配置
   2. 支持配置文件、配置项、json配置支持
   3. 支持加密配置(server自动加密，client自动解密)
   4. 支持spring、springboot无缝对接
   5. 支持环境+项目维度精细化权限(只读/读写)控制
   6. 支持http和zookeeper方式配置实时同步
   7. 支持在线查看应用当前运行时配置（配置中心与本地合并后的最终配置）
   8. 支持查看配置历史版本、一键回滚、版本对比
   9. 安全功能支持:可选开启安全ip功能与内网拉取配置限制
   
#### 在线演示
http://config.jeesuite.com/admin.html
 - 测试管理员账号：testadmin/123456
 - 测试普通用户账号：testuser/123456
 
#### 文档
 - [部署文档](http://www.jeesuite.com/docs/quickstart/confcenter.html) 
 - [使用文档](http://www.jeesuite.com/docs/integration/confcenter.html) 

#### 关联项目
 - 基础库
  - [http://git.oschina.net/vakinge/jeesuite-libs](http://git.oschina.net/vakinge/jeesuite-libs)
  - [https://github.com/vakinge/jeesuite-libs](https://github.com/vakinge/jeesuite-libs)
  - 用户统一认证平台
  - [https://gitee.com/vakinge/jeesuite-passport](https://gitee.com/vakinge/jeesuite-passport)
  - [https://github.com/vakinge/jeesuite-passport](https://github.com/vakinge/jeesuite-passport)
  - 企业开发基础脚手架
  - [https://gitee.com/vakinge/oneplatform](https://gitee.com/vakinge/oneplatform)
  - [https://github.com/vakinge/oneplatform](https://github.com/vakinge/oneplatform)


#### 部分页面截图
页面基于`layui`构建。
##### 新建配置
![输入图片说明](https://images.gitee.com/uploads/images/2019/0612/162039_66a74f81_12388.png "新增配置.png")
##### 查看配置
![输入图片说明](https://images.gitee.com/uploads/images/2019/0612/162551_55f1292e_12388.png "查看配置.png")
##### 用户权限管理
![输入图片说明](https://images.gitee.com/uploads/images/2019/0612/163124_86171edc_12388.png "用户权限.png")





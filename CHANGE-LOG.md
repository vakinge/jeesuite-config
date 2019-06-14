### 1.3.3
#### 客户端
 - `ADD` [Ignore]配置语义支持:标记配置忽略
#### 服务端
 - `ADD` 增加本地查询缓存
 - `ADD` 支持自定义配置加密密钥
 - `FIXED` 普通用户未分配任何权限查询列表报错
 - `UPDATE` 支持手机号登录
 - `UPDATE` 优化配置加解密逻辑
 - `UPDATE` 升级jeesuite-libs版本
 - `UPDATE` 管理前端样式优化

### 1.3.2
#### 客户端
 - `ADD` 配置启动完成初始化InstanceFactory
 - `ADD` springboot下配置了`spring.profiles.active`自动忽略拉取远程配置
 - `ADD` 兼容springboot2.x
 - `ADD` 本地配置缓存，远程拉取失败加载本地缓存配置
 - `FIXED` 某些情况重复创建zookeeper监控节点
 
#### 服务端
 - `ADD` 配置变更历史
 - `ADD` 配置回滚
 - `ADD` 配置跨应用/环境复制
 - `ADD` 环境+APP维度权限(只读/读写)控制
 - `UPDATE` 移除RSA加密支持(配置复杂,场景少)
 - `UPDATE` 强化配置加密方式(密钥+DES+AES混合加密)
 - `UPDATE` 基于安全性及便利性考虑优化加密配置交互逻辑
 - `FIXED` 新增配置未发送更新事件
 

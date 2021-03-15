# 介绍

## 为什么要造轮子？
之前试用过几个开源的配置中心(disconf、Apollo、diamond以及Spring Cloud Config)，各有弊端或者是不满足我们的需求(譬如部署复杂、功能太复杂、不支持全局配置、不支持敏感配置加密，对springboot支持不友好,界面不友好或者无界面等等)。所以从2016年开始有了一个初级版本，经过三年生产环境实际需求不断完善于是就有了这个开源项目。

## 主要功能清单
   1. 支持多环境、多版本配置管理
   1. 支持全局配置
   2. 支持配置文件(properties,yaml,xml)、配置项、json配置支持
   3. 支持加密配置(server自动加密，client自动解密)
   4. 支持spring、springboot无缝对接
   5. 支持环境+项目维度精细化权限(只读/读写)控制
   6. 支持http和zookeeper方式配置实时同步
   7. 支持查看配置历史版本、一键回滚、版本对比
   8. 安全功能支持:可选开启安全ip功能与内网拉取配置限制
   9. 多业务组/部门数据隔离
   10. 支持集群部署

## 特点
 - 轻量级：无需任何中间件(http方式下发同步)即可运行
 - 代码简单：二开成本低
 - 安全性高：除了配置加密功能外，还提供多种拉取配置安全策略

# 服务端部署
### 下载项目

```
git clone https://gitee.com/vakinge/jeesuite-config.git
```

## 编译项目

```
mvn clean package -DskipTests=true
```

**最终生成部署包为：**jeesuite-config-server/target/jeesuite-config-server.jar

## 创建数据库表

```
CREATE DATABASE IF NOT EXISTS `configcenter` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
```

执行建表脚本:db.sql

## 关键配置说明（启动前请自行修改各个环境的配置）

```
#数据库配置
db_host=127.0.0.1
db_username=root
db_password=123456

#是否允许外网拉取配置（白名单机制生效）
api.extranet.enabled=true
#敏感配置是否强制加密
sensitive.config.force.encrypt=false

#开启自动缓存（依赖redis）
jeesuite.mybatis.cacheEnabled=true
jeesuite.cache.mode=standalone
jeesuite.cache.servers=127.0.0.1:6379
jeesuite.cache.password=123456
jeesuite.cache.database=0

#开启共享session（集群部署必须）
security.cache.storage-type=redis
security.cache.servers=127.0.0.1:6379
security.cache.password=123456
security.cache.database=1
```


## 启动
拷贝**jeesuite-config-server.jar** 与 _**application.properties**_在同一目录，springboot会优先加载同一目录下名为_application.properties的配置文件。_

```
nohup java -jar jeesuite-config-server.jar > config-server.out 2>&1 &
```

[](http://127.0.0.1:8080/admin.html)

admin/admin123


## 操作指引
 - 业务组管理：如果需要分多个业务组，各个组需要隔离，请先`添加业务组`;
 - 用户管理：添加用户，可以指定业务组，默认密码为手机号后八位；
 - profile(环境)管理：可以为每个环境配置配置`同步的zookeeper`以及拉取配置`IP白名单`
 - 应用管理：添加应用,会自动生成对应每个环境的token
 - 配置管理：配置分全局配置与应用配置，支持配置文件、key-value配置、json配置

---
# 应用集成（客户端）

## 添加依赖

```
<dependency>
  <groupId>com.jeesuite</groupId>
  <artifactId>jeesuite-config-client</artifactId>
 <version>[版本号]</version>
</dependency>
```

## 添加配置

在项目增加如下配置：
```
#是否启用配置中心，默认：true
jeesuite.configcenter.enabled=false
#应用名：对应server端的应用配置
jeesuite.configcenter.appName=account
jeesuite.configcenter.base.url=http://configserver:8080
#当前环境
jeesuite.configcenter.profile=dev
#拉取配置版本，默认:latest
jeesuite.configcenter.version=latest
#是否忽略全局配置,默认：false
jeesuite.configcenter.global-ignore=false
#拉取配置认证的token
jeesuite.configcenter.token=
#同步方式，默认:http
jeesuite.configcenter.sync-type=zookeeper
# 同步间隔，同步方式为：http时生效
jeesuite.configcenter.sync-interval-seconds=30
```

>通过JVM参数外部设置配置方式

```
-Djeesuite.configcenter.profile=dev
```

>docker外部设置配置方式

```
-e jeesuite.configcenter.profile="dev"
```

>springboot项目增加以上配置即可，spring项目还需要做如下修改：


1. 去掉原加载配置相关配置
2. 新增配置

```xml
<bean class="com.jeesuite.confcenter.spring.CCPropertyPlaceholderConfigurer">
    <property name="remoteEnabled" value="true" />
    <!-- 本地配置文件，无本地配置可不配置 -->
    <property name="locations">
      <list>
        <value>classpath*:application.properties</value>
      </list>
    </property>
</bean>
```

## 一些用法

### 配置优先级

1. 应用本地配置 &gt; 远程应用配置 &gt; 远程全局配置
2. 配置中心配置`jeesuite.configcenter.remote-config-first=true`可以启用远程配置覆盖本地配置

### 配置实时生效

配置变更后会实时下发到应用，可以通过以下方式实时读取最新配置  
1. 在代码中使用`ResourceUtils`实时读取  
2. 依赖注入`Environment`，在代码中实时读取  
3. 实现`ConfigChangeHanlder`接口，自定义刷新逻辑

---

```java
@Controller  
@RequestMapping(value = "/sms")
public class AuthCommonController implements ConfigChangeHanlder{

    @Value("${sms.send.open}")
    private boolean open = false;


    @Override
    public void onConfigChanged(Map<String, Object> changedConfigs) {
        if(changedConfigs.containsKey("sms.send.open")){
            open = Boolean.parseBoolean(changedConfigs.get("sms.send.open").toString());
        }
    }
}
```

### 配置忽略
正常情况应用的配置为全局配置与应用自身配置合并的结果，如果不需要某些全局配置项目如下配置即可
```
db.maxActive=[Ignore]
```

#### 部分页面截图(基于`layui`构建)
##### 新建配置
![输入图片说明](https://images.gitee.com/uploads/images/2019/0612/162039_66a74f81_12388.png "新增配置.png")
##### 查看配置
![输入图片说明](https://images.gitee.com/uploads/images/2019/0612/162551_55f1292e_12388.png "查看配置.png")
##### 用户权限管理
![输入图片说明](https://images.gitee.com/uploads/images/2019/0612/163124_86171edc_12388.png "用户权限.png")

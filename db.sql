SET NAMES utf8;

DROP TABLE IF EXISTS `profiles`;
CREATE TABLE `profiles` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `alias` varchar(32) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  `enabled` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `app_configs`
-- ----------------------------
DROP TABLE IF EXISTS `app_configs`;
CREATE TABLE `app_configs` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `app_ids` varchar(100) DEFAULT NULL,
  `env` varchar(16) DEFAULT NULL,
  `version` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `type` smallint(1) DEFAULT NULL COMMENT '类型(1:文件，2:配置项，3：JSON)',
  `contents` text,
  `enabled` tinyint(1) DEFAULT 1,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `apps`
-- ----------------------------
DROP TABLE IF EXISTS `apps`;
CREATE TABLE `apps` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `alias` varchar(32) DEFAULT NULL,
  `master` varchar(16) DEFAULT NULL,
  `master_uid` int(10) DEFAULT NULL,
  `scm_link` varchar(100) DEFAULT NULL,
  `app_type` int(1) DEFAULT 1 COMMENT '类型(1:微服务，2:传统web服务，3：dubbo，4：其他)',
  `remarks` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `app_extr_attrs`;
CREATE TABLE `app_extr_attrs` (
  `app_id` int(10) NOT NULL,
  `attr_name` varchar(32) NOT NULL,
  `attr_value` varchar(32) NOT NULL,
  PRIMARY KEY (`app_id`,`attr_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `users`
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(16) DEFAULT NULL,
  `password` varchar(32) DEFAULT NULL,
  `mobile` char(11) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `type` smallint(1) DEFAULT NULL,
  `status` smallint(1) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `app_configs_history`;
CREATE TABLE `app_configs_history` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `origin_id` int(10) NOT NULL,
  `app_ids` varchar(100) DEFAULT NULL,
  `app_names` varchar(200) DEFAULT NULL,
  `env` varchar(16) DEFAULT NULL,
  `version` varchar(32) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `type` smallint(1) DEFAULT NULL COMMENT '类型(1:文件，2:配置项，3：JSON)',
  `contents` text,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_permissions`;
CREATE TABLE `user_permissions` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_id` int(10) NOT NULL,
  `env` varchar(16) NOT NULL,
  `app_id` int(10) NOT NULL,
  `operate` ENUM('RO', 'RW') DEFAULT 'RW',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `profiles` VALUES (1,'test','测试环境',1,1),(2,'dev','开发环境',1,1),(3,'prep',' 预发布环境',1,1),(4,'prod','线上环境',1,1);
INSERT INTO `users` (`name`, `password`, `type`, `status`) VALUES ('admin', 'be6895cdcfdc99ab54fb8b93590439eb', '1', '1');

-- ----------------------------
--  以下是测试数据可以选择性导入
-- ----------------------------
INSERT INTO `apps` VALUES (1,'oneplatform','oneplatform基础平台',NULL,'vakinge',21,NULL,NULL),(2,'common-service','通用服务',NULL,'vakinge',21,NULL,NULL);
INSERT INTO `app_configs` VALUES (1,NULL,'dev','release','global.properties',1,'swagger.enable=true\r\nzookeeper.servers=127.0.0.1:2181\r\n#cache\r\njeesuite.cache.mode=standalone\r\njeesuite.cache.servers=127.0.0.1:6379\r\njeesuite.cache.maxPoolSize=10\r\njeesuite.cache.maxPoolIdle=2\r\njeesuite.cache.minPoolIdle=1\r\njeesuite.cache.maxPoolWaitMillis=30000\r\njeesuite.cache.password=123456\r\n\r\n#datasource\r\ndb.group.size=1\r\ndb.shard.size=1000\r\ndb.driverClass=com.mysql.jdbc.Driver\r\ndb.initialSize=2\r\ndb.minIdle=1\r\ndb.maxActive=50\r\ndb.maxWait=60000\r\ndb.timeBetweenEvictionRunsMillis=60000\r\ndb.minEvictableIdleTimeMillis=300000\r\ndb.testOnBorrow=true\r\ndb.testOnReturn=false\r\n\r\n#mybatis\r\njeesuite.mybatis.cacheEnabled=true\r\njeesuite.mybatis.dynamicExpire=false\r\njeesuite.mybatis.cacheExpireSeconds=3600\r\njeesuite.mybatis.paginationEnabled=true\r\njeesuite.mybatis.cacheEnabled=true\r\n\r\n#kafka\r\nkafka.bootstrap.servers=127.0.0.1:9092\r\nkafka.zkServers=${zookeeper.servers}\r\njeesuite.kafka.producer.monitorZkServers=${zookeeper.servers}\r\n\r\n#eureka\r\neureka.instance.preferIpAddress=true\r\neureka.client.serviceUrl.defaultZone=http://oneplatform:oneplatform2018@127.0.0.1:19991/eureka/\r\n#eureka.client.healthcheck.enabled=true   \r\neureka.client.registry-fetch-interval-seconds=5\r\neureka.instance.lease-expiration-duration-in-seconds=30\r\neureka.instance.lease-renewal-interval-in-seconds=10\r\neureka.region=default\r\neureka.registration.enabled=true\r\neureka.preferIpAddress=true\r\neureka.preferSameZone=true\r\neureka.shouldUseDns=false\r\neureka.serviceUrl.default=${eureka.client.serviceUrl.defaultZone}\r\neureka.decoderName=JacksonJson\r\n\r\n#hystrix\r\nhystrix.command.default.execution.timeout.enabled=true\r\nhystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=10000\r\n\r\n#ribbon\r\nribbon.eureka.enabled=true\r\nribbon.ServerListRefreshInterval=2000\r\nribbon.ConnectTimeout = 2000\r\nribbon.ReadTimeout = 10000\r\nribbon.OkToRetryOnAllOperations=false\r\nribbon.MaxAutoRetriesNextServer=0\r\nribbon.MaxAutoRetries = 0\r\n\r\n#loadbalancer\r\nspring.cloud.loadbalancer.retry.enabled=false\r\n\r\n#file\r\n#指定全局组id （默认为：public & private）\r\npublic.filesystem.id=public\r\nprivate.filesystem.id=private\r\n\r\n#全局公共空间\r\npublic.filesystem.provider=qiniu\r\npublic.filesystem.bucketName=test112233\r\npublic.filesystem.urlprefix=http://owep828p6.bkt.clouddn.com\r\npublic.filesystem.accessKey=iqq3aa-ncqfdGGubCcS-N8EUV-qale2ezndnrtKS\r\npublic.filesystem.secretKey=1RmdaMVjrjXkyRVPOmyMa6BzcdG5VDdF-SH_HUTe\r\npublic.filesystem.private=false\r\n\r\n#全局私有空间\r\nprivate.filesystem.provider=qiniu\r\nprivate.filesystem.bucketName=testa1b2c3\r\nprivate.filesystem.urlprefix=http://ovjjqjpmp.bkt.clouddn.com\r\nprivate.filesystem.accessKey=iqq3aa-ncqfdGGubCcS-N8EUV-qale2ezndnrtKS\r\nprivate.filesystem.secretKey=1RmdaMVjrjXkyRVPOmyMa6BzcdG5VDdF-SH_HUTe\r\nprivate.filesystem.private=true\r\n\r\n#spring\r\nspring.jackson.time-zone=GMT+8\r\nspring.http.encoding.charset=UTF-8\r\nspring.http.encoding.force=true\r\nspring.http.encoding.enabled=true\r\nspring.http.multipart.max-file-size=1Mb\r\nspring.http.multipart.max-request-size=1Mb\r\nserver.tomcat.uri-encoding=UTF-8\r\nspring.http.multipart.location=/datas/tmp\r\n\r\n#configcenter\r\njeesuite.configcenter.sync-interval-seconds=30\r\njeesuite.configcenter.sync-zk-servers=${zookeeper.servers}\r\njeesuite.configcenter.sync-type=zookeeper\r\n\r\n#other\r\njeesuite.task.registryServers=${zookeeper.servers}\r\n\r\n',NULL,NULL,NULL,NULL),(2,'1','dev','release','application.properties',1,'jeesuite.cache.database=0\r\n\r\n#zuul\r\nzuul.host.maxTotalConnections=100\r\nzuul.host.maxPerRouteConnections=20\r\nzuul.semaphore.max-semaphores=200\r\nzuul.host.connect-timeout-millis=3000\r\nzuul.host.socket-timeout-millis=10000\r\n\r\n#rate limit\r\nrequest.limit.enabled=true\r\nrequest.limit.max-connections=100\r\nrequest.limit.per-frequency=10/10s\r\nrequest.limit.post-only=true\r\n\r\n#db\r\nmaster.db.url=jdbc:mysql://127.0.0.1:3306/oneplatform?useUnicode=true&characterEncoding=UTF-8\r\nmaster.db.username=root\r\nmaster.db.password=123456',NULL,NULL,NULL,NULL),(3,'2','dev','release','application.properties',1,'#db\r\nmaster.db.url=jdbc:mysql://127.0.0.1:3306/oneplatform?useUnicode=true&characterEncoding=UTF-8\r\nmaster.db.username=root\r\nmaster.db.password=123456',NULL,NULL,NULL,NULL);

INSERT INTO `users` (`name`, `password`, `type`, `status`) VALUES ('testadmin', '318f22743d115c77a89ebb8ee6c394e8', '1', '1');
INSERT INTO `users` (`name`, `password`, `type`, `status`) VALUES ('testuser', '318f22743d115c77a89ebb8ee6c394e8', '2', '1');


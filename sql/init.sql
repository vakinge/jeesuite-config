DROP TABLE IF EXISTS `business_group`;
CREATE TABLE `business_group` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `master` varchar(16) DEFAULT NULL COMMENT '负责人姓名',
  `master_uid` int(10) DEFAULT NULL,
  `remarks` text,
  `enabled` tinyint(1) DEFAULT 1,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务组';


DROP TABLE IF EXISTS `profiles`;
CREATE TABLE `profiles` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL,
  `alias` varchar(32) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  `enabled` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `profile_extr_attrs`;
CREATE TABLE `profile_extr_attrs` (
  `profile` varchar(32) NOT NULL,
  `attr_name` varchar(50) NOT NULL,
  `attr_value` varchar(100) NOT NULL,
  PRIMARY KEY (`profile`,`attr_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `app_configs`
-- ----------------------------
DROP TABLE IF EXISTS `app_configs`;
CREATE TABLE `app_configs` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `group_id` INT(10) NULL COMMENT '所属业务组',
  `app_id` INT(10) DEFAULT NULL,
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
  `group_id` INT(10) NULL COMMENT '所属业务组',
  `app_key` varchar(32) DEFAULT NULL,
  `app_name` varchar(32) DEFAULT NULL,
  `service_id` varchar(64) DEFAULT NULL,
  `app_type` varchar(32) DEFAULT 1 COMMENT '应用类型',
  `master` varchar(16) DEFAULT NULL,
  `master_uid` int(10) DEFAULT NULL,
  `health_uri` varchar(200) DEFAULT NULL,
  `scm_link` varchar(100) DEFAULT NULL,
  `remarks` text,
  `enabled` tinyint(1) DEFAULT 1,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `app_extr_attrs`;
CREATE TABLE `app_extr_attrs` (
  `app_id` int(10) NOT NULL,
  `env` varchar(16) NOT NULL,
  `attr_name` varchar(32) NOT NULL,
  `attr_value` varchar(100) NOT NULL,
  PRIMARY KEY (`app_id`,`env`,`attr_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `users`
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `group_id` INT(10) NULL COMMENT '所属业务组',
  `name` varchar(16) DEFAULT NULL,
  `password` varchar(64) DEFAULT NULL,
  `mobile` char(11) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `type` ENUM('user','groupAdmin','superAdmin') DEFAULT 'user',
  `enabled` tinyint(1) DEFAULT 1,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `app_configs_history`;
CREATE TABLE `app_configs_history` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `origin_id` int(10) NOT NULL,
  `app_id` int(10) DEFAULT NULL,
  `app_name` varchar(32) DEFAULT NULL,
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


INSERT INTO `profiles` VALUES (1,'dev','开发环境',1,1),(2,'test','测试环境',1,1),(3,'pre',' 预发布环境',1,1),(4,'prd','线上环境',1,1);
INSERT INTO `users` (`name`, `password`, `type`, `enabled`) VALUES ('admin', '$2a$04$/KqvyU5TPlrygqmIAlBLUeE1fEIO5SGQgOsCuJ1wGMod.vr5YX8/S', 'superAdmin', 1);
INSERT INTO `business_group` (`name`, `enabled`) VALUES ('默认组', '1');



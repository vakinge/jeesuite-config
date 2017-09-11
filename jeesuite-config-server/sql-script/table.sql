SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

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
  `app_name` varchar(32) DEFAULT NULL,
  `env` varchar(16) DEFAULT NULL,
  `version` varchar(16) DEFAULT NULL,
  `name` varchar(32) DEFAULT NULL,
  `type` smallint(1) DEFAULT NULL COMMENT '类型(1:文件，2:配置项)',
  `contents` text,
  `created_at` datetime DEFAULT NULL,
  `created_by` int(10) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` int(10) DEFAULT NULL,
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
  `secret` varchar(128) DEFAULT NULL,
  `notify_emails` varchar(255) DEFAULT NULL,
  `master` varchar(16) DEFAULT NULL,
  `master_uid` int(10) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`id`)
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
  `gant_envs` varchar(64) DEFAULT NULL,
  `status` smallint(1) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `monitor_servers`;
CREATE TABLE `monitor_servers` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `moudule` varchar(32) DEFAULT NULL,
  `env` varchar(32) DEFAULT NULL,
  `servers` varchar(128) DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `profiles` VALUES (1,'test','测试环境',1,1),(2,'dev','开发环境',1,1),(3,'prep',' 预发布环境',1,1),(4,'prod','线上环境',1,1);
INSERT INTO `users` (`name`, `password`, `type`, `status`) VALUES ('admin', 'f5866c4a4d6014ecced47960c2e3d07f', '1', '1');


DROP TABLE IF EXISTS `operate_logs`;
CREATE TABLE `operate_logs` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `uid` int(10) DEFAULT NULL,
  `uname` varchar(32) DEFAULT NULL,
  `act_name` varchar(32) DEFAULT NULL,
  `biz_data` varchar(100) DEFAULT NULL,
  `before_data` text DEFAULT NULL,
  `after_data` text DEFAULT NULL,
  `ip_addr` varchar(15) DEFAULT NULL,
  `act_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `app_secret`;
CREATE TABLE `app_secret` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `app_id` int(10) DEFAULT NULL,
  `env` varchar(16) DEFAULT NULL,
  `secret_type` ENUM('RSA', 'DES') DEFAULT NULL,
  `secret_key` varchar(100) DEFAULT NULL,
  `secret_pass` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `aet_uq_index` (`app_id`,`env`,`secret_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


ALTER TABLE `apps` DROP COLUMN `secret`;

ALTER TABLE `app_configs` 
CHANGE COLUMN `app_name` `app_name` VARCHAR(200) NULL DEFAULT NULL ;

ALTER TABLE `operate_logs` 
CHANGE COLUMN `biz_data` `biz_data` VARCHAR(500) NULL DEFAULT NULL ;

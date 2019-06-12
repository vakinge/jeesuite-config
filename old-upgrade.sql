DROP TABLE IF EXISTS `app_secret`;
DROP TABLE IF EXISTS `operate_logs`;

ALTER TABLE `app_configs` 
CHANGE COLUMN `created_by` `created_by` VARCHAR(32) NULL DEFAULT NULL ,
CHANGE COLUMN `updated_by` `updated_by` VARCHAR(32) NULL DEFAULT NULL ,
ADD COLUMN `enabled` TINYINT(1) NULL DEFAULT 1 AFTER `contents`;

ALTER TABLE `apps` 
DROP COLUMN `notify_emails`,
ADD COLUMN `app_type` int(1) DEFAULT 1 ;

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

DROP TABLE IF EXISTS `app_extr_attrs`;
CREATE TABLE `app_extr_attrs` (
  `app_id` int(10) NOT NULL,
  `attr_name` varchar(32) NOT NULL,
  `attr_value` varchar(32) NOT NULL,
  PRIMARY KEY (`app_id`,`attr_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `groups`
-- ----------------------------
DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups` (
  `id` int(11) auto_increment PRIMARY KEY,
  `name` varchar(45) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  UNIQUE KEY `groups_name_UNIQUE` (`name`)
);

-- ----------------------------
--  Records of `groups`
-- ----------------------------
BEGIN;
INSERT INTO `groups` VALUES (1, 'Default', 'Default Group', now());
COMMIT;

-- ----------------------------
--  Table structure for `task`
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` int(11) auto_increment PRIMARY KEY,
  `name` varchar(45) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `schedule_str` varchar(255) DEFAULT NULL,
  `code` longtext NOT NULL,
  `code_type` varchar(255) DEFAULT NULL,
  `group_id` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `version` varchar(32) NOT NULL,
  `type` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `create_user` varchar(45) DEFAULT NULL,
  `update_user` varchar(45) DEFAULT NULL,
  UNIQUE KEY `task_name_UNIQUE` (`name`)
) ;



-- ----------------------------
--  Table structure for `task_history`
-- ----------------------------
DROP TABLE IF EXISTS `task_history`;
CREATE TABLE `task_history` (
  `id` int(11) auto_increment PRIMARY KEY,
  `task_id` varchar(32) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `schedule_str` varchar(255) DEFAULT NULL,
  `code` longtext NOT NULL,
  `code_type` varchar(255) DEFAULT NULL,
  `group_id` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `version` varchar(32) NOT NULL,
  `type` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `create_user` varchar(45) DEFAULT NULL,
  `update_user` varchar(45) DEFAULT NULL
) ;


-- ----------------------------
--  Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) auto_increment PRIMARY KEY,
  `name` varchar(45) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `type` int(11) NOT NULL,
  `mail` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL
) ;

-- ----------------------------
--  Records of `user`
-- ----------------------------

BEGIN;
INSERT INTO `user` VALUES ('1', 'admin', 'd70900a860013845b32be1d0fbc89d58', '1', 'yourmail', now());
COMMIT;

-- ----------------------------
--  Table structure for `user_group`
-- ----------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
  `id` int(11) auto_increment PRIMARY KEY,
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `auth` int(11) NOT NULL,
  `create_time` datetime DEFAULT NULL
) ;



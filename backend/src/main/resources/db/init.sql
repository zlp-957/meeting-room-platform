-- =====================================================
-- 会议室预约平台 数据库初始化脚本
-- 数据库名：meeting_room
-- 说明：先手动创建数据库，再执行本脚本
--   CREATE DATABASE meeting_room DEFAULT CHARACTER SET utf8mb4;
-- =====================================================

USE meeting_room;

-- ---------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '加密密码(BCrypt)',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1正常/0禁用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ---------------------------------------------------
-- 2. 会议室表
-- ---------------------------------------------------
DROP TABLE IF EXISTS `meeting_room`;
CREATE TABLE `meeting_room` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_name`           VARCHAR(100) NOT NULL COMMENT '会议室名称',
    `location`            VARCHAR(200) DEFAULT NULL COMMENT '位置',
    `capacity`            INT          DEFAULT 0 COMMENT '容纳人数',
    `equipment`           VARCHAR(500) DEFAULT NULL COMMENT '设备描述',
    `available_time_slot` VARCHAR(1000) DEFAULT NULL COMMENT '可预约时段配置(JSON数组)',
    `remark`              VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='会议室表';

-- ---------------------------------------------------
-- 3. 预约表（核心表）
-- ---------------------------------------------------
DROP TABLE IF EXISTS `meeting_book`;
CREATE TABLE `meeting_book` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT       NOT NULL COMMENT '预约人id',
    `room_id`       BIGINT       NOT NULL COMMENT '会议室id',
    `book_title`    VARCHAR(200) NOT NULL COMMENT '预约主题',
    `time_slot`     VARCHAR(50)  NOT NULL COMMENT '预约时段，如 08:00-08:45',
    `book_date`     DATE         NOT NULL COMMENT '预约日期',
    `people_num`    INT          DEFAULT 1 COMMENT '参会人数',
    `real_name`     VARCHAR(50)  DEFAULT NULL COMMENT '预约人真实姓名（实名认证）',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '预约人手机号（实名认证）',
    `audit_status`  VARCHAR(20)  NOT NULL DEFAULT 'wait' COMMENT 'wait待审核/pass通过/reject驳回/cancel取消',
    `slot_flag`     VARCHAR(1)   DEFAULT NULL COMMENT '有效预约标志：wait/pass为1，reject/cancel为NULL。配合唯一索引保证同一会议室同一时段仅一条有效预约',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    UNIQUE KEY `uk_active_slot` (`room_id`, `book_date`, `time_slot`, `slot_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='预约表';

-- ---------------------------------------------------
-- 初始化数据
-- ---------------------------------------------------

-- 用户：admin / 123456  （管理员）
-- 用户：user  / 123456  （普通用户）
-- 密码均为 BCrypt 加密后的 "123456"
INSERT INTO `user` (`username`, `password`, `nickname`, `real_name`, `phone`, `role`) VALUES
('admin', '$2a$10$LId15JH1v7A8sx0wvgjZIODS0EHHBtabi0jL9NDf0hqzgy1jjKo6.', '管理员', '系统管理员', '13800000000', 'admin'),
('user',  '$2a$10$LId15JH1v7A8sx0wvgjZIODS0EHHBtabi0jL9NDf0hqzgy1jjKo6.', '测试用户', '张三', '13900000000', 'user');

-- 会议室（含预设可预约时段）
INSERT INTO `meeting_room` (`room_name`, `location`, `capacity`, `equipment`, `available_time_slot`, `remark`) VALUES
('一号会议室', 'A栋1层101', 10, '投影仪、白板、视频会议', '["08:00-08:45","09:00-09:45","10:00-10:45","11:00-11:45","14:00-14:45","15:00-15:45"]', '小型会议室'),
('二号会议室', 'A栋2层201', 20, '投影仪、白板、音响', '["08:00-08:45","09:00-09:45","10:00-10:45","14:00-14:45","15:00-15:45","16:00-16:45"]', '中型会议室'),
('三号会议室', 'B栋1层103', 40, '投影仪、视频会议、电子白板', '["09:00-09:45","10:00-10:45","11:00-11:45","14:00-14:45","15:00-15:45","16:00-16:45"]', '大型会议室');

-- =====================================================
-- 会议室预约平台 增量迁移脚本（对已有数据库执行）
-- 用途：新增实名认证字段、用户禁用状态字段
-- 执行方式：mysql -uroot -p meeting_room < upgrade.sql
-- =====================================================

USE meeting_room;

-- 1. 预约表新增实名认证字段（若已存在则忽略）
ALTER TABLE `meeting_book`
    ADD COLUMN `real_name` VARCHAR(50) DEFAULT NULL COMMENT '预约人真实姓名（实名认证）' AFTER `people_num`,
    ADD COLUMN `phone`     VARCHAR(20) DEFAULT NULL COMMENT '预约人手机号（实名认证）' AFTER `real_name`;

-- 2. 用户表新增状态字段（若已存在则忽略）
ALTER TABLE `user`
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1正常/0禁用' AFTER `role`;

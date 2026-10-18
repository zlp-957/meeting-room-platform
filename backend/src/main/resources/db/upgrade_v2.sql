-- =====================================================
-- 会议室预约平台 增量迁移脚本 v2
-- 用途：手机号唯一绑定（一个手机号只能注册一个账号）
-- 前置：确认 user 表 phone 列无重复、无 NULL
-- 执行方式：mysql -uroot -p meeting_room < upgrade_v2.sql
-- =====================================================

USE meeting_room;

-- 1. 手机号唯一索引（若已存在会报错，可先手动 DROP INDEX uk_phone）
ALTER TABLE `user`
    ADD UNIQUE KEY `uk_phone` (`phone`);

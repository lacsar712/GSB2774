-- 回滚脚本（执行前请确保不存在 owner_user_id 为空的数据）
-- SELECT COUNT(*) FROM venues WHERE owner_user_id IS NULL;

ALTER TABLE venues
    DROP INDEX uk_owner_user_id;

ALTER TABLE venues
    MODIFY COLUMN owner_user_id BIGINT NOT NULL COMMENT '球馆管理员ID';

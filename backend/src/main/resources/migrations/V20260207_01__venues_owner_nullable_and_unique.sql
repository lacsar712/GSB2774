-- 目的：
-- 1) 允许球馆暂时不绑定管理员（owner_user_id 可空）
-- 2) 保证一个管理员最多绑定一个球馆（唯一索引）
--
-- 执行前检查（建议）：
-- SELECT owner_user_id, COUNT(*) cnt
-- FROM venues
-- WHERE owner_user_id IS NOT NULL
-- GROUP BY owner_user_id
-- HAVING cnt > 1;
-- 若返回记录，请先清理重复绑定再执行本迁移。

ALTER TABLE venues
    MODIFY COLUMN owner_user_id BIGINT NULL COMMENT '球馆管理员ID';

ALTER TABLE venues
    ADD UNIQUE INDEX uk_owner_user_id (owner_user_id);

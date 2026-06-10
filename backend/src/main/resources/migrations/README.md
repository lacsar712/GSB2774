# 数据库迁移说明

## 本目录约定

- `V*.sql`：正向迁移脚本（版本化）
- `R*.sql`：对应回滚脚本

## 本次迁移

- 正向：`V20260207_01__venues_owner_nullable_and_unique.sql`
- 回滚：`R20260207_01__venues_owner_nullable_and_unique.sql`

## 执行步骤（MySQL 8）

1. 先做备份。
2. 执行正向迁移前检查重复绑定：
   ```sql
   SELECT owner_user_id, COUNT(*) cnt
   FROM venues
   WHERE owner_user_id IS NOT NULL
   GROUP BY owner_user_id
   HAVING cnt > 1;
   ```
3. 执行迁移：
   ```bash
   mysql -h <host> -u <user> -p <db_name> < V20260207_01__venues_owner_nullable_and_unique.sql
   ```
4. 验证：
   ```sql
   SHOW INDEX FROM venues WHERE Key_name = 'uk_owner_user_id';
   DESC venues;
   ```

## 回滚步骤

1. 回滚前确认 `owner_user_id` 没有 `NULL`：
   ```sql
   SELECT COUNT(*) FROM venues WHERE owner_user_id IS NULL;
   ```
2. 执行回滚：
   ```bash
   mysql -h <host> -u <user> -p <db_name> < R20260207_01__venues_owner_nullable_and_unique.sql
   ```

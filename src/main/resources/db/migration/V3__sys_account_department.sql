-- ==================== 账号表：新增所属部门 ====================
-- 部门为可选项，所以 department_id 允许为 NULL（老数据保持 NULL 即为"未分配部门"）。
-- sys_department 只做软删除、从不物理删除，因此这里加外键不会因为删部门而失败。
ALTER TABLE `sys_account`
    ADD COLUMN `department_id` BIGINT DEFAULT NULL COMMENT '所属部门ID' AFTER `phone`,
    ADD INDEX `idx_account_department` (`department_id`),
    ADD CONSTRAINT `fk_account_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`);

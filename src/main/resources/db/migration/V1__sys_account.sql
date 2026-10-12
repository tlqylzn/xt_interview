-- ==================== 账号表 ====================
CREATE TABLE IF NOT EXISTS `sys_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账号ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `name` VARCHAR(100) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别：男/女',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系方式',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `role_level` INT DEFAULT 1 COMMENT '权限级别：1-用户权限，2-管理员权限，3-超级管理员权限',
    `status` INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_super` INT DEFAULT 0 COMMENT '是否超级管理员：0-否，1-是',
    `deleted` INT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_role_level` (`role_level`),
    INDEX `idx_account_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账号表';

-- ==================== 账号初始数据 ====================
-- 默认管理员账号：admin / 123456
INSERT IGNORE INTO `sys_account` (`username`, `name`, `gender`, `phone`, `password`, `role_level`, `status`, `is_super`, `deleted`) VALUES
('admin', '超级管理员', '男', NULL, 'e10adc3949ba59abbe56e057f20f883e', 3, 1, 1, 0);

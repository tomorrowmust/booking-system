CREATE TABLE `chat_session` (
                                `id` BIGINT NOT NULL COMMENT '主键ID, 使用雪花算法生成',
                                `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识(UUID)',
                                `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题(AI自动生成,最多100字)',
                                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_session_id` (`session_id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话会话表';
-- ms_notification_service database schema

CREATE DATABASE IF NOT EXISTS ms_notification_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ms_notification_service;

CREATE TABLE IF NOT EXISTS announcement (
    announcement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    meeting_id BIGINT COMMENT '关联运动会ID',
    publisher_id BIGINT,
    status BIGINT DEFAULT 0 COMMENT '0=draft, 1=published',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type INT COMMENT '1=system, 2=game_reminder, 3=result_notice',
    related_id BIGINT COMMENT '关联ID（如赛程ID）',
    is_read TINYINT DEFAULT 0 COMMENT '0=unread, 1=read',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

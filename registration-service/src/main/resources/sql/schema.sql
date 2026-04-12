-- ms_registration_service database schema

CREATE DATABASE IF NOT EXISTS ms_registration_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ms_registration_service;

CREATE TABLE IF NOT EXISTS personal_entry (
    entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    status BIGINT DEFAULT 0 COMMENT '0=pending, 1=confirmed, 2=cancelled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人报名表';

CREATE TABLE IF NOT EXISTS team_entry (
    team_entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    item_id BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    captain_id BIGINT COMMENT '队长用户ID',
    status BIGINT DEFAULT 0 COMMENT '0=pending, 1=confirmed, 2=cancelled',
    max_members INT DEFAULT 10,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队报名表';

CREATE TABLE IF NOT EXISTS team_member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_entry_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团队成员表';

-- ms_score_service database schema

CREATE DATABASE IF NOT EXISTS ms_score_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ms_score_service;

CREATE TABLE IF NOT EXISTS personal_result (
    result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    score DECIMAL(10,2) COMMENT '成绩（时间/分数）',
    `rank` INT COMMENT '排名',
    status BIGINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人成绩表';

CREATE TABLE IF NOT EXISTS team_result (
    team_result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_entry_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    meeting_id BIGINT NOT NULL,
    score DECIMAL(10,2) COMMENT '成绩',
    `rank` INT COMMENT '排名',
    status BIGINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团体成绩表';

CREATE TABLE IF NOT EXISTS award (
    award_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    user_id BIGINT COMMENT '个人奖项关联用户',
    team_entry_id BIGINT COMMENT '团体奖项关联团队',
    award_name VARCHAR(100) NOT NULL,
    award_level INT COMMENT '1=一等奖, 2=二等奖, 3=三等奖',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖项表';

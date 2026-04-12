-- Additions to ms_event_service database

-- Event category table
CREATE TABLE IF NOT EXISTS event_category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    status BIGINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目分类表';

-- Score rule table
CREATE TABLE IF NOT EXISTS score_rule (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    `rank` INT NOT NULL COMMENT '名次',
    score INT NOT NULL COMMENT '积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- Default score rules
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 1, 9);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 2, 7);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 3, 6);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 4, 5);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 5, 4);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 6, 3);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 7, 2);
INSERT INTO score_rule (meeting_id, `rank`, score) VALUES (1, 8, 1);

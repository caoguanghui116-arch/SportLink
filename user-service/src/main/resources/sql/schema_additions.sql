-- Additions to ms_user_service database

-- Role table
CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_key VARCHAR(50) NOT NULL,
    role_sort INT DEFAULT 0,
    status CHAR(1) DEFAULT '0',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag VARCHAR(2) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- Insert default roles
INSERT INTO sys_role (role_name, role_key, role_sort) VALUES ('管理员', 'admin', 1);
INSERT INTO sys_role (role_name, role_key, role_sort) VALUES ('裁判', 'referee', 2);
INSERT INTO sys_role (role_name, role_key, role_sort) VALUES ('运动员', 'athlete', 3);

-- Announcement table
CREATE TABLE IF NOT EXISTS announcement (
    announcement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    meeting_id BIGINT COMMENT '关联运动会ID，null=全系统公告',
    publisher_id BIGINT,
    status BIGINT DEFAULT 0 COMMENT '0=draft, 1=published',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag BIGINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================
-- TeamHub 数据库初始化脚本
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',  -- MEMBER / CAPTAIN
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 球员档案表
CREATE TABLE IF NOT EXISTS player (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    jersey_number INT,
    position VARCHAR(50),
    entry_year INT NOT NULL,        -- 入学年份，用于自动计算年级
    photo_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE / ALUMNI
    degree VARCHAR(20) DEFAULT '本科',     -- 本科 / 硕士 / 博士
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 训练表
CREATE TABLE IF NOT EXISTS training (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    train_time DATETIME NOT NULL,
    content TEXT,
    video_url VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES user(id)
);

-- 训练报名表
CREATE TABLE IF NOT EXISTS training_signup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,   -- ATTEND / ABSENT / PENDING
    reason VARCHAR(200),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_training_user (training_id, user_id),
    FOREIGN KEY (training_id) REFERENCES training(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 比赛表
CREATE TABLE IF NOT EXISTS match_game (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    opponent VARCHAR(100) NOT NULL,
    game_time DATETIME NOT NULL,
    location VARCHAR(100),
    game_type VARCHAR(50) NOT NULL,  -- 训练赛/新生杯/华工杯/其他
    status VARCHAR(20) DEFAULT 'UPCOMING',  -- UPCOMING/ONGOING/FINISHED
    lineup TEXT,          -- 首发名单（JSON字符串）
    notes TEXT,
    video_url VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES user(id)
);

-- 比赛小局比分表
CREATE TABLE IF NOT EXISTS match_set (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    set_number INT NOT NULL,
    our_score INT NOT NULL,
    opp_score INT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES match_game(id)
);

-- 个人建议表
CREATE TABLE IF NOT EXISTS player_suggestion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_user_id BIGINT NOT NULL,   -- 被建议的球员
    from_user_id BIGINT NOT NULL,     -- 建议人
    is_anonymous BOOLEAN DEFAULT FALSE,
    category VARCHAR(20) NOT NULL,    -- 技术/体能/意识/态度/配合
    content VARCHAR(200) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/ADOPTED/TRYING/SKIP
    likes INT DEFAULT 0,
    is_reported BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_user_id) REFERENCES user(id),
    FOREIGN KEY (from_user_id) REFERENCES user(id)
);

-- 球队建议表
CREATE TABLE IF NOT EXISTS team_suggestion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    urgency VARCHAR(20) DEFAULT 'NORMAL',  -- NORMAL/URGENT
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING/ADOPTED/REJECTED/DONE
    captain_reply TEXT,
    likes INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_user_id) REFERENCES user(id)
);

-- 球员自评表
CREATE TABLE IF NOT EXISTS player_self_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category VARCHAR(20) DEFAULT '技术',
    current_problem TEXT NOT NULL,
    improvement_goal TEXT NOT NULL,
    expected_training TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 个人建议点赞记录表
CREATE TABLE IF NOT EXISTS player_suggestion_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    suggestion_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_suggestion (user_id, suggestion_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (suggestion_id) REFERENCES player_suggestion(id)
);

-- 球队建议点赞记录表
CREATE TABLE IF NOT EXISTS team_suggestion_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    suggestion_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_suggestion (user_id, suggestion_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (suggestion_id) REFERENCES team_suggestion(id)
);

-- 时光轴事件表
CREATE TABLE IF NOT EXISTS timeline_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(20) NOT NULL,   -- AUTO / MANUAL
    title VARCHAR(200) NOT NULL,
    description TEXT,
    image_urls TEXT,        -- 最多3个图片链接，JSON数组
    album_url VARCHAR(500),
    ref_training_id BIGINT,
    ref_match_id BIGINT,
    ref_player_id BIGINT,
    team_id BIGINT,
    event_time DATETIME NOT NULL,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 本次新增：球队表（邀请码机制）
-- ============================================

CREATE TABLE IF NOT EXISTS team (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '球队名称',
    captain_id BIGINT NOT NULL COMMENT '队长用户ID',
    invite_code VARCHAR(6) NOT NULL COMMENT '6位邀请码',
    invite_code_expires_at DATETIME NOT NULL COMMENT '邀请码过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_team_invite_code ON team(invite_code);

-- 为用户表和球员表添加 team_id 关联字段
ALTER TABLE user ADD COLUMN team_id BIGINT NULL COMMENT '所属球队ID';
ALTER TABLE player ADD COLUMN team_id BIGINT NULL COMMENT '所属球队ID';

-- 为时光轴表添加 team_id 字段（按球队隔离事件）
ALTER TABLE timeline_event ADD COLUMN team_id BIGINT NULL COMMENT '所属球队ID';

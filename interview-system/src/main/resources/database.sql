-- 面试题目抽取系统 - PostgreSQL 数据库初始化脚本
-- 支持四种角色：管理员、考官、试题编制者、考生

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EXAMINER', 'QUESTION_CREATOR', 'CANDIDATE')),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- =============================================
-- 题目表
-- =============================================
CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    answer TEXT,
    type VARCHAR(50) NOT NULL CHECK (type IN ('TECHNICAL', 'BEHAVIORAL', 'SCENARIO', 'ALGORITHM', 'SYSTEM_DESIGN', 'OTHER')),
    difficulty VARCHAR(20) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD', 'EXPERT')),
    category VARCHAR(100),
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- =============================================
-- 面试记录表（支持语音文件存储）
-- =============================================
CREATE TABLE IF NOT EXISTS interview_records (
    id SERIAL PRIMARY KEY,
    candidate_username VARCHAR(50) NOT NULL,
    examiner_username VARCHAR(50),
    interview_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    voice_file_path VARCHAR(500),
    voice_file_name VARCHAR(255),
    voice_file_size BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 创建索引优化查询
-- =============================================
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(type);
CREATE INDEX IF NOT EXISTS idx_questions_difficulty ON questions(difficulty);
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category);
CREATE INDEX IF NOT EXISTS idx_interview_candidate ON interview_records(candidate_username);
CREATE INDEX IF NOT EXISTS idx_interview_status ON interview_records(status);

-- =============================================
-- 插入默认管理员账号
-- 密码: admin123 (需要使用 BCrypt 加密后插入)
-- =============================================
-- 注意：以下密码哈希对应 "admin123"
-- 实际运行时通过程序自动生成
-- INSERT INTO users (username, password_hash, real_name, email, role, active)
-- VALUES ('admin', '$2a$12$...', '系统管理员', 'admin@interview.com', 'ADMIN', TRUE);

-- =============================================
-- 插入示例考生账号
-- 密码: candidate123
-- =============================================
-- INSERT INTO users (username, password_hash, real_name, email, role, active)
-- VALUES ('candidate', '$2a$12$...', '示例考生', 'candidate@interview.com', 'CANDIDATE', TRUE);

-- =============================================
-- 示例题目数据
-- =============================================
-- INSERT INTO questions (title, content, answer, type, difficulty, category, created_by)
-- VALUES (
--     'Java 中 ArrayList 和 LinkedList 的区别',
--     '请详细说明 ArrayList 和 LinkedList 在底层实现、性能特点、适用场景等方面的区别。',
--     'ArrayList 基于动态数组实现，随机访问快，插入删除慢；LinkedList 基于双向链表，插入删除快，随机访问慢...',
--     'TECHNICAL',
--     'MEDIUM',
--     'Java基础',
--     'admin'
-- );

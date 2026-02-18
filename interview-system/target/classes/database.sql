-- 面试题目抽取系统 - PostgreSQL 数据库初始化脚本
-- 支持 SSL/HTTPS 连接和角色权限控制

-- =============================================
-- 1. 创建数据库角色（用户）
-- 所有用户都通过 HTTPS/SSL 协议访问数据库
-- =============================================

-- 考生角色：只能查看题目和提交面试记录
-- 所有考生共享此数据库用户，通过应用层区分具体考生
DROP ROLE IF EXISTS candidate;
CREATE ROLE candidate WITH LOGIN PASSWORD 'candidate_secure_pass';
COMMENT ON ROLE candidate IS '考生数据库用户 - 通过 HTTPS 访问，权限受限';

-- 出题人角色：可以创建、修改、删除题目
-- 所有出题人共享此数据库用户，通过应用层区分具体出题人
DROP ROLE IF EXISTS test_setter;
CREATE ROLE test_setter WITH LOGIN PASSWORD 'setter_secure_pass';
COMMENT ON ROLE test_setter IS '出题人数据库用户 - 通过 HTTPS 访问，管理题目';

-- 考官角色：可以查看面试记录、评分
-- 所有考官共享此数据库用户，通过应用层区分具体考官
DROP ROLE IF EXISTS judge;
CREATE ROLE judge WITH LOGIN PASSWORD 'judge_secure_pass';
COMMENT ON ROLE judge IS '考官数据库用户 - 通过 HTTPS 访问，查看记录和评分';

-- 管理员角色：完整权限
DROP ROLE IF EXISTS admin_user;
CREATE ROLE admin_user WITH LOGIN PASSWORD 'admin_secure_pass' SUPERUSER;
COMMENT ON ROLE admin_user IS '管理员数据库用户 - 完整权限';

-- =============================================
-- 2. 用户表
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
-- 3. 题目表
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
-- 4. 面试记录表（支持语音文件存储）
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
-- 5. 评分明细表
-- =============================================
CREATE TABLE IF NOT EXISTS evaluation_scores (
    id SERIAL PRIMARY KEY,
    interview_id INTEGER REFERENCES interview_records(id) ON DELETE CASCADE,
    dimension VARCHAR(50) NOT NULL,
    ai_score INTEGER CHECK (ai_score >= 0 AND ai_score <= 100),
    human_score INTEGER CHECK (human_score >= 0 AND human_score <= 100),
    final_score INTEGER CHECK (final_score >= 0 AND final_score <= 100),
    ai_comment TEXT,
    human_comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 6. 配置角色权限
-- =============================================

-- 考生权限：只读访问题目，可查看和创建自己的面试记录
GRANT USAGE ON SCHEMA public TO candidate;
GRANT SELECT ON questions TO candidate;
GRANT SELECT, INSERT ON interview_records TO candidate;
GRANT SELECT, INSERT ON evaluation_scores TO candidate;
GRANT USAGE, SELECT ON SEQUENCE interview_records_id_seq TO candidate;
GRANT USAGE, SELECT ON SEQUENCE evaluation_scores_id_seq TO candidate;

-- 出题人权限：完全控制题目表
GRANT USAGE ON SCHEMA public TO test_setter;
GRANT SELECT, INSERT, UPDATE, DELETE ON questions TO test_setter;
GRANT SELECT ON interview_records TO test_setter;
GRANT SELECT ON evaluation_scores TO test_setter;
GRANT USAGE, SELECT ON SEQUENCE questions_id_seq TO test_setter;

-- 考官权限：查看题目、完全控制面试记录和评分
GRANT USAGE ON SCHEMA public TO judge;
GRANT SELECT ON questions TO judge;
GRANT SELECT, INSERT, UPDATE ON interview_records TO judge;
GRANT SELECT, INSERT, UPDATE ON evaluation_scores TO judge;
GRANT USAGE, SELECT ON SEQUENCE interview_records_id_seq TO judge;
GRANT USAGE, SELECT ON SEQUENCE evaluation_scores_id_seq TO judge;

-- 管理员权限：完整权限
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin_user;

-- =============================================
-- 7. 创建索引优化查询
-- =============================================
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_questions_type ON questions(type);
CREATE INDEX IF NOT EXISTS idx_questions_difficulty ON questions(difficulty);
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category);
CREATE INDEX IF NOT EXISTS idx_interview_candidate ON interview_records(candidate_username);
CREATE INDEX IF NOT EXISTS idx_interview_status ON interview_records(status);
CREATE INDEX IF NOT EXISTS idx_eval_interview ON evaluation_scores(interview_id);

-- =============================================
-- 8. 插入默认账号
-- =============================================

-- 注意：密码需要使用 BCrypt 加密
-- 以下 INSERT 语句需要在应用中执行，使用 BCrypt.hashpw(password, BCrypt.gensalt(12))

-- 默认管理员账号（应用层用户，不是数据库用户）
-- INSERT INTO users (username, password_hash, real_name, email, role, active)
-- VALUES ('admin', '$2a$12$...', '系统管理员', 'admin@interview.com', 'ADMIN', TRUE);

-- 示例考生账号（应用层）
-- INSERT INTO users (username, password_hash, real_name, email, role, active)
-- VALUES ('candidate1', '$2a$12$...', '示例考生', 'candidate@interview.com', 'CANDIDATE', TRUE);

-- 示例出题人账号（应用层）
-- INSERT INTO users (username, password_hash, real_name, email, role, active)
-- VALUES ('setter1', '$2a$12$...', '示例出题人', 'setter@interview.com', 'QUESTION_CREATOR', TRUE);

-- 示例考官账号（应用层）
-- INSERT INTO users (username, password_hash, real_name, email, role, active)
-- VALUES ('judge1', '$2a$12$...', '示例考官', 'judge@interview.com', 'EXAMINER', TRUE);

-- =============================================
-- 9. SSL 配置说明
-- =============================================
-- 在 postgresql.conf 中配置：
-- ssl = on
-- ssl_cert_file = 'server.crt'
-- ssl_key_file = 'server.key'
-- ssl_ca_file = 'root.crt'

-- 在 pg_hba.conf 中配置强制 SSL：
-- hostssl all all 0.0.0.0/0 md5

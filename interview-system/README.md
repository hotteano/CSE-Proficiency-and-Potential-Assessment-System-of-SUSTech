# 面试评估系统（JavaFX + PostgreSQL + AI 智能分析）

一个基于 JavaFX 和 PostgreSQL 的全功能面试评估系统，采用现代化 UI 设计，支持四种用户角色，集成 AI 智能分析、语音录制、多维度评估等先进功能。

## 功能特性

### 用户角色与权限

系统支持四种用户角色：

| 角色 | 权限 |
|------|------|
| **管理员** | 用户管理、题目管理、题目抽取、面试记录管理、系统配置、LLM 配置 |
| **考官** | 浏览题目、抽取题目、面试控制、查看所有面试记录、添加面试评价、生成评估报告 |
| **试题编制者** | 创建/编辑/删除题目、浏览题目 |
| **考生** | 创建面试记录、语音录制/上传、查看自己的面试记录、查看 AI 评估报告 |

### 核心技术栈

- **Java 17**
- **JavaFX 19** - 现代化 GUI 框架
- **PostgreSQL** - 关系型数据库
- **Maven** - 项目构建
- **jBCrypt** - 密码加密
- **大语言模型 (LLM)** - AI 智能分析面试表现

## 项目结构

```
interview-system/
├── src/main/java/com/interview/
│   ├── Main.java                      # 程序入口
│   ├── JavaFXApp.java                 # JavaFX 应用主类
│   ├── config/                        # 配置层
│   │   ├── AppConfig.java             # 应用配置
│   │   ├── DatabaseConfig.java        # 数据库配置
│   │   └── SSLConfig.java             # SSL 配置
│   ├── dao/                           # 数据访问对象
│   │   ├── UserDao.java               # 用户数据访问
│   │   ├── QuestionDao.java           # 题目数据访问
│   │   ├── InterviewRecordDao.java    # 面试记录数据访问
│   │   ├── EvaluationScoreDao.java    # 评分数据访问
│   │   ├── EvaluationSummaryDao.java  # 评估汇总数据访问
│   │   └── LLMConfigDao.java          # LLM 配置数据访问
│   ├── model/                         # 数据模型
│   │   ├── User.java                  # 用户模型
│   │   ├── Role.java                  # 角色枚举
│   │   ├── Permission.java            # 权限枚举
│   │   ├── Question.java              # 题目模型（含难度等级、专业方向）
│   │   ├── InterviewRecord.java       # 面试记录模型
│   │   ├── EvaluationScore.java       # 评分模型
│   │   ├── EvaluationSummary.java     # 评估汇总模型
│   │   ├── EvaluationReport.java      # 评估报告模型
│   │   ├── EvaluationDimension.java   # 评估维度模型
│   │   ├── LLMConfig.java             # LLM 配置模型
│   │   ├── EvaluatorRole.java         # 评估者角色枚举
│   │   ├── InterviewAnalysisResult.java  # AI 分析结果模型
│   │   └── QuestionExtractRecord.java    # 题目抽取记录
│   ├── service/                       # 业务逻辑层
│   │   ├── AuthService.java           # 认证服务
│   │   ├── UserService.java           # 用户服务
│   │   ├── QuestionService.java       # 题目服务
│   │   ├── InterviewRecordService.java   # 面试记录服务
│   │   ├── InterviewControlService.java  # 面试控制服务
│   │   ├── EvaluationService.java        # 评估服务
│   │   ├── MultiEvaluatorService.java    # 多评估者服务
│   │   ├── AIAnalysisService.java        # AI 分析服务
│   │   ├── MockAIAnalysisService.java    # AI 分析模拟服务
│   │   ├── SpeechRecognitionService.java # 语音识别服务
│   │   ├── LLMConfigService.java         # LLM 配置服务
│   │   └── LLMManager.java               # LLM 管理器
│   ├── llm/                           # LLM 模块
│   │   └── LLMManager.java            # 大语言模型管理器
│   ├── util/                          # 工具类
│   │   ├── DatabaseConnection.java    # 数据库连接工具
│   │   ├── DatabaseInitializer.java   # 数据库初始化
│   │   └── AudioRecorder.java         # 音频录制工具
│   └── view/                          # JavaFX 视图层
│       ├── LoginView.java             # 登录界面
│       ├── RegisterDialog.java        # 注册对话框
│       ├── MainView.java              # 主界面
│       ├── DatabaseConfigView.java    # 数据库配置界面
│       ├── UserManageView.java        # 用户管理
│       ├── UserEditDialog.java        # 用户编辑对话框
│       ├── QuestionBrowseView.java    # 题目浏览
│       ├── QuestionManageView.java    # 题目管理
│       ├── QuestionEditDialog.java    # 题目编辑对话框
│       ├── QuestionExtractView.java   # 题目抽取
│       ├── CandidateInterviewView.java   # 考生面试中心
│       ├── InterviewControlView.java     # 面试控制界面 ⭐
│       ├── InterviewRecordManageView.java # 面试记录管理
│       ├── EvaluationView.java           # 评估界面 ⭐
│       ├── ReportView.java               # 评估报告界面 ⭐
│       ├── LLMConfigView.java            # LLM 配置界面 ⭐
│       └── ChangePasswordDialog.java     # 修改密码
├── src/main/resources/
│   ├── styles.css                 # JavaFX 样式表
│   └── database.sql               # 数据库初始化脚本
├── develop_log/                   # 开发日志目录
│   ├── 0.0.1_fix_evaluation_view.md
│   ├── 0.0.1_fix_log.md
│   ├── 0.0.1_log.md
│   ├── 0.0.3_fix_log.md
│   ├── 0.0.3_llm_api_update.md
│   ├── 0.0.4_question_level_update.md
│   ├── 0.0.5_llm_json_parser_fix.md
│   ├── 0.0.6_api_url_fix.md
│   ├── 0.0.7_fix_score_submission.md
│   ├── NEW_FEATURES.md
│   └── todo_prompt.md
├── config.properties              # 应用配置文件
├── llm_configs.dat                # LLM 配置文件
├── pom.xml                        # Maven 配置
└── voice_records/                 # 语音文件存储目录
```

## 核心功能模块

### 1. 题目管理
- 支持创建、编辑、删除题目
- 题目分类：CS基础、编程、算法、系统等
- 难度等级：1-5 级评分
- 专业方向：计算机科学与技术、智能科学与技术、数据科学与大数据技术

### 2. 题目抽取
- 按题目数量、类型、难度抽取
- 随机抽取算法
- 保存抽取记录

### 3. 面试控制 ⭐
- 实时面试流程控制
- 语音录制功能（支持 WAV 格式）
- 面试状态管理（待开始、进行中、已完成、已取消）

### 4. AI 智能分析 ⭐
- 集成大语言模型（LLM）
- 自动分析面试表现
- 多维度评估：知识掌握、编程能力、逻辑思维、沟通表达、学习能力
- 生成结构化评估报告

### 5. 多评估者支持 ⭐
- 支持多考官独立评分
- 评估者角色区分（专家评估员、同行评估员、监督评估员）
- 评分汇总与对比分析

### 6. LLM 配置管理 ⭐
- 支持多种 LLM 提供商（OpenAI、Moonshot AI 等）
- 可配置的 API 端点和模型参数
- 模拟模式用于测试

## 快速开始

### 1. 安装 PostgreSQL

```bash
# 创建数据库
createdb interview_system

# 或使用 psql
psql -c "CREATE DATABASE interview_system;"
```

### 2. 配置数据库连接

首次运行时，系统会弹出数据库配置对话框，或编辑 `config.properties`：

```properties
db.host=localhost
db.port=5432
db.name=interview_system
db.user=postgres
db.password=your_password
```

### 3. 编译运行

```bash
# 编译项目
mvn clean package

# 运行（使用 JavaFX Maven 插件）
mvn javafx:run

# 或运行 jar（需要 JavaFX 模块）
java --module-path "${PATH_TO_FX}" --add-modules javafx.controls,javafx.fxml \
  -jar target/interview-system-1.0-SNAPSHOT.jar
```

### 4. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| candidate | candidate123 | 考生（示例）|

## 命令行参数

```bash
java -jar interview-system.jar \
  --db-host localhost \
  --db-port 5432 \
  --db-name interview_system \
  --db-user postgres \
  --db-password your_password
```

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--db-host` | PostgreSQL 主机地址 | localhost |
| `--db-port` | PostgreSQL 端口 | 5432 |
| `--db-name` | PostgreSQL 数据库名 | interview_system |
| `--db-user` | PostgreSQL 用户名 | postgres |
| `--db-password` | PostgreSQL 密码 | postgres |

## 数据库表结构

### users（用户表）
```sql
id, username, password_hash, real_name, email, 
role, active, created_at, last_login
```

### questions（题目表）
```sql
id, title, content, answer, type, difficulty_level, 
specialization, category, tags, created_by, active, created_at, updated_at
```

### interview_records（面试记录表）
```sql
id, candidate_username, examiner_username, interview_time, status,
voice_file_path, voice_file_name, voice_file_size, duration_seconds,
transcript, notes, created_at, updated_at
```

### evaluation_scores（评分表）
```sql
id, interview_id, evaluator_username, evaluator_role, score_type,
knowledge_score, coding_score, logic_score, communication_score, 
learning_score, total_score, weighted_score, comments, created_at
```

### evaluation_summaries（评估汇总表）
```sql
id, interview_id, final_score, grade, summary, strengths, 
weaknesses, recommendations, ai_analysis_enabled, created_at
```

### llm_configs（LLM 配置表）
```sql
id, provider, name, api_url, api_key, model, temperature, 
max_tokens, enabled, created_at, updated_at
```

## 语音文件存储

语音文件默认存储在项目目录下的 `voice_records/` 文件夹中：

```
voice_records/
└── {candidate_username}_{recordId}_{timestamp}.wav
```

示例：`candidate_1_1708001234567.wav`

## AI 评估维度

系统从以下五个维度对考生进行评估：

| 维度 | 权重 | 说明 |
|------|------|------|
| 知识掌握 | 25% | 计算机基础知识的掌握程度 |
| 编程能力 | 25% | 代码编写和调试能力 |
| 逻辑思维 | 20% | 问题分析和解决思路 |
| 沟通表达 | 15% | 表达清晰度和沟通能力 |
| 学习能力 | 15% | 学习意愿和潜力评估 |

## 开发日志

详见 `develop_log/` 目录：
- `0.0.1_log.md` - 初始版本开发日志
- `0.0.3_llm_api_update.md` - LLM API 集成更新
- `0.0.4_question_level_update.md` - 题目等级功能更新
- `0.0.5_llm_json_parser_fix.md` - LLM JSON 解析修复
- `0.0.6_api_url_fix.md` - API URL 修复
- `0.0.7_fix_score_submission.md` - 评分提交修复
- `NEW_FEATURES.md` - 新功能规划

## 界面特点

JavaFX 版本的优势：

1. **现代化外观** - 使用 CSS 样式，支持主题定制
2. **流畅动画** - 内置过渡动画效果
3. **响应式布局** - 自适应窗口大小变化
4. **丰富的控件** - 日期选择器、进度条、对话框、标签页等
5. **音频可视化** - 实时录音波形显示
6. **Markdown 支持** - AI 评估报告支持格式化显示

## 许可证

MIT License

# 面试题目抽取与评测系统（JavaFX + PostgreSQL + AI）

一个基于 JavaFX 和 PostgreSQL 的面试题目管理与评测系统，采用现代化 UI 设计，支持四种用户角色，集成 AI 大模型评测、语音识别、面试录音等高级功能。

## 功能特性

### 用户角色与权限

系统支持四种用户角色：

| 角色 | 权限 |
|------|------|
| **管理员** | 用户管理、题目管理、题目抽取、面试记录管理、系统配置、LLM API配置 |
| **考官** | 浏览题目、抽取题目、面试控制、查看所有面试记录、添加面试评价、生成评测报告 |
| **试题编制者** | 创建/编辑/删除题目、浏览题目 |
| **考生** | 创建面试记录、上传语音文件、查看自己的面试记录 |

### 核心功能

1. **题目管理** - 创建、编辑、删除面试题目，支持多种题型和难度级别
2. **题目抽取** - 根据条件随机抽取题目，支持导出
3. **面试控制中心** - 考官控制面试流程，自动录音，计时显示
4. **语音识别** - 自动将面试录音转写为文本并进行精修
5. **AI 智能评测** - 集成 DeepSeek、GPT-4 等大模型，自动分析面试表现
6. **多维度评测体系** - 5大类别17个维度的综合能力评测
7. **评测报告生成** - 综合人工评分和 AI 评分，生成详细评测报告
8. **LLM 配置管理** - 支持多模型配置，可切换不同 AI 提供商

### 技术栈

- **Java 17** - 编程语言
- **JavaFX 19** - 现代化 GUI 框架
- **PostgreSQL 14+** - 关系型数据库
- **Maven 3.8+** - 项目构建
- **jBCrypt** - 密码加密
- **org.json** - JSON 处理

---

## 环境要求

### 必需环境

| 组件 | 版本要求 | 下载链接 |
|------|----------|----------|
| Java JDK | 17+ | https://adoptium.net/ |
| PostgreSQL | 14+ | https://www.postgresql.org/download/ |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |

### 验证环境

```bash
# 检查 Java 版本
java -version
# 应显示：openjdk version "17" 或更高

# 检查 Maven 版本
mvn -version
# 应显示：Apache Maven 3.8.x 或更高

# 检查 PostgreSQL
psql --version
# 应显示：psql (PostgreSQL) 14.x 或更高
```

---

## 快速开始

### 1. 安装 PostgreSQL

#### Windows

1. 下载并安装 PostgreSQL：https://www.postgresql.org/download/windows/
2. 安装时记住设置的密码（默认用户 postgres）
3. 打开 pgAdmin 或命令行创建数据库

```bash
# 使用 psql 创建数据库（在 PostgreSQL 安装目录的 bin 文件夹中）
psql -U postgres -c "CREATE DATABASE interview_system;"
```

#### Linux (Ubuntu/Debian)

```bash
# 安装 PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# 启动服务
sudo systemctl start postgresql
sudo systemctl enable postgresql

# 创建数据库
sudo -u postgres psql -c "CREATE DATABASE interview_system;"
```

#### macOS

```bash
# 使用 Homebrew 安装
brew install postgresql

# 启动服务
brew services start postgresql

# 创建数据库
createdb interview_system
```

### 2. 克隆项目并配置

```bash
# 克隆项目
git clone <repository-url>
cd interview-system/interview-system

# 复制配置文件模板（如果需要）
cp config.properties.example config.properties
```

编辑 `config.properties` 文件：

```properties
# 数据库配置
db.host=localhost
db.port=5432
db.name=interview_system
db.user=postgres
db.password=your_password_here
```

### 3. 编译项目

```bash
# 方式一：使用 Maven 默认配置
mvn clean package

# 方式二：使用阿里云镜像（推荐国内用户使用）
mvn clean package -s settings.xml

# 方式三：跳过测试快速编译
mvn clean package -DskipTests

# 方式四：清理并重新编译
mvn clean compile
```

编译成功后，会在 `target/` 目录生成：
- `interview-system-1.0-SNAPSHOT.jar` - 可执行 JAR 文件

### 4. 运行程序

#### 方式一：直接运行 JAR

```bash
# 基本运行
java -jar target/interview-system-1.0-SNAPSHOT.jar

# 带数据库密码参数
java -jar target/interview-system-1.0-SNAPSHOT.jar --db-password your_password

# 完整参数运行
java -jar target/interview-system-1.0-SNAPSHOT.jar \
  --db-host localhost \
  --db-port 5432 \
  --db-name interview_system \
  --db-user postgres \
  --db-password your_password
```

#### 方式二：使用 Maven JavaFX 插件（开发调试）

```bash
# 运行（需要先配置好数据库）
mvn javafx:run

# 或者指定配置文件
mvn javafx:run -Dconfig.file=config.properties
```

#### 方式三：Windows 批处理脚本

创建 `run.bat`：

```batch
@echo off
set DB_PASSWORD=your_password
java -jar target/interview-system-1.0-SNAPSHOT.jar --db-password %DB_PASSWORD%
pause
```

#### 方式四：Linux/macOS Shell 脚本

创建 `run.sh`：

```bash
#!/bin/bash
export DB_PASSWORD="your_password"
java -jar target/interview-system-1.0-SNAPSHOT.jar --db-password "$DB_PASSWORD"
```

赋予执行权限并运行：

```bash
chmod +x run.sh
./run.sh
```

### 5. 首次登录

使用默认管理员账号登录：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| candidate | candidate123 | 考生（示例）|

> **注意**：首次运行时会自动初始化数据库表结构。

---

## 详细配置说明

### 命令行参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--db-host` | PostgreSQL 主机地址 | localhost |
| `--db-port` | PostgreSQL 端口 | 5432 |
| `--db-name` | PostgreSQL 数据库名 | interview_system |
| `--db-user` | PostgreSQL 用户名 | postgres |
| `--db-password` | PostgreSQL 密码 | postgres |

### 配置文件 (config.properties)

```properties
# =============================================
# 数据库配置
# =============================================
db.host=localhost
db.port=5432
db.name=interview_system
db.user=postgres
db.password=your_password

# =============================================
# SSL/HTTPS 配置（可选）
# =============================================
# 启用 SSL 连接数据库
ssl.enabled=false
# SSL 模式: disable, allow, prefer, require, verify-ca, verify-full
ssl.mode=prefer
# 信任库路径（用于 verify-ca 或 verify-full 模式）
ssl.truststore.path=
ssl.truststore.password=

# =============================================
# 应用配置
# =============================================
# 语音文件存储路径（相对或绝对路径）
voice.records.path=voice_records
# 是否启用 AI 自动分析
ai.analysis.enabled=true
# 默认 LLM 配置 ID
default.llm.config.id=
```

### SSL/HTTPS 配置（可选）

如需使用 SSL 连接数据库：

1. 获取数据库服务器的证书
2. 创建信任库：

```bash
# 导入证书到信任库
keytool -import -alias postgres -file server.crt -keystore truststore.jks
```

3. 配置 `config.properties`：

```properties
ssl.enabled=true
ssl.mode=verify-full
ssl.truststore.path=/path/to/truststore.jks
ssl.truststore.password=your_keystore_password
```

### LLM API 配置

系统支持以下大模型提供商：

| 提供商 | 默认端点 | 默认模型 |
|--------|----------|----------|
| DeepSeek | https://api.deepseek.com/v1 | deepseek-chat |
| DeepSeek-Thinking | https://api.deepseek.com/v1 | deepseek-reasoner |
| OpenAI | https://api.openai.com/v1 | gpt-4 |
| OpenAI GPT-4 Turbo | https://api.openai.com/v1 | gpt-4-turbo-preview |
| Azure OpenAI | 自定义 | gpt-4 |
| Anthropic Claude | https://api.anthropic.com | claude-3-opus-20240229 |
| 本地模型 | http://localhost:8000/v1 | local-model |

配置步骤：

1. 使用管理员账号登录
2. 进入「API配置」标签页
3. 点击「添加配置」
4. 填写配置信息：
   - 配置名称：如 "DeepSeek-Production"
   - 提供商：选择 DeepSeek-Thinking（推荐）
   - 模型名称：deepseek-reasoner
   - API端点：https://api.deepseek.com/v1/chat/completions
   - API Key：您的 API 密钥
   - 超时时间：60秒
5. 保存并设为默认配置

---

## 使用流程

### 管理员配置流程

1. **配置 LLM API**
   - 登录管理员账号
   - 进入「API配置」标签页
   - 添加大模型配置（支持 DeepSeek、OpenAI、Azure OpenAI、Claude 等）
   - 设置默认配置

2. **用户管理**
   - 创建考官、试题编制者、考生账号
   - 设置用户权限和状态

3. **题目管理**
   - 添加面试题目，设置类型、难度、分类
   - 管理题目状态（启用/禁用）

### 考官面试流程

1. **题目抽取**
   - 进入「题目抽取」标签页
   - 设置抽取条件（数量、类型、难度）
   - 随机抽取题目

2. **面试控制**
   - 进入「面试控制」标签页
   - 选择考生和题目
   - 点击「开始面试」，系统自动录音
   - 面试结束后点击「结束面试」

3. **自动处理**
   - 系统自动进行语音识别
   - 自动调用 AI 分析面试表现
   - 生成 JSON 格式的分析结果

4. **人工评分**
   - 进入「面试评分」标签页
   - 选择面试记录
   - 为17个维度打分（0-100分）
   - 填写评语和发展建议

5. **查看报告**
   - 进入「评测报告」标签页
   - 查看综合评分、维度分析、发展建议

---

## 评测维度体系

系统采用 5 大类别 17 维度的综合能力评测体系：

### 技能（Skill）
- 编程能力（Programming Skills）- 权重 25%
- 经典框架和环境配置 - 权重 15%
- 工程优化能力与代码品味 - 权重 15%
- 代码纠错能力 - 权重 10%
- 系统设计能力 - 权重 20%

### 学术与研究潜力
- 创意表述能力 - 权重 15%
- 研究直觉与品味 - 权重 20%
- 创新点的验证能力 - 权重 20%
- 理论系统构建能力 - 权重 20%

### 沟通能力
- 严谨性 - 权重 20%
- 逻辑性 - 权重 20%
- 说服力 - 权重 15%

### 数学能力
- 基本算术能力 - 权重 10%
- 数学建模能力 - 权重 25%
- 数学证明能力 - 权重 20%

### 设计与商业远见
- 产品设计能力 - 权重 20%
- 商业与市场洞察力 - 权重 20%
- 开源产品设计与维护能力 - 权重 15%

---

## 数据库表结构

### 核心表

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| questions | 题目表 |
| interview_records | 面试记录表 |
| evaluation_scores | 评测分数表 |
| evaluation_dimension_scores | 维度分数表 |
| llm_configs | 大模型配置表 |

### interview_records（面试记录表）

```sql
id, candidate_username, examiner_username, interview_time, status,
voice_file_path, voice_file_name, voice_file_size,
transcribed_text, refined_text, ai_analysis_result,  -- AI相关字段
is_recording, recording_start_time, recording_duration,  -- 录音相关字段
notes, created_at, updated_at
```

---

## 项目结构

```
interview-system/
├── src/main/java/com/interview/
│   ├── Main.java                      # 程序入口
│   ├── JavaFXApp.java                 # JavaFX 应用主类
│   ├── config/                        # 配置类
│   │   ├── AppConfig.java             # 应用配置
│   │   ├── DatabaseConfig.java        # 数据库配置
│   │   └── SSLConfig.java             # SSL/HTTPS配置
│   ├── dao/                           # 数据访问对象
│   │   ├── EvaluationScoreDao.java    # 评测分数 DAO
│   │   ├── InterviewRecordDao.java    # 面试记录 DAO
│   │   ├── LLMConfigDao.java          # LLM配置 DAO
│   │   ├── QuestionDao.java           # 题目 DAO
│   │   └── UserDao.java               # 用户 DAO
│   ├── model/                         # 数据模型
│   │   ├── EvaluationDimension.java   # 评测维度枚举（17维度）
│   │   ├── EvaluationReport.java      # 综合评测报告
│   │   ├── EvaluationScore.java       # 评测分数
│   │   ├── InterviewAnalysisResult.java # 面试分析结果
│   │   ├── InterviewRecord.java       # 面试记录
│   │   ├── LLMConfig.java             # 大模型配置
│   │   ├── Permission.java            # 权限枚举
│   │   ├── Question.java              # 题目实体
│   │   ├── QuestionExtractRecord.java # 题目抽取记录
│   │   ├── Role.java                  # 角色枚举
│   │   └── User.java                  # 用户实体
│   ├── service/                       # 业务逻辑层
│   │   ├── AIAnalysisService.java     # AI 分析服务
│   │   ├── AuthService.java           # 认证服务
│   │   ├── EvaluationService.java     # 评测服务
│   │   ├── InterviewControlService.java # 面试控制服务
│   │   ├── InterviewRecordService.java # 面试记录服务
│   │   ├── LLMConfigService.java      # LLM配置服务
│   │   ├── LLMManager.java            # LLM管理器
│   │   ├── MockAIAnalysisService.java # AI分析模拟服务
│   │   ├── QuestionService.java       # 题目服务
│   │   ├── SpeechRecognitionService.java # 语音识别服务
│   │   └── UserService.java           # 用户服务
│   ├── util/                          # 工具类
│   │   ├── AudioRecorder.java         # 音频录制工具
│   │   ├── DatabaseConnection.java    # 数据库连接
│   │   └── DatabaseInitializer.java   # 数据库初始化
│   └── view/                          # JavaFX 视图层
│       ├── LoginView.java             # 登录界面
│       ├── MainView.java              # 主界面
│       ├── RegisterDialog.java        # 注册对话框
│       ├── ChangePasswordDialog.java  # 修改密码对话框
│       ├── QuestionBrowseView.java    # 题目浏览
│       ├── QuestionManageView.java    # 题目管理
│       ├── QuestionExtractView.java   # 题目抽取
│       ├── QuestionEditDialog.java    # 题目编辑
│       ├── UserManageView.java        # 用户管理
│       ├── UserEditDialog.java        # 用户编辑
│       ├── CandidateInterviewView.java # 考生面试中心
│       ├── InterviewRecordManageView.java # 面试记录管理
│       ├── InterviewControlView.java  # 面试控制中心 ⭐
│       ├── EvaluationView.java        # 面试评分界面 ⭐
│       ├── ReportView.java            # 评测报告界面 ⭐
│       └── LLMConfigView.java         # LLM配置界面 ⭐
├── src/main/resources/
│   ├── styles.css                     # JavaFX 样式表
│   └── database.sql                   # 数据库初始化脚本
├── develop_log/                       # 开发日志
│   ├── NEW_FEATURES.md               # 新功能说明
│   └── *.md                          # 版本更新日志
├── config.properties                  # 配置文件
├── settings.xml                       # Maven 阿里云镜像配置
├── pom.xml                            # Maven 配置
└── README.md                          # 项目说明
```

---

## 语音文件存储

语音文件默认存储在项目目录下的 `voice_records/` 文件夹中：

```
voice_records/
└── interview_{candidateUsername}_{recordId}_{timestamp}.wav
```

示例：`interview_candidate_1_1708001234567.wav`

---

## 常见问题排查

### 1. 数据库连接失败

**问题**：`Connection refused` 或 `FATAL: database "interview_system" does not exist`

**解决**：
```bash
# 检查 PostgreSQL 服务是否运行
# Windows: 服务管理器中找到 postgresql 服务
# Linux: sudo systemctl status postgresql
# macOS: brew services list

# 创建数据库
psql -U postgres -c "CREATE DATABASE interview_system;"

# 检查 config.properties 中的密码是否正确
```

### 2. JavaFX 运行时错误

**问题**：`Error: JavaFX runtime components are missing`

**解决**：
```bash
# 确保使用 Java 17+ 且包含 JavaFX 模块
# 或者使用 Maven JavaFX 插件运行
mvn javafx:run
```

### 3. Maven 下载依赖慢

**问题**：编译时下载依赖很慢

**解决**：
```bash
# 使用阿里云镜像
mvn clean package -s settings.xml

# 或者在 ~/.m2/settings.xml 中配置镜像
```

### 4. AI 分析失败

**问题**：AI 分析返回错误或超时

**解决**：
- 检查 LLM API 配置是否正确
- 检查 API Key 是否有效
- 检查网络连接
- 查看日志中的详细错误信息

### 5. 录音功能无法使用

**问题**：无法开始录音或录音文件为空

**解决**：
- 确保系统有麦克风设备
- 检查麦克风权限是否开启
- 检查 `voice_records/` 目录是否有写入权限

---

## 开发说明

### 构建可执行 JAR

```bash
# 打包包含所有依赖的 JAR
mvn clean package

# 生成的文件在 target/interview-system-1.0-SNAPSHOT.jar
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=ClassName

# 跳过测试
mvn clean package -DskipTests
```

### 代码格式化

项目使用标准 Java 代码风格，建议配置 IDE：
- **IntelliJ IDEA**: 使用默认 Java 代码风格
- **Eclipse**: 使用默认 Java 代码风格

---

## 界面说明

### 登录界面
- 简洁的登录表单
- 用户名/密码验证
- 用户注册入口

### 主界面
- 基于角色的动态菜单
- 标签页导航
- 状态栏显示当前用户信息

### 面试控制中心
- 开始/结束面试控制
- 实时录音状态显示
- 音量监测和计时器
- 题目展示区域

### 面试评分
- 17维度评分滑块
- AI评分自动加载
- 评语和推荐理由输入
- 发展建议填写

### 评测报告
- 综合评分和评级（S/A/B/C/D）
- 5大类别得分柱状图
- 优势/待提升维度分析
- 发展建议和适合岗位

---

## 许可证

MIT License

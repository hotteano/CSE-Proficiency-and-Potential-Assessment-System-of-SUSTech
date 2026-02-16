# 面试题目抽取系统（JavaFX + PostgreSQL 版本）

一个基于 JavaFX 和 PostgreSQL 的面试题目管理系统，采用现代化 UI 设计，支持四种用户角色，包含面试语音录制功能。

## 功能特性

### 用户角色与权限

系统支持四种用户角色：

| 角色 | 权限 |
|------|------|
| **管理员** | 用户管理、题目管理、题目抽取、面试记录管理、系统配置 |
| **考官** | 浏览题目、抽取题目、查看所有面试记录、添加面试评价 |
| **试题编制者** | 创建/编辑/删除题目、浏览题目 |
| **考生** | 创建面试记录、上传语音文件、查看自己的面试记录 |

### 技术栈

- **Java 17**
- **JavaFX 19** - 现代化 GUI 框架
- **PostgreSQL** - 关系型数据库
- **Maven** - 项目构建
- **jBCrypt** - 密码加密

## 项目结构

```
interview-system/
├── src/main/java/com/interview/
│   ├── Main.java                 # 程序入口
│   ├── JavaFXApp.java            # JavaFX 应用主类
│   ├── config/                   # 数据库配置
│   ├── dao/                      # 数据访问对象（PostgreSQL）
│   ├── model/                    # 数据模型
│   ├── service/                  # 业务逻辑
│   ├── util/                     # 工具类
│   └── view/                     # JavaFX 视图层 ⭐
│       ├── LoginView.java        # 登录界面
│       ├── MainView.java         # 主界面
│       ├── RegisterDialog.java   # 注册对话框
│       ├── QuestionBrowseView.java      # 题目浏览
│       ├── QuestionManageView.java      # 题目管理
│       ├── QuestionExtractView.java     # 题目抽取
│       ├── QuestionEditDialog.java      # 题目编辑
│       ├── UserManageView.java          # 用户管理
│       ├── UserEditDialog.java          # 用户编辑
│       ├── CandidateInterviewView.java  # 考生面试中心
│       ├── InterviewRecordManageView.java # 面试记录管理
│       └── ChangePasswordDialog.java    # 修改密码
├── src/main/resources/
│   ├── styles.css                # JavaFX 样式表
│   └── database.sql              # 数据库初始化脚本
└── pom.xml                       # Maven 配置
```

## 快速开始

### 1. 安装 PostgreSQL

```bash
# 创建数据库
createdb interview_system

# 或使用 psql
psql -c "CREATE DATABASE interview_system;"
```

### 2. 编译运行

```bash
# 编译项目
mvn clean package

# 运行（使用 JavaFX Maven 插件）
mvn javafx:run

# 或运行 jar（需要 JavaFX 模块）
java --module-path "${PATH_TO_FX}" --add-modules javafx.controls,javafx.fxml \
  -jar target/interview-system-1.0-SNAPSHOT.jar
```

### 3. 默认账号

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

## UI 截图说明

### 登录界面
- 简洁的登录表单
- 用户名/密码验证
- 用户注册入口

### 主界面
- 基于角色的动态菜单
- 标签页导航
- 状态栏显示当前用户信息

### 题目管理
- 表格展示题目列表
- 搜索筛选功能
- 新增/编辑/删除题目

### 题目抽取
- 设置抽取条件（数量、类型、难度）
- 随机抽取算法
- 结果导出功能

### 考生面试中心
- 开始新面试
- 上传语音文件（MP3, WAV, OGG, M4A）
- 查看面试历史

### 面试记录管理
- 查看所有面试记录（考官/管理员）
- 播放语音文件
- 添加面试评价
- 更新面试状态

## 数据库表结构

### users（用户表）
```sql
id, username, password_hash, real_name, email, 
role, active, created_at, last_login
```

### questions（题目表）
```sql
id, title, content, answer, type, difficulty, 
category, created_by, active, created_at, updated_at
```

### interview_records（面试记录表）
```sql
id, candidate_username, examiner_username, interview_time, status,
voice_file_path, voice_file_name, voice_file_size, notes, created_at, updated_at
```

## 语音文件存储

语音文件默认存储在项目目录下的 `voice_records/` 文件夹中：

```
voice_records/
└── {candidate_username}_{recordId}_{timestamp}.{ext}
```

示例：`candidate_1_1708001234567.mp3`

## 界面特点

JavaFX 版本相比 Swing 版本的优势：

1. **现代化外观** - 使用 CSS 样式，支持主题定制
2. **流畅动画** - 内置过渡动画效果
3. **响应式布局** - 自适应窗口大小变化
4. **更好的控件** - 日期选择器、进度条、对话框等
5. **FXML 支持** - 可使用 XML 定义界面（当前使用代码构建）

## 开发说明

### 保持后端不变

以下包内容完全保持不变：
- `com.interview.model.*` - 所有数据模型
- `com.interview.dao.*` - 所有数据访问对象
- `com.interview.service.*` - 所有业务逻辑
- `com.interview.config.DatabaseConfig`
- `com.interview.util.*`

### 仅替换 GUI 层

将 `com.interview.gui` (Swing) 替换为 `com.interview.view` (JavaFX)

## 许可证

MIT License

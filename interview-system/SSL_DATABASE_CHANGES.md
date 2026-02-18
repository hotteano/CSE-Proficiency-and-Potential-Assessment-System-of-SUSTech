# HTTPS/SSL 数据库连接修改说明

## 修改概述

本次修改将系统改造为支持通过 HTTPS/SSL 协议安全访问 PostgreSQL 数据库，并为不同角色配置独立的数据库用户。

## 核心修改文件

### 1. 配置文件 `config.properties`
- 添加 SSL/HTTPS 配置选项
- 添加角色数据库用户配置（candidate, setter, judge, admin）
- 支持向后兼容

### 2. 数据库配置 `DatabaseConfig.java`
- 新增 `DbUserRole` 枚举：CANDIDATE, TEST_SETTER, JUDGE, ADMIN
- 按角色存储独立的数据库凭据
- 支持 SSL URL 参数生成
- 提供角色到数据库用户的映射方法

### 3. 数据库连接 `DatabaseConnection.java`
- 使用 ThreadLocal 管理按角色的数据库连接
- 新增 `setCurrentDbRole()` 方法切换数据库角色
- 新增 `setRoleByAppRole()` 根据应用角色自动映射
- 新增角色专用连接方法：`getCandidateConnection()`, `getSetterConnection()`, `getJudgeConnection()`

### 4. 认证服务 `AuthService.java`
- 登录时根据用户角色自动设置数据库连接角色
- 新增 `getCurrentDbRole()` 方法
- 登出时清除连接和角色设置

### 5. 应用配置 `AppConfig.java`
- 支持从配置文件读取角色数据库凭据
- 支持 SSL 配置
- 打印角色配置信息

### 6. 数据库初始化 `DatabaseInitializer.java`
- 使用管理员角色执行初始化
- 创建示例出题人和考官账号
- 测试所有角色连接

### 7. 数据库脚本 `database.sql`
- 创建四个数据库角色（candidate, test_setter, judge, admin_user）
- 配置各角色的权限
- 添加评分明细表

### 8. 主应用 `JavaFXApp.java`
- 更新初始化流程以支持角色连接测试

## 数据库用户角色映射

| 应用角色 | 数据库用户 | 权限 |
|---------|-----------|------|
| CANDIDATE (考生) | candidate | SELECT questions, SELECT/INSERT interview_records |
| QUESTION_CREATOR (出题人) | test_setter | 完全控制 questions 表 |
| EXAMINER (考官) | judge | SELECT questions, 完全控制 interview_records 和 evaluation_scores |
| ADMIN (管理员) | admin_user | 所有权限 |

## 使用方式

### 登录自动切换
```java
AuthService authService = new AuthService();
authService.login("candidate", "password");
// 自动使用 candidate 数据库用户连接
```

### 手动切换角色
```java
DatabaseConnection.setCurrentDbRole(DbUserRole.JUDGE);
Connection conn = DatabaseConnection.getConnection();
```

### 根据应用角色切换
```java
DatabaseConnection.setRoleByAppRole(Role.CANDIDATE);
```

## SSL 配置

在 `config.properties` 中配置：
```properties
db.ssl.enabled=true
db.ssl.mode=require
# db.ssl.cert.path=/path/to/client-cert.pem
# db.ssl.key.path=/path/to/client-key.pem
```

## 默认账号

初始化后系统创建以下账号：

| 账号 | 密码 | 角色 |
|-----|------|------|
| admin | admin123 | 管理员 |
| candidate | candidate123 | 考生 |
| setter | setter123 | 出题人 |
| judge | judge123 | 考官 |

## 数据库初始化

执行 `database.sql` 创建数据库角色和权限：
```bash
psql -U postgres -d interview_system -f src/main/resources/database.sql
```

## 注意事项

1. 所有数据库用户都通过 SSL/HTTPS 连接
2. PostgreSQL 服务器需要配置 SSL 证书
3. 应用层用户（存储在 users 表）与数据库用户（candidate, test_setter, judge）是分开的
4. 同一角色的所有应用用户共享同一个数据库用户连接

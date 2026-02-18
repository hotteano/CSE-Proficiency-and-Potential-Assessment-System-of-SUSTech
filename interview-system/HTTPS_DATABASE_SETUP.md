# HTTPS/SSL 数据库连接配置指南

## 概述

本系统支持通过 HTTPS/SSL 协议安全地访问 PostgreSQL 数据库。系统为不同角色配置了不同的数据库用户，实现细粒度的权限控制。

## 数据库用户角色

系统使用以下数据库用户（所有用户都通过 HTTPS/SSL 访问）：

| 数据库用户 | 对应角色 | 权限说明 |
|-----------|---------|---------|
| `candidate` | 考生 | 只读访问题目，可创建/查看自己的面试记录 |
| `test_setter` | 出题人 | 完全控制题目表（增删改查） |
| `judge` | 考官 | 查看题目，管理面试记录和评分 |
| `admin_user` | 管理员 | 完整数据库权限 |

## 快速开始

### 1. 初始化数据库

执行 `src/main/resources/database.sql` 脚本创建数据库用户和权限：

```bash
# 以 postgres 用户登录
psql -U postgres -d interview_system -f src/main/resources/database.sql
```

### 2. 配置 PostgreSQL SSL

编辑 `postgresql.conf`：

```conf
ssl = on
ssl_cert_file = 'server.crt'
ssl_key_file = 'server.key'
ssl_ca_file = 'root.crt'
```

编辑 `pg_hba.conf` 强制使用 SSL：

```conf
# 强制所有连接使用 SSL
hostssl all all 0.0.0.0/0 md5
```

重启 PostgreSQL 服务：

```bash
# Windows
net postgresql-x64-15 restart

# Linux
sudo systemctl restart postgresql
```

### 3. 配置客户端

编辑 `config.properties`：

```properties
# 启用 SSL
db.ssl.enabled=true
db.ssl.mode=require

# 角色数据库用户配置
db.candidate.user=candidate
db.candidate.password=candidate_secure_pass

db.setter.user=test_setter
db.setter.password=setter_secure_pass

db.judge.user=judge
db.judge.password=judge_secure_pass
```

## 生成 SSL 证书（测试环境）

```bash
# 生成服务器私钥和证书
openssl req -new -x509 -days 365 -nodes -text -out server.crt \
  -keyout server.key -subj "/CN=dbhost.yourdomain.com"

# 设置权限
chmod 600 server.key
chmod 644 server.crt

# 复制到 PostgreSQL 数据目录
cp server.crt server.key /var/lib/pgsql/data/
```

## 代码中使用

### 登录时自动设置数据库角色

```java
AuthService authService = new AuthService();
String result = authService.login(username, password);
// 登录成功后，会自动根据用户角色设置对应的数据库连接
```

### 手动切换数据库角色

```java
import com.interview.util.DatabaseConnection;
import com.interview.config.DatabaseConfig.DbUserRole;

// 切换到考生角色连接
DatabaseConnection.setCurrentDbRole(DbUserRole.CANDIDATE);
Connection conn = DatabaseConnection.getConnection();

// 切换到考官角色连接
DatabaseConnection.setCurrentDbRole(DbUserRole.JUDGE);
Connection conn = DatabaseConnection.getConnection();
```

### 根据应用角色获取连接

```java
import com.interview.model.Role;

// 根据应用角色自动映射到数据库角色
DatabaseConnection.setRoleByAppRole(Role.CANDIDATE);
Connection conn = DatabaseConnection.getConnection();
```

## SSL 模式说明

| 模式 | 说明 | 适用场景 |
|-----|------|---------|
| `disable` | 不使用 SSL | 仅本地开发 |
| `allow` | 优先非 SSL，如不可用则使用 SSL | 不推荐 |
| `prefer` | 优先 SSL，如不可用则使用非 SSL | 开发/测试 |
| `require` | 必须使用 SSL，不验证证书 | 内网生产环境 |
| `verify-ca` | 使用 SSL 并验证 CA 证书 | 生产环境 |
| `verify-full` | 使用 SSL 并验证主机名和证书 | 高安全要求 |

## 安全建议

1. **生产环境** 使用 `verify-ca` 或 `verify-full` 模式
2. **证书管理** 定期更新 SSL 证书（建议每年）
3. **密码安全** 使用强密码，并定期更换
4. **网络隔离** 数据库服务器应该在内网，不直接暴露到公网
5. **防火墙** 限制只有应用服务器可以访问数据库端口

## 故障排除

### SSL 连接失败

1. 检查 PostgreSQL SSL 是否启用：
   ```sql
   SHOW ssl;
   ```

2. 检查证书文件路径和权限

3. 查看 PostgreSQL 日志获取详细错误信息

### 权限不足错误

确保已执行 `database.sql` 中的权限授予语句：

```sql
-- 重新授予权限
GRANT USAGE ON SCHEMA public TO candidate;
GRANT SELECT ON questions TO candidate;
-- ...
```

## 相关文件

- `config.properties` - 数据库连接配置
- `src/main/resources/database.sql` - 数据库初始化脚本
- `src/main/java/com/interview/config/DatabaseConfig.java` - 数据库配置类
- `src/main/java/com/interview/util/DatabaseConnection.java` - 数据库连接工具

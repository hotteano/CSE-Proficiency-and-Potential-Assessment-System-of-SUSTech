# PostgreSQL 远程连接配置指南

## 概述

为了让出题人可以在不同机器上远程连接数据库进行出题，需要配置 PostgreSQL 允许远程连接。

## 服务端配置（数据库服务器）

### 1. 修改 postgresql.conf

找到 PostgreSQL 配置文件（通常位于）：
- Linux: `/etc/postgresql/14/main/postgresql.conf`
- Windows: `C:\Program Files\PostgreSQL\14\data\postgresql.conf`

修改监听地址：
```conf
# 从：
listen_addresses = 'localhost'

# 改为：
listen_addresses = '*'
```

### 2. 修改 pg_hba.conf

找到访问控制文件（通常位于）：
- Linux: `/etc/postgresql/14/main/pg_hba.conf`
- Windows: `C:\Program Files\PostgreSQL\14\data\pg_hba.conf`

添加远程访问规则：
```conf
# IPv4 远程连接（使用 md5 密码认证）
host    all             all             0.0.0.0/0               md5

# 或者限制特定网段（推荐）
host    all             all             192.168.1.0/24          md5
```

### 3. 重启 PostgreSQL 服务

**Linux:**
```bash
sudo systemctl restart postgresql
```

**Windows:**
```powershell
net stop postgresql-x64-14
net start postgresql-x64-14
```

### 4. 配置防火墙

**Linux (ufw):**
```bash
sudo ufw allow 5432/tcp
```

**Linux (firewalld):**
```bash
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --reload
```

**Windows 防火墙:**
1. 打开"高级安全 Windows Defender 防火墙"
2. 新建入站规则
3. 选择"端口"，输入 5432
4. 允许连接

## 客户端配置（出题人电脑）

### 1. 启动应用程序

```bash
java -jar interview-system-1.0-SNAPSHOT.jar
```

### 2. 配置数据库连接

1. 使用**出题人账户**登录（QUESTION_CREATOR 角色）
2. 点击"数据库配置"标签
3. 填写远程数据库信息：
   - 主机地址：数据库服务器 IP（如 `192.168.1.100`）
   - 端口：`5432`
   - 数据库名：`interview_system`
   - 用户名：`postgres`
   - 密码：数据库密码

4. 点击"测试连接"验证
5. 点击"保存配置"

### 3. 开始使用

配置成功后，出题人可以正常使用：
- 题目浏览
- 题目管理（创建、编辑题目）

所有数据将存储在远程数据库中。

## 安全建议

### 1. 使用强密码
```sql
ALTER USER postgres WITH PASSWORD 'YourStrongPassword123!';
```

### 2. 创建专用账户
```sql
-- 创建出题人专用账户
CREATE USER question_creator WITH PASSWORD 'creator_password';

-- 授予必要权限
GRANT CONNECT ON DATABASE interview_system TO question_creator;
GRANT USAGE ON SCHEMA public TO question_creator;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO question_creator;
```

### 3. 使用 VPN 或 SSH 隧道（生产环境推荐）

**SSH 隧道方式：**
```bash
# 在本地建立隧道
ssh -L 5433:localhost:5432 user@db-server

# 然后在应用中连接 localhost:5433
```

### 4. 限制访问 IP

在 `pg_hba.conf` 中只允许的 IP：
```conf
host    interview_system    question_creator    192.168.1.0/24    md5
```

## 故障排查

### 连接超时
- 检查防火墙设置
- 确认 PostgreSQL 服务运行中
- 验证端口是否开放：
  ```bash
  telnet db-server 5432
  ```

### 认证失败
- 检查用户名密码
- 确认 pg_hba.conf 配置正确
- 查看 PostgreSQL 日志

### 权限不足
- 确认用户有数据库访问权限
- 检查表权限设置

## 常见问题

**Q: 是否需要安装 PostgreSQL 客户端？**
A: 不需要，JDBC 驱动已包含在应用中。

**Q: 多个出题人可以同时出题吗？**
A: 可以，支持并发访问。

**Q: 连接断开后需要重新配置吗？**
A: 不需要，配置会保存在内存中直到应用重启。

**Q: 如何恢复到本地连接？**
A: 在数据库配置界面点击"恢复默认"即可。

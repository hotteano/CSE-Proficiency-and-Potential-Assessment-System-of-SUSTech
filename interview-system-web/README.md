# 面试系统 Web 版

基于 Spring Boot + Vue 3 的 Web 版本面试系统。

## 项目结构

```
interview-system-web/
├── backend/          # Spring Boot 后端
│   ├── src/main/java/com/interview/web/
│   │   ├── entity/       # 实体类（复用原模型）
│   │   ├── repository/   # 数据访问层
│   │   ├── service/      # 业务逻辑层
│   │   ├── controller/   # REST API 控制器
│   │   ├── security/     # JWT 安全组件
│   │   └── config/       # 配置类
│   └── pom.xml
│
└── frontend/         # Vue 3 前端
    ├── src/
    │   ├── api/           # API 接口
    │   ├── views/         # 页面视图
    │   ├── stores/        # Pinia 状态管理
    │   ├── router/        # 路由配置
    │   └── components/    # 组件
    └── package.json
```

## 快速开始

### 1. 启动后端

```bash
cd backend

# 编译运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/interview-system-web-1.0.0.jar
```

后端默认运行在 `http://localhost:8080`

### 2. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 构建生产版本
npm run build
```

前端默认运行在 `http://localhost:3000`

## 配置说明

### 后端配置 (application.yml)

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/interview_system
    username: postgres
    password: your_password

# JWT 密钥
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

### 前端代理配置

开发模式下，前端通过 Vite 代理访问后端 API：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## API 接口

| 接口 | 方法 | 说明 |
|-----|------|------|
| /api/auth/login | POST | 登录 |
| /api/auth/register | POST | 注册 |
| /api/auth/me | GET | 获取当前用户 |
| /api/questions | GET | 获取所有题目 |
| /api/questions | POST | 创建题目 |
| /api/questions/{id} | PUT | 更新题目 |
| /api/questions/{id} | DELETE | 删除题目 |

## 与原 JavaFX 版本的区别

| 特性 | JavaFX 版 | Web 版 |
|-----|----------|--------|
| 运行方式 | 本地桌面应用 | 浏览器访问 |
| 数据库连接 | 直连 PostgreSQL | 通过 REST API |
| 部署方式 | 分发 JAR 文件 | 服务器部署 |
| 互联网访问 | 需 VPN/SSH 隧道 | 直接访问 |
| 语音录制 | 原生支持 | WebRTC |
| 跨平台 | 需安装 JRE | 任意浏览器 |

## 生产部署

### Docker 部署

```bash
# 构建镜像
docker build -t interview-web .

# 运行容器
docker run -p 8080:8080 interview-web
```

### 云服务器部署

1. 购买云服务器（推荐阿里云/腾讯云）
2. 安装 Docker
3. 部署 PostgreSQL
4. 部署后端应用
5. 使用 Nginx 反向代理

详细部署文档请参考 `DEPLOY.md`

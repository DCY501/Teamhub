# teamhub-backend

TeamHub — 高校球队数字档案系统的 Spring Boot 后端服务。

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.5.14 |
| Java | 17 |
| MyBatis-Plus | 3.5.12 |
| Spring Security | 6.5.10 |
| JWT (jjwt) | 0.12.6 |
| Knife4j / SpringDoc | 4.4.0 / 2.7.0 |
| MySQL | 8.0 |

## 项目结构

```
teamhub-backend/
├── src/main/java/com/teamhub/teamhub/
│   ├── config/          # SecurityConfig、JWT Filter、CORS、全局异常处理
│   ├── controller/      # REST API（User / Training / Match / Player / Team / Timeline / Development / Dashboard）
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # MyBatis-Plus Mapper 接口
│   ├── entity/          # 数据实体
│   └── dto/             # 请求 DTO
├── src/main/resources/
│   └── application.yml  # 数据源、MyBatis-Plus、Knife4j 配置
├── pom.xml
└── init.sql             # 数据库初始化脚本（表结构 + 初始数据）
```

## 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+

## 本地运行

```bash
# 1. 创建数据库并执行初始化脚本
mysql -u root -p < init.sql

# 2. 修改数据库配置
# 编辑 src/main/resources/application.yml
# spring.datasource.url / username / password

# 3. 编译运行
mvn clean package -DskipTests
java -jar target/teamhub-0.0.1-SNAPSHOT.jar
```

服务默认监听 `http://localhost:8080`。

## API 文档

启动后访问 Knife4j 文档页面：

```
http://localhost:8080/doc.html
```

## 核心模块接口

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 用户 | `/api/user/**` | 登录、注册、JWT Token 刷新 |
| 训练 | `/api/training/**` | 训练 CRUD、报名、统计 |
| 比赛 | `/api/match/**` | 比赛创建、局分编辑、状态流转 |
| 球员 | `/api/player/**` | 球员档案、年级推算、毕业迁移 |
| 球队 | `/api/team/**` | 球队设置、邀请码、队长传承、解散 |
| 时光轴 | `/api/timeline/**` | 事件列表、自动/人工事件 |
| 发展中心 | `/api/development/**` | 自评、建议、点赞、举报 |
| 仪表盘 | `/api/dashboard/**` | 数据统计 |

## 安全设计

- **JWT 无状态认证**：HS256 签名，7 天有效期
- **Spring Security 方法级权限**：`@PreAuthorize("hasRole('CAPTAIN')")` 控制队长专属操作
- **CORS 配置**：支持 localhost、127.0.0.1、局域网 IP 及生产服务器域名

## 生产部署

```bash
# 编译
mvn clean package -DskipTests

# 上传 jar 到服务器
scp target/teamhub-0.0.1-SNAPSHOT.jar root@124.222.51.177:/opt/teamhub/

# 重启服务
systemctl restart teamhub
```

服务器环境：Ubuntu 22.04 + Nginx 1.24 + systemd

# TeamHub — 高校球队数字档案系统

> 基于 Vue 3 + Spring Boot 的高校院队管理 Web 应用，覆盖训练、比赛、球员档案、发展中心、时光轴等核心模块。

## 技术栈

| 层级 | 技术 |
|-----|------|
| 前端 | Vue 3.5 + TypeScript + Vite 8 + Element Plus 2.13 |
| 后端 | Spring Boot 3.5 + Java 17 + MyBatis-Plus 3.5 + Spring Security |
| 数据库 | MySQL 8.0 |
| 部署 | Nginx + systemd (Ubuntu 22.04) |

## 功能模块

- **训练中心**：训练发布、接龙报名（参加/请假/待定）、报名统计、出勤率计算
- **比赛中心**：比赛创建、多小局比分编辑、状态流转（UPCOMING → ONGOING → FINISHED）
- **球员档案**：双标签页（当打之年/功勋人物）、年级自动推算、毕业迁移
- **发展中心**：球员自评、队友互评建议、球队建议、点赞 toggle、举报审核
- **时光轴**：自动事件（训练创建/比赛结束/新成员入队）+ 人工事件发布与编辑
- **球队设置**：邀请码管理、队长传承、成员管理、解散球队（级联删除）
- **管理后台**：被举报建议审核

## 项目结构

```
Teamhub/
├── teamhub-backend/          # Spring Boot 后端
│   ├── src/main/java/...     # Controller / Service / Mapper / Entity
│   ├── pom.xml
│   └── init.sql              # 数据库初始化脚本
├── teamhub-frontend/         # Vue 3 前端
│   ├── src/views/            # 页面组件
│   ├── src/router/           # 路由 + 导航守卫
│   └── package.json
└── README.md
```

## 快速开始

### 后端

```bash
cd teamhub-backend
# 修改 src/main/resources/application.yml 中的数据库配置
mvn clean package -DskipTests
java -jar target/teamhub-0.0.1-SNAPSHOT.jar
```

### 前端

```bash
cd teamhub-frontend
npm install
npm run dev
```

### 数据库

执行 `teamhub-backend/init.sql` 创建表结构和初始数据。

## 在线演示

- **公网地址**：`http://124.222.51.177`
- **API 文档**：`http://124.222.51.177/doc.html`

## 部署

生产环境部署参考 `云服务器操作指南.md`（本地文档，不在仓库中）。

**服务器环境**：腾讯云/阿里云 2核4G，Ubuntu 22.04 LTS，Nginx 1.24

**关键路径**：
| 项目 | 路径 |
|------|------|
| 后端 jar | `/opt/teamhub/teamhub-0.0.1-SNAPSHOT.jar` |
| 后端配置 | `/opt/teamhub/application.yml` |
| 前端静态文件 | `/var/www/teamhub/dist/` |
| Nginx 配置 | `/etc/nginx/sites-available/teamhub` |
| systemd 服务 | `/etc/systemd/system/teamhub.service` |
| 数据库备份 | `/opt/teamhub/backup/`（每日凌晨 3 点自动备份，保留 7 天） |

**部署流程**：
1. 后端 `mvn clean package -DskipTests` 产出 jar，上传到 `/opt/teamhub/`
2. 前端 `npm run build` 产出 `dist/`，上传到 `/var/www/teamhub/dist/`
3. Nginx 反向代理 `/api` 到本地 8080
4. `systemctl restart teamhub` 重启后端服务

## API 文档

后端集成 Knife4j，启动后访问：`http://localhost:8080/doc.html`

核心接口：
- `POST /api/user/login` — 登录
- `POST /api/user/register` — 注册
- `GET /api/training/list` — 训练列表
- `POST /api/training/create` — 创建训练（队长）
- `PUT /api/training/{id}` — 编辑训练（队长）
- `DELETE /api/training/{id}` — 删除训练（队长）
- `POST /api/training/{id}/signup` — 训练报名

## 主要特性

- **JWT 无状态认证**：HS256 签名，7 天过期，支持 Token 刷新
- **Spring Security 方法级权限**：`@PreAuthorize("hasRole('CAPTAIN')")` 控制队长专属接口
- **年级自动推算**：根据入学年份 + 学位类型自动计算当前年级（大一~大四、研一~研三）
- **时区安全处理**：前端发送本地时间字符串，避免 UTC 偏移导致的 8 小时误差
- **响应式布局**：桌面端侧边栏 + 移动端抽屉导航

## 开发者

- **杜辰宇**（U202315265）：后端开发、数据库设计、服务器部署
- **许睿廷**（U202315287）：前端开发、功能完善、测试用例

华中科技大学 软件工程课程项目

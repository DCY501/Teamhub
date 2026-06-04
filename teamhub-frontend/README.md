# teamhub-frontend

TeamHub — 高校球队数字档案系统的 Vue 3 前端。

## 技术栈

| 组件 | 版本 |
|------|------|
| Vue | 3.5.32 |
| Vite | 8.0.8 |
| TypeScript | 5.7 |
| Element Plus | 2.13.7 |
| Vue Router | 4.5 |
| Pinia | 3.0 |
| Axios | 1.8 |

## 项目结构

```
teamhub-frontend/
├── public/
├── src/
│   ├── api/           # Axios 封装 + 各模块 API 请求
│   ├── assets/        # 图片、字体、全局样式
│   ├── components/    # 公共组件
│   ├── router/        # 路由配置 + 导航守卫
│   ├── stores/        # Pinia 状态管理
│   ├── views/         # 页面组件
│   │   ├── Dashboard.vue
│   │   ├── Training.vue / TrainingDetail.vue
│   │   ├── Match.vue / MatchDetail.vue
│   │   ├── Player.vue / PlayerDetail.vue
│   │   ├── Development.vue
│   │   ├── Timeline.vue
│   │   ├── TeamSettings.vue
│   │   └── AdminReport.vue
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 环境要求

- Node.js 20+
- npm 10+

## 本地开发

```bash
npm install
npm run dev
```

服务默认运行在 `http://localhost:5173`，代理 `/api` 到后端 `http://localhost:8080`。

## 生产构建

```bash
npm run build
```

产物输出至 `dist/` 目录，可直接部署到 Nginx 静态资源目录。

## 部署

```bash
# 构建
npm run build

# 上传 dist 到服务器
scp -r dist/* root@124.222.51.177:/var/www/teamhub/dist/
```

Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name 124.222.51.177;

    location / {
        root /var/www/teamhub/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Knife4j API 文档
    location /doc.html {
        proxy_pass http://127.0.0.1:8080/doc.html;
    }
    location /webjars/ {
        proxy_pass http://127.0.0.1:8080/webjars/;
    }
    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080/v3/api-docs;
    }
    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080/swagger-ui/;
    }
}
```

## 主要功能页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 仪表盘 | `/dashboard` | 数据统计、出勤率 |
| 训练中心 | `/training` / `/training/:id` | 训练列表、详情、报名、队长编辑删除 |
| 比赛中心 | `/match` / `/match/:id` | 比赛列表、局分编辑、状态流转 |
| 球员档案 | `/player` / `/player/:id` | 双标签页、年级推算、毕业迁移 |
| 发展中心 | `/development` | 自评、建议、点赞、举报 |
| 时光轴 | `/timeline` | 自动事件 + 人工事件 |
| 球队设置 | `/team-settings` | 邀请码、队长传承、成员管理 |
| 管理后台 | `/admin-report` | 被举报建议审核 |

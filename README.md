# 社交媒体平台（MongoDB + Vue + Spring Boot）


- 数据库：MongoDB
- 前端：Vue 3 + Vite
- 后端：Spring Boot + Spring Data MongoDB
- JDK / Java：17
- Maven：项目管理
- 数据库名：`News`
- MongoDB 地址：`localhost:27017`
- 评论：嵌套文档存储在新闻集合内

## 目录说明
- `backend/` 后端工程
- `frontend/` 前端工程

## 快速启动
### 1）启动 MongoDB
确保本地 MongoDB 已运行，数据库名为 `News`

### 2）启动后端
```bash
cd backend
mvn clean spring-boot:run
```

### 3）启动前端
```bash
cd frontend
npm install
npm run dev
```

## 默认访问地址
- 后端接口：`http://localhost:8080`
- Swagger：`http://localhost:8080/swagger-ui.html`
- 前台首页：`http://localhost:5173`
- 管理后台：`http://localhost:5173/admin`

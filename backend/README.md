# 小蓝书日常发布系统后端（Spring Boot + MongoDB）

## 1. 技术栈
- JDK 17 / Java 17
- Spring Boot 4.0.4
- Spring Web
- Spring Data MongoDB
- Maven
- springdoc-openapi

## 2. 核心设计
### 2.1 集合设计
- `bb_tags`：内容标签/分类集合（如体育、科技、教育、日常等）
- `bb_users`：用户集合
- `bb_posts`：发布内容集合
- 评论不单独建集合，作为 `bb_posts.comments` 的嵌套文档存储

### 2.2 领域模型
- 标签分类：编码、名称、描述、排序、启用状态
- 用户信息：用户名、密码摘要、昵称、头像、简介、所在地、登录令牌
- 发布内容：标题、摘要、正文、封面、图片列表、标签快照、作者快照、话题列表、浏览量、点赞数、评论数、评论嵌套文档

### 2.3 实现要点
- 分层架构：controller / service / repository / dto / model / config / exception
- MongoDB 索引：用户名唯一索引、标签唯一索引、内容标签+作者+发布时间组合索引
- 全局异常处理 + 参数校验
- 登录注册 + Bearer Token 简易鉴权
- 初始化种子数据
- Swagger/OpenAPI 文档
- 点赞原子自增、评论数冗余字段

## 3. 运行环境
MongoDB 连接要求：
- Host: `localhost`
- Port: `27017`
- Database: `BBook`
- 无用户名密码

## 4. 启动方式
```bash
mvn clean spring-boot:run
```

或者：
```bash
mvn clean package
java -jar target/news-publishing-system-1.0.0.jar
```

## 5. 默认测试账号
初始化数据会自动生成 2 个账号：

```text
用户名：lanmei
密码：123456

用户名：aning
密码：123456
```

## 6. 主要接口
### 公开接口
- `POST /api/v1/auth/register` 注册
- `POST /api/v1/auth/login` 登录
- `GET /api/v1/tags` 标签列表
- `GET /api/v1/posts` 内容分页查询
- `GET /api/v1/posts/{id}` 内容详情（自动 +1 浏览量）
- `GET /api/v1/users/{id}` 用户主页

### 需要登录的接口
- `GET /api/v1/auth/me` 当前登录用户
- `POST /api/v1/posts` 发布内容
- `POST /api/v1/posts/{id}/comments` 发表评论
- `POST /api/v1/posts/{id}/likes` 点赞

## 7. Swagger
启动后访问：
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/api-docs`

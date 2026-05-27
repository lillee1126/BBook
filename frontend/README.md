# 小蓝书日常发布系统前端（Vue 3 + Vite）

## 技术栈
- Vue 3
- Vue Router
- Pinia
- Axios
- Vite

## 功能说明
### 用户端
- 首页瀑布流卡片列表
- 标签筛选 / 关键词搜索
- 登录 / 注册
- 发布日常内容
- 内容详情页
- 点赞功能
- 评论功能
- 用户个人主页

## 环境变量
项目默认请求：`http://localhost:8080`

你也可以创建 `.env.development`：
```env
VITE_API_BASE_URL=http://localhost:8080
```

## 启动方式
建议 Node.js 版本满足 Vite 要求（20.19+ 或 22.12+）。

```bash
npm install
npm run dev
```

## 打包
```bash
npm run build
```

## 页面地址
- 前台首页：`http://localhost:5173/`
- 发布页面：`http://localhost:5173/publish`

251008 CODEX V0.1
 
 # 每日朋友圈助手 (Web + Java Server)

一款帮助用户“每日快速生成朋友圈内容”的 Web 应用：上传图片 → 视觉理解 → 生成多条中文文案 → 选择模板并由大模型直接渲染成图 → 下载/复制发圈。已接入火山引擎（Ark）聊天与图像生成接口，并内置游客/订阅两档配额与简易支付闭环（微信渠道）。

## 功能特性
- 看图生成文案：视觉模型理解图片 + 人设/受众 → 3–5 条候选
- 文案重写：更短/更专业/更生活化/少营销等
- AI 渲染成图：根据提示要求由图片模型直接加字排版（支持水印/分辨率/比例）
- 模板参数：主色、字体、字号、行距、边距、比例/分辨率
- 历史与收藏：记录生成结果（当前为内存存储占位）
- 配额策略：游客每日免费额度，订阅（月配额）
- 支付闭环：仅微信（现含模拟回调；可扩展为正式微信支付）
- 埋点：服务端事件日志（可扩展第三方 SDK）

## 目录结构
- `server/` Java Spring Boot 后端（API、LLM/渲染 Provider、配额/支付/埋点）
- `web/` Next.js 14 前端（App Router）
- `docs/` 需求与集成文档（PRD、OpenAPI、提示词与集成指南）

## 技术栈
- 后端：Java 17 + Spring Boot 3.3、WebFlux WebClient（调用火山接口）
- 前端：Next.js 14（App Router）+ React 18
- AI：火山引擎 Ark Chat Completions 与 Images Generations（可切换 Mock）

## 快速开始
1) 启动后端
- 安装 Java 17+、Maven 3.9+
- 配置火山参数（见下方“AI 配置”），或先用默认 mock 试跑
- 运行：`mvn spring-boot:run -f server/pom.xml`
- API 根路径：`http://localhost:8080/api/v1`

2) 启动前端
- 安装 Node 18+
- 在 `web/` 下：`npm install && npm run dev`
- 配置环境变量（可选）：`NEXT_PUBLIC_API_BASE=http://localhost:8080/api/v1`
- 打开 `http://localhost:3000` 访问 Web 界面

## AI 配置（火山引擎 Ark）
- 文件：`server/src/main/resources/application.yml`
- 关键项：
  - `ai.provider`: `volc` 或 `mock`
  - `ai.volc-base-url`: 如 `https://ark.cn-beijing.volces.com/api/v3`
  - `ai.volc-text-endpoint`: `/chat/completions`
  - `ai.volc-vision-endpoint`: `/chat/completions`
  - `ai.volc-image-edit-endpoint`: `/images/generations`
  - `ai.volc-*-model`: 对应 endpoint_id（形如 `ep-xxxx`）
  - `ai.volc-api-key`: Ark 平台 API Key（作为 `Authorization: Bearer`）
- 对齐示例与注意事项见：`docs/integration/volcengine.md`

## 支付与配额
- 仅微信渠道（模拟回调已接通）：
  - 创建订单：`POST /api/v1/billing/orders`（Header: `X-Client-Id` 建议携带）
  - 模拟回调：`POST /api/v1/billing/callback/wechat?orderId=...`
  - 查询配额：`GET /api/v1/quota`
- 配额与计费口径可配：`quota.*`（见 yml）。详见：`docs/billing_quota.md`

## 关键 API（节选）
- 生成链路：
  - `POST /generate/caption` → 图片理解（占位）
  - `POST /generate/copy` → 文案候选（触发视觉/文本模型）
  - `POST /generate/rewrite` → 文案重写
  - `POST /image/render` → 由图片模型直接渲染加字
- 账户与数据：
  - `GET /history`、`POST /favorites`
  - `GET /quota` → 查询额度
- 调试：
  - `GET /debug/ai` → 查看当前 AI Provider 与配置是否生效

## 前端页面（MVP）
- `/` 首页 → 入口导航
- `/upload` 上传/解析/生成/渲染一体化闭环页（已调用后端接口）
- `/history` 历史/收藏（占位）
- `/account` 账号/购买（占位）

## 开发脚手架与切换
- AI Provider 可在 `AiConfig` 中按 `ai.provider` 切换（`mock`/`volc`）
- LLM 文案提示词：`docs/prompts/copy_zh.md`
- 渲染提示词：`docs/prompts/rendering_zh.md`
- 日志：
  - 调用火山接口时会打印 `Volc POST ...` 与错误时的 response body 便于排查
  - 埋点事件由 `analytics` logger 输出

## 常见问题
- 400/Bad Request：检查 `endpoint` 是否为“路径”（`/chat/completions`），模型是否为 `ep-xxxx`，以及请求体字段命名是否与 docs 对齐。
- 404：常见于把 `ep-xxxx` 填到 endpoint 上，或 baseUrl/路径错误。
- 图片不可读：确保 `image` URL 公网可访问。

## 贡献与许可证
- 欢迎提交 Issue/PR 共建（许可证待定）。

---

## 推送到 GitHub（步骤）
1) 初始化仓库（如未初始化）：
```
 git init
 git add -A
 git commit -m "Initial import: WeChat Moments Assistant"
```
2) 在 GitHub 创建空仓库（不要勾选初始化 README）
3) 关联远程并推送：
```
 git remote add origin https://github.com/<YOUR_USER>/<YOUR_REPO>.git
 git branch -M main
 git push -u origin main
```
- 使用 PAT：`https://<YOUR_USER>:<TOKEN>@github.com/<YOUR_USER>/<YOUR_REPO>.git`
- 使用 SSH：`git@github.com:<YOUR_USER>/<YOUR_REPO>.git`
# 每日朋友圈助理 v1.0

一套「图文生成 + 海报渲染」的一体化解决方案：用户上传图片或给出场景，服务会调用视觉模型理解内容、生成多条中文文案，并可套用模板直接渲染成图，支持复制、下载与后续分享。本次 v1.0 版本完成了后端 API、Web 前端、AI Provider 接入、配额与支付闭环的打通。

## 功能亮点
- 看图写文：视觉模型解读图片，结合人设与受众生成 3 条备选文案
- AI 改写：支持一键润色成更口语、更营销或更生活化的表达
- 图片渲染：调用图片模型直接排版出图，可调节字号、行距、水印等参数
- 用户体系：提供游客/订阅双档配额与微信渠道的模拟支付闭环
- 数据记录：提供历史与收藏入口（当前为占位实现，可扩展持久化）
- 埋点分析：服务端输出关键事件日志，便于接入第三方统计

## 代码结构
- `server/`：Spring Boot 3.3 后端，包含 AI/支付/配额等业务逻辑
- `web/`：Next.js 14 前端（App Router），覆盖上传、生成、历史等页面
- `docs/`：需求文档、OpenAPI、提示词、Provider 对接指南

## 技术栈
- 后端：Java 17、Spring Boot 3.3、MyBatis-Plus、WebFlux WebClient
- 前端：Next.js 14、React 18、Tailwind CSS（可选）
- AI Provider：火山引擎 Ark Chat Completions 与 Images Generations（可切换 Mock）
- 数据库：MySQL 8（示例连接字符串见配置）

## 快速启动
1. 启动后端  
   - 安装 Java 17 与 Maven 3.9+  
   - 在 `server/src/main/resources/application.yml` 或环境变量中填写配置  
   - 运行 `mvn spring-boot:run -f server/pom.xml`  
   - 后端默认监听 `http://localhost:8080/api/v1`
2. 启动前端  
   - 安装 Node.js 18+  
   - 进入 `web/` 执行 `npm install && npm run dev`  
   - 可选设置：`NEXT_PUBLIC_API_BASE=http://localhost:8080/api/v1`  
   - 访问 `http://localhost:3000`

## 核心配置
- 数据库：  
  - `spring.datasource.url`、`spring.datasource.username`、`spring.datasource.password`
- 火山引擎：  
  - `ai.provider` 取值 `volc` 或 `mock`  
  - `ai.volc-api-key`、`ai.volc-*-model`、`ai.volc-*-endpoint`
- 阿里云 OSS：  
  - `oss.endpoint` 与 `oss.bucket` 使用默认值或按环境覆写  
  - `OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET`、`OSS_PUBLIC_DOMAIN`、`OSS_SIGNED_URL_EXPIRE_SECONDS`
- 配额与订单：  
  - `quota.*` 可调整免费额度、订阅策略  
  - 微信模拟回调路径：`/api/v1/billing/callback/wechat`

## 核心 API
- 生成链路  
  - `POST /api/v1/generate/caption`：图片理解（占位）  
  - `POST /api/v1/generate/copy`：生成朋友圈文案  
  - `POST /api/v1/generate/rewrite`：文案润色  
  - `POST /api/v1/image/render`：AI 渲染海报
- 账号与配额  
  - `GET /api/v1/quota`：查询剩余额度  
  - `POST /api/v1/billing/orders`：创建订单  
  - `POST /api/v1/billing/callback/wechat`：模拟支付回调
- 调试辅助  
  - `GET /api/v1/debug/ai`：查看当前 AI Provider 状态

## 版本日志
- **v1.0 (2025-01-29)**  
  - 完成 Web + Server 全链路打通  
  - 接入火山引擎文案与图片模型  
  - 加入配额管理与微信支付模拟  
  - 提供 OSS 存储配置与签名下载能力

## 贡献指南
- 欢迎提交 Issue/PR，建议先在 Issue 中描述需求或问题
- 代码提交请遵循项目现有的分层结构与命名规范
- 提交前确保本地构建通过，并补充必要的单元测试/集成测试

## 许可证
许可证暂未确定，默认保留全部权利。如需使用请先联系作者。

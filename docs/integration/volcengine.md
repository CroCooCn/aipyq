Volcengine（火山引擎）对接准备

目标
- 使用具备视觉解析能力的模型进行“看图生成文案”（Vision Chat）
- 使用具备图片编辑/生成能力的模型进行“图片渲染/加字”（Image Edit）

服务端开关
- 配置文件：server/src/main/resources/application.yml 下 `ai.*`
- 或通过环境变量覆盖（适合容器化/本地调试）

关键配置项（务必分清“endpoint 路径”与“模型/endpoint_id”）
- ai.provider：`mock` 或 `volc`（切换为 volc 即启用火山）
- VOLC_BASE_URL：例如 `https://ark.cn-beijing.volces.com/api/v3`
- VOLC_API_KEY：火山引擎 Ark 平台 API Key（Bearer）
- 文本与视觉对话接口：
  - VOLC_TEXT_ENDPOINT：必须是“路径”，例如 `/chat/completions`
  - VOLC_VISION_ENDPOINT：必须是“路径”，例如 `/chat/completions`
  - VOLC_TEXT_MODEL：`model`/`endpoint_id`（形如 `ep-xxxxxxxx`）
  - VOLC_VISION_MODEL：`model`/`endpoint_id`（形如 `ep-xxxxxxxx`）
- 图片编辑接口：
  - VOLC_IMAGE_EDIT_ENDPOINT：必须是“路径”，例如 `/images/edits`
  - VOLC_IMAGE_EDIT_MODEL：`model`/`endpoint_id`（形如 `ep-xxxxxxxx`）

如何启用火山
1) 获取 API Key 与可用模型（文本/视觉/图片编辑）
2) 设置环境变量并启动后端：
   - `AI_PROVIDER=volc`
   - `VOLC_BASE_URL=...`
   - `VOLC_API_KEY=...`
   - `VOLC_TEXT_ENDPOINT=/chat/completions` ← 路径
   - `VOLC_VISION_ENDPOINT=/chat/completions` ← 路径
   - `VOLC_IMAGE_EDIT_ENDPOINT=/images/edits` ← 路径
   - `VOLC_TEXT_MODEL=ep-xxxx`
   - `VOLC_VISION_MODEL=ep-xxxx`
   - `VOLC_IMAGE_EDIT_MODEL=ep-xxxx`

调用说明
- 生成文案：POST `/api/v1/generate/copy`
  - 可直接传 `imageUrl` 触发视觉模型路径；否则走纯文本模型
- 重写文案：POST `/api/v1/generate/rewrite`
- 渲染图片：POST `/api/v1/image/render`
  - 若 `ai.provider=volc`，将调用 `VOLC_IMAGE_EDIT_ENDPOINT`，将 `imageUrl` 与文案作为编辑指令发送

注意
- 千万不要把 `ep-xxxx` 配到 `VOLC_*_ENDPOINT` 上！`ep-xxxx` 应配置到 `VOLC_*_MODEL`。
- 以上路径/模型名称为占位，需按火山引擎实际开通的产品与模型替换。
- 若配置不完整或请求失败，服务将回退为占位结果或原图。

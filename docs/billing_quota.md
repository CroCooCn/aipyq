支付与配额（简版）

支付渠道
- 仅支持微信（服务端校验 channel=wechat）

套餐
- 游客：每日免费额度 `quota.visitor-daily-credits`（默认 80）
- 月订阅：购买后获得 30 天配额 `quota.monthly-credits`（默认 5000）

计费口径
- 生成文案：消耗 `quota.cost-generate`（默认 1）
- 渲染成图：消耗 `quota.cost-render`（默认 1）

接口
- 创建订单：POST `/api/v1/billing/orders`（Header: `X-Client-Id` 建议携带）
  - 响应 `id` 为订单号；demo 金额 9.90
- 支付回调（模拟）：POST `/api/v1/billing/callback/wechat?orderId=xxx`
  - 发放 30 天订阅额度到下单时的 `X-Client-Id`
- 查询配额：GET `/api/v1/quota`（Header: `X-Client-Id`）

客户端标识
- 建议前端生成持久化 `X-Client-Id`（如 UUID 存 localStorage），并在每次请求携带
- 未携带时将退化为按 IP 识别（不精确）

配置
- `server/src/main/resources/application.yml` 中 `quota.*` 可调整额度与单次消耗


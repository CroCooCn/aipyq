渲染提示词（图片加字/编辑）

用途：调用火山引擎图片编辑模型，在原图上以高可读性排版叠加中文文案。

提示词模板（服务端动态拼接）

任务：在给定图片上，排版并叠加以下中文文案，用于发布微信朋友圈。
文案：
{copyText}

要求：
1) 不裁切主体，优先保留原图构图；输出分辨率为 {resolution}，画面比例 {ratio}。
2) 自动选择不遮挡主体的空白区域放置文字；若对比度不足，添加半透明深色文字背景条（圆角），保证可读性。
3) 字体：{fontFamily}，字号约 {fontSize}px（随分辨率等比），行距 {lineHeight} 倍，颜色 {primaryColor}；确保与背景对比度 ≥ 4.5:1，必要时自动反色或加描边。
4) 段落优化：按中文阅读习惯自动换行，避免过长一行；保留表情与标点；不新增无关口号。
5) 页面安全边距：四周预留 ≥ {margin}px，避免贴边。
6) 仅使用提供的文案内容，不额外添加文字或水印；{watermarkRule}
{gridRule}

输出：返回编辑后的图片URL（或Base64）数组；若包含九宫格，请先返回完整图，再返回九宫格9张。

变量说明
- {copyText}：RenderRequest.copyText
- {resolution}：RenderRequest.resolution（默认1080x1080）
- {ratio}：RenderRequest.ratio（默认1:1）
- {fontFamily}/{fontSize}/{lineHeight}/{primaryColor}/{margin}：来自 RenderRequest
- {watermarkRule}：若 watermarkOn=true，追加“另外在右下角添加极小号半透明水印（10% 不透明度），不影响主体与文案阅读；”
- {gridRule}：若 grid.enabled=true，追加“额外输出九宫格版本（3x3）...”

对应代码位置
- 构造逻辑：server/src/main/java/com/aipyq/friendapp/ai/providers/VolcImageRenderProvider.java 的 buildInstruction()


---
inclusion: always
---

# Chrome DevTools 调试规则

每次完成前端功能开发后，必须使用 chrome-devtools MCP 工具进行调试验证：

1. 使用 `navigate_page` 导航到对应页面
2. 使用 `take_snapshot` 检查页面 DOM 结构和内容是否正确渲染
3. 使用 `list_console_messages` 检查是否有 JavaScript 错误
4. 使用 `list_network_requests` 检查 API 请求是否正常（状态码、响应数据）
5. 如果有交互功能（表单提交、按钮点击等），使用 `click`、`fill` 等工具模拟用户操作并验证结果
6. 发现问题时立即修复，修复后重新验证

确保页面能够正常显示且功能可用后，才能标记任务为完成。

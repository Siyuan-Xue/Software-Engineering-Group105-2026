# 非功能需求与验证方法

本文档把软件工程课件中要求可验证的质量属性转化为 TA 招聘系统的工程验收标准。

| 类别 | 目标 | 验证方法 | 当前证据 |
|---|---|---|---|
| 安全 | 所有状态变更请求必须具备 CSRF 防护。 | 人工检查 `CsrfFilter`，运行 `CsrfTokensTest`，抽查 POST 表单和同源 fetch。 | `CsrfFilter`、`CsrfTokens`、`portal_theme.jsp`。 |
| 认证 | 登录成功后必须更换 session id。 | 代码审查 `LoginServlet`，后续可补 servlet 测试。 | `LoginServlet` 调用 `changeSessionId()`。 |
| 授权 | 管理员入口和敏感操作必须做服务端角色校验。 | 静态搜索 `sendError(SC_FORBIDDEN)`、`UserRole`，人工审查关键 Servlet。 | `/admin/database`、`/admin/users`、AI MO 端点。 |
| 上传 | 简历文件最大 15MB，请求最大 20MB，只允许白名单扩展名、MIME 和文件头。 | 运行 `ResumeFileUploadTest`；人工上传伪装文件。 | `ResumeFileUpload`、`@MultipartConfig`。 |
| AI 隐私 | 每次向 Qwen 发送个人数据前必须有用户同意，并记录审计事件。 | 运行 `AiRequestGuardTest`；检查 AI 请求是否携带 `aiConsent=true`。 | `AiRequestGuard`、`AI_REQUEST` audit。 |
| 可用性 | Qwen API 不可用时核心招聘流程仍可使用。 | 移除 `QWEN_API_KEY` 后运行页面人工验收；`mvn test`。 | UI 禁用 AI 按钮；持久化匹配使用规则兜底。 |
| 可靠性 | JSON 文件写入必须避免半写文件。 | 运行 `JsonTableStoreTest`；审查 atomic move 实现。 | `AtomicJsonFileWriter`。 |
| 数据一致性 | 当前只承诺单 JVM 内互斥写入，不承诺跨文件事务回滚。 | 审查 `FileTaDatabase.executeAtomically`；部署时确认单实例。 | `docs/guides/deployment.md`。 |
| 兼容性 | Java 17、Maven、Jakarta Servlet 6、Tomcat 10+。 | `mvn test`、`mvn package`、Tomcat 手工启动。 | `pom.xml`、README。 |
| 性能 | 课程演示数据量下页面应在本机 2 秒内完成主要请求；大文件上传受 15MB 限制。 | 浏览器人工验收；后续可补 JMeter/Playwright 测量。 | 当前未建立自动性能基线。 |

## 验收命令

```bash
mvn test
mvn clean package
```

## 后续增强

- 为关键页面增加 Playwright 冒烟测试。
- 引入依赖漏洞检查和静态分析。
- 当多实例部署或更高一致性成为需求时，迁移到事务型数据库。

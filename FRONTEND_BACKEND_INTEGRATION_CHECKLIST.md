# Frontend-Backend Integration Checklist

更新时间：2026-05-02

关联文件：
- `FRONTEND_BACKEND_INTERFACE_CONTRACT.md`
- `src/main/webapp/WEB-INF/jsp/components/header.jsp`
- `src/main/webapp/WEB-INF/jsp/components/sidebar.jsp`
- `src/main/webapp/portal/dashboard.jsp`
- `src/main/webapp/portal/messages.jsp`
- `src/main/webapp/portal/settings.jsp`

## 1. 当前状态概览

- 前端 JSP 页面、公共提示组件、空状态组件、header/sidebar、portal 样式已经整理完。
- 当前 Java Web 层已经有大部分 portal 路由和 `AuthFilter` 共享注入逻辑；不再是只有基础 filter 的阶段。
- `settings` 已经接到真实 servlet，当前支持资料更新、改密码、语言/外观偏好。
- 当前最大的联调缺口仍然是 `messages`，因为项目里还没有对应的 `MessagesServlet`。

### 1.1 升级文档与代码现实差异

以下几项当前需要按“代码现实”理解，而不是只按升级说明文档理解：

- `/db-demo` 当前仍然存在，`DbDemoServlet` 和 `DbDemoService` 也仍在仓库中；它更像数据库能力演示入口，不应被当作正式业务联调基线。
- `messages` 仍然没有后端 servlet；因此虽然数据库模块已经升级，消息链路依然未闭环。
- `settings` 已经进一步扩展到资料更新、改密码、语言/外观偏好，范围比早期 checklist 和部分口头说明更大。

联调时建议优先以当前代码和本 checklist 为准，再回头校正文档。

## 2. 全局联调结论

### 2.1 路由命名

前端当前已经统一改为以下路由，不再直接访问 `/portal/*.jsp`：

| 页面/动作 | 方法 | 前端当前使用的路由 | 状态 |
|---|---|---|---|
| Login page | `GET` | `/login` | 已实现 |
| Login action | `POST` | `/login` | 已实现 |
| Logout action | `POST` | `/logout` | 已实现，前端固定为 `POST` |
| Dashboard | `GET` | `/dashboard` | 已实现 |
| Vacancies | `GET` | `/vacancies` | 已实现 |
| Vacancy detail | `GET` | `/vacancy` | 已实现 |
| Submit application | `POST` | `/application` | 已实现 |
| Applications | `GET` | `/applications` | 已实现 |
| Resumes page | `GET` | `/resumes` | 已实现 |
| Resumes upload | `POST` | `/resumes` | 已实现 |
| Messages page | `GET` | `/messages` | 前端已就绪，后端待实现 |
| Messages send action | `POST` | `/messages` | 前端已使用，合同已补充，后端待实现 |
| Settings page | `GET` | `/settings` | 已实现 |
| Settings action | `POST` | `/settings` | 已实现 |
| Workloads | `GET` | `/workloads` | 已实现 |

### 2.2 全局提示信息

以下页面已经统一通过共享组件消费反馈信息：

- `login`
- `dashboard`
- `vacancies`
- `vacancy_detail`
- `applications`
- `resumes`
- `messages`
- `settings`

联调要求：

- 后端统一只传 `errorMessage` 和 `successMessage`
- 不要再引入 `msg`、`notice`、`alertText` 之类的新字段名

### 2.3 登录与鉴权

前端假设：

- 除 `/login` 外，portal 页都必须登录后访问
- 未登录用户访问 portal 路由时，后端应重定向到 `/login`
- 可附带 `errorMessage=Please log in to access this page`

当前状态：

- `AuthFilter` 已经负责登录校验和共享 request attribute 注入
- Session 建立与销毁已接入 `LoginServlet` / `LogoutServlet`
- 真正仍待后端补齐的主链路是 `messages`

## 3. Shared Layout 联调检查

`header.jsp` 和 `sidebar.jsp` 是所有 portal 页面共用的，所以联调不能只看单页字段，还要看共享布局字段。

### 3.1 Header 当前需要的展示字段

Header 优先读取这些字段：

- `userProfile.firstName`
- `userProfile.lastName`
- `userProfile.department`

Header 兼容读取这些 fallback 字段：

- `userName`
- `userRoleLabel`
- `userSecondaryLabel`
- `userAvatarUrl`

联调建议：

- 所有已登录页面统一提供一个简化版 `userProfile`
- 至少保证 `firstName` / `lastName` / `department` 三项有值
- `userAvatarUrl` 可选，没有时前端会自动用首字母头像

如果后端不提供这些字段：

- header 会退回到默认文案 `Student User`
- 二级信息会退回到默认文案 `TA Applicant`

### 3.2 Sidebar 当前需要的字段

Sidebar 会读取：

- `profileCompletionPercentage`

用途：

- 显示左下角 profile strength 进度条

当前风险：

- 合同里只在 Dashboard 明确写了 `profileCompletionPercentage`
- 但 `sidebar.jsp` 是所有 portal 页都共用的
- 如果后端在 `applications/resumes/messages/settings/vacancies` 不传这个字段，侧边栏会默认显示 `0%`

联调建议：

- 统一把 `profileCompletionPercentage` 作为所有已登录页面的共享 attribute

### 3.3 Sidebar active 判断依据

当前前端实现：

- 通过 `requestURI` 判断 active
- 不需要后端额外传 `activeTab` 一类 attribute

后端要求：

- 只要请求真正走 `/dashboard`、`/messages`、`/settings` 这些标准路由即可

## 4. Dashboard 联调检查

### 4.1 必传 attributes

- `userName`
- `savedResumesCount`
- `submittedApplicationsCount`
- `underReviewApplicationsCount`
- `recentActivities`
- `upcomingDeadlines`
- `profileCompletionPercentage`
- `errorMessage` optional
- `successMessage` optional

### 4.2 支持状态

- `normal`
- `emptyActivities`
- `emptyDeadlines`
- `loadError`

### 4.3 联调注意点

- `recentActivities` 为空时，页面会自动显示空状态
- `upcomingDeadlines` 为空时，页面会自动显示空状态
- 若要显示完整 header/sidebar，建议同时提供共享布局字段

## 5. Messages 联调检查

### 5.1 当前页面约定的 canonical 字段

会话列表和当前会话请优先按下面字段返回：

- `conversations`
- `activeConversation`
- `conversationId`
- `contactName`
- `contactRole`
- `contactAvatar`
- `lastMessage`
- `lastMessageTime`
- `unreadCount`
- `messages`
- `messageId`
- `content`
- `timestamp`
- `isMine`

### 5.2 支持状态

- `normal`
- `empty`
- `noActiveConversation`
- `loadError`

### 5.3 当前动作约定

1. `GET /messages` 仍需后端按 canonical 字段提供 `conversations`、`activeConversation` 和 `pageState`
2. `POST /messages` 已补进合同：
   - 路由：`POST /messages`
   - 参数：`conversationId`、`messageContent`
3. 返回策略建议固定为：
   - 成功后 redirect 到 `/messages?conversationId=...&successMessage=...`
   - 失败时 redirect 到 `/messages?conversationId=...&errorMessage=...`

### 5.4 当前页面里的兼容 fallback

为了兼容旧数据，JSP 还会 fallback 读取：

- `id`
- `otherUserName`
- `lastMessagePreview`
- `time`
- `isFromCurrentUser`

联调建议：

- 后端正式接线时只提供 canonical 字段
- 不要继续扩散旧字段名

### 5.5 额外可选字段

当前 JSP 还判断了：

- `msg.isSystemMessage`

说明：

- 这个字段当前不是强制字段
- 如果后端不传，页面也能工作
- 如果后端后续要支持系统消息，建议把它补进合同并定义为 optional boolean

## 6. Settings 联调检查

### 6.1 必传 attributes

- `userProfile`
- `pageState`
- `errorMessage` optional
- `successMessage` optional

### 6.2 `userProfile` 当前需要的字段

- `fullName`
- `phone`
- `firstName`
- `lastName`
- `email`
- `studentId`
- `department`
- `bio`
- `notificationsEnabled`
- `preferredLanguage`
- `preferredAppearance`

### 6.3 支持状态

- `normal`
- `updateSuccess`
- `updateFailure`
- `pwdSuccess`
- `pwdFailure`
- `prefSuccess`
- `prefFailure`
- `loadError`

### 6.4 当前实现范围（已确认保留）

当前代码实现已经包含：

- 资料编辑表单
- 偏好设置表单
- 改密码表单
- `POST /settings` 下的 `updateProfile` / `changePassword` / `updatePreferences`
- 对应的 `updateSuccess` / `updateFailure` / `pwdSuccess` / `pwdFailure` / `prefSuccess` / `prefFailure` 状态

### 6.5 当前展示行为

- `loadError` 会渲染独立的错误状态卡片
- 其余成功/失败状态继续渲染正常页面，并通过 `flash_messages` 展示 `errorMessage` / `successMessage`

### 6.6 本轮联调需要确认的点

- `GET /settings` 返回的 `userProfile` 是否包含 `preferredLanguage` / `preferredAppearance`
- `POST /settings` 的 `action=updatePreferences` 是否和合同保持一致
- `prefSuccess` / `prefFailure` 的 redirect 参数是否稳定
- 语言切换后 `header/sidebar/settings` 是否都跟着当前 session 语言刷新

联调建议：

- 这一轮已经确认保留“可编辑资料 + 改密码”
- 前后端联调时直接按当前实现对齐，不再按只读 baseline 理解
- 合同需要同步覆盖 `POST /settings`、`changePassword` 和新增状态，避免后续再出现范围误解

## 7. 额外合同缺口

### 7.1 Applications 页的 department 与列表状态

已对齐方案（见 `FRONTEND_BACKEND_INTERFACE_CONTRACT.md` §4.2）：

- `Application.department`（optional）：有值则表格展示；无值时前端显示 `—`，不再使用硬编码院系。
- `Application.vacancyId`（optional）：有值时「Actions」列为指向 `/vacancy?vacancyId=...` 的链接；无值时退化为指向 `/vacancies`。
- `pageState`：`empty` / `noFilterResults` / `loadError` 与 JSP 一致；未传 `pageState` 时，空列表下若存在 `keyword`、`status` 或 `date=oldest` 等筛选，JSP 会按「无筛选结果」文案展示，并提供「Clear filters」链到 `GET /applications`。

联调时后端请尽量下发 `department`、`vacancyId` 与明确的 `pageState`，以减少与启发式不一致的情况。

### 7.2 Resumes 上传（POST）

- 路由：`POST /resumes`
- `Content-Type`：`multipart/form-data`
- 文件字段名：**`resumeFile`**（与 `resumes.jsp` 一致）
- 建议成功：`redirect` 到 `GET /resumes` 并带 `successMessage`（及可选 `pageState=uploadSuccess`）
- 建议失败：同上并带 `errorMessage`（及可选 `pageState=uploadFailure`）
- 下载、删除、设默认、标签编辑等按钮在前端已 `disabled`，待后续合同与接口再启用

### 7.3 Logout 方法

当前前端已经统一改成：

- `POST /logout`

联调建议：

- 后端按 `POST` 实现即可
- 不建议再保留 `GET /logout` 作为主流程

## 8. 联调前 Done Checklist

联调前至少确认下面这些点：

- [ ] 后端已实现 `/login`、`/logout`、`/dashboard`、`/vacancies`、`/vacancy`、`/application`、`/applications`（含 `GET` 与筛选）、`POST /resumes`（上传）、`GET /resumes`、`/messages`、`/settings`
- [ ] 合同已补充 `POST /messages`
- [ ] 所有页面统一使用 `errorMessage` / `successMessage`
- [ ] 所有已登录页面统一提供 header 所需用户展示字段
- [ ] 所有已登录页面统一提供 `profileCompletionPercentage`，避免 sidebar 退回 `0%`
- [ ] Messages 最终字段命名不再使用旧 fallback 字段名
- [x] Settings 本轮范围已确认：保留当前可编辑实现
- [ ] Applications 列表已按合同提供 `department`（可空）、`vacancyId`（可空）及 `pageState`（建议）
- [ ] 未登录访问 portal 页面时的 redirect 行为已实现

## 9. 建议发给后端同学的最短结论

可以直接同步下面这段：

> 前端 JSP 已经按 `/dashboard`、`/applications`、`/resumes`、`/vacancies`、`/vacancy`、`/messages`、`/settings` 这些标准路由整理完成，不再直接走 `/portal/*.jsp`。  
> 现在联调前需要后端统一补 servlet 路由、按合同提供 request attributes，并额外确认三件事：  
> 1. `POST /messages` 发送消息动作要不要写进合同；  
> 2. 所有已登录页面是否统一提供 header/sidebar 所需共享字段；  
> 3. `POST /resumes`（字段名 `resumeFile`）与上传成功/失败后的 redirect 策略；`Application` 是否下发 `department`、`vacancyId` 与 `pageState`。

## 10. 申请 / 简历模块（前端负责范围）需同步给后端的事项

以下条目对应 **Applications 列表页**与 **Resumes 管理页** 的 JSP 与 `FRONTEND_BACKEND_INTERFACE_CONTRACT.md` 约定，联调前请后端同学按清单实现或确认；细节以合同正文为准。

### 10.1 路由与 HTTP 方法

| 路由 | 方法 | 说明 |
|------|------|------|
| `/applications` | `GET` | 渲染申请列表；支持查询参数 `keyword`、`status`、`date`（与合同 §4.2 一致） |
| `/resumes` | `GET` | 渲染简历列表与管理 UI |
| `/resumes` | `POST` | 上传简历：`multipart/form-data`，文件字段名必须为 **`resumeFile`**（见 §7.2、合同 §5.2） |
| `/application` | `POST` | 提交申请（`vacancyId`、`resumeId`）；成功后跳转 `/applications` 等（合同 §4.1，与 vacancies 流程衔接） |

### 10.2 `GET /applications` 需放入的 request attributes

- **`applications`**：`List`，元素字段至少包含合同中的 `applicationId`、`vacancyTitle`、`courseCode`、`status`、`appliedDate`、`resumeName`。
- **建议补充（合同已写 optional）**：`department`（无则前端显示 `—`）、`vacancyId`（有则「查看职位」链到 `/vacancy?vacancyId=...`，无则链到 `/vacancies`）。
- **建议 `pageState`**：`empty`（从未申请）、`noFilterResults`（有筛选但结果为空）、`loadError`（加载失败）；未传时前端会用查询参数做启发式判断，易与后端语义不一致，**建议后端显式下发**。
- **`errorMessage` / `successMessage`**：与其它 portal 页相同，全局统一命名。

### 10.3 `GET /resumes` / `POST /resumes` 需约定行为

- **`GET /resumes`**：`resumes` 列表元素为合同 **`ResumeDetail`**（`resumeId`、`resumeName`、`uploadDate`、`fileSize`、`isDefault`、`tags`、`activeApplicationsCount`）；`tags` 建议永不为 `null`（空则给空列表）。
- **`pageState`**：`normal`、`empty`、`uploadSuccess`、`uploadFailure`、`loadError` 等与 JSP 一致（见合同 §5.1）。
- **`POST /resumes` 成功后**：`redirect` 到 `GET /resumes`，并带上 **`successMessage`**（及可选 **`pageState=uploadSuccess`**）；失败则 **`errorMessage`**（及可选 **`pageState=uploadFailure`**）。
- 下载、删除、设默认、标签编辑等 **前端当前为 disabled**，后端可暂不实现；若提前实现，需另开合同版本再改前端。

### 10.4 与其它同学页面的共享依赖（申请/简历页也会用到）

- 所有已登录页（含 `/applications`、`/resumes`）：为 **header** 提供 `userProfile`（至少 `firstName` / `lastName` / `department`）或合同中的 fallback 字段；为 **sidebar** 提供 **`profileCompletionPercentage`**，避免进度条长期为 `0%`。
- 未登录访问上述路由时：**重定向到 `/login`**，可带 `errorMessage`（全局规则，见 §2.3）。

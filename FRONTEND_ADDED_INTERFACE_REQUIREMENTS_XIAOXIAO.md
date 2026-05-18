# Frontend Added Interface Requirements - Xiaoxiao Ma

更新时间：2026-05-15

用途：给后端同学或 AI 快速读取。本文只列前端收尾后新增或需要重点保持一致的接口需求，不替代完整合同 `FRONTEND_BACKEND_INTERFACE_CONTRACT.md`。

## 0. 总原则

- 本项目是 JSP + Servlet 服务端渲染，不要求新增独立 JSON API。
- 页面通过 Servlet `forward` 到 JSP，前端主要依赖 request attributes。
- 所有登录后 portal 页面都必须经过 `AuthFilter`，并提供 header/sidebar 共享字段。
- 全局反馈字段统一使用 `successMessage` 和 `errorMessage`。
- 页面状态字段统一使用 `pageState`，状态名使用 camelCase。

## 1. 全局共享字段

适用页面：

- `/dashboard`
- `/applications`
- `/resumes`
- `/vacancies`
- `/vacancy`
- `/vacancy/edit`
- `/messages`
- `/settings`
- `/workloads`
- `/admin/skills`

后端需要保证：

| Attribute | Type | Required | Description |
|---|---|---:|---|
| `userRole` | String | yes | `TA` / `MO` / `ADMIN`，用于 header/sidebar 和页面条件渲染 |
| `userProfile` | Map/Object | yes | Header、settings 页面使用的用户资料 |
| `profileCompletionPercentage` | Integer | yes | Sidebar 资料完成度，缺失时页面会退回 `0%` |
| `language` | String | yes | `en` / `zh` |
| `appearance` | String | yes | `light` / `dark` |
| `successMessage` | String | optional | 成功提示 |
| `errorMessage` | String | optional | 失败提示 |

`userProfile` 推荐字段：

| Field | Type | Description |
|---|---|---|
| `firstName` | String | Header 显示名 |
| `lastName` | String | Header 显示名 |
| `fullName` | String | Settings 表单 |
| `email` | String | Settings 只读邮箱 |
| `phone` | String | Settings 表单 |
| `department` | String | Header 二级信息、Settings 表单 |
| `studentId` | String | Settings 表单 |
| `bio` | String | Settings 表单 |
| `notificationsEnabled` | Boolean | Settings checkbox |
| `preferredLanguage` | String | `en` / `zh` |
| `preferredAppearance` | String | `light` / `dark` |

## 2. Applications - TA/MO 双角色页面

### 2.1 GET `/applications`

JSP：`/portal/applications.jsp`

Servlet：`ApplicationsServlet`

Query parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `keyword` | String | no | 搜索岗位标题、课程代码、院系 |
| `status` | String | no | 申请状态筛选 |
| `date` | String | no | `newest` / `oldest` |

角色语义必须区分：

| Role | Data Meaning |
|---|---|
| `TA` | 显示当前 TA 自己提交的申请，即按当前用户的 resumeIds 过滤 applications |
| `MO` | 显示投递到当前 MO 发布岗位上的申请，即按当前用户发布的 jobIds 过滤 applications |

Request attributes：

| Attribute | Type | Required | Description |
|---|---|---:|---|
| `applications` | List<ApplicationDTO> | yes | 申请列表 |
| `pageState` | String | yes | `normal` / `empty` / `noFilterResults` / `loadError` |
| `moRankJobOptions` | List<MoJobRankOption> | MO only | MO AI applicant rank 下拉选项 |
| `qwenConfigured` | Boolean | optional | 控制 AI 按钮可用态 |
| `successMessage` | String | optional | 成功提示 |
| `errorMessage` | String | optional | 失败提示 |

`ApplicationDTO` 字段：

| Field | Type | Required | Description |
|---|---|---:|---|
| `applicationId` | String/UUID | yes | 申请 ID |
| `vacancyId` | String/UUID | yes | 跳转 `/vacancy?vacancyId=...` |
| `vacancyTitle` | String | yes | 岗位标题 |
| `courseCode` | String | yes | 课程代码 |
| `department` | String | optional | 缺失时前端显示占位符 |
| `status` | String | yes | 展示态状态 |
| `appliedDate` | String | yes | 建议 `yyyy-MM-dd` |
| `resumeName` | String | yes | 简历名称 |
| `resumeId` | String/UUID | yes | AI draft / 关联简历使用 |

状态值需要覆盖：

- `Submitted`
- `Under Review`
- `Offer Pending`
- `Accepted`
- `Rejected`
- `Declined`
- `Withdrawn`

### 2.2 POST `/application`

JSP 来源：`vacancy_detail.jsp` 申请表单

Parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `vacancyId` | String/UUID | yes | 岗位 ID |
| `resumeId` | String/UUID | yes | 简历 ID |

Redirect behavior：

| Result | Redirect |
|---|---|
| success | `/applications?successMessage=...` |
| failure | `/vacancy?vacancyId=...&errorMessage=...` |

## 3. Vacancy Edit - MO owner 修改岗位

### 3.1 GET `/vacancy/edit`

JSP：`/portal/vacancy_edit.jsp`

Servlet：`VacancyEditServlet`

权限：

- 仅 `MO` 可访问。
- 仅岗位 owner 可编辑。
- 非 owner 或非 MO 应返回 403 或等价拒绝访问。

Query parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `vacancyId` | String/UUID | yes | 被编辑岗位 |
| `returnTo` | String | no | `detail` / `list`，控制保存后的返回位置 |

Request attributes：

| Attribute | Type | Required | Description |
|---|---|---:|---|
| `editVacancyId` | String | yes | hidden input |
| `editTitle` | String | yes | 标题 |
| `editCourseCode` | String | yes | 课程代码 |
| `editDescription` | String | yes | 描述 |
| `editHoursPerWeek` | Integer | yes | 每周小时数 |
| `editHourlyRate` | String/Decimal | yes | 时薪 |
| `editDeadline` | String | yes | `yyyy-MM-dd'T'HH:mm` |
| `editTerm` | String | optional | 如 `Spring 2026` / `Fall 2026` |
| `editStatus` | String | yes | `OPEN` / `CLOSED` 等后端枚举 |
| `editLabels` | String | optional | 逗号分隔标签 |
| `termOptions` | List<String> | yes | 学期下拉选项 |
| `editReturnTo` | String | yes | `detail` / `list` |
| `pageState` | String | yes | `normal` |

### 3.2 POST `/vacancy/edit`

Parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `vacancyId` | String/UUID | yes | 岗位 ID |
| `title` | String | yes | 标题 |
| `courseCode` | String | optional | 课程代码 |
| `description` | String | optional | 描述 |
| `hoursPerWeek` | Integer | optional | 每周小时数 |
| `hourlyRate` | Decimal | optional | 时薪 |
| `deadline` | String | optional | `yyyy-MM-dd'T'HH:mm` 或可被后端 normalize |
| `term` | String | optional | 学期 |
| `status` | String | optional | 岗位状态，前端不应允许直接取消岗位 |
| `labels` | String | optional | 逗号、分号、竖线、换行分隔 |
| `returnTo` | String | optional | `detail` / `list` |

Redirect behavior：

| `returnTo` | Success Redirect |
|---|---|
| `detail` or empty | `/vacancy?vacancyId=...&successMessage=...` |
| `list` | `/vacancies?successMessage=...` |

Failure redirect：

- `/vacancy/edit?vacancyId=...&errorMessage=...&returnTo=...`

## 4. Messages - 会话列表和发送消息

### 4.1 GET `/messages`

JSP：`/portal/messages.jsp`

Servlet：`MessagesServlet`

Query parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `conversationId` | String | no | 当前选中的会话 |

Request attributes：

| Attribute | Type | Required | Description |
|---|---|---:|---|
| `conversations` | List<ConversationDTO> | yes | 左侧会话列表 |
| `activeConversation` | ConversationDTO | only when selected | 当前会话详情 |
| `pageState` | String | yes | `normal` / `empty` / `noActiveConversation` / `loadError` |
| `successMessage` | String | optional | 成功提示 |
| `errorMessage` | String | optional | 失败提示 |

`ConversationDTO` 字段：

| Field | Type | Required | Description |
|---|---|---:|---|
| `conversationId` | String | yes | 会话 ID |
| `contactName` | String | yes | 联系人名称 |
| `contactRole` | String | optional | 联系人角色 |
| `contactAvatar` | String | optional | 头像 URL |
| `lastMessage` | String | optional | 最后一条消息摘要 |
| `lastMessageTime` | String | optional | 展示时间 |
| `unreadCount` | Integer | yes | 未读数量 |
| `messages` | List<MessageDTO> | active only | 当前会话消息 |

`MessageDTO` 字段：

| Field | Type | Required | Description |
|---|---|---:|---|
| `messageId` | String | optional | 消息 ID |
| `content` | String | yes | 消息正文 |
| `timestamp` | String | yes | 展示时间 |
| `isMine` | Boolean | yes | 是否当前用户发送 |
| `isSystemMessage` | Boolean | optional | 是否系统消息 |

### 4.2 POST `/messages`

Parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `conversationId` | String | yes | 当前会话 |
| `messageContent` | String | yes | 消息正文 |

Redirect behavior：

| Result | Redirect |
|---|---|
| success | `/messages?conversationId=...&successMessage=...` |
| failure | `/messages?conversationId=...&errorMessage=...` |

## 5. Settings - 个人设置三组表单

### 5.1 GET `/settings`

JSP：`/portal/settings.jsp`

Request attributes：

| Attribute | Type | Required | Description |
|---|---|---:|---|
| `userProfile` | Map/Object | yes | 见第 1 节 |
| `pageState` | String | yes | 见下方状态 |
| `successMessage` | String | optional | 成功提示 |
| `errorMessage` | String | optional | 失败提示 |

Supported `pageState`：

- `normal`
- `updateSuccess`
- `updateFailure`
- `pwdSuccess`
- `pwdFailure`
- `prefSuccess`
- `prefFailure`
- `loadError`

### 5.2 POST `/settings` - updateProfile

Parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `action` | String | yes | `updateProfile` |
| `fullName` | String | yes | 姓名 |
| `phone` | String | optional | 电话 |
| `department` | String | optional | 院系 |
| `studentId` | String | optional | 学号 |
| `bio` | String | optional | 个人简介 |
| `notificationsEnabled` | String | optional | checkbox 选中时传 `on` |

Redirect behavior：

- Success: `/settings?state=updateSuccess&successMessage=...`
- Failure: `/settings?state=updateFailure&errorMessage=...`

### 5.3 POST `/settings` - changePassword

Parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `action` | String | yes | `changePassword` |
| `currentPassword` | String | yes | 当前密码 |
| `newPassword` | String | yes | 新密码 |
| `confirmPassword` | String | yes | 确认新密码 |

Redirect behavior：

- Success: `/settings?state=pwdSuccess&successMessage=...`
- Failure: `/settings?state=pwdFailure&errorMessage=...`

### 5.4 POST `/settings` - updatePreferences

Parameters：

| Parameter | Type | Required | Description |
|---|---|---:|---|
| `action` | String | yes | `updatePreferences` |
| `preferredLanguage` | String | yes | `en` / `zh` |
| `preferredAppearance` | String | yes | `light` / `dark` |

Redirect behavior：

- Success: `/settings?state=prefSuccess&successMessage=...`
- Failure: `/settings?state=prefFailure&errorMessage=...`

## 6. Page State 要求

后端应尽量显式设置 `pageState`，不要完全依赖 JSP 根据空列表推断。

| Page | Supported States |
|---|---|
| `/applications` | `normal`, `empty`, `noFilterResults`, `loadError` |
| `/messages` | `normal`, `empty`, `noActiveConversation`, `loadError` |
| `/settings` | `normal`, `updateSuccess`, `updateFailure`, `pwdSuccess`, `pwdFailure`, `prefSuccess`, `prefFailure`, `loadError` |
| `/vacancy` | `normal`, `loadError` |
| `/vacancies` | `normal`, `empty`, `loadError` |
| `/workloads` | `normal`, `empty`, `loadError` |

## 7. 明确不属于本文件的内容

- 不要求新增 REST JSON API。
- 不要求改变现有业务路由。
- 不要求前端直接读写数据库。
- `/db-demo` 不是正式业务联调入口。
- AI 相关按钮是否可用由 `qwenConfigured` 和后端 AI servlet 控制，本文只列页面字段依赖。

## 8. 当前本地验证结果

- `mvn clean package` 已通过。
- 本地 Tomcat 11 smoke test 已通过。
- TA/MO/ADMIN 核心页面均返回 200。
- MO `/applications` 已能显示投递到自己岗位的申请。
- MO owner `/vacancy/edit?vacancyId=...` 已能打开并带出岗位字段。
- `POST /messages` 已能发送并回显。

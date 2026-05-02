# Frontend-Backend Interface Contract

This document outlines the standardized interface contract between the frontend JSP pages and the backend Java Servlets.

**设计意图（为何需要这份契约）**  
前后端并行开发时，若各自约定 route、HTTP method、request parameter / attribute 名称，联调阶段会出现「Servlet 读了 A 字段、JSP 写了 B 字段」这类隐性 bug。统一命名与入口，可以让接口讨论集中在契约文档上，而不是散落在聊天记录里。  
`errorMessage` / `successMessage` 作为唯一反馈通道，是为了让 JSP 里已有的一套 flash / 提示组件可复用：后端无论 forward 还是 redirect，只要写入这两个 attribute（或按约定带 query），前端展示路径一致，避免每种操作各发明一种提示字段。  
注入 `userRole` 是为了在 **view 层** 做轻量条件渲染（导航、按钮显隐）：真正的权限校验仍必须在 Servlet / Service；JSP 只根据角色决定「给用户看什么」，不替代鉴权。  
区分 **TA / MO / ADMIN** 是因为业务职责不同：助教侧重申请与简历，模块负责人侧重发布与维护岗位，管理员侧重全局工作量视图；三类角色在同一套页面骨架上走不同入口，契约里写清楚可减少误用接口。  
每条能力拆成 **Route URL**（浏览器与表单 action）、**JSP View File**（实际渲染文件）、**Servlet**（处理类）三列，是因为一次用户操作往往「URL → Filter → Servlet → forward 到 JSP」链路较长；拆开写便于查映射、也方便新人对照 `web.xml` / `@WebServlet`。**Supported Page States** 列出页面在数据为空、加载失败、资源不存在等下的 UI 分支名，便于与 JSP 里 `pageState` / 条件标签对齐，减少「状态名口头约定」的歧义。

## Global Rules

### A. Global Naming Rules
统一列出全项目关键命名约定，确保前后端字段一致：
- `email`: User's email address
- `password`: User's password
- `userRole`: User's role in the system (`TA`, `MO`, `ADMIN`)
- `vacancyId`: Unique identifier for a vacancy
- `applicationId`: Unique identifier for an application
- `resumeId`: Unique identifier for a resume
- `conversationId`: Unique identifier for a message conversation
- `keyword`: Search query string
- `department`: Department filter value
- `term`: Academic term filter value
- `status`: Status filter or state value
- `errorMessage`: Standard attribute for error feedback
- `successMessage`: Standard attribute for success feedback

### B. Global Message Passing Rules
- 所有页面统一使用 `errorMessage` 和 `successMessage` 作为 Request Attributes 传递反馈信息（与 § 开头说明一致：统一通道便于 JSP 组件化展示与联调排查）。
- `errorMessage` (String): 用于显示错误警告（例如：“Invalid credentials”、“Failed to load data”）。
- `successMessage` (String): 用于显示成功提示（例如：“Application submitted successfully”、“Profile updated”）。
- JSP 页面中已内置对应的 UI 组件，当这些 attribute 不为空时会自动显示。

### C. Global Authentication & Authorization Rules
- **Authentication**: 除 Login 页面外的所有页面（Dashboard, Vacancies, Applications, Resumes, Messages, Settings）均需要用户登录。未登录时建议后端 Servlet 拦截未认证的请求，并重定向（Redirect）到 `/login` 页面，可附带 `errorMessage` 提示“Please log in to access this page”。
- **Authorization (RBAC)**: 系统包含三种角色：`TA` (Teaching Assistant), `MO` (Module Organiser), `ADMIN` (Administrator)。
  - **TA**: 可以浏览职位、提交申请、管理简历。
  - **MO**: 可以发布职位 (Vacancy)、修改职位详情。
  - **ADMIN**: 可以查看 TA 的整体工作负载 (Overall Workload)。
- 所有需要登录的页面，后端都应在 Request Attributes 中注入 `userRole` (String)，以便前端根据角色渲染不同的导航栏或操作按钮。

### D. Global Page State Rules
- 状态命名统一风格：使用 camelCase 命名状态（如 `normal`, `empty`, `notFound`）。在契约中列出 **Supported Page States** 是为了让后端设置 `pageState`（或等价 attribute）时与 JSP 分支一一对应，避免「口头约定」导致空状态/错误态串线。
- JSP 页面如何基于状态和 attribute 展示不同内容：JSP 页面主要通过 JSTL 的 `<c:choose>`, `<c:when>`, `<c:if>` 标签，根据 Request Attributes 的值（如列表是否为空、对象是否为 null）来决定渲染哪个状态的 UI。

---

## 1. Authentication

### 1.1. Login Page
1. **Page Name:** Login Page
2. **Route URL:** `/login`
3. **JSP View File:** `/login.jsp`
4. **Servlet:** `LoginServlet`
5. **Authentication Required:** No
6. **Method:** `GET`
7. **Description:** 渲染用户登录页面。
8. **Request Parameters:** None
9. **Request Attributes:**
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** None
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `logoutSuccess`, `authenticationFailed`
14. **Main Functionalities:** 提供用户输入邮箱和密码的表单。

### 1.2. Login Action
1. **Page Name:** Login Action
2. **Route URL:** `/login`
3. **JSP View File:** None (Redirects)
4. **Servlet:** `LoginServlet`
5. **Authentication Required:** No
6. **Method:** `POST`
7. **Description:** 处理用户登录请求进行身份验证。
8. **Request Parameters:**
   - `email` (String, required)
   - `password` (String, required)
9. **Request Attributes:** None
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect to `/dashboard`
12. **Failure Behavior:** Forward or Redirect to `/login` with `errorMessage`
13. **Supported Page States:** N/A
14. **Main Functionalities:** 验证凭据并建立用户会话。

### 1.3. Logout Action
1. **Page Name:** Logout Action
2. **Route URL:** `/logout`
3. **JSP View File:** None (Redirects)
4. **Servlet:** `LogoutServlet`
5. **Authentication Required:** Yes
6. **Method:** `POST` or `GET`
7. **Description:** 结束当前用户会话。
8. **Request Parameters:** None
9. **Request Attributes:** None
10. **Form Submission:** None
11. **Success Behavior:** Redirect to `/login` with `successMessage`
12. **Failure Behavior:** N/A
13. **Supported Page States:** N/A
14. **Main Functionalities:** 清除 Session 数据并登出用户。

---

## 2. Dashboard

### 2.1. Dashboard Page
1. **Page Name:** Dashboard Page
2. **Route URL:** `/dashboard`
3. **JSP View File:** `/portal/dashboard.jsp`
4. **Servlet:** `DashboardServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染用户主控制台，展示统计数据、近期活动和截止日期。
8. **Request Parameters:** None
9. **Request Attributes:**
   - `userName` (String): 用户名
   - `userRole` (String): 用户角色 (`TA`, `MO`, `ADMIN`)
   - `savedResumesCount` (Integer): 已保存简历数量 (TA)
   - `submittedApplicationsCount` (Integer): 已提交申请数量 (TA)
   - `underReviewApplicationsCount` (Integer): 审核中申请数量 (TA)
   - `postedVacanciesCount` (Integer): 已发布职位数量 (MO)
   - `receivedApplicationsCount` (Integer): 收到的申请数量 (MO)
   - `totalTAsCount` (Integer): 系统中 TA 的总数 (ADMIN)
   - `activeVacanciesCount` (Integer): 活跃职位总数 (ADMIN)
   - `recentActivities` (List<Activity>): 近期活动列表
   - `upcomingDeadlines` (List<Deadline>): 即将到期的截止日期列表
   - `profileCompletionPercentage` (Integer): 资料完成度百分比
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** None
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `emptyActivities`, `emptyDeadlines`, `loadError`
14. **Main Functionalities:** 提供用户学术旅程的概览。
15. **Object Structure:**
    - **`Activity`**
      - `title` (String)
      - `description` (String)
      - `timeAgo` (String)
      - `icon` (String)
      - `iconColorClass` (String)
      - `iconBgClass` (String)
      - `statusBadge` (String, optional)
      - `statusBadgeClass` (String, optional)
    - **`Deadline`**
      - `title` (String)
      - `timeRemaining` (String)
      - `colorClass` (String)
      - `textColorClass` (String)

---

## 3. Vacancies

### 3.1. Vacancies List Page
1. **Page Name:** Vacancies List Page
2. **Route URL:** `/vacancies`
3. **JSP View File:** `/portal/vacancies.jsp`
4. **Servlet:** `VacanciesServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染可用助教职位列表，支持搜索和过滤。MO 角色可看到“发布职位”按钮。
8. **Request Parameters:**
   - `keyword` (String, optional): 搜索关键词
   - `department` (String, optional): 部门过滤
   - `term` (String, optional): 学期过滤
9. **Request Attributes:**
   - `userRole` (String): 用户角色
   - `vacancies` (List<Vacancy>): 职位列表
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** GET form for search/filter
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `empty`, `noSearchResults`, `loadError`
14. **Main Functionalities:** 浏览和搜索职位。MO 可发起发布职位。
15. **Object Structure:**
    - **`Vacancy`**
      - `vacancyId` (String)
      - `title` (String)
      - `courseCode` (String)
      - `department` (String)
      - `term` (String)
      - `description` (String)
      - `requirements` (List<String>)
      - `responsibilities` (List<String>)
      - `status` (String)
      - `createdAt` (String)
      - `hoursPerWeek` (Integer or String)
      - `hourlyRate` (Double or String)
      - `deadline` (String)
      - `moduleOwner` (String)
      - `isOwner` (Boolean, optional): 标识当前 MO 是否是该职位的发布者

### 3.2. Vacancy Detail Page
1. **Page Name:** Vacancy Detail Page
2. **Route URL:** `/vacancy`
3. **JSP View File:** `/portal/vacancy_detail.jsp`
4. **Servlet:** `VacancyDetailServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染特定职位的详细信息。TA 可申请，MO 如果是 owner 则可编辑。
8. **Request Parameters:**
   - `vacancyId` (String, required)
9. **Request Attributes:**
   - `userRole` (String): 用户角色
   - `vacancy` (Vacancy): 职位详情对象
   - `resumeList` (List<Resume>): 用户的简历列表（用于 TA 申请弹窗）
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** None (The apply modal submits to `/application`, edit modal submits to `/vacancy/edit`)
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `notFound`, `noResumeAvailable`, `applicationSuccess`, `applicationFailure`, `loadError`
14. **Main Functionalities:** 查看职位详情，TA 选择简历并提交申请，MO 编辑职位。
15. **Object Structure:**
    - **`Vacancy`** (Same as above)
    - **`Resume`**
      - `resumeId` (String)
      - `resumeName` (String)

### 3.3. Create Vacancy Action (MO Only)
1. **Page Name:** Create Vacancy Action
2. **Route URL:** `/vacancy/create`
3. **JSP View File:** None (Redirects)
4. **Servlet:** `VacancyCreateServlet`
5. **Authentication Required:** Yes (MO Role)
6. **Method:** `POST`
7. **Description:** MO 发布新的助教职位。
8. **Request Parameters:**
   - `title` (String, required)
   - `courseCode` (String, required)
   - `department` (String, required)
   - `term` (String, required)
   - `description` (String, required)
   - `requirements` (String, required): 可以是换行符分隔的字符串
   - `responsibilities` (String, required): 可以是换行符分隔的字符串
   - `hoursPerWeek` (Integer, required)
   - `hourlyRate` (Double, required)
   - `deadline` (String, required)
9. **Request Attributes:** None
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect to `/vacancies` or `/vacancy?vacancyId={newId}` with `successMessage`
12. **Failure Behavior:** Redirect to `/vacancies` with `errorMessage`
13. **Supported Page States:** N/A
14. **Main Functionalities:** 处理发布新职位的逻辑。

### 3.4. Edit Vacancy Action (MO Only)
1. **Page Name:** Edit Vacancy Action
2. **Route URL:** `/vacancy/edit`
3. **JSP View File:** None (Redirects)
4. **Servlet:** `VacancyEditServlet`
5. **Authentication Required:** Yes (MO Role)
6. **Method:** `POST`
7. **Description:** MO 修改自己发布的职位详情。
8. **Request Parameters:**
   - `vacancyId` (String, required)
   - `title` (String, required)
   - `courseCode` (String, required)
   - `department` (String, required)
   - `term` (String, required)
   - `description` (String, required)
   - `requirements` (String, required)
   - `responsibilities` (String, required)
   - `hoursPerWeek` (Integer, required)
   - `hourlyRate` (Double, required)
   - `deadline` (String, required)
   - `status` (String, required): e.g., 'Open', 'Closed'
9. **Request Attributes:** None
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect to `/vacancy?vacancyId={vacancyId}` with `successMessage`
12. **Failure Behavior:** Redirect to `/vacancy?vacancyId={vacancyId}` with `errorMessage`
13. **Supported Page States:** N/A
14. **Main Functionalities:** 处理职位详情更新的逻辑。

---

## 4. Applications

### 4.1. Submit Application Action
1. **Page Name:** Submit Application Action
2. **Route URL:** `/application`
3. **JSP View File:** None (Redirects)
4. **Servlet:** `ApplicationSubmitServlet`
5. **Authentication Required:** Yes
6. **Method:** `POST`
7. **Description:** 提交针对某个职位的申请。
8. **Request Parameters:**
   - `vacancyId` (String, required)
   - `resumeId` (String, required)
9. **Request Attributes:** None
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect to `/applications` with `successMessage`
12. **Failure Behavior:** Redirect to `/vacancy?vacancyId={vacancyId}` with `errorMessage`
13. **Supported Page States:** N/A
14. **Main Functionalities:** 处理申请提交逻辑。

### 4.2. Applications List Page
1. **Page Name:** Applications List Page
2. **Route URL:** `/applications`
3. **JSP View File:** `/portal/applications.jsp`
4. **Servlet:** `ApplicationsServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染用户的申请历史记录。
8. **Request Parameters:**
   - `keyword` (String, optional)
   - `status` (String, optional)
   - `date` (String, optional)
9. **Request Attributes:**
   - `applications` (List<Application>): 申请列表
   - `pageState` (String, optional): 建议与 JSP 对齐；`empty` 表示无任何申请记录；`noFilterResults` 表示有筛选条件但结果为空；`loadError` 表示加载失败。未设置时 JSP 会根据查询参数做启发式判断
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** GET form for search/filter
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `empty`, `noFilterResults`, `loadError`
14. **Main Functionalities:** 查看和过滤已提交的申请。
15. **Object Structure:**
    - **`Application`**
      - `applicationId` (String)
      - `vacancyTitle` (String)
      - `courseCode` (String)
      - `status` (String)
      - `appliedDate` (String)
      - `resumeName` (String)
      - `department` (String, optional): 所属院系；未提供时前端显示占位符
      - `vacancyId` (String, optional): 用于「查看职位」链接跳转至 `/vacancy?vacancyId=...`

---

## 5. Resumes

### 5.1. Resumes Management Page
1. **Page Name:** Resumes Management Page
2. **Route URL:** `/resumes`
3. **JSP View File:** `/portal/resumes.jsp`
4. **Servlet:** `ResumesServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染简历管理页面。
8. **Request Parameters:** None
9. **Request Attributes:**
   - `resumes` (List<ResumeDetail>): 简历详情列表
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** None（上传见 §5.2）
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `empty`, `uploadSuccess`, `uploadFailure`, `loadError`
14. **Main Functionalities:** 查看、上传和管理简历。
15. **Object Structure:**
    - **`ResumeDetail`**
      - `resumeId` (String)
      - `resumeName` (String)
      - `uploadDate` (String)
      - `fileSize` (String)
      - `isDefault` (Boolean)
      - `tags` (List<String>)
      - `activeApplicationsCount` (Integer)

### 5.2. Resume Upload Action
1. **Page Name:** Resume Upload Action
2. **Route URL:** `/resumes`
3. **JSP View File:** None（处理完成后 Redirect）
4. **Servlet:** `ResumesServlet`（或与列表共用控制器，按 HTTP 方法区分）
5. **Authentication Required:** Yes
6. **Method:** `POST`
7. **Description:** 接收用户上传的简历文件。
8. **Request Parameters:**
   - `resumeFile` (File / Part, required): multipart 表单中的文件字段名，与 `resumes.jsp` 中 `<input name="resumeFile">` 一致
9. **Content-Type:** `multipart/form-data`
10. **Request Attributes:** None（错误/成功信息通过 redirect 的 query 或 flash/session 传递，见下）
11. **Form Submission:** Yes（页面底部与顶栏「Upload Resume」共用同一 file input）
12. **Success Behavior:** Redirect 到 `GET /resumes`，建议附带 `successMessage`（例如 URL 编码后作为 query 参数，或使用 session flash），并可设置 `pageState=uploadSuccess` 供 JSP 展示额外提示条
13. **Failure Behavior:** Redirect 到 `GET /resumes`，附带 `errorMessage`，可选 `pageState=uploadFailure`
14. **Supported Page States:** N/A（动作本身）；目标页见 §5.1
15. **Main Functionalities:** 持久化文件并在列表中展示新简历

---

## 6. Messages

### 6.1. Messages Page
1. **Page Name:** Messages Page
2. **Route URL:** `/messages`
3. **JSP View File:** `/portal/messages.jsp`
4. **Servlet:** `MessagesServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染消息和聊天界面。
8. **Request Parameters:**
   - `conversationId` (String, optional): 当前选中的会话
9. **Request Attributes:**
   - `conversations` (List<Conversation>): 会话列表
   - `activeConversation` (Conversation): 当前激活的会话详情（包含消息）
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** 页面底部发送消息表单会提交到 `POST /messages`
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `empty`, `noActiveConversation`, `loadError`
14. **Main Functionalities:** 查看会话列表和发送消息。
15. **Object Structure:**
    - **`Conversation`**
      - `conversationId` (String)
      - `contactName` (String)
      - `contactRole` (String)
      - `contactAvatar` (String)
      - `lastMessage` (String)
      - `lastMessageTime` (String)
      - `unreadCount` (Integer)
      - `messages` (List<Message>)
    - **`Message`**
      - `messageId` (String)
      - `content` (String)
      - `timestamp` (String)
      - `isMine` (Boolean)
      - `isSystemMessage` (Boolean, optional)

### 6.2. Send Message Action
1. **Page Name:** Messages Send Action
2. **Route URL:** `/messages`
3. **JSP View File:** None（处理完成后 Redirect）
4. **Servlet:** `MessagesServlet`
5. **Authentication Required:** Yes
6. **Method:** `POST`
7. **Description:** 在当前会话中发送一条消息，并回到对应消息页。
8. **Request Parameters:**
   - `conversationId` (String, required): 当前回复的会话 ID
   - `messageContent` (String, required): 发送的消息内容
9. **Request Attributes:** None（反馈信息通过 redirect query 传递）
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect 到 `GET /messages?conversationId=...&successMessage=...`
12. **Failure Behavior:** Redirect 到 `GET /messages?conversationId=...&errorMessage=...`
13. **Supported Page States:** N/A（动作本身）；目标页见 §6.1
14. **Main Functionalities:** 校验会话访问权限，提交消息并刷新当前线程视图。

---

## 7. Settings

### 7.1. Settings Page
1. **Page Name:** Settings Page
2. **Route URL:** `/settings`
3. **JSP View File:** `/portal/settings.jsp`
4. **Servlet:** `SettingsServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染用户设置页面。
8. **Request Parameters:**
   - `state` (String, optional): 由 `POST /settings` redirect 回来时携带，用于标识反馈状态
   - `successMessage` (String, optional): 成功反馈文案
   - `errorMessage` (String, optional): 失败反馈文案
9. **Request Attributes:**
   - `userProfile` (UserProfile): 用户个人资料
   - `pageState` (String, optional): 页面状态，支持 `normal`、`updateSuccess`、`updateFailure`、`pwdSuccess`、`pwdFailure`、`prefSuccess`、`prefFailure`、`loadError`
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** 页面包含资料更新、偏好更新、密码更新三个表单，均提交到 `POST /settings`
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `updateSuccess`, `updateFailure`, `pwdSuccess`, `pwdFailure`, `prefSuccess`, `prefFailure`, `loadError`
14. **Main Functionalities:** 查看和更新个人资料、密码及界面偏好设置。
15. **Object Structure:**
    - **`UserProfile`**
      - `firstName` (String)
      - `lastName` (String)
      - `fullName` (String)
      - `email` (String)
      - `phone` (String)
      - `studentId` (String)
      - `department` (String)
      - `bio` (String)
      - `notificationsEnabled` (Boolean)
      - `preferredLanguage` (String): `en` / `zh`
      - `preferredAppearance` (String): `light` / `dark`
16. **Rendering Notes:**
    - `loadError` 会渲染独立的错误状态卡片。
    - 其余成功/失败状态仍渲染正常页面布局，并通过共享 `flash_messages` 组件展示 `errorMessage` / `successMessage`。

### 7.2. Update Profile Action
1. **Page Name:** Settings Update Profile Action
2. **Route URL:** `/settings`
3. **JSP View File:** None（处理完成后 Redirect）
4. **Servlet:** `SettingsServlet`
5. **Authentication Required:** Yes
6. **Method:** `POST`
7. **Description:** 更新用户的基本资料和通知偏好。
8. **Request Parameters:**
   - `action` (String, required): 固定为 `updateProfile`
   - `fullName` (String, required)
   - `phone` (String, optional)
   - `department` (String, optional)
   - `studentId` (String, optional)
   - `bio` (String, optional)
   - `notificationsEnabled` (String, optional): 选中时传 `on`
9. **Request Attributes:** None（反馈信息通过 redirect query 传递）
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect 到 `GET /settings?state=updateSuccess&successMessage=...`
12. **Failure Behavior:** Redirect 到 `GET /settings?state=updateFailure&errorMessage=...`
13. **Supported Page States:** N/A（动作本身）；目标页见 §7.1
14. **Main Functionalities:** 更新资料卡片和 Preferences 所依赖的用户信息。

### 7.3. Change Password Action
1. **Page Name:** Settings Change Password Action
2. **Route URL:** `/settings`
3. **JSP View File:** None（处理完成后 Redirect）
4. **Servlet:** `SettingsServlet`
5. **Authentication Required:** Yes
6. **Method:** `POST`
7. **Description:** 校验当前密码并更新用户密码。
8. **Request Parameters:**
   - `action` (String, required): 固定为 `changePassword`
   - `currentPassword` (String, required)
   - `newPassword` (String, required)
   - `confirmPassword` (String, required)
9. **Request Attributes:** None（反馈信息通过 redirect query 传递）
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect 到 `GET /settings?state=pwdSuccess&successMessage=...`
12. **Failure Behavior:** Redirect 到 `GET /settings?state=pwdFailure&errorMessage=...`
13. **Supported Page States:** N/A（动作本身）；目标页见 §7.1
14. **Main Functionalities:** 校验密码并在成功后刷新当前会话用户数据。

### 7.4. Update Preferences Action
1. **Page Name:** Settings Update Preferences Action
2. **Route URL:** `/settings`
3. **JSP View File:** None（处理完成后 Redirect）
4. **Servlet:** `SettingsServlet`
5. **Authentication Required:** Yes
6. **Method:** `POST`
7. **Description:** 更新用户的界面语言和外观模式偏好。
8. **Request Parameters:**
   - `action` (String, required): 固定为 `updatePreferences`
   - `preferredLanguage` (String, required): `en` 或 `zh`
   - `preferredAppearance` (String, required): `light` 或 `dark`
9. **Request Attributes:** None（反馈信息通过 redirect query 传递）
10. **Form Submission:** Yes
11. **Success Behavior:** Redirect 到 `GET /settings?state=prefSuccess&successMessage=...`
12. **Failure Behavior:** Redirect 到 `GET /settings?state=prefFailure&errorMessage=...`
13. **Supported Page States:** N/A（动作本身）；目标页见 §7.1
14. **Main Functionalities:** 保存用户的国际化与主题偏好，并刷新当前会话中的语言/外观设置。

---

## 8. Workloads (Admin Only)

### 8.1. TA Workloads Page
1. **Page Name:** TA Workloads Page
2. **Route URL:** `/workloads`
3. **JSP View File:** `/portal/workloads.jsp`
4. **Servlet:** `WorkloadsServlet`
5. **Authentication Required:** Yes (ADMIN Role)
6. **Method:** `GET`
7. **Description:** Admin 查看所有 TA 的 overall workload。
8. **Request Parameters:**
   - `keyword` (String, optional): 搜索 TA 姓名或学号
   - `department` (String, optional): 部门过滤
9. **Request Attributes:**
   - `userRole` (String): 用户角色
   - `workloads` (List<TAWorkload>): TA 工作负载列表
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** GET form for search/filter
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `empty`, `noSearchResults`, `loadError`
14. **Main Functionalities:** 监控和管理 TA 的工作时间，确保没有超负荷工作。
15. **Object Structure:**
    - **`TAWorkload`**
      - `taId` (String)
      - `taName` (String)
      - `department` (String)
      - `activeJobsCount` (Integer)
      - `totalHoursPerWeek` (Integer)
      - `status` (String): e.g., 'Normal', 'Overloaded'

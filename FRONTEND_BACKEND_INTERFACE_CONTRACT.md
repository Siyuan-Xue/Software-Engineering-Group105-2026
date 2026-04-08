# Frontend-Backend Interface Contract

This document outlines the standardized interface contract between the frontend JSP pages and the backend Java Servlets.

## Global Rules

### A. Global Naming Rules
统一列出全项目关键命名约定，确保前后端字段一致：
- `email`: User's email address
- `password`: User's password
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
- 所有页面统一使用 `errorMessage` 和 `successMessage` 作为 Request Attributes 传递反馈信息。
- `errorMessage` (String): 用于显示错误警告（例如：“Invalid credentials”、“Failed to load data”）。
- `successMessage` (String): 用于显示成功提示（例如：“Application submitted successfully”、“Profile updated”）。
- JSP 页面中已内置对应的 UI 组件，当这些 attribute 不为空时会自动显示。

### C. Global Authentication Rules
- 哪些页面需要登录：除 Login 页面外的所有页面（Dashboard, Vacancies, Applications, Resumes, Messages, Settings）均需要用户登录。
- 未登录时建议如何处理：后端 Servlet 应该拦截未认证的请求，并重定向（Redirect）到 `/login` 页面，可附带 `errorMessage` 提示“Please log in to access this page”。

### D. Global Page State Rules
- 状态命名统一风格：使用 camelCase 命名状态（如 `normal`, `empty`, `notFound`）。
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
   - `savedResumesCount` (Integer): 已保存简历数量
   - `submittedApplicationsCount` (Integer): 已提交申请数量
   - `underReviewApplicationsCount` (Integer): 审核中申请数量
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
7. **Description:** 渲染可用助教职位列表，支持搜索和过滤。
8. **Request Parameters:**
   - `keyword` (String, optional): 搜索关键词
   - `department` (String, optional): 部门过滤
   - `term` (String, optional): 学期过滤
9. **Request Attributes:**
   - `vacancies` (List<Vacancy>): 职位列表
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** GET form for search/filter
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `empty`, `noSearchResults`, `loadError`
14. **Main Functionalities:** 浏览和搜索职位。
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

### 3.2. Vacancy Detail Page
1. **Page Name:** Vacancy Detail Page
2. **Route URL:** `/vacancy`
3. **JSP View File:** `/portal/vacancy_detail.jsp`
4. **Servlet:** `VacancyDetailServlet`
5. **Authentication Required:** Yes
6. **Method:** `GET`
7. **Description:** 渲染特定职位的详细信息，并提供申请弹窗。
8. **Request Parameters:**
   - `vacancyId` (String, required)
9. **Request Attributes:**
   - `vacancy` (Vacancy): 职位详情对象
   - `resumeList` (List<Resume>): 用户的简历列表（用于申请弹窗）
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** None (The apply modal submits to `/application`)
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `notFound`, `noResumeAvailable`, `applicationSuccess`, `applicationFailure`, `loadError`
14. **Main Functionalities:** 查看职位详情，选择简历并提交申请。
15. **Object Structure:**
    - **`Vacancy`** (Same as above)
    - **`Resume`**
      - `resumeId` (String)
      - `resumeName` (String)

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
10. **Form Submission:** None
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
8. **Request Parameters:** None
9. **Request Attributes:**
   - `userProfile` (UserProfile): 用户个人资料
   - `errorMessage` (String, optional)
   - `successMessage` (String, optional)
10. **Form Submission:** None
11. **Success Behavior:** N/A
12. **Failure Behavior:** N/A
13. **Supported Page States:** `normal`, `updateSuccess`, `updateFailure`, `loadError`
14. **Main Functionalities:** 查看和更新个人资料及偏好设置。
15. **Object Structure:**
    - **`UserProfile`**
      - `firstName` (String)
      - `lastName` (String)
      - `email` (String)
      - `studentId` (String)
      - `department` (String)
      - `bio` (String)
      - `notificationsEnabled` (Boolean)


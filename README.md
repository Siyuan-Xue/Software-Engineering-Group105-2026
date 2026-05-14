# ta105 - 105组TA招聘系统

## 组员信息


| Name          | QM Stu Number | BUPT Stu Number | GitHub email                                              |
| ------------- | ------------- | --------------- | --------------------------------------------------------- |
| Wanran Sun    | 231223254     | 2023213626      | [112358wan@gmail.com](mailto:112358wan@gmail.com)         |
| Xiankun Jiang | 231223542     | 2023213655      | [jp2023213655@qmul.ac.uk](mailto:jp2023213655@qmul.ac.uk) |
| Siyuan Xue    | 231223564     | 2023213657      | [jp2023213657@qmul.ac.uk](mailto:jp2023213657@qmul.ac.uk) |
| Yutong Wu     | 231223575     | 2023213658      | [serovia@126.com](mailto:serovia@126.com)                 |
| Xiaoxiao Ma   | 231223715     | 2023213672      | [maxiaoxiao@bupt.edu.cn](mailto:maxiaoxiao@bupt.edu.cn)   |
| Rui Ma        | 231223151     | 2023213616      | [940874485@qq.com](mailto:940874485@qq.com)               |


## TA-information

Wang Ruijia [wang_ruijia@bupt.edu.cn](mailto:wang_ruijia@bupt.edu.cn)

## Stack

- OpenJDK 25.0.2 (or any Java 17+ runtime)
- Tomcat 11
- Servlet API (Jakarta) + JSP
- Jackson-powered local JSON database
- JUnit 6
- Maven WAR project

## Architecture

- 单 WAR 同源部署
- 前端：JSP 页面与共享组件
- 后端：Servlet/JSP Web 应用 + `TaDatabase` 统一数据库门面
- 数据存储：`data/*.json`，Sprint 3 终态 11 表，不使用外部数据库
- 注解注册：`@WebFilter`，`web.xml` 仅保留最小描述符

## 岗位编辑与标签（MO）

- **独立编辑页**：`GET/POST` [`/vacancy/edit`](./src/main/java/com/bupt/ta/web/servlet/VacancyEditServlet.java) → JSP [`portal/vacancy_edit.jsp`](./src/main/webapp/portal/vacancy_edit.jsp)（整页布局，含顶栏与侧栏）。
- **入口与 `returnTo`**：岗位列表（本人岗位）「编辑」带 `returnTo=list`；岗位详情「编辑岗位」带 `returnTo=detail`。编辑页内「保存后跳转到」下拉与之一致。
- **提交成功**：按 `returnTo` 重定向到 **`/vacancies?successMessage=...`** 或 **`/vacancy?vacancyId=...&successMessage=...`**（列表/详情 Servlet 已从 query 读取 flash）。
- **提交失败**：重定向回 **`/vacancy/edit?vacancyId=...&errorMessage=...&returnTo=...`**，避免丢失来源页，保证「返回」链正确。
- **标签（labels）**：[`Labels.parseList`](./src/main/java/com/bupt/ta/util/Labels.java) 使用逗号、中文逗号、分号、**竖线 `|` / 全角 `｜`**、换行分隔；`strip()` 去空白；与后端一致为 **最多 24 个标签、单标签最长 48 字符**（创建/编辑表单的提示文案已对齐）。

## AI 能力与文档

部署上下文路径为 **`/ta105`** 时，下表中的 Servlet 路径均指 **`/ta105/...`**（例如 `/ta105/ai-match`）。

### 还可以引入 AI 的方向（建议）

以下为**未实现**、但与当前业务契合度较高的扩展点，便于组内分工或后续迭代：

| 方向 | 说明 |
|------|------|
| **MO 发布/编辑岗位** | **发布**：列表页弹窗 → `POST /vacancy/create`。**编辑**：独立页 `GET/POST /vacancy/edit`（见上文「岗位编辑与标签」）。可选后续：AI 辅助生成描述/与 labels 一致性检查。 |
| **MO 审阅申请** | **已实现（初版）**：申请记录页「AI 决策建议」— 对单份申请给出 offer/拒绝/待定的**参考**要点（免责声明后调用；**不**代替录用决定）。 |
| **TA 撰写辅助** | **已实现（初版）**：申请记录页「AI 动机草稿」— 基于所选简历与目标岗位生成**可编辑**短述/动机段（免责声明后调用）。 |
| **Dashboard** | 根据当前角色与数据（开放岗位数、申请状态）生成**下一步行动建议**（纯建议文案）。 |
| **站内消息** | 可选：语气润色或翻译（需注意隐私与是否出站调用模型）。 |

实现时建议继续沿用现有模式：**浏览器只请求本站 Servlet → 服务端持有 Key 调用 DashScope**；凡调用大模型的功能，**必须**在页面上提供免责声明，且用户**必须点击明确的确认按钮**（如「同意并分析」「同意并匹配」等）之后才能发起请求。**不得以**要求模型在输出中生成法律/隐私免责段落的方式，代替界面上的知情同意。

### 用户同意与免责（界面强制）

| 原则 | 说明 |
|------|------|
| **同意方式** | 仅通过各功能弹窗内的说明文字 + **「同意并…」类按钮**完成；未点击确认前，前端不得调用对应 Servlet。 |
| **与提示词的关系** | `QwenAiService` 中的 system prompt 仅约束**任务行为**（如不得代为录用、不得输出 JSON 外文字等），**不**承担向用户展示法律条款的职责；亦**不**指示模型在正文中撰写可替代上述弹窗的「同意书」或免责段落。 |
| **岗位详情申请弹窗** | TA 在已配置 `QWEN_API_KEY` 且名下有简历时，点击「立即申请」会先弹出 **AI 简历匹配排序** 免责层；点击「同意并排序」后才会打开选简历弹窗并请求 `/ai-resume-rank`。未配置 Key 或无简历时直接进入选简历弹窗且不发起 AI 请求。 |

### AI 功能用途说明

以下为当前版本中 **会调用 Qwen（DashScope）大模型** 的能力及其业务用途（不含仅规则落库的 `MatchingService`）。

| 功能 | 角色 / 页面 | 用途（用户价值） | 典型输入数据（出站摘要） | 输出形态 |
|------|-------------|------------------|---------------------------|----------|
| **AI 简历分析** | TA · 简历页 | 结合简历字段与可选上传文件，给出可执行的简历改进建议（结构、措辞、TA 岗位相关性等）。 | 简历标题/院系/学位/GPA/个人陈述；可选图片或文本附件；可选若干岗位标题作语境。 | Markdown 分段建议 |
| **AI 岗位匹配** | TA · 岗位列表 | 对当前页每条开放岗位，根据学生简历摘要生成 **0–100** 匹配分，便于浏览筛选。 | 学生档案摘要 + 各岗位标题/院系/描述片段。 | JSON：`jobId → score` |
| **AI 简历匹配排序** | TA · 岗位详情 → 申请 | 将本人多份简历相对**当前岗位**打分并标出推荐项，辅助选择提交哪一份。 | 单岗位标题/院系/描述 + 各简历 id 与摘要字段。 | JSON：分数与推荐标记 |
| **AI 决策建议** | MO · 申请记录 | 结合岗位与申请材料，给出 **Offer / Reject / Unclear** 倾向与理由要点（**非**录用决定）。 | 岗位描述/标签、申请状态、申请人显示名、简历快照、求职信片段。 | Markdown（不含独立「免责声明」节） |
| **AI 申请者排序** | MO · 申请记录 | 对**同一岗位**下至少 2 名（非撤回）申请者，生成建议浏览顺序、0–100 匹配分与一句理由，辅助筛选。 | 岗位描述/标签 + 各申请 id、状态、简历与求职信摘要。 | JSON 数组（经 Servlet 校验后返回 `rankings`） |
| **AI 动机草稿** | TA · 申请记录 | 基于目标岗位与所选简历生成 **第一人称** 可编辑短述/动机段草稿。 | 岗位描述/标签 + 所选简历字段与个人陈述。 | Markdown（Draft / Tips 等；不含法律免责段） |

**说明**：`MatchingService` 写入 `match_scores` 的持久化分析以**规则分**为主；`aiExplanation` 仅随是否配置 Key 切换说明文案，**不**调用大模型，故未列入上表。

### AI 功能一览表

| 用户入口（页面 / 操作） | 调用的 Servlet / 入口 | 底层服务 | 需要配置 `QWEN_API_KEY`？ | 失败时的典型表现 |
|-------------------------|------------------------|----------|---------------------------|-------------------|
| **简历页** → 同意免责后「AI 简历分析」 | `POST /resumes`（`action=aiReview`），`ResumesServlet` | `QwenAiService.analyzeResumeForOptimization`（多模态/文本） | **是** | 返回 JSON `ok: false`，文案含 *Qwen API key is not configured*（`msg.aiKeyMissing`）；前端弹错误提示。 |
| **岗位列表（TA）** → 同意免责后「AI 匹配」 | `POST /ai-match`，`AiMatchServlet` | `QwenAiService` 批量文本打分 | **是** | JSON `ok: false`，`error` 为 *QWEN_API_KEY is not configured* 或网络/模型错误；列表顶部横幅提示失败。 |
| **岗位详情（TA）** → 申请：先弹窗阅读免责并点击「同意并排序」 | `POST /ai-resume-rank`，`AiResumeRankServlet` | `QwenAiService.rankResumesForJob` | **是** | 未配置 Key 或无简历时不弹 AI 层、不请求；同意后失败则弹窗内无分数条。 |
| **申请记录（MO）** → 同意免责后「AI 推荐排序」（同一岗位 ≥2 人） | `POST /ai-mo-applicants-rank`，`AiMoApplicantsRankServlet` | `QwenAiService.rankMoApplicantsForJobJson` | **是** | 未配置 Key 时按钮禁用；不足 2 人时接口返回说明；失败时 JSON `error` 或 HTTP 错误。 |
| **申请记录（MO）** → 同意免责后「AI 决策建议」 | `POST /ai-mo-application-advice`，`AiMoApplicationAdviceServlet` | `QwenAiService.suggestMoOfferRejectAdvice` | **是** | 除 **Withdrawn（已撤回）** 外各状态均显示入口；已录用/已拒绝等状态下模型按「回顾性说明」提示词输出。失败时 JSON `ok: false` 等。 |
| **申请记录（TA）** → 同意免责后「AI 动机草稿」 | `POST /ai-ta-cover-letter`，`AiTaCoverLetterServlet` | `QwenAiService.draftTaCoverLetterMotivation` | **是** | 同上；需行内同时具备 `vacancyId` 与 `resumeId` 且状态非 Withdrawn 才显示入口。 |
| **（后端）申请匹配分落库** | `MatchingService`（随申请等业务写入 `match_scores`） | 以**规则分**为主；`aiExplanation` 仅根据是否配置 Key 切换说明文案 | **否**（不配置 Key 也会落库） | 无单独「AI 按钮」；与申请流程联动，可在相关通知或管理/演示入口查看匹配记录。 |

**开发与运维提示**：若页面上 AI 按钮灰色或弹窗报错含 *QWEN_API_KEY* / *HTTP 5xx*，请检查 Tomcat 进程环境变量、重启服务，并查看 `catalina` 日志；课程演示环境可阅读 README「Qwen API Key 配置指南」。

未登录用户调用上述需登录的接口时，一般返回 **401** 与 JSON `ok: false`（以各 Servlet 实现为准）。

### Qwen（DashScope）API Key 配置指南

所有密钥**仅通过环境变量或 Tomcat 启动参数**注入，**不要**写入仓库、不要写进 `data/*.json`。

**1. 必填**

| 变量名 | 含义 |
|--------|------|
| `QWEN_API_KEY` | 阿里云 DashScope 兼容 OpenAI 格式的 API Key（`sk-...`）。Java 代码通过 `System.getenv("QWEN_API_KEY")` 读取（见 `QwenAiService.resolveApiKey()`）。 |

**2. 可选（模型名）**

| 变量 / JVM 属性 | 含义 | 默认值（代码内） |
|-----------------|------|------------------|
| `QWEN_MODEL` 或 JVM `-DQWEN_MODEL=...` | 多模态（简历文件/图）分析用 VL 模型 | `qwen2.5-vl-72b-instruct` |
| `QWEN_TEXT_MODEL` 或 JVM `-DQWEN_TEXT_MODEL=...` | 纯文本批量打分、简历排序用文本模型 | `qwen3.5-plus` |

**3. Windows + 独立 Tomcat（推荐 `setenv.bat`）**

在 Tomcat 安装目录的 **`bin`** 下新建或编辑 **`setenv.bat`**（若不存在则新建），例如：

```bat
set "QWEN_API_KEY=sk-你的密钥"
rem 可选：
rem set "QWEN_MODEL=qwen2.5-vl-72b-instruct"
rem set "QWEN_TEXT_MODEL=qwen3.5-plus"
```

保存后**重启** Tomcat（`shutdown.bat` 再 `startup.bat`，或重启托管服务）。`setenv.bat` 会在 `catalina.bat` 启动时被自动加载。

也可在「系统环境变量」中为当前用户或整机设置 **`QWEN_API_KEY`**，再启动 Tomcat，效果等价（需保证 Tomcat 进程能继承到该变量）。

**4. macOS / Linux**

在启动 Tomcat 的 shell 中 `export QWEN_API_KEY=sk-...`，或使用 `bin/setenv.sh`（若你使用 Tomcat 官方 `setenv.sh` 机制），同样**重启** Tomcat。

使用 Homebrew 服务时，需通过 `brew services` 对应的环境配置或包装脚本注入变量，确保 **`catalina` 子进程**可见 `QWEN_API_KEY`。

**5. 验证是否生效**

- 登录 TA 账号打开 **简历页**：未配置时侧栏常见「KEY REQUIRED」类提示；配置后「AI 简历分析」应能返回分析内容（仍受网络与额度影响）。  
- 或临时查看 Tomcat 日志中是否仍有 *QWEN_API_KEY is not configured* 类输出。

**6. 安全提醒**

- 不要把 Key 提交到 Git；演示截图前打码。  
- 生产环境建议使用密钥托管（如云厂商密钥管理），而非明文写在服务器磁盘上的脚本（课程项目 `setenv.bat` 本地使用可接受）。

## Database Docs

- 数据层设计总入口：[docs/ta-recruitment-system/README.md](./docs/ta-recruitment-system/README.md)
- 后端使用指南：[docs/ta-recruitment-system/backend-database-usage.md](./docs/ta-recruitment-system/backend-database-usage.md)
- 升级影响说明：[docs/ta-recruitment-system/database-upgrade-impact.md](./docs/ta-recruitment-system/database-upgrade-impact.md)

## Project Layout

```text
.
├── pom.xml
└── src/
    └── main/
        ├── java/com/bupt/ta/
        │   ├── config/
        │   ├── bootstrap/
        │   ├── domain/
        │   │   ├── entity/
        │   │   ├── enums/
        │   │   └── value/
        │   ├── db/
        │   │   ├── core/
        │   │   ├── store/
        │   │   ├── repository/
        │   │   └── facade/
        │   ├── model/
        │   ├── service/
        │   └── web/
        ├── test/
        └── webapp/
            ├── index.jsp
            └── portal/
├── docs/ta-recruitment-system/
└── data/
    ├── users.json
    ├── resumes.json
    ├── jobs.json
    ├── applications.json
    ├── skills.json
    ├── resume_skills.json
    ├── job_requirements.json
    ├── workload_records.json
    ├── match_scores.json
    ├── notifications.json
    └── audit_logs.json
```

## macOS 从零安装并运行（Homebrew）

### 1) 安装依赖

```bash
brew update
brew install openjdk maven tomcat
```

### 2) 配置 Java（zsh）

```bash
sudo ln -sfn /opt/homebrew/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
echo 'export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### 3) 验证安装

```bash
java -version
mvn -version
"$(brew --prefix tomcat)/bin/catalina" version
```

### 4) 构建与测试项目

```bash
cd /path/to/your/TA_recruitment_system
mvn test
mvn clean package
```

### 5) 部署到 Tomcat 11

```bash
cp target/ta105.war "$(brew --prefix tomcat)/libexec/webapps/"
```

### 6) 启动 Tomcat

前台运行（便于看日志）：

```bash
"$(brew --prefix tomcat)/bin/catalina" run
```

或后台服务：

```bash
brew services start tomcat
```

### 7) 验证

前端入口：

- `http://localhost:8080/ta105/`

运行项目：
启动 Tomcat。
访问 `http://localhost:8080/`（或你配置的端口），你将看到 QM HIRE 的落地页。
测试账号：系统首次启动空数据目录时会自动生成完整演示数据，包含账号、简历、岗位、申请、匹配分、工作量、通知、消息和审计日志。以下三个基础账号会始终出现在这套演示数据中，密码均为 `password`：
- TA 账号：[test@example.com](mailto:test@example.com)
- MO 账号：[mo@example.com](mailto:mo@example.com)
- Admin 账号：[admin@example.com](mailto:admin@example.com)

运行期数据目录：

- 默认：`./data`
- 可覆盖：`-Dta105.data.dir=/absolute/path/to/data`
- 新系统要求使用全新的空 `data/` 目录
- 自动演示种子只在所有业务表为空时创建；只要数据目录里已有任何业务数据，系统就不会补种、覆盖或清空现有数据
- 如果需要重新生成完整演示库，请先备份并清空当前数据目录
- 不做旧版 4 表 JSON 的自动迁移；若检测到旧数据目录，启动会失败并提示先备份/清空

数据库层使用方式：

```java
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.value.JobQuery;
import java.time.Instant;

TaDatabase db = DatabaseProvider.get(servletContext);

db.users().findByEmail("user@example.com");
db.resumes().listByUserId(userId);
JobQuery query = new JobQuery();
query.setNow(Instant.now());
db.jobs().listOpen(query);
db.applications().listByJobId(jobId);
```

### 8) 停止 Tomcat

前台 `catalina run`：`Ctrl + C`

后台服务：

```bash
brew services stop tomcat
```

## Windows 从零安装并运行

### 1) 安装依赖

- **JDK**：安装 **Java 17 或以上**（项目编译目标为 17，运行时用 17/21 等均可）。可从 [Eclipse Temurin](https://adoptium.net/) 或 Oracle 下载 Windows x64 安装包，或使用 `winget`（包名以当时仓库为准，例如 Temurin 17）。
- **Maven**：从 [Maven 下载页](https://maven.apache.org/download.cgi) 解压到任意目录（如 `D:\Tools\apache-maven-3.9.x`），把其中的 `bin` 加入系统 **Path**。
- **Tomcat 11**：从 [Tomcat 11 下载页](https://tomcat.apache.org/download-11.cgi) 获取 **`apache-tomcat-*-windows-x64.zip`**（含 Windows 本机库与服务封装；Tomcat 11 需要 **Java 17+**），解压到任意目录（下文以 `D:\Programs\Tomcat 11.0` 为例）。

### 2) 配置环境变量

在「系统属性 → 高级 → 环境变量」中：

- 新建或编辑 **`JAVA_HOME`**，指向 JDK 根目录（例如 `D:\Programs\JAVA17`）。
- 编辑 **`Path`**，确保包含 **`%JAVA_HOME%\bin`**，且排在其他 JDK 路径之前（避免 `java -version` 仍指向旧版本）。

若希望 Tomcat 脚本固定使用某套 JRE，可另设 **`JRE_HOME`**（与 `JAVA_HOME` 指向同一 JDK 根目录即可）。

### 3) 验证安装

在 **新的** PowerShell 或 CMD 中：

```powershell
java -version
mvn -version
```

进入 Tomcat 的 `bin` 目录执行：

```powershell
cd "D:\Programs\Tomcat 11.0\bin"
.\catalina.bat version
```

### 4) 构建与测试项目

```powershell
cd "C:\path\to\Software-Engineering-Group105-2026"
mvn test
mvn clean package
```

生成的 WAR 为 **`target\ta105.war`**。

### 5) 部署到 Tomcat 11

将 WAR 复制到 Tomcat 的 **`webapps`** 目录（路径按你的解压位置修改）：

```powershell
Copy-Item ".\target\ta105.war" -Destination "D:\Programs\Tomcat 11.0\webapps\ta105.war" -Force
```

或在 CMD 中：

```bat
copy /Y target\ta105.war "D:\Programs\Tomcat 11.0\webapps\ta105.war"
```

### 6) 启动 Tomcat

在 **`Tomcat\bin`** 下：

**后台启动**（当前终端立即返回）：

```powershell
cd "D:\Programs\Tomcat 11.0\bin"
.\startup.bat
```

若 PowerShell 下提示 **`CATALINA_HOME` 未正确设置**，可在同一目录改用：

```powershell
cmd /c startup.bat
```

**前台启动**（便于看日志，用 `Ctrl+C` 停止）：

```powershell
.\catalina.bat run
```

### 7) 验证

- 应用入口：<http://localhost:8080/ta105/>（HTTP 端口以 `conf\server.xml` 中 `Connector` 为准，默认 **8080**。）
- 访问 <http://localhost:8080/> 可打开 Tomcat 根应用 / 欢迎页。
- 测试账号（首次启动自动生成），密码均为 `password`：
  - TA 账号：`test@example.com`
  - MO 账号：`mo@example.com`
  - Admin 账号：`admin@example.com`

运行期数据目录、`-Dta105.data.dir` 与 macOS 一节相同。

### 8) 停止 Tomcat

```powershell
cd "D:\Programs\Tomcat 11.0\bin"
.\shutdown.bat
```

**说明**：Tomcat 11 默认 `conf\server.xml` 中可能为 `<Server port="-1" ...>`，此时 **不会监听关闭端口**，`shutdown.bat` 会失败。本地开发可将 **`Server` 的 `port` 改为 `8005`**（仅本机）后**重启 Tomcat**，再使用 `shutdown.bat`。若未启用关闭端口，可在运行 `catalina.bat run` 的窗口中 **`Ctrl+C`**，或在任务管理器中结束对应的 **`java.exe`**（注意勿误杀其他 Java 程序）。

### 9) 常见问题（Windows）

- **`Address already in use: bind`（8080）**：本机已有进程占用 8080（常见于已启动的 Tomcat）。先执行 `shutdown.bat` 或结束占用进程后再启动。可用 `netstat -ano | findstr :8080` 查看 PID。
- **控制台中文乱码**：简体中文 Windows 默认控制台多为 **GBK**；若 Tomcat 控制台使用 UTF-8 输出会乱码。可在 **`conf\logging.properties`** 中将 **`java.util.logging.ConsoleHandler.encoding`** 设为 **`GBK`**，文件日志仍可保持 **`UTF-8`**。
- **勿同时重复启动**：不要对同一 Tomcat 目录同时使用 **`startup.bat`** 与多个 **`catalina.bat run`**，否则易端口冲突。

## Deploy To External Tomcat 11

```bash
mvn clean package
cp target/ta105.war "$(brew --prefix tomcat)/libexec/webapps/"
"$(brew --prefix tomcat)/bin/catalina" start
```

Windows 上将第三行替换为复制到外部 Tomcat 的 `webapps` 即可，例如：

```powershell
Copy-Item target\ta105.war -Destination "D:\path\to\apache-tomcat-11.x\webapps\ta105.war" -Force
cd "D:\path\to\apache-tomcat-11.x\bin"
.\catalina.bat start
```

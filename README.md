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
测试账号：系统首次启动时会自动生成三个测试账号，密码均为 `password`：
- TA 账号：[test@example.com](mailto:test@example.com)
- MO 账号：[mo@example.com](mailto:mo@example.com)
- Admin 账号：[admin@example.com](mailto:admin@example.com)

运行期数据目录：

- 默认：`./data`
- 可覆盖：`-Dta105.data.dir=/absolute/path/to/data`
- 新系统要求使用全新的空 `data/` 目录
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

# ta105 - 105组TA招聘系统

## 组员信息

| Name          | QM Stu Number | BUPT Stu Number | GitHub email              |
|---------------|---------------|-----------------|---------------------------|
| Wanran Sun    | 231223254     | 2023213626      | <112358wan@gmail.com>     |
| Xiankun Jiang | 231223542     | 2023213655      | <jp2023213655@qmul.ac.uk> |
| Siyuan Xue    | 231223564     | 2023213657      | <jp2023213657@qmul.ac.uk> |
| Yutong Wu     | 231223575     | 2023213658      | <serovia@126.com>         |
| Xiaoxiao Ma   | 231223715     | 2023213672      | <maxiaoxiao@bupt.edu.cn>  |
| Rui Ma        | 231223151     | 2023213616      | <940874485@qq.com>        |

## TA-information
Wang Ruijia wang_ruijia@bupt.edu.cn

## Stack

- OpenJDK 25.0.2 (or any Java 17+ runtime)
- Tomcat 11
- Servlet API (Jakarta) + JSP
- Jackson JSON persistence
- JUnit 6
- Maven WAR project

## Architecture

- 单 WAR 同源部署
- 前端：JSP 页面与共享组件
- 后端：Servlet/JSP Web 应用 + 基于文件的持久化层
- 数据存储：`data/*.json`，不使用数据库
- 注解注册：`@WebFilter`，`web.xml` 仅保留最小描述符

## Project Layout

```text
.
├── pom.xml
└── src/
    └── main/
        ├── java/com/bupt/ta/
        │   ├── config/
        │   ├── bootstrap/
        │   ├── model/
        │   ├── persistence/
        │   ├── repository/
        │   ├── service/
        │   └── web/
        ├── test/
        └── webapp/
            ├── index.jsp
            └── portal/
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
访问 http://localhost:8080/（或你配置的端口），你将看到 QM HIRE 的落地页。
测试账号：系统首次启动时会自动生成一个测试账号：
邮箱：test@example.com
密码：password

运行期数据目录：

- 默认：`./data`
- 可覆盖：`-Dta105.data.dir=/absolute/path/to/data`

数据库层使用方式：

```java
TaDatabase db = DatabaseProvider.get(servletContext);
db.users().findByEmail("user@example.com");
db.resumes().listByUserId(userId);
db.jobs().listOpen(Instant.now());
db.applications().listByJobId(jobId);
```

### 8) 停止 Tomcat

前台 `catalina run`：`Ctrl + C`

后台服务：

```bash
brew services stop tomcat
```

## Deploy To External Tomcat 11

```bash
mvn clean package
cp target/ta105.war "$(brew --prefix tomcat)/libexec/webapps/"
"$(brew --prefix tomcat)/bin/catalina" start
```

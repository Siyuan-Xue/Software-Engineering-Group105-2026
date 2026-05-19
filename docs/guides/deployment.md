# Deployment

## WAR Deployment

Build the application:

```bash
mvn clean package
```

Deploy `target/ta105.war` to Tomcat 11 `webapps/`. The app context path is `/ta105`.

## Data Directory

By default, runtime JSON data is stored under `./data`.

The JSON database is designed for a single JVM writing to one `data/` directory. `FileTaDatabase.executeAtomically` provides in-process mutual exclusion, not database-grade rollback across multiple JSON files.

Do not point multiple Tomcat instances at the same writable `data/` directory. If the project needs multi-instance deployment, stronger rollback, or cross-table transactions, migrate the persistence layer to SQLite/MySQL/PostgreSQL behind the existing repository/facade interfaces.

Override with either:

```bash
export TA105_DATA_DIR=/absolute/path/to/data
```

or:

```bash
-Dta105.data.dir=/absolute/path/to/data
```

The app stores resumes under `data/resumes/uploads` and application snapshots under `data/applications/submissions`.

## Reset Demo Data

Stop Tomcat, back up `data/`, clear the business JSON files, and restart. Seeding only runs when the business tables are empty.

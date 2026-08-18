# Backend local database setup

The shared Spring configuration reads database values from environment variables or JVM system properties. Do not commit a password or a `.env` file.

## Required environment variables

```text
DB_URL=jdbc:mysql://localhost:3306/azas?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
DB_USERNAME=azas_app
DB_PASSWORD=<local password>
```

`DB_DRIVER_CLASS_NAME` is optional and defaults to `com.mysql.cj.jdbc.Driver`.

For a shared RDS instance, use its endpoint and port in `DB_URL`. Add the SSL mode required by the team's infrastructure policy as a JDBC URL parameter; do not hard-code it in application source.

## Initialize a local database

The application does not execute `schema.sql` or `seed.sql` automatically at startup.

When a new table is added to an existing local database, rerunning the entire
`schema.sql` resets all local data. To preserve existing data, execute only the
new feature's `CREATE TABLE` statement against the local database.

Run the schema first, then the seed data.

```powershell
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p azas < backend/src/main/resources/db/seed.sql
```

Both scripts reset their target tables. Run them only against a local development database, never against a shared environment without team approval.

## MyBatis conventions

Every MyBatis mapper interface must use `@Mapper` so the shared mapper scanner can register it. Place its XML file below `backend/src/main/resources/mapper/` and set the XML namespace to the mapper interface's fully qualified class name.

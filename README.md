# Student Result Management System

A Java desktop application for schools and colleges to manage student marks, generate results, and import/export data — built with Java Swing, MySQL, and a layered architecture.

---

## What It Does

- **Secure login** — Admin authentication (passwords stored with PBKDF2 hashing)
- **Dashboard stats** — Total students, pass %, class average
- **Student CRUD** — Add, update, delete with automatic grade calculation
- **Search & filter** — By name or roll number; pass/fail filter
- **Custom subject names** — Rename subjects; table headers update instantly
- **Bulk CSV import** — Import many students at once
- **CSV export** — Download all results
- **Text report** — Printable `.txt` result report (open and Ctrl+P)
- **Input validation** — Marks limited to 0–100

---

## Security

| Practice | Detail |
|----------|--------|
| Password hashing | PBKDF2-HMAC-SHA256 with salt (600k iterations) |
| External config | DB credentials in `config.properties` (not in source) |
| SQL injection | Prepared statements for login and CRUD |
| Legacy migration | Existing plain-text passwords are upgraded on first run |

**Default local dev login** (first install only): `admin` / `admin123` — change after setup.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Java 17+ | Core language |
| Java Swing + FlatLaf | Desktop UI |
| MySQL 8.0 | Database |
| JDBC | Database access |

---

## Project Structure

```
StudentResultSystem/
├── config.properties.example   # Copy to config.properties
├── schema.sql                  # Optional manual DB setup
├── src/
│   ├── model/Student.java
│   ├── dao/StudentDAO.java
│   ├── service/StudentService.java
│   ├── util/AppConfig, DBConnection, PasswordUtil, ReportExporter, UiStyles
│   └── view/Main.java, LoginView.java, MainView.java
└── test/model/StudentGradeTest.java
```

---

## How to Run

### Prerequisites

- Java JDK 17 or above
- MySQL 8.0
- NetBeans (recommended) or any Java IDE
- JARs in project root: `mysql-connector-j-*.jar`, `flatlaf-*.jar`

### 1. Clone

```bash
git clone https://github.com/Anuj230977/Student-Result-System.git
cd Student-Result-System
```

### 2. Create database

```sql
CREATE DATABASE student_result_db;
```

Or run `schema.sql`.

### 3. Configure database

```bash
copy config.properties.example config.properties
```

Edit `config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/student_result_db
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

If `config.properties` is missing, the app falls back to `root` / `root123` (change this for your machine).

### 4. Add JAR libraries (NetBeans)

- MySQL Connector/J: https://dev.mysql.com/downloads/connector/j/
- FlatLaf: https://www.formdev.com/flatlaf/
- Add both to project **Libraries**

### 5. Run

- Main class: `view.Main`
- Login: `admin` / `admin123` (first run only; password is stored hashed)

### Grade tests

Run `test.model.StudentGradeTest` main method, or from `build/classes` after compile:

```bash
java -cp build/classes test.model.StudentGradeTest
```

---

## CSV Import Format

```
roll_number,name,subject1,subject2,subject3,subject4,subject5
BCA001,Rahul Sharma,85,78,92,88,76
```

- First row is header (skipped)
- Duplicate roll numbers are skipped (`INSERT IGNORE`)

---

## Grade System

| Percentage | Grade |
|------------|-------|
| 90%+ | A+ |
| 75–89 | A |
| 60–74 | B |
| 50–59 | C |
| 35–49 | D |
| Below 35 | F |

---

## Author

**Anuj Jadhav** — TY BBA-CA  
[LinkedIn](https://www.linkedin.com/in/anujjadhav) · [GitHub](https://github.com/Anuj230977)

---

## License

[MIT License](LICENSE)

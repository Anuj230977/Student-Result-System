# Project Blueprint: Student Result Management System (Professional Edition)

Technical specification and source of truth for architecture, boundaries, and current implementation status.

---

## 1. Project Overview

**Goal:** A professional-grade Java desktop app for school/college admins to manage student marks, generate results, and import/export data — suitable for portfolio, LinkedIn, and small freelance clients.

**Target user:** Single admin/teacher on a local Windows machine.

**Core value:** Fast data entry, secure local storage, bulk CSV handling, modern UI.

---

## 2. Current State (Implemented)

The following refinements from the original blueprint are **done**:

### Architecture

| Layer | Status | Details |
|-------|--------|---------|
| `model` | Done | `Student.java` — POJO + auto grading |
| `dao` | Done | `StudentDAO.java` — all SQL, try-with-resources |
| `service` | Done | `StudentService.java` — UI talks to service only |
| `util` | Done | `AppConfig`, `DBConnection`, `PasswordUtil`, `ReportExporter`, `UiStyles` |
| `view` | Done | `Main`, `LoginView`, `MainView` |

### UI/UX

- FlatDarkLaf modern look
- Quick stats dashboard (total students, pass %, class average)
- DocumentFilter on marks fields (0–100, numbers only)
- Custom subject names with live table header update
- Search by roll/name; Pass/Fail filter
- Input fields styled for visibility with FlatLaf (`UiStyles`)

### Security

- Login screen before dashboard
- Passwords stored with **PBKDF2-HMAC-SHA256** (not plain text)
- DB credentials in **`config.properties`** (example file committed; real file gitignored)
- Prepared statements for login and CRUD
- Automatic migration of legacy plain-text passwords on startup

### Data & reporting

- CSV import (`INSERT IGNORE` for duplicates)
- CSV export
- **Text report** export (`.txt`) — printable via Ctrl+P  
  *(PDF export was considered optional; not implemented to avoid extra dependencies.)*

### Quality

- `schema.sql` for manual DB setup
- `test/model/StudentGradeTest.java` for grade boundary checks
- Friendly error messages (e.g. duplicate roll number, DB connection)

---

## 3. Project Structure

```
StudentResultSystem/
├── config.properties.example    # Template — copy to config.properties
├── config.properties            # Local only (gitignored)
├── schema.sql
├── src/
│   ├── model/Student.java
│   ├── dao/StudentDAO.java
│   ├── service/StudentService.java
│   ├── util/
│   │   ├── AppConfig.java
│   │   ├── DBConnection.java
│   │   ├── PasswordUtil.java
│   │   ├── ReportExporter.java
│   │   └── UiStyles.java
│   └── view/Main.java, LoginView.java, MainView.java
└── test/model/StudentGradeTest.java
```

---

## 4. Boundaries (No-Go Zone)

Excluded to keep scope appropriate for a fresher portfolio:

- No student-facing portal
- No cloud hosting (localhost MySQL only)
- No complex backup system (CSV import/export is the backup)
- No web version
- No real PDF library (text report instead)

---

## 5. Feature Specification

| Feature | Behavior |
|---------|----------|
| Subject config | 5 subjects; names in DB; headers update in UI |
| CSV import | Header row skipped; duplicates ignored |
| CSV export | All students with custom subject column names |
| Search | By roll number or name (partial match) |
| Filter | All / Pass / Fail |
| Grades | Auto from average of 5 subjects (see README grade table) |

---

## 6. Setup Requirements

1. Java JDK 17+
2. MySQL 8.0 — database `student_result_db`
3. Copy `config.properties.example` → `config.properties`; set `db.password`
4. JARs: MySQL Connector/J, FlatLaf (project libraries in NetBeans)
5. Run `view.Main` — login `admin` / `admin123` on first install

---

## 7. Optional Future Enhancements

Not required for current portfolio release:

- PDF export (iText / PDFBox)
- Change-password screen in UI
- Maven/Gradle build
- JUnit test suite in CI
- Configurable number of subjects (beyond 5)
- GitHub Actions compile on push

---

## 8. Portfolio Positioning

**Title:** Local Academic Result Management Tool

**Selling points:**

1. Secure admin login with hashed passwords and external DB config  
2. Productivity UI — stats dashboard, validation, bulk CSV  
3. Clean layered code — model, DAO, service, view  

**Tech keywords for LinkedIn:** Java · Swing · FlatLaf · MySQL · JDBC · PBKDF2 · MVC/layered architecture

---

*Last aligned with codebase: security refactor + service layer + config.properties + UI fixes.*

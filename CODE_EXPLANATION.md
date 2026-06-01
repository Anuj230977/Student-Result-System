# Code Explanation — Student Result Management System

A walkthrough of every package, class, and design decision in the current codebase.

---

## Project Architecture — Layered Design

The app uses a **layered architecture** (Model → DAO → Service → View):

```
┌──────────────┐
│    VIEW      │  LoginView, MainView, Main
│  (Swing UI)  │
└──────┬───────┘
       │
┌──────▼───────┐
│   SERVICE    │  StudentService — friendly errors, orchestration
└──────┬───────┘
       │
┌──────▼───────┐     ┌──────────────┐
│     DAO      │────►│    UTIL      │
│ StudentDAO   │     │ DBConnection │
└──────┬───────┘     │ AppConfig    │
       │             │ PasswordUtil │
┌──────▼───────┐     │ ReportExporter, UiStyles
│    MODEL     │     └──────────────┘
│  Student.java│
└──────────────┘
```

| Package | File | Responsibility |
|---------|------|----------------|
| `model` | `Student.java` | Student data + automatic grade calculation |
| `dao` | `StudentDAO.java` | All SQL; password hashing integration |
| `service` | `StudentService.java` | Bridge between UI and DAO; user-friendly error messages |
| `util` | `AppConfig.java` | Loads `config.properties` (DB URL, user, password) |
| `util` | `DBConnection.java` | Singleton JDBC connection |
| `util` | `PasswordUtil.java` | PBKDF2 password hash and verify |
| `util` | `ReportExporter.java` | Writes printable `.txt` result reports |
| `util` | `UiStyles.java` | Consistent text-field styling (works with FlatLaf) |
| `view` | `Main.java` | Entry point, FlatLaf, DB init |
| `view` | `LoginView.java` | Login screen |
| `view` | `MainView.java` | Main dashboard |

**Note:** There is no separate `Controller` class. `StudentService` acts as the application layer so views stay thin.

---

## FILE 1 — model/Student.java

A **POJO** — holds one student's data and calculates results. No SQL, no Swing.

### Auto calculation in constructor

```java
public Student(String rollNumber, String name,
               double subject1, ... double subject5) {
    // assign fields...
    calculateResult();
}
```

Every new `Student` immediately has `total`, `percentage`, and `grade`. The UI, CSV import, and DAO all use the same rules.

### Grade logic

Percentage = average of 5 subjects (`total / 5`). Grades: A+ (90+), A (75+), B (60+), C (50+), D (35+), F (below 35).

---

## FILE 2 — util/AppConfig.java

Loads database settings from **`config.properties`** in the project folder (not committed to Git).

```properties
db.url=jdbc:mysql://localhost:3306/student_result_db
db.user=root
db.password=your_password
```

If the file is missing, defaults match the old hardcoded values (`root` / `root123`) so existing setups still work.

---

## FILE 3 — util/DBConnection.java

**Singleton** — one shared `Connection` for the whole app.

```java
connection = DriverManager.getConnection(
    AppConfig.getDbUrl(),
    AppConfig.getDbUser(),
    AppConfig.getDbPassword());
```

Credentials come from `AppConfig`, not from Java source code.

---

## FILE 4 — util/PasswordUtil.java

Passwords are **never stored in plain text**.

- **Hash:** PBKDF2-HMAC-SHA256, 600,000 iterations, random salt
- **Stored format:** `pbkdf2$sha256$<iterations>$<salt>$<hash>`
- **Verify:** `PasswordUtil.verify(plainPassword, storedHash)`

On first run after upgrade, old plain-text passwords in MySQL are automatically re-hashed in `StudentDAO.migratePlaintextPasswords()`.

---

## FILE 5 — dao/StudentDAO.java

The **only** class that runs SQL. Uses **try-with-resources** so `PreparedStatement` and `ResultSet` are closed properly.

### Key methods

| Method | Purpose |
|--------|---------|
| `setupTables()` | Creates tables, seeds subjects + admin, migrates passwords |
| `login()` | Loads hash by username, verifies with `PasswordUtil` |
| `addStudent()` | INSERT (throws on duplicate roll — caught by service) |
| `getStats()` | One query: total, passed, average |
| `importCSV()` | Bulk insert with `INSERT IGNORE` |
| `exportCSV()` | Export all students to CSV |

### Login (secure)

```java
PreparedStatement ps = con.prepareStatement(
    "SELECT password FROM users WHERE username=?");
// ...
if (PasswordUtil.verify(password, stored)) return true;
// Legacy: plain text once, then upgrade to hash
```

### Default admin

If `users` is empty, inserts `admin` with **hashed** password (`admin123` at first login only).

---

## FILE 6 — service/StudentService.java

Views call **service**, not DAO directly.

```java
public void addStudent(Student student) throws SQLException {
    dao.addStudent(student);
}

public static String friendlyMessage(Exception e) {
    // e.g. MySQL 1062 → "This roll number already exists..."
}
```

This keeps error messages helpful without exposing raw SQL errors to users.

---

## FILE 7 — util/ReportExporter.java

Builds the **text report** (`.txt`) — not PDF. Used by the **Export Report (.txt)** button.

Includes student rows, subject columns, and a summary (total, pass %, class average).

---

## FILE 8 — util/UiStyles.java

Applies shared styling to text fields and password fields:

- White text on dark background (visible with FlatLaf)
- Visible border; blue border on focus
- `setOpaque(true)` so typing works reliably on Windows

Used by `LoginView` and `MainView`.

---

## FILE 9 — view/Main.java

1. Sets **FlatDarkLaf** and text-field UI colors  
2. Calls `service.initialize()` (creates tables, migrates passwords)  
3. On DB error → dialog with setup hint  
4. Opens `LoginView`

```java
SwingUtilities.invokeLater(() -> {
    service.initialize();
    new LoginView(service);
});
```

---

## FILE 10 — view/LoginView.java

- Username + password fields (styled with `UiStyles`)
- Enter key submits login
- On success: `dispose()` → `new MainView(service)`
- No password hint on screen (see README for default dev credentials)

---

## FILE 11 — view/MainView.java

Largest UI class: form (left), table + toolbar (center), stats (top).

### Flow: add student

```
Read form → validate marks (0–100)
    → new Student(...)  // auto grade
    → service.addStudent(s)
    → refresh table + stats
```

### DocumentFilter (marks only)

Only **Marks 1–5** use `DocumentFilter` — letters blocked, values must be 0–100.  
**Student Name** accepts normal text.

### Export buttons

| Button | Output |
|--------|--------|
| Export CSV | `.csv` file |
| Export Report (.txt) | Printable text report |

---

## FILE 12 — test/model/StudentGradeTest.java

Simple tests for grade boundaries (run `main` from IDE or command line). No JUnit required.

---

## Database schema

See **`schema.sql`** for full DDL.

| Table | Notes |
|-------|--------|
| `students` | Marks, total, %, grade; `roll_number` UNIQUE |
| `subject_config` | 5 custom subject names |
| `users` | `password` VARCHAR(255) — stores **hash**, not plain text |

---

## Security checklist

| Practice | Where |
|----------|--------|
| External DB config | `config.properties` + `AppConfig` |
| Password hashing | `PasswordUtil` + `StudentDAO` |
| SQL injection prevention | `PreparedStatement` everywhere for user input |
| Legacy password upgrade | `migratePlaintextPasswords()` on startup |

---

## Key concepts

| Concept | Where |
|---------|--------|
| Layered architecture | view → service → dao → model |
| Singleton | `DBConnection` |
| DAO pattern | `StudentDAO` |
| PBKDF2 hashing | `PasswordUtil` |
| try-with-resources | `StudentDAO` |
| DocumentFilter | `MainView` marks fields |
| INSERT IGNORE | CSV import |
| Externalized config | `config.properties` |

---

*Built by Anuj Jadhav — portfolio / academic project*

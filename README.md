# 🎓 Student Result Management System

A professional Java desktop application for school and college administrators to manage student marks, generate results, and handle data portability — built with Java Swing, MySQL, and MVC architecture.

---

## 🖼️ What It Does

- **Secure Login** — Admin authentication before accessing the dashboard
- **Quick Stats Dashboard** — Total students, Pass %, and Class Average shown at a glance
- **Add / Update / Delete Students** — Full CRUD with instant result calculation
- **Auto Grade Calculation** — Total, Percentage, and Grade calculated automatically
- **Search & Filter** — Search by name or roll number, filter by Pass/Fail
- **Custom Subject Names** — Change subject names and reflect them in table headers instantly
- **Bulk CSV Import** — Upload an existing CSV of 50+ students in one click
- **CSV Export** — Download all results as a clean CSV file
- **Result Report Export** — Generate a formatted printable result report
- **Input Validation** — Marks fields only accept numbers between 0 and 100

---

## 📸 Grade System

| Percentage | Grade |
|---|---|
| 90% and above | A+ |
| 75% – 89% | A |
| 60% – 74% | B |
| 50% – 59% | C |
| 35% – 49% | D |
| Below 35% | F (Fail) |

---

## 🛠️ Tech Stack

| Tool | Purpose |
|---|---|
| Java 24 | Core language |
| Java Swing | Desktop GUI |
| MySQL 8.0 | Local database |
| JDBC | Java-MySQL connection |
| MVC Architecture | Clean code structure |
| FlatLaf | Modern UI look and feel |

---

## 📂 Project Structure

```
StudentResultSystem/
└── src/
    ├── model/
    │   └── Student.java          # Student data class (POJO)
    ├── dao/
    │   └── StudentDAO.java       # All database queries (SQL)
    ├── util/
    │   └── DBConnection.java     # Singleton DB connection manager
    └── view/
        ├── Main.java             # Entry point
        ├── LoginView.java        # Login screen
        └── MainView.java         # Main dashboard
```

---

## 🚀 How to Run

### Prerequisites
- Java JDK 17 or above
- MySQL 8.0
- NetBeans IDE (recommended) or any Java IDE
- MySQL JDBC Connector JAR

### Step 1 — Clone the repo
```bash
git clone https://github.com/Anuj230977/student-result-system.git
```

### Step 2 — Set up MySQL database
```sql
CREATE DATABASE student_result_db;
USE student_result_db;
```

### Step 3 — Configure DB credentials
Open `util/DBConnection.java` and update:
```java
private static final String PASS = "your_mysql_password";
```

### Step 4 — Add MySQL JDBC JAR
- Download from: https://dev.mysql.com/downloads/connector/j/
- Add to project Libraries in your IDE

### Step 5 — Run the project
- Run `view/Main.java` as the main class
- Default login: **admin / admin123**

---

## 📋 CSV Import Format

When importing bulk student data, your CSV must follow this format:

```
roll_number, name, subject1, subject2, subject3, subject4, subject5
BCA001, Rahul Sharma, 85, 78, 92, 88, 76
BCA002, Priya Patel, 72, 65, 80, 70, 68
```

- First row is the header (skipped automatically)
- Duplicate roll numbers are ignored via INSERT IGNORE
- Grades and totals are calculated automatically on import

---

## 💡 Use Cases

- **Schools & Colleges** — Manage semester or annual exam results
- **Coaching Centers** — Track student performance across subjects
- **Teachers** — Generate and print class result reports
- **Freelance Clients** — Custom result management for any institution

---

## 🔐 Default Login Credentials

| Field | Value |
|---|---|
| Username | admin |
| Password | admin123 |

To change credentials, update the `users` table directly in MySQL.

---

## 👤 Author

**Anuj Jadhav**
- 🎓 TY BBA-CA Student
- 📧 anuj1230567@gmail.com
- 💼 [LinkedIn](https://www.linkedin.com/in/anujjadhav)
- 🐙 [GitHub](https://github.com/Anuj230977)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

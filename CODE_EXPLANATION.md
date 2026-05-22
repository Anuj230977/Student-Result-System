# 🧠 Code Explanation — Student Result Management System

A complete walkthrough of every file, every class, every design decision — explained simply and clearly.

---

## 📁 Project Architecture — MVC Pattern

This project follows the **MVC (Model-View-Controller)** pattern, which separates code into clear responsibilities:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    MODEL    │     │    VIEW     │     │     DAO     │
│  Student.java│◄───│  MainView   │────►│ StudentDAO  │
│  (Data)     │     │  LoginView  │     │  (Database) │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │
                                        ┌──────▼──────┐
                                        │    UTIL     │
                                        │DBConnection │
                                        │ (Singleton) │
                                        └─────────────┘
```

| Package | File | Responsibility |
|---|---|---|
| `model` | `Student.java` | Holds student data and calculates results |
| `dao` | `StudentDAO.java` | All SQL queries — no SQL anywhere else |
| `util` | `DBConnection.java` | Single shared database connection |
| `view` | `LoginView.java` | Login screen UI |
| `view` | `MainView.java` | Main dashboard UI |
| `view` | `Main.java` | Entry point — starts the app |

---

## 📄 FILE 1 — model/Student.java

### What it is:
A **POJO (Plain Old Java Object)** — a simple class that holds one student's data. No database code, no UI code — just data and one calculation.

### Key concept — Auto Calculation in Constructor:
```java
public Student(String rollNumber, String name,
               double subject1, double subject2, double subject3,
               double subject4, double subject5) {
    this.rollNumber = rollNumber;
    this.name = name;
    // ... assign subjects ...
    calculateResult(); // ← called automatically
}
```
Every time a Student object is created, the result is calculated immediately. The UI never has to manually calculate grades — it just creates a Student and the object already knows its grade.

### Grade Calculation Logic:
```java
private void calculateResult() {
    this.total = subject1 + subject2 + subject3 + subject4 + subject5;
    this.percentage = total / 5.0;
    if (percentage >= 90)      this.grade = "A+";
    else if (percentage >= 75) this.grade = "A";
    else if (percentage >= 60) this.grade = "B";
    else if (percentage >= 50) this.grade = "C";
    else if (percentage >= 35) this.grade = "D";
    else                       this.grade = "F";
}
```
Percentage is calculated as average of 5 subjects (total / 5), not total / 500. This gives a clean 0-100 percentage.

### Why Getters Only (No Setters):
Once a Student is created, its data shouldn't change randomly. If marks change, we create a new Student object with updated marks. This is called **immutability** — a professional coding practice.

---

## 📄 FILE 2 — util/DBConnection.java

### What it is:
A **Singleton** — a class that ensures only ONE database connection exists at a time across the entire application.

### The Singleton Pattern:
```java
private static Connection connection = null;

public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(URL, USER, PASS);
    }
    return connection;
}
```

### Why Singleton?
Without it, every method would open a new connection to MySQL — wasteful and slow. With Singleton, the connection is opened once and reused everywhere.

### Flow:
```
First call  → connection is null → create new connection → return it
Second call → connection exists  → return same connection
App closes  → closeConnection() → connection set to null
```

---

## 📄 FILE 3 — dao/StudentDAO.java

### What it is:
The **Data Access Object** — the only place in the entire project that talks to MySQL. All SQL queries live here.

### Why DAO Pattern?
If the database changes (e.g. switching from MySQL to PostgreSQL), you only change this one file. The UI never needs to know how data is stored.

### Key Methods:

**setupTables()** — Creates all tables if they don't exist on first run:
```java
con.createStatement().executeUpdate(
    "CREATE TABLE IF NOT EXISTS students (..."
);
```
`IF NOT EXISTS` means running it multiple times is safe — it won't crash or duplicate tables.

**login()** — Simple credential check:
```java
PreparedStatement ps = con.prepareStatement(
    "SELECT * FROM users WHERE username=? AND password=?");
ps.setString(1, username);
ps.setString(2, password);
ResultSet rs = ps.executeQuery();
return rs.next(); // true if a row was found
```
Uses `PreparedStatement` instead of string concatenation — this prevents **SQL Injection** attacks.

**addStudent()** — Inserts a Student object into the DB:
```java
public boolean addStudent(Student s) throws SQLException {
    PreparedStatement ps = con.prepareStatement(
        "INSERT INTO students (...) VALUES (?,?,?,?,?,?,?,?,?,?)");
    ps.setString(1, s.getRollNumber());
    // ... set all values from the Student object
    return ps.executeUpdate() > 0;
}
```
Returns `true` if the insert succeeded, `false` if it failed. The UI uses this to show the right message.

**mapResultSet()** — Converts database rows into Student objects:
```java
private List<Student> mapResultSet(ResultSet rs) throws SQLException {
    List<Student> list = new ArrayList<>();
    while (rs.next()) {
        Student s = new Student(
            rs.getString("roll_number"), rs.getString("name"),
            rs.getDouble("subject1"), ...
        );
        s.setId(rs.getInt("id"));
        list.add(s);
    }
    return list;
}
```
This is called by `getAllStudents()`, `searchStudents()`, `getPassStudents()`, `getFailStudents()` — all of them reuse this one method to avoid repeating the same conversion code.

**getStats()** — Single SQL query that returns 4 stats at once:
```java
"SELECT COUNT(*) as total, " +
"SUM(CASE WHEN grade != 'F' THEN 1 ELSE 0 END) as passed, " +
"AVG(percentage) as avg_percent FROM students"
```
`CASE WHEN` inside `SUM` is a SQL trick — it counts only rows where the condition is true. More efficient than running 3 separate queries.

**importCSV()** — Reads a CSV file line by line:
```java
String line;
while ((line = br.readLine()) != null) {
    String[] p = line.split(",");
    Student s = new Student(p[0], p[1],
        Double.parseDouble(p[2]), ...);
    // INSERT IGNORE skips duplicates
}
return new int[]{success, failed};
```
`INSERT IGNORE` is a MySQL feature — if a student with the same roll number already exists, it skips that row silently instead of throwing an error.

---

## 📄 FILE 4 — view/LoginView.java

### What it is:
The login screen that appears first. Blocks access to the main dashboard until valid credentials are entered.

### Login Flow:
```
User types username + password
        │
        ▼
dao.login(username, password)
        │
        ├── Match found → dispose() login window → open MainView
        │
        └── No match → show error → clear password field
```

### Enter Key Support:
```java
txtPassword.addKeyListener(new KeyAdapter() {
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) login();
    }
});
```
Users can press Enter instead of clicking the button — standard UX behavior.

### Default Credentials:
Created automatically in `StudentDAO.setupTables()` if the users table is empty:
```sql
INSERT IGNORE INTO users (username, password) VALUES ('admin', 'admin123')
```

---

## 📄 FILE 5 — view/MainView.java

### What it is:
The main dashboard — the largest file, handles all UI interactions.

### Layout Structure:
```
┌─────────────────────────────────────────────────┐
│  TOP PANEL — Title + Stats Cards                │
├──────────────┬──────────────────────────────────┤
│ FORM PANEL   │  TABLE PANEL                     │
│ Subject Names│  Search Bar + Filter + Buttons   │
│ Student Input│  ─────────────────────────────   │
│ Result Labels│  Student Table (scrollable)      │
│ Action Buttons│                                 │
└──────────────┴──────────────────────────────────┘
```

### Stats Cards:
```java
JPanel makeStatCard(String title, JLabel valueLbl, Color bg) {
    JPanel card = new JPanel(new BorderLayout(3, 3));
    card.setBackground(bg);
    card.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    // title on top, value in center
    card.add(titleLbl, BorderLayout.NORTH);
    card.add(valueLbl, BorderLayout.CENTER);
    return card;
}
```
Each stat (Total, Pass %, Average) is a colored card panel with a title and a value label that updates every time data changes.

### DocumentFilter — Marks Validation:
```java
void applyMarksFilter(JTextField field) {
    ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
        boolean isValid(String text) {
            if (text.isEmpty()) return true;
            try {
                double val = Double.parseDouble(text);
                return val >= 0 && val <= 100;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    });
}
```
Instead of showing an error after the user types wrong input, `DocumentFilter` prevents wrong input from being typed at all. Letters are blocked. Numbers above 100 are blocked. The field only accepts valid marks.

### addStudent() Flow:
```
Read txtRoll, txtName, txtS1...S5
        │
        ▼
Validate: empty check + marks range check
        │
        ▼
Create new Student object (auto-calculates grade)
        │
        ▼
Update result labels (Total, %, Grade)
        │
        ▼
dao.addStudent(student) → INSERT into MySQL
        │
        ▼
loadAllStudents() → refresh table
updateStats()     → refresh stat cards
clearFields()     → reset form
```

### fillFormFromTable():
```java
void fillFormFromTable() {
    int row = table.getSelectedRow();
    if (row == -1) return;
    txtRoll.setText(tableModel.getValueAt(row, 1).toString());
    // ... fill all fields from selected row
}
```
When you click any row in the table, all form fields auto-fill with that student's data. This makes editing and deleting much faster — no manual re-typing.

### updateTableHeaders():
```java
void updateTableHeaders() {
    tableModel.setColumnIdentifiers(new Object[]{
        "ID", "Roll No", "Name",
        subjectNames[0], subjectNames[1], subjectNames[2],
        subjectNames[3], subjectNames[4],
        "Total", "%", "Grade"
    });
}
```
When subject names are saved, this updates the table column headers instantly without restarting the app.

---

## 📄 FILE 6 — view/Main.java

### What it is:
The entry point — 5 lines, starts everything.

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(LoginView::new);
}
```

`SwingUtilities.invokeLater` ensures the GUI is created on the **Event Dispatch Thread (EDT)** — Java's dedicated thread for UI. Creating Swing components on the wrong thread causes random crashes. This one line prevents that.

---

## 🗄️ Database Schema

### students table
| Column | Type | Purpose |
|---|---|---|
| id | INT AUTO_INCREMENT | Unique identifier |
| roll_number | VARCHAR(20) UNIQUE | Student's roll number |
| name | VARCHAR(100) | Student's full name |
| subject1-5 | FLOAT | Individual subject marks |
| total | FLOAT | Sum of all 5 subjects |
| percentage | FLOAT | Average (total / 5) |
| grade | VARCHAR(5) | A+, A, B, C, D, or F |
| created_at | TIMESTAMP | Auto-set on insert |

### subject_config table
| Column | Type | Purpose |
|---|---|---|
| id | INT | Subject number (1-5) |
| subject_name | VARCHAR(100) | Custom name for each subject |

### users table
| Column | Type | Purpose |
|---|---|---|
| id | INT AUTO_INCREMENT | Unique identifier |
| username | VARCHAR(50) UNIQUE | Login username |
| password | VARCHAR(50) | Login password |

---

## 💡 Key Concepts Learned

| Concept | Where Used |
|---|---|
| MVC Architecture | Entire project structure |
| Singleton Pattern | DBConnection.java |
| DAO Pattern | StudentDAO.java |
| POJO / Data Class | Student.java |
| PreparedStatement | All SQL queries (SQL Injection prevention) |
| INSERT IGNORE | CSV bulk import (duplicate handling) |
| DocumentFilter | Marks field input validation |
| Event Dispatch Thread | Main.java SwingUtilities.invokeLater |
| CASE WHEN in SQL | getStats() — conditional counting |
| ResultSet mapping | mapResultSet() — DB rows to objects |

---

*Built by Anuj Jadhav — TY BBA-CA Student*
*Part of the Freelance Portfolio Series — Project 3 of 3*

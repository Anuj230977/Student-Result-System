Since you are using this to prompt Claude (or any other AI) to finalize the
code, this needs to be written as a Technical Specification Document. AI models
work best when they have a clear "Source of Truth" that defines the
architecture, the boundaries, and the goals.

Copy and paste the content below into a file named project_blueprint.md or just
paste the entire block into your chat with Claude.

PROJECT BLUEPRINT: Student Result Management System (Professional Edition)

1. Project Overview

Goal: To transform a basic Student Result System into a professional-grade
portfolio/Fiverr project. The application is a Java Swing Desktop Application
designed for school administrators or class teachers to manage student marks,
generate results, and handle data portability.

Target User: A single Admin/Teacher operating on a local machine. Core Value
Proposition: Ease of data entry, secure local storage, and high-fidelity data
import/export.

2. Current State Analysis

  - Current Logic: Single-class "God Class" architecture.
  - UI: Custom dark-themed Swing components.
  - Database: MySQL (Localhost).
  - Features: CRUD operations, CSV Import/Export, Text-based report generation,
    Basic Search/Filter.

3. Professional Refinement Goals (The "Pro" Touch)

The goal is to move the project from a "Student Assignment" level to a
"Freelance Professional" level.

A. Architectural Overhaul (MVC Pattern)

The code must be refactored from a single file into a structured package system:

1.  model Package: Create a Student POJO class to hold data.
2.  dao (Data Access Object) Package: Create a StudentDAO class. All SQL queries
    (SELECT, INSERT, UPDATE, DELETE) must reside here. The UI should never call
    DriverManager directly.
3.  view Package: The JFrame and UI logic.
4.  util Package: Database connection management (Singleton pattern) and helper
    methods.

B. UI/UX Modernization

  - Look & Feel: Implement FlatLaf (Flat Dark Laf) to replace the standard Swing
    look with a modern, IntelliJ-like interface.
  - Input Validation: Move away from try-catch error popups for marks. Use
    DocumentFilter on JTextFields to prevent users from typing non-numeric
    characters or numbers outside the 0–100 range.
  - Dashboard Elements: Instead of just a table, add a "Quick Stats" panel at
    the top showing:
      - Total Students.
      - Overall Pass Percentage.
      - Class Average.

C. Security & Access

  - Admin Authentication: Implement a simple Login Screen. Since this is a local
    tool, a simple users table in MySQL is sufficient.
  - Role: The system is strictly for Admin/Teacher use. No student-facing GUI is
    required (to avoid cloud database complexity and costs).

D. Enhanced Reporting

  - PDF Export: Replace the .txt export with a professional PDF report using a
    library like iText or Apache PDFBox, featuring a clean table layout and a
    professional header.

4. Technical Constraints & Boundaries (The "No-Go" Zone)

To avoid over-engineering and unnecessary costs, the following are EXCLUDED from
this project:

  - NO Student Portal/GUI: The app remains a local desktop tool. Students do not
    log in.
  - NO Cloud Hosting: The database remains localhost.
  - NO Complex Backup Systems: The CSV Export/Import serves as the primary data
    backup and portability mechanism.
  - NO Web Integration: This is a pure Java Swing application.

5. Feature Specification Detail

  - Subject Configuration: Ability to change subject names in the DB and have
    them reflect immediately in the table headers.
  - CSV Engine:
      - Import: Must handle bulk data (e.g., 50+ students) and ignore duplicates
        via INSERT IGNORE.
      - Export: Must export all current database records into a clean CSV
        format.
  - Search & Filter: Real-time search by Name/Roll Number and a dropdown filter
    for "Pass" vs "Fail" status.

6. Implementation Roadmap for AI

When updating the code, follow these steps in order:

1.  Step 1: Refactor to MVC. Split the existing "God Class" into Student,
    StudentDAO, and StudentView.
2.  Step 2: Implement FlatLaf. Update the UI to use a modern Look and Feel.
3.  Step 3: Add Login Screen. Create a simple authentication gate before the
    main dashboard.
4.  Step 4: Enhance Validation. Add DocumentFilters to mark fields.
5.  Step 5: Upgrade Export. Transition from .txt to a real .pdf report.
6.  Step 6: Final Polish. Add the "Quick Stats" dashboard cards.

7. Portfolio Positioning (Marketing)

This project will be presented as a "Local Academic Management Tool."

  - Selling Point 1: Secure local data management for educators.
  - Selling Point 2: High-efficiency bulk data handling via CSV.
  - Selling Point 3: Modern UI/UX focused on productivity and reduced data-entry
    error.

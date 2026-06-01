package util;

import dao.StudentDAO;
import model.Student;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/** Writes a plain-text printable result report (.txt). */
public final class ReportExporter {

    private ReportExporter() {
    }

    public static void exportTextReport(String filePath, String[] subjectNames,
            List<Student> students, int[] stats) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("=".repeat(95));
            pw.println("              STUDENT RESULT MANAGEMENT SYSTEM — RESULT REPORT");
            pw.println("=".repeat(95));
            pw.println("Generated: " + new Date());
            pw.println();
            pw.printf("%-12s %-20s %-8s %-8s %-8s %-8s %-8s %-8s %-8s %-6s%n",
                    "Roll No", "Name",
                    shorten(subjectNames[0]), shorten(subjectNames[1]),
                    shorten(subjectNames[2]), shorten(subjectNames[3]),
                    shorten(subjectNames[4]), "Total", "Percent", "Grade");
            pw.println("-".repeat(95));

            for (Student s : students) {
                pw.printf("%-12s %-20s %-8.1f %-8.1f %-8.1f %-8.1f %-8.1f %-8.1f %-8.2f %-6s%n",
                        s.getRollNumber(), s.getName(),
                        s.getSubject1(), s.getSubject2(), s.getSubject3(),
                        s.getSubject4(), s.getSubject5(),
                        s.getTotal(), s.getPercentage(), s.getGrade());
            }

            int total = stats[0], passed = stats[1], failed = stats[2], avg = stats[3];
            pw.println("=".repeat(95));
            pw.println();
            pw.println("SUMMARY");
            pw.println("-".repeat(30));
            pw.println("Total Students : " + total);
            pw.println("Passed         : " + passed);
            pw.println("Failed         : " + failed);
            pw.println("Pass %         : "
                    + (total > 0 ? String.format("%.1f%%", passed * 100.0 / total) : "N/A"));
            pw.println("Class Average  : " + avg + "%");
            pw.println("=".repeat(95));
        }
    }

    public static void exportTextReport(String filePath, String[] subjectNames,
            StudentDAO dao) throws SQLException, IOException {
        exportTextReport(filePath, subjectNames, dao.getAllStudents(), dao.getStats());
    }

    private static String shorten(String s) {
        return s.length() > 7 ? s.substring(0, 7) : s;
    }
}

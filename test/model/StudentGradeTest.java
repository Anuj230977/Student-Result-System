package test.model;

import model.Student;

/**
 * Simple grade-calculation tests (run main from IDE or: java -cp build/classes model.StudentGradeTest).
 */
public class StudentGradeTest {

    public static void main(String[] args) {
        int failed = 0;
        failed += assertGrade(90, 90, 90, 90, 90, "A+");
        failed += assertGrade(75, 75, 75, 75, 75, "A");
        failed += assertGrade(60, 60, 60, 60, 60, "B");
        failed += assertGrade(50, 50, 50, 50, 50, "C");
        failed += assertGrade(35, 35, 35, 35, 35, "D");
        failed += assertGrade(34, 34, 34, 34, 34, "F");
        failed += assertGrade(89, 89, 89, 89, 89, "A");

        if (failed == 0) {
            System.out.println("All grade tests passed.");
        } else {
            System.err.println(failed + " test(s) failed.");
            System.exit(1);
        }
    }

    private static int assertGrade(double s1, double s2, double s3, double s4, double s5,
            String expected) {
        Student s = new Student("T001", "Test", s1, s2, s3, s4, s5);
        if (expected.equals(s.getGrade())) {
            return 0;
        }
        System.err.println("Expected " + expected + " but got " + s.getGrade()
                + " for average " + s.getPercentage());
        return 1;
    }
}

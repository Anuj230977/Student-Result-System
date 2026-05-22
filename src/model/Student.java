package model;

public class Student {
    private int id;
    private String rollNumber;
    private String name;
    private double subject1, subject2, subject3, subject4, subject5;
    private double total, percentage;
    private String grade;

    // Constructor
    public Student(String rollNumber, String name,
                   double subject1, double subject2, double subject3,
                   double subject4, double subject5) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.subject1 = subject1;
        this.subject2 = subject2;
        this.subject3 = subject3;
        this.subject4 = subject4;
        this.subject5 = subject5;
        calculateResult();
    }

    // Auto calculate total, percentage, grade
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

    // Getters
    public int getId()             { return id; }
    public String getRollNumber()  { return rollNumber; }
    public String getName()        { return name; }
    public double getSubject1()    { return subject1; }
    public double getSubject2()    { return subject2; }
    public double getSubject3()    { return subject3; }
    public double getSubject4()    { return subject4; }
    public double getSubject5()    { return subject5; }
    public double getTotal()       { return total; }
    public double getPercentage()  { return percentage; }
    public String getGrade()       { return grade; }
    public boolean isPass()        { return !grade.equals("F"); }

    // Setter for id (set after DB insert)
    public void setId(int id)      { this.id = id; }
}
// SOLUTION: Exercise 2 — Variables
// ==================================
// Study this only AFTER genuinely attempting Exercise02_Variables.java yourself.

public class Exercise02_Solution {

    public static void main(String[] args) {
        // Solution to TODO 1: Declare a String variable 'name'
        String name = "Alice";

        // Solution to TODO 2: Declare an int variable 'age'
        int age = 25;

        // Solution to TODO 3: Declare a double variable 'height' (in metres)
        double height = 1.75;

        // Solution to TODO 4: Declare a boolean variable 'isStudent'
        boolean isStudent = true;

        // Solution to TODO 5: Declare a char variable 'grade'
        char grade = 'A';

        System.out.println("--- Person Info ---");

        // Solution to TODO 6: Print one combined sentence with all the info.
        // The + operator concatenates Strings. Non-String values are automatically
        // converted to their String representation when combined with a String.
        System.out.println(name + " is " + age + " years old, " +
                           height + "m tall, is a student: " + isStudent +
                           ", and got grade: " + grade);

        System.out.println();
        System.out.println("--- Individual Fields ---");

        // Solution to TODO 7: Print each field on its own labelled line.
        // "Name: " + name concatenates the label with the value.
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Height: " + height + "m");
        System.out.println("Student: " + isStudent);
        System.out.println("Grade: " + grade);
    }
}

// This is your very first Java program.
// Every Java program starts with a class. The class name must match the filename.
// This file is named HelloWorld.java, so the class is named HelloWorld.
public class HelloWorld {

    // This is the main method — Java starts executing your program here.
    // Think of it as the "start button" of your program.
    //
    // Every Java application must have exactly one main method with this signature:
    //   public static void main(String[] args)
    //
    // Breaking it down:
    //   public  — anyone can call this method (it's accessible from outside the class)
    //   static  — belongs to the class itself, not to a specific object
    //   void    — this method doesn't return a value
    //   main    — this specific name is what Java looks for to start the program
    //   String[] args — an array of command-line arguments (ignore this for now)
    public static void main(String[] args) {

        // System.out.println() prints a line of text to the console.
        //
        // Breaking it down:
        //   System   — a built-in Java class (part of java.lang package)
        //   out      — a field of System that represents standard output (your terminal)
        //   println  — a method that prints text followed by a newline
        //
        // The text inside the parentheses and quotes is called a String literal.
        System.out.println("Hello, World!");

        // You can print as many lines as you want.
        // Each println() call starts a new line.
        System.out.println("Welcome to Java!");

        // Variables let you store and reuse values.
        // Here we create a variable named 'name' of type String.
        //
        //   String  — the type (this variable holds text)
        //   name    — the variable name (you choose this)
        //   =       — the assignment operator (stores the value on the right into the variable on the left)
        //   "Learner" — the value being stored (a String literal)
        String name = "Learner";

        // The + operator, when used with Strings, concatenates (joins) them together.
        // "Hello, " + name + "! Let's learn Java." joins three pieces:
        //   "Hello, "       — a String literal
        //   name            — the value of our variable (currently "Learner")
        //   "! Let's learn Java." — another String literal
        System.out.println("Hello, " + name + "! Let's learn Java.");

        // TRY IT: Change "Learner" on line 34 to your own name,
        // then recompile and run. See your name appear in the output!
    }
}

// PersonalInfoCard.java
// =====================
// Mini-Project: Display a personal info card using variables and arithmetic.
//
// This file has two clearly marked sections:
//   STARTER SECTION  — read this first. Try to build the solution yourself.
//   SOLUTION SECTION — complete working solution. Study AFTER attempting it yourself.
//
// ==============================================================================
// STARTER SECTION — Your Starting Point
// ==============================================================================
//
// Your task: Complete this program so it prints a formatted personal info card.
//
// The program should:
//   1. Declare variables for: name, age, height (metres), weight (kg),
//      favourite language, isStudent, and first initial
//   2. Calculate: birth year (2024 - age), BMI (weight / height^2)
//   3. Determine age category: Young Adult (<30), Adult (30-60), Senior (>60)
//   4. Print a formatted card showing all this information
//
// Example output:
//   ================================
//   PERSONAL INFO CARD
//   ================================
//   Name:      Alice Johnson
//   Initial:   A
//   Age:       25 (born ~1999)
//   Height:    1.75m
//   Weight:    70.0kg
//   BMI:       22.9
//   Category:  Young Adult
//   Language:  Java
//   Student:   Yes
//   ================================
//
// STOP HERE. Try to write the program yourself.
// Only scroll down to the SOLUTION SECTION after attempting it.
//
// ==============================================================================

public class PersonalInfoCard {

    public static void main(String[] args) {

        // ===========================================================
        // SOLUTION SECTION — Complete Working Solution
        // ===========================================================
        // Study this after attempting it yourself.
        // Read every comment — they explain not just WHAT but WHY.

        // ---- STEP 1: Declare and initialise variables ---------------

        // The person's data — these are the "inputs" to our program.
        // In a real application, these might come from a database or user input.
        String fullName = "Alice Johnson";
        int age = 25;
        double heightMetres = 1.75;   // height in metres
        double weightKg = 70.0;       // weight in kilograms
        String favouriteLanguage = "Java";
        boolean isStudent = true;
        char firstInitial = 'A';

        // ---- STEP 2: Calculate derived values ----------------------

        // Birth year: approximate (ignores whether birthday has passed this year).
        // Using int for birth year because years are whole numbers.
        int currentYear = 2024;
        int birthYear = currentYear - age;

        // BMI (Body Mass Index): weight in kg / (height in metres)^2
        // We need a double result here, so we use double arithmetic.
        // heightMetres * heightMetres computes height squared.
        double bmi = weightKg / (heightMetres * heightMetres);

        // Round BMI to 1 decimal place for display.
        // Math.round(x * 10) / 10.0 is a common trick:
        //   bmi * 10 = 229.something
        //   Math.round rounds to nearest integer: 229 (or 230 etc.)
        //   / 10.0 gives us back 22.9
        //
        // Alternative (cleaner): use String.format("%.1f", bmi) for display.
        // We'll use the manual approach here to stay with Module 01 concepts.
        double bmiRounded = Math.round(bmi * 10) / 10.0;

        // Age category: determine which life stage this person is in.
        // We use a ternary operator: condition ? valueIfTrue : valueIfFalse
        // Nested ternaries work but can be hard to read — keep them shallow.
        String ageCategory;
        if (age < 30) {
            ageCategory = "Young Adult";
        } else if (age <= 60) {
            ageCategory = "Adult";
        } else {
            ageCategory = "Senior";
        }

        // Convert boolean isStudent to a more user-friendly "Yes" / "No"
        // Printing "true"/"false" is fine, but "Yes"/"No" looks better in a card.
        String studentDisplay = isStudent ? "Yes" : "No";

        // ---- STEP 3: Print the formatted card ----------------------

        // Define the card's width as a constant so we can reuse it.
        // A String of dashes creates our horizontal border.
        String border = "================================";

        System.out.println(border);
        System.out.println("PERSONAL INFO CARD");
        System.out.println(border);

        // Print each field on its own line.
        // The label is padded to 10 characters so values line up neatly.
        // In Module 02, you'll learn String.format for cleaner alignment.
        System.out.println("Name:      " + fullName);
        System.out.println("Initial:   " + firstInitial);
        System.out.println("Age:       " + age + " (born ~" + birthYear + ")");
        System.out.println("Height:    " + heightMetres + "m");
        System.out.println("Weight:    " + weightKg + "kg");
        System.out.println("BMI:       " + bmiRounded);
        System.out.println("Category:  " + ageCategory);
        System.out.println("Language:  " + favouriteLanguage);
        System.out.println("Student:   " + studentDisplay);

        System.out.println(border);

        // ---- STEP 4: Verify logic with a couple of checks ----------

        // It is good practice to sanity-check your calculations.
        // These would not be in a production program, but they help while learning.
        System.out.println();
        System.out.println("--- Calculation Check ---");
        System.out.println("BMI formula: " + weightKg + " / (" + heightMetres + "^2)");
        System.out.println("         = " + weightKg + " / " + (heightMetres * heightMetres));
        System.out.println("         = " + bmi + " (unrounded)");
        System.out.println("         = " + bmiRounded + " (rounded to 1dp)");

        // ---- WHAT TO CHANGE TO MAKE IT YOUR OWN -------------------
        // Change the values in STEP 1 to your own real (or fictional) data.
        // The calculations and display automatically update.
        // Try different ages to see how the age category changes.
    }
}

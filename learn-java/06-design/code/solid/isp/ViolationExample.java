package solid.isp;

/**
 * ============================================================
 *  SOLID PRINCIPLE #4 — Interface Segregation Principle (ISP)
 * ============================================================
 *
 *  DEFINITION:
 *    "Clients should not be forced to depend upon interfaces that they
 *     do not use."
 *    — Robert C. Martin (Uncle Bob)
 *
 *  Equivalently:
 *    "Make fine-grained interfaces that are client-specific."
 *
 *  What is a "fat interface"?
 *    A fat interface is one that has too many methods, forcing every class
 *    that implements it to provide implementations for methods that are
 *    irrelevant to that class.
 *
 *  WHY FAT INTERFACES ARE HARMFUL:
 *    1. Classes must provide "dummy" implementations that throw exceptions
 *       or do nothing — this is a red flag that the interface is wrong.
 *    2. Clients of the interface must import/depend on methods they never use.
 *    3. When the interface changes (a new method is added), ALL implementing
 *       classes must be updated — even those that don't need the new method.
 *    4. It becomes impossible to reason about what a class "can do" by looking
 *       at its interface.
 *
 *  THE VIOLATION BELOW:
 *    Worker interface is fat — it combines methods for humans and robots.
 *    Robot class is forced to implement eat(), sleep(), and attendMeetings()
 *    even though robots do none of these things. The only honest implementation
 *    is to throw UnsupportedOperationException — which is a broken contract.
 *
 *    This also violates LSP: you can't substitute Robot for Worker in code
 *    that calls eat() without getting an exception.
 */
public class ViolationExample {

    // =========================================================================
    //  THE FAT INTERFACE: Worker
    //
    //  This interface tries to describe EVERYTHING any worker can do.
    //  But "workers" is a broad category — human developers and factory robots
    //  are both workers, yet they have completely different capabilities.
    //
    //  Forcing all workers to implement all methods is the ISP violation.
    // =========================================================================

    /**
     * Fat interface — tries to represent ALL types of workers in ONE interface.
     *
     * ISP VIOLATION:
     *   This interface has methods that only apply to SOME workers.
     *   - eat(), sleep() apply to biological beings, not machines.
     *   - attendMeetings() applies to office workers, not factory robots.
     *   - doPhysicalLabor() applies to physical workers, not office workers.
     *   - writeCode() applies to software developers, not all workers.
     *
     *   Any class implementing this interface must implement ALL methods,
     *   even the irrelevant ones. That forces nonsensical implementations.
     */
    interface Worker {
        /**
         * Performs the core work of this worker.
         * Applies to: ALL workers.
         */
        void work();

        /**
         * Eats food for energy.
         * Applies to: humans and animals. NOT robots, NOT software agents.
         *
         * ISP VIOLATION: Robots must implement this despite being machines.
         */
        void eat();

        /**
         * Sleeps to recover.
         * Applies to: biological beings. NOT robots.
         *
         * ISP VIOLATION: Robots must implement this method with a fake body.
         */
        void sleep();

        /**
         * Attends office meetings.
         * Applies to: office workers. NOT factory robots or remote systems.
         *
         * ISP VIOLATION: A factory robot has no concept of "attending a meeting".
         * Yet it is forced to provide an implementation.
         */
        void attendMeetings();

        /**
         * Writes code.
         * Applies to: software developers. NOT managers, NOT factory robots.
         *
         * ISP VIOLATION: A manager class would be forced to stub this out
         * even though managers don't write code.
         */
        void writeCode();

        /**
         * Performs physical labor.
         * Applies to: factory workers, physical robots. NOT office workers.
         *
         * ISP VIOLATION: Office developers must implement this method even
         * though they never do physical labor (beyond typing).
         */
        void doPhysicalLabor();
    }

    // =========================================================================
    //  HUMAN DEVELOPER: Implements most methods legitimately
    //
    //  A human developer can work, eat, sleep, attend meetings, and write code.
    //  BUT they don't do "physical labor" in the factory sense.
    //  They still have to implement doPhysicalLabor() because the interface
    //  demands it — and that implementation is either empty (misleading) or
    //  throws an exception (dangerous).
    // =========================================================================

    static class HumanDeveloper implements Worker {

        private final String name;

        HumanDeveloper(String name) { this.name = name; }

        @Override
        public void work() {
            System.out.println("[" + name + "] Working on feature tickets...");
        }

        @Override
        public void eat() {
            System.out.println("[" + name + "] Eating lunch at the cafeteria.");
        }

        @Override
        public void sleep() {
            System.out.println("[" + name + "] Sleeping 8 hours.");
        }

        @Override
        public void attendMeetings() {
            System.out.println("[" + name + "] Attending daily standup and sprint review.");
        }

        @Override
        public void writeCode() {
            System.out.println("[" + name + "] Writing Java code in IntelliJ IDEA.");
        }

        @Override
        // ISP VIOLATION: HumanDeveloper is forced to implement a method that
        // doesn't apply to them. The honest answer is "I don't do this."
        public void doPhysicalLabor() {
            // Option A: Do nothing — misleading (caller gets no feedback)
            // Option B: Throw exception — dangerous (breaks calling code)
            // Neither option is good. This is a symptom of the fat interface.
            throw new UnsupportedOperationException(
                name + " is a software developer and does not perform physical labor. "
                + "But the Worker interface forces this method to exist. "
                + "This is the ISP violation."
            );
        }
    }

    // =========================================================================
    //  ROBOT: The clearest demonstration of the violation
    //
    //  A factory robot can work and do physical labor. That's it.
    //  It cannot eat, sleep, attend meetings, or write code (in this scenario).
    //  But the fat Worker interface FORCES it to implement all these methods.
    //  The only honest implementation is UnsupportedOperationException — which
    //  means any code that calls eat() on a Worker could crash at runtime.
    // =========================================================================

    static class Robot implements Worker {

        private final String robotId;

        Robot(String robotId) { this.robotId = robotId; }

        @Override
        public void work() {
            System.out.println("[Robot " + robotId + "] Running assembly line program...");
        }

        @Override
        public void doPhysicalLabor() {
            System.out.println("[Robot " + robotId + "] Welding car chassis on the assembly line.");
        }

        @Override
        // ISP VIOLATION: Robots don't eat. But Worker interface requires this method.
        // The only honest implementation is to throw an exception.
        // Now any caller who iterates over a List<Worker> and calls eat()
        // will get a RuntimeException when they hit a Robot. This is a hidden
        // runtime bug caused by the fat interface.
        public void eat() {
            throw new UnsupportedOperationException(
                "Robot " + robotId + " does not eat. "
                + "But Worker interface forces this method to exist! "
                + "This will crash any code that calls eat() on a Worker list."
            );
        }

        @Override
        // ISP VIOLATION: Robots don't sleep.
        public void sleep() {
            throw new UnsupportedOperationException(
                "Robot " + robotId + " does not sleep. ISP violation."
            );
        }

        @Override
        // ISP VIOLATION: Robots don't attend meetings (in this scenario).
        public void attendMeetings() {
            throw new UnsupportedOperationException(
                "Robot " + robotId + " does not attend meetings. ISP violation."
            );
        }

        @Override
        // ISP VIOLATION: Robots don't write software code (in this scenario).
        public void writeCode() {
            throw new UnsupportedOperationException(
                "Robot " + robotId + " does not write code. ISP violation."
            );
        }
    }

    // =========================================================================
    //  MAIN — Demonstrating the ISP violation
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== ISP VIOLATION DEMO ===\n");

        HumanDeveloper dev   = new HumanDeveloper("Alice");
        Robot          robot = new Robot("R2D2");

        // ----- Normal usage works fine -----
        System.out.println("--- Normal usage ---");
        dev.work();
        dev.writeCode();
        dev.eat();
        robot.work();
        robot.doPhysicalLabor();
        System.out.println();

        // ----- The danger: polymorphic list of Worker -----
        System.out.println("--- Polymorphic usage (DANGER!) ---");
        Worker[] workers = { dev, robot };

        // Imagine iterating over all workers and calling a "break time" routine.
        // This looks perfectly safe — we're just calling a method on Worker.
        // But it CRASHES when the Worker is a Robot!
        System.out.println("Sending all workers to eat...");
        for (Worker w : workers) {
            try {
                w.eat(); // CRASH when w is a Robot!
            } catch (UnsupportedOperationException e) {
                System.out.println("  EXCEPTION: " + e.getMessage());
                System.out.println("  -> Root cause: Robot was forced to implement eat()");
                System.out.println("     by the fat Worker interface, but can't support it.");
            }
        }
        System.out.println();

        // ----- The attempted workaround makes things worse -----
        System.out.println("--- Workaround with instanceof (makes ISP violation worse!) ---");
        for (Worker w : workers) {
            // To avoid the crash, callers now have to add instanceof checks.
            // This is the code smell that indicates the interface is wrong.
            // Good polymorphism should NOT require instanceof checks.
            if (w instanceof HumanDeveloper) {
                w.eat();
            } else {
                System.out.println("  Skipping eat() for non-human: " + w.getClass().getSimpleName());
            }
        }
        System.out.println();

        System.out.println("CONCLUSION: The fat Worker interface forces:");
        System.out.println("  - Robot to provide 4 UnsupportedOperationException stubs.");
        System.out.println("  - HumanDeveloper to provide 1 stub.");
        System.out.println("  - Calling code to use instanceof to avoid crashes.");
        System.out.println("  All of these are symptoms of the ISP violation.");
        System.out.println("\nSee CorrectExample.java for the ISP-compliant solution.");
    }
}

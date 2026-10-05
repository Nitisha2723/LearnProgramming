package solid.isp;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  ISP CORRECT EXAMPLE — Interface Segregation Principle
 * ============================================================
 *
 *  THE FIX:
 *    Split the fat Worker interface into four small, focused interfaces:
 *
 *    - Workable       : can perform their primary work task
 *    - Eatable        : has biological need for food
 *    - Sleepable      : has biological need for sleep
 *    - Codeable       : can write software code
 *    - MeetingCapable : can attend and participate in meetings
 *    - PhysicalLaborer: can perform physical/manual labor
 *
 *    Each class then implements ONLY the interfaces that apply to it.
 *    No more UnsupportedOperationException stubs. No more instanceof checks.
 *    No more forcing robots to pretend they eat lunch.
 *
 *  BENEFITS:
 *    1. Each class only depends on what it actually uses.
 *    2. No stub/fake implementations needed.
 *    3. Type-safe polymorphism: if you call eat() on an Eatable, you KNOW
 *       it can eat — the compiler enforces it.
 *    4. New interfaces can be added without affecting existing classes.
 *    5. Methods that apply to multiple types can be called on a combined
 *       intersection type (e.g., Workable + Codeable for a code reviewer).
 *
 *  RULE OF THUMB:
 *    If implementing an interface requires you to throw
 *    UnsupportedOperationException or leave a method blank, the interface
 *    is probably violating ISP. Split it.
 */
public class CorrectExample {

    // =========================================================================
    //  SEGREGATED INTERFACES — each one is small and cohesive
    // =========================================================================

    /**
     * Anything that can perform its primary work task.
     * Applies to: ALL workers (human, robot, AI agent, etc.).
     */
    interface Workable {
        void work();
    }

    /**
     * Anything that needs to eat food.
     * Applies to: biological beings (humans, animals).
     * Does NOT apply to: robots, software agents.
     *
     * ISP BENEFIT: Robot never sees this interface. No stubs needed.
     */
    interface Eatable {
        void eat();
    }

    /**
     * Anything that needs sleep.
     * Applies to: biological beings.
     * Does NOT apply to: robots, software agents.
     */
    interface Sleepable {
        void sleep();
    }

    /**
     * Anything that can write software code.
     * Applies to: developers (human or AI).
     * Does NOT apply to: managers, factory robots.
     */
    interface Codeable {
        void writeCode();
    }

    /**
     * Anything that can attend and participate in meetings.
     * Applies to: office workers (human developers, managers).
     * Does NOT apply to: factory robots, fully automated systems.
     */
    interface MeetingCapable {
        void attendMeetings();
    }

    /**
     * Anything that can perform physical/manual labor.
     * Applies to: factory workers, physical robots.
     * Does NOT apply to: office developers (in the software sense).
     */
    interface PhysicalLaborer {
        void doPhysicalLabor();
    }

    // =========================================================================
    //  CLASS: HumanDeveloper
    //
    //  Implements: Workable, Eatable, Sleepable, Codeable, MeetingCapable
    //  Does NOT implement: PhysicalLaborer
    //
    //  ISP BENEFIT: HumanDeveloper only implements what it actually does.
    //  No PhysicalLaborer stub. No UnsupportedOperationException.
    //  The interface list documents exactly what a HumanDeveloper can do.
    // =========================================================================

    static class HumanDeveloper implements Workable, Eatable, Sleepable, Codeable, MeetingCapable {

        private final String name;

        HumanDeveloper(String name) { this.name = name; }

        @Override
        public void work() {
            System.out.println("[" + name + "] Resolving feature tickets and reviewing PRs.");
        }

        @Override
        public void eat() {
            System.out.println("[" + name + "] Having lunch (a biological need).");
        }

        @Override
        public void sleep() {
            System.out.println("[" + name + "] Sleeping 8 hours (a biological need).");
        }

        @Override
        public void writeCode() {
            System.out.println("[" + name + "] Writing Java — clean, well-tested code.");
        }

        @Override
        public void attendMeetings() {
            System.out.println("[" + name + "] Attending daily standup and sprint review.");
        }

        // Notice: NO doPhysicalLabor() and NO UnsupportedOperationException.
        // If the task doesn't apply, we simply don't implement that interface.
    }

    // =========================================================================
    //  CLASS: Robot
    //
    //  Implements: Workable, Codeable, PhysicalLaborer
    //  Does NOT implement: Eatable, Sleepable, MeetingCapable
    //
    //  ISP BENEFIT: Robot has ZERO stubs. Every method it implements is real.
    //  No eat(), no sleep(), no attendMeetings(). The compiler enforces this.
    // =========================================================================

    static class Robot implements Workable, Codeable, PhysicalLaborer {

        private final String robotId;

        Robot(String robotId) { this.robotId = robotId; }

        @Override
        public void work() {
            System.out.println("[Robot " + robotId + "] Running assigned automation task.");
        }

        @Override
        public void writeCode() {
            // Modern industrial robots CAN run embedded code / scripts.
            System.out.println("[Robot " + robotId + "] Executing embedded assembly/control code.");
        }

        @Override
        public void doPhysicalLabor() {
            System.out.println("[Robot " + robotId + "] Welding, lifting, and assembling on production line.");
        }

        // No eat(), no sleep(), no attendMeetings() — Robot never sees those interfaces.
        // No UnsupportedOperationException anywhere. The ISP violation is gone.
    }

    // =========================================================================
    //  CLASS: Manager
    //
    //  Implements: Workable, Eatable, Sleepable, MeetingCapable
    //  Does NOT implement: Codeable, PhysicalLaborer
    //
    //  ISP BENEFIT: Manager doesn't need to pretend it writes code or does
    //  physical labor. Its interface list clearly communicates its capabilities.
    // =========================================================================

    static class Manager implements Workable, Eatable, Sleepable, MeetingCapable {

        private final String name;

        Manager(String name) { this.name = name; }

        @Override
        public void work() {
            System.out.println("[" + name + "] Managing team, removing blockers, reporting status.");
        }

        @Override
        public void eat() {
            System.out.println("[" + name + "] Having a working lunch with stakeholders.");
        }

        @Override
        public void sleep() {
            System.out.println("[" + name + "] Sleeping (sometimes thinking about roadmap).");
        }

        @Override
        public void attendMeetings() {
            System.out.println("[" + name + "] Leading sprint planning and stakeholder sync.");
        }

        // No writeCode(), no doPhysicalLabor(). No stubs. Clean and honest.
    }

    // =========================================================================
    //  MAIN — Demonstrating ISP-compliant design
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== ISP CORRECT EXAMPLE ===\n");

        HumanDeveloper dev   = new HumanDeveloper("Alice");
        Robot          robot = new Robot("R2D2");
        Manager        mgr   = new Manager("Bob");

        // ----- Each type does what it can, nothing more -----
        System.out.println("--- Individual capabilities ---");
        dev.work();
        dev.eat();
        dev.writeCode();
        dev.attendMeetings();

        System.out.println();
        robot.work();
        robot.doPhysicalLabor();
        robot.writeCode(); // Robots CAN run code — this is legitimate

        System.out.println();
        mgr.work();
        mgr.attendMeetings();
        mgr.eat();
        System.out.println();

        // ----- Type-safe polymorphism — no instanceof needed -----
        // Workers who can work (everyone):
        System.out.println("--- All workers: call work() (Workable) ---");
        List<Workable> allWorkers = new ArrayList<>();
        allWorkers.add(dev);
        allWorkers.add(robot);
        allWorkers.add(mgr);
        for (Workable w : allWorkers) {
            w.work(); // Safe! Everyone in this list DEFINITELY can work.
        }
        System.out.println();

        // Workers who need to eat (biological beings only):
        System.out.println("--- Lunch break: call eat() (Eatable only) ---");
        List<Eatable> hungerWorkers = new ArrayList<>();
        hungerWorkers.add(dev);  // HumanDeveloper implements Eatable
        hungerWorkers.add(mgr);  // Manager implements Eatable
        // robot is NOT added — Robot doesn't implement Eatable
        // The compiler would prevent: hungerWorkers.add(robot); ← COMPILE ERROR
        for (Eatable e : hungerWorkers) {
            e.eat(); // Safe! Everyone in this list DEFINITELY can eat.
            // No try/catch. No instanceof check. No runtime surprises.
        }
        System.out.println();

        // Workers who can write code (developers and robots):
        System.out.println("--- Code review pool: Codeable workers ---");
        List<Codeable> coders = new ArrayList<>();
        coders.add(dev);   // HumanDeveloper implements Codeable
        coders.add(robot); // Robot implements Codeable
        // mgr is NOT added — Manager doesn't implement Codeable
        for (Codeable c : coders) {
            c.writeCode(); // Safe! Everyone here definitely codes.
        }
        System.out.println();

        // Workers who attend meetings (office workers only):
        System.out.println("--- Meeting room: MeetingCapable workers ---");
        List<MeetingCapable> meeters = new ArrayList<>();
        meeters.add(dev);
        meeters.add(mgr);
        // robot is NOT added — no meeting attendance needed
        for (MeetingCapable m : meeters) {
            m.attendMeetings(); // Safe! Only meeting-capable workers here.
        }
        System.out.println();

        // ----- Combining interfaces for precise requirements -----
        System.out.println("--- Combined interface constraint ---");
        // A method that needs BOTH Workable AND Codeable (e.g., a code reviewer)
        performCodeReview(dev);
        performCodeReview(robot);
        // performCodeReview(mgr); // Would NOT compile — Manager doesn't implement Codeable!
        System.out.println();

        // ----- Summary -----
        System.out.println("=== ISP TAKEAWAY ===");
        System.out.println("+ No UnsupportedOperationException anywhere.");
        System.out.println("+ No instanceof checks in calling code.");
        System.out.println("+ Each class implements exactly what it needs.");
        System.out.println("+ The compiler enforces interface contracts.");
        System.out.println("+ Adding a new interface (e.g., Trainable) affects");
        System.out.println("  ONLY classes that need to implement it.");
    }

    /**
     * A method that requires a worker to be BOTH Workable AND Codeable.
     *
     * ISP BENEFIT:
     *   The method signature precisely documents what it needs.
     *   You cannot accidentally pass a Manager here — the compiler prevents it.
     *   No instanceof check, no runtime exceptions.
     */
    static <T extends Workable & Codeable> void performCodeReview(T worker) {
        System.out.println("Code review session:");
        worker.work();     // The work part
        worker.writeCode(); // The coding part
    }
}

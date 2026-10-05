package patterns.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DESIGN PATTERN: Observer (also known as Publish-Subscribe, Event Listener)
 * ===========================================================================
 * Intent: Define a one-to-many dependency between objects so that when one
 * object (the Subject/Observable) changes state, all its dependents (Observers)
 * are notified and updated automatically.
 *
 * REAL-WORLD USE CASE: Stock Market Price Feed
 * ---------------------------------------------
 * A stock exchange (Subject) publishes real-time price updates.
 * Multiple systems (Observers) react to these updates independently:
 *   - A portfolio tracks value changes
 *   - An alert system watches for significant price moves
 *   - A news ticker displays formatted price information
 *
 * WHY OBSERVER?
 * Without Observer, StockMarket would need explicit references to every
 * system that cares about prices:
 *
 *   public void updatePrice(String ticker, double price) {
 *       portfolio.onPriceChange(ticker, price);   // tight coupling
 *       alertSystem.onPriceChange(ticker, price); // must know all consumers
 *       newsTicker.onPriceChange(ticker, price);  // adding new consumer = code change
 *   }
 *
 * With Observer, StockMarket just calls notifyObservers() — it has no idea
 * WHO is listening or how many listeners there are.
 *
 * ============================================================
 * PUSH vs PULL NOTIFICATION MODELS:
 * ============================================================
 *
 * PUSH MODEL (used here):
 *   - Subject pushes ALL data to observers in the update() call
 *   - Signature: update(ticker, oldPrice, newPrice)
 *   - Pros: Observers get data immediately, no extra calls needed
 *   - Cons: Observers receive data they might not need (waste if many fields)
 *   - Use when: Observers typically need the changed data
 *
 * PULL MODEL:
 *   - Subject sends minimal notification: "I changed"
 *   - Signature: update(StockMarket source)
 *   - Observer then calls source.getPrice(ticker) to fetch what it needs
 *   - Pros: Observers fetch only what they need, more flexible
 *   - Cons: Extra calls, possible staleness if subject changes again before pull
 *   - Use when: Observers need selective data or the change event has many fields
 *
 * Java's built-in Observable/Observer (deprecated in Java 9) used the pull model.
 * Modern Java prefers custom interfaces or java.util.function.Consumer<T>.
 *
 * ============================================================
 * OBSERVER IN THE JAVA ECOSYSTEM:
 * ============================================================
 *   - java.awt.event.ActionListener     — GUI events
 *   - java.util.EventListener           — standard marker interface
 *   - PropertyChangeListener            — JavaBeans property changes
 *   - Spring's @EventListener           — application events
 *   - RxJava/Project Reactor            — reactive streams
 *   - Java 9+ Flow API                  — reactive publisher/subscriber
 */

// =============================================================================
// FILE STRUCTURE:
//   1. StockObserver interface        — Observer contract
//   2. StockMarket class              — Subject/Observable
//   3. Portfolio class                — Observer: tracks holdings and P&L
//   4. PriceAlertObserver class       — Observer: sends threshold alerts
//   5. NewsTickerObserver class       — Observer: formats for display
//   6. StockPriceObserver             — Demo runner with main()
// =============================================================================

// -----------------------------------------------------------------------------
// OBSERVER INTERFACE: what all observers must implement
// -----------------------------------------------------------------------------
/**
 * StockObserver — the Observer interface.
 *
 * All classes that want to receive stock price updates implement this interface.
 * The Subject (StockMarket) only knows about this interface, not concrete classes.
 *
 * This is the PUSH model: we push ticker, oldPrice, and newPrice to observers.
 * The observer gets everything it needs in one call.
 */
interface StockObserver {
    /**
     * Called by StockMarket whenever a stock price changes.
     *
     * @param ticker   Stock symbol (e.g., "AAPL", "GOOGL")
     * @param oldPrice Previous price (0.0 if this is the first price for this ticker)
     * @param newPrice New current price
     */
    void update(String ticker, double oldPrice, double newPrice);

    /**
     * Name identifying this observer (for logging/debugging)
     */
    String getObserverName();
}

// -----------------------------------------------------------------------------
// SUBJECT (OBSERVABLE): StockMarket manages observers and publishes updates
// -----------------------------------------------------------------------------
/**
 * StockMarket — the Subject (Observable).
 *
 * Responsibilities:
 *   1. Maintain a list of observers (registration/deregistration)
 *   2. Maintain its own state (current stock prices)
 *   3. Notify observers when state changes
 *
 * The Subject does NOT know what observers do with the data.
 * It only knows they implement StockObserver.
 *
 * IMPORTANT: The observer list and the notification loop must be thread-safe
 * in a real application. Here we use ArrayList for clarity.
 */
class StockMarket {
    // The list of interested parties
    // In a real app, this might be CopyOnWriteArrayList for thread safety
    private final List<StockObserver> observers = new ArrayList<>();

    // Current prices, keyed by ticker symbol
    private final Map<String, Double> currentPrices = new HashMap<>();

    private final String marketName;

    public StockMarket(String marketName) {
        this.marketName = marketName;
    }

    // ------------------------------------------------------------------
    // Observer Registration (Subscribe/Unsubscribe)
    // ------------------------------------------------------------------

    /**
     * Register a new observer. After this call, the observer will receive
     * all future price update notifications.
     *
     * @param observer The observer to add (must not be null, duplicates ignored)
     */
    public void addObserver(StockObserver observer) {
        if (observer == null) throw new IllegalArgumentException("Observer cannot be null");
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("[" + marketName + "] Registered observer: " + observer.getObserverName());
        }
    }

    /**
     * Unregister an observer. After this call, the observer will no longer
     * receive notifications. Useful for cleanup (avoid memory leaks!).
     *
     * MEMORY LEAK WARNING: If observers hold references back to long-lived
     * subjects and are never removed, they cannot be garbage collected.
     * Always remove observers when done (WeakReference is an alternative).
     *
     * @param observer The observer to remove
     */
    public void removeObserver(StockObserver observer) {
        boolean removed = observers.remove(observer);
        if (removed) {
            System.out.println("[" + marketName + "] Removed observer: " + observer.getObserverName());
        }
    }

    /**
     * Notify all registered observers about a price change.
     *
     * The notification loop is separated from the state update
     * to make the pattern clear. Some implementations inline this.
     *
     * @param ticker   The stock symbol that changed
     * @param oldPrice The previous price
     * @param newPrice The new price
     */
    private void notifyObservers(String ticker, double oldPrice, double newPrice) {
        // Create a snapshot of the observers list before iterating
        // This prevents ConcurrentModificationException if an observer
        // modifies the list during notification (adds/removes itself)
        List<StockObserver> observerSnapshot = new ArrayList<>(observers);

        for (StockObserver observer : observerSnapshot) {
            // If one observer throws, we catch it to ensure others still get notified
            try {
                observer.update(ticker, oldPrice, newPrice);
            } catch (Exception e) {
                System.err.println("[" + marketName + "] Observer " + observer.getObserverName()
                        + " threw exception: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // State Changes — what triggers notifications
    // ------------------------------------------------------------------

    /**
     * Update the price of a stock and notify all observers.
     * This is the state-change operation that drives the whole pattern.
     *
     * @param ticker The stock symbol (e.g., "AAPL")
     * @param price  The new price (must be positive)
     */
    public void updatePrice(String ticker, double price) {
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");

        double oldPrice = currentPrices.getOrDefault(ticker, 0.0);
        currentPrices.put(ticker, price);

        System.out.printf("%n[%s] Price update: %s %.2f → %.2f%n",
                marketName, ticker, oldPrice, price);

        // Notify ALL observers — each decides independently how to react
        notifyObservers(ticker, oldPrice, price);
    }

    /** Get current price for a ticker (useful in pull-model extensions) */
    public double getCurrentPrice(String ticker) {
        return currentPrices.getOrDefault(ticker, 0.0);
    }

    public int getObserverCount() { return observers.size(); }
    public String getMarketName() { return marketName; }
}

// -----------------------------------------------------------------------------
// CONCRETE OBSERVER 1: Portfolio — tracks holdings and calculates P&L
// -----------------------------------------------------------------------------
/**
 * Portfolio observer: maintains a list of stock holdings and recalculates
 * the portfolio value whenever prices change.
 *
 * This observer STORES STATE — it maintains its own view of the world
 * based on the stream of updates it receives.
 */
class Portfolio implements StockObserver {
    private final String portfolioName;
    // Holdings: ticker → number of shares
    private final Map<String, Integer> holdings = new HashMap<>();
    // Purchase prices: ticker → price paid per share
    private final Map<String, Double> purchasePrices = new HashMap<>();
    // Current market prices (updated by observer)
    private final Map<String, Double> currentPrices = new HashMap<>();

    public Portfolio(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    /** Add a stock position to this portfolio */
    public void addHolding(String ticker, int shares, double purchasePrice) {
        holdings.put(ticker, shares);
        purchasePrices.put(ticker, purchasePrice);
        currentPrices.put(ticker, purchasePrice); // Initialize to purchase price
        System.out.printf("[%s] Added holding: %d shares of %s @ $%.2f%n",
                portfolioName, shares, ticker, purchasePrice);
    }

    @Override
    public void update(String ticker, double oldPrice, double newPrice) {
        // Only care about tickers we hold
        if (!holdings.containsKey(ticker)) return;

        currentPrices.put(ticker, newPrice);

        int shares = holdings.get(ticker);
        double purchasePrice = purchasePrices.get(ticker);
        double positionPnL = (newPrice - purchasePrice) * shares;
        double positionValue = newPrice * shares;
        double changePercent = oldPrice > 0 ? ((newPrice - oldPrice) / oldPrice) * 100 : 0;

        System.out.printf("  [%s] %s: %d shares × $%.2f = $%.2f | P&L: %+.2f (from $%.2f) | Δ%+.1f%%%n",
                portfolioName, ticker, shares, newPrice, positionValue,
                positionPnL, purchasePrice * shares, changePercent);

        // Print total portfolio value after each update
        System.out.printf("  [%s] Total portfolio value: $%.2f%n",
                portfolioName, calculateTotalValue());
    }

    /** Calculate total current market value of all holdings */
    public double calculateTotalValue() {
        return holdings.entrySet().stream()
                .mapToDouble(e -> e.getValue() * currentPrices.getOrDefault(e.getKey(), 0.0))
                .sum();
    }

    /** Calculate total unrealized P&L */
    public double calculateTotalPnL() {
        return holdings.entrySet().stream()
                .mapToDouble(e -> {
                    String ticker = e.getKey();
                    int shares = e.getValue();
                    double current = currentPrices.getOrDefault(ticker, 0.0);
                    double purchase = purchasePrices.getOrDefault(ticker, 0.0);
                    return (current - purchase) * shares;
                })
                .sum();
    }

    @Override
    public String getObserverName() { return "Portfolio[" + portfolioName + "]"; }
}

// -----------------------------------------------------------------------------
// CONCRETE OBSERVER 2: PriceAlertObserver — fires when price changes >5%
// -----------------------------------------------------------------------------
/**
 * PriceAlertObserver: watches for significant price movements and sends alerts.
 *
 * This observer is STATELESS from a business perspective — it doesn't track
 * history, just responds to each event independently.
 */
class PriceAlertObserver implements StockObserver {
    // Alert when price changes by more than this threshold
    private final double alertThresholdPercent;
    private int alertsFired = 0;

    /**
     * @param alertThresholdPercent Minimum % change to trigger an alert (e.g., 5.0 = 5%)
     */
    public PriceAlertObserver(double alertThresholdPercent) {
        this.alertThresholdPercent = alertThresholdPercent;
    }

    @Override
    public void update(String ticker, double oldPrice, double newPrice) {
        // Cannot calculate percent change if no old price
        if (oldPrice <= 0) return;

        double percentChange = Math.abs((newPrice - oldPrice) / oldPrice) * 100;

        if (percentChange >= alertThresholdPercent) {
            alertsFired++;
            String direction = newPrice > oldPrice ? "SURGED ▲" : "DROPPED ▼";
            System.out.printf("  [ALERT #%d] %s %s %.1f%% | %.2f → %.2f%n",
                    alertsFired, ticker, direction, percentChange, oldPrice, newPrice);

            // In a real system, this would send an email, push notification, SMS
            // The observer pattern keeps this logic completely separate from StockMarket
        }
    }

    public int getAlertsFired() { return alertsFired; }

    @Override
    public String getObserverName() {
        return "PriceAlertObserver[>" + alertThresholdPercent + "%]";
    }
}

// -----------------------------------------------------------------------------
// CONCRETE OBSERVER 3: NewsTickerObserver — formats and displays price changes
// -----------------------------------------------------------------------------
/**
 * NewsTickerObserver: formats price change information for display.
 *
 * Different observers can transform the same data in completely different ways.
 * The Subject doesn't care — it just calls update() on all of them.
 */
class NewsTickerObserver implements StockObserver {
    private final List<String> tickerHistory = new ArrayList<>();

    @Override
    public void update(String ticker, double oldPrice, double newPrice) {
        String arrow = newPrice > oldPrice ? "↑" : (newPrice < oldPrice ? "↓" : "→");
        String changeStr;
        if (oldPrice > 0) {
            double change = newPrice - oldPrice;
            double pct = (change / oldPrice) * 100;
            changeStr = String.format("%+.2f (%+.1f%%)", change, pct);
        } else {
            changeStr = "NEW";
        }

        // Format a news ticker line
        String tickerLine = String.format("  [TICKER] %s %s $%.2f [%s]",
                ticker, arrow, newPrice, changeStr);

        System.out.println(tickerLine);
        tickerHistory.add(tickerLine);
    }

    public void printHistory() {
        System.out.println("\n[NewsTicker] History (" + tickerHistory.size() + " events):");
        tickerHistory.forEach(System.out::println);
    }

    @Override
    public String getObserverName() { return "NewsTickerObserver"; }
}

// =============================================================================
// DEMO RUNNER
// =============================================================================
/**
 * Demonstrates the Observer pattern with a stock market simulation.
 */
public class StockPriceObserver {

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("DESIGN PATTERN: Observer (Publish-Subscribe)");
        System.out.println("USE CASE: Stock Market Price Feed");
        System.out.println("=".repeat(65) + "\n");

        // -------------------------------------------------------
        // Setup: Create the Subject and Observers
        // -------------------------------------------------------
        StockMarket nasdaq = new StockMarket("NASDAQ");

        // Create observers (each handles the same data differently)
        Portfolio myPortfolio = new Portfolio("MyPortfolio");
        myPortfolio.addHolding("AAPL", 100, 170.00);  // 100 shares bought at $170
        myPortfolio.addHolding("GOOGL", 10, 130.00);  // 10 shares bought at $130

        PriceAlertObserver alertSystem = new PriceAlertObserver(5.0); // alert on >5% change
        NewsTickerObserver newsTicker = new NewsTickerObserver();

        System.out.println();

        // -------------------------------------------------------
        // DEMO 1: Register observers
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: Registering Observers ---");
        nasdaq.addObserver(myPortfolio);
        nasdaq.addObserver(alertSystem);
        nasdaq.addObserver(newsTicker);
        System.out.println("Total observers: " + nasdaq.getObserverCount() + "\n");

        // -------------------------------------------------------
        // DEMO 2: Normal price updates — all observers notified
        // -------------------------------------------------------
        System.out.println("--- DEMO 2: Normal Price Updates ---");

        // Small AAPL change — no alert, portfolio updates, ticker shows
        nasdaq.updatePrice("AAPL", 172.50);

        // Small GOOGL change
        nasdaq.updatePrice("GOOGL", 131.20);

        // -------------------------------------------------------
        // DEMO 3: Large price change triggers alert
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 3: Large Price Change (>5%) ---");

        // AAPL drops significantly — alert should fire
        nasdaq.updatePrice("AAPL", 162.00); // ~6% drop from 172.50

        // GOOGL jumps — alert fires
        nasdaq.updatePrice("GOOGL", 142.00); // ~8% jump from 131.20

        // -------------------------------------------------------
        // DEMO 4: Stock not in portfolio — portfolio observer ignores it
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 4: Stock Not In Portfolio ---");
        System.out.println("(Portfolio observer skips TSLA — not a holding)");
        nasdaq.updatePrice("TSLA", 250.00);

        // -------------------------------------------------------
        // DEMO 5: Dynamic observer removal
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 5: Removing an Observer ---");
        nasdaq.removeObserver(newsTicker);
        System.out.println("Observers after removal: " + nasdaq.getObserverCount());
        System.out.println("Updating AAPL — ticker should NOT appear:");
        nasdaq.updatePrice("AAPL", 165.00);

        // -------------------------------------------------------
        // Final Summary
        // -------------------------------------------------------
        System.out.println("\n--- Final Summary ---");
        System.out.printf("Portfolio total value: $%.2f%n", myPortfolio.calculateTotalValue());
        System.out.printf("Portfolio total P&L:   %+$.2f%n", myPortfolio.calculateTotalPnL());
        System.out.println("Alerts fired: " + alertSystem.getAlertsFired());

        newsTicker.printHistory();

        System.out.println("\n--- Observer Pattern Key Points ---");
        System.out.println("Subject (StockMarket) never imports Portfolio, PriceAlertObserver,");
        System.out.println("or NewsTickerObserver. It only knows the StockObserver interface.");
        System.out.println("Adding a new observer requires ZERO changes to StockMarket.");
        System.out.println("\nDone!");
    }
}

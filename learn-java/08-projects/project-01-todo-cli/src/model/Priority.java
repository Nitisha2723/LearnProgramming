package model;

public enum Priority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4);

    private final String displayLabel;
    private final int sortOrder;

    Priority(String displayLabel, int sortOrder) {
        this.displayLabel = displayLabel;
        this.sortOrder = sortOrder;
    }

    public String getDisplayLabel() { return displayLabel; }
    public int getSortOrder() { return sortOrder; }

    @Override
    public String toString() { return displayLabel; }
}

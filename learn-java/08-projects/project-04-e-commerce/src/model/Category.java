package model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a product category (supports parent–child hierarchy).
 */
public class Category {

    private final String categoryId;
    private String name;
    private String description;
    /** Null for top-level categories. */
    private String parentCategoryId;

    public Category(String name, String description) {
        this(name, description, null);
    }

    public Category(String name, String description, String parentCategoryId) {
        this.categoryId       = UUID.randomUUID().toString();
        this.name             = name;
        this.description      = description;
        this.parentCategoryId = parentCategoryId;
    }

    /** Reconstruction constructor. */
    public Category(String categoryId, String name, String description,
                    String parentCategoryId) {
        this.categoryId       = categoryId;
        this.name             = name;
        this.description      = description;
        this.parentCategoryId = parentCategoryId;
    }

    // Getters & Setters
    public String getCategoryId()                               { return categoryId; }
    public String getName()                                     { return name; }
    public String getDescription()                              { return description; }
    public String getParentCategoryId()                         { return parentCategoryId; }
    public boolean isTopLevel()                                 { return parentCategoryId == null; }

    public void setName(String name)                            { this.name = name; }
    public void setDescription(String description)              { this.description = description; }
    public void setParentCategoryId(String parentCategoryId)    { this.parentCategoryId = parentCategoryId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        return Objects.equals(categoryId, ((Category) o).categoryId);
    }

    @Override
    public int hashCode() { return Objects.hash(categoryId); }

    @Override
    public String toString() {
        return "Category{id='" + categoryId + "', name='" + name + "'}";
    }
}

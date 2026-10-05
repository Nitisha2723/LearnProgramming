package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * An e-commerce customer.
 */
public class Customer {

    private final String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String shippingAddress;
    private final LocalDateTime registeredAt;

    public Customer(String firstName, String lastName, String email, String shippingAddress) {
        this.customerId      = UUID.randomUUID().toString();
        this.firstName       = firstName;
        this.lastName        = lastName;
        this.email           = email;
        this.shippingAddress = shippingAddress;
        this.registeredAt    = LocalDateTime.now();
    }

    /** Reconstruction constructor. */
    public Customer(String customerId, String firstName, String lastName, String email,
                    String shippingAddress, LocalDateTime registeredAt) {
        this.customerId      = customerId;
        this.firstName       = firstName;
        this.lastName        = lastName;
        this.email           = email;
        this.shippingAddress = shippingAddress;
        this.registeredAt    = registeredAt;
    }

    public String getFullName() { return firstName + " " + lastName; }

    // Getters & Setters
    public String getCustomerId()                       { return customerId; }
    public String getFirstName()                        { return firstName; }
    public String getLastName()                         { return lastName; }
    public String getEmail()                            { return email; }
    public String getShippingAddress()                  { return shippingAddress; }
    public LocalDateTime getRegisteredAt()              { return registeredAt; }

    public void setFirstName(String firstName)          { this.firstName = firstName; }
    public void setLastName(String lastName)            { this.lastName  = lastName; }
    public void setEmail(String email)                  { this.email     = email; }
    public void setShippingAddress(String address)      { this.shippingAddress = address; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        return Objects.equals(customerId, ((Customer) o).customerId);
    }

    @Override
    public int hashCode() { return Objects.hash(customerId); }

    @Override
    public String toString() {
        return "Customer{id='" + customerId + "', name='" + getFullName()
                + "', email='" + email + "'}";
    }
}

package org.informatics.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a cashier in the store
 */
public class Cashier implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final double monthlySalary;

    public Cashier(String id, String name, double monthlySalary) {
        this.id = id;
        this.name = name;
        this.monthlySalary = monthlySalary;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cashier cashier = (Cashier) o;
        return Objects.equals(id, cashier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cashier{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", monthlySalary=" + monthlySalary +
                '}';
    }
}
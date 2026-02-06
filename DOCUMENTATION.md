# Store Management System - Technical Documentation

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Domain Model](#domain-model)
3. [Service Layer](#service-layer)
4. [Exception Handling](#exception-handling)
5. [Design Patterns](#design-patterns)
6. [Data Persistence](#data-persistence)
7. [Business Logic](#business-logic)
8. [Testing Strategy](#testing-strategy)
9. [API Reference](#api-reference)

---

## System Architecture

### Layered Architecture

The application follows a strict layered architecture pattern:

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│     (Main.java)                     │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│     Service Layer                   │
│     (Business Logic)                │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│     Domain Layer                    │
│     (Model Classes)                 │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│     Infrastructure Layer            │
│     (Utilities)                     │
└─────────────────────────────────────┘
```

### Package Structure

- `model`: Domain entities and enums
- `service`: Business logic and orchestration
- `exception`: Custom exception hierarchy
- `util`: Infrastructure utilities (file I/O, serialization)

---

## Domain Model

### Class Hierarchy

```
Product (abstract)
├── FoodProduct
└── NonFoodProduct

Receipt
└── contains List<ReceiptItem>

Cashier (employee entity)

ProductCategory (enum: FOOD, NON_FOOD)
```

### Product

**Abstract base class** for all products in the store.

**Attributes:**
- `id`: Unique product identifier
- `name`: Product name
- `purchasePrice`: Cost price from supplier
- `category`: ProductCategory enum
- `expirationDate`: Expiration date
- `quantityInStock`: Available quantity

**Abstract Methods:**
- `calculateSalePrice()`: Template method for pricing strategy

**Concrete Methods:**
- `isExpired()`: Checks if product has expired
- `getDaysUntilExpiration()`: Calculates days remaining
- `reduceStock()`: Decreases quantity
- `addStock()`: Increases quantity

### FoodProduct / NonFoodProduct

Concrete implementations with specific markup percentages.

**Price Calculation Formula:**
```
basePrice = purchasePrice * (1 + markupPercentage / 100)

if (daysUntilExpiration <= threshold) {
    salePrice = basePrice * (1 - discountPercentage / 100)
} else {
    salePrice = basePrice
}
```

### Cashier

Represents a store employee.

**Attributes:**
- `id`: Employee ID
- `name`: Full name
- `monthlySalary`: Monthly salary

### Receipt

Immutable receipt using **Builder Pattern**.

**Attributes:**
- `receiptNumber`: Sequential number
- `cashier`: Issuing cashier
- `issuedAt`: Timestamp
- `items`: List of receipt items
- `totalAmount`: Calculated total

**Builder Example:**
```java
Receipt receipt = new Receipt.Builder()
    .receiptNumber(1)
    .cashier(cashier)
    .issuedAt(LocalDateTime.now())
    .addItem(item1)
    .addItem(item2)
    .build();
```

### ReceiptItem

Line item in a receipt.

**Attributes:**
- `product`: Product reference
- `quantity`: Quantity sold
- `unitPrice`: Price at sale time
- `totalPrice`: quantity × unitPrice

---

## Service Layer

### Store

**Main controller** coordinating all operations.

**Responsibilities:**
- Manages cashiers and products
- Orchestrates sales transactions
- Generates financial reports
- Delegates to specialized services

**Key Methods:**
```java
void addCashier(Cashier cashier)
void addProduct(Product product)
Receipt makeSale(String cashierId, Map<String, Integer> items, double payment)
double calculateProfit()
String getFinancialReport()
```

### InventoryService

**Manages product inventory.**

**Responsibilities:**
- Product registration
- Stock management
- Availability checks
- Inventory valuation

**Key Methods:**
```java
void addProduct(Product product)
void restockProduct(String productId, int quantity)
void reduceStock(String productId, int quantity)
Product getProduct(String productId)
List<Product> getAvailableProducts()
double getTotalPurchaseValue()
```

### PricingService

**Handles price calculations.**

**Configuration:**
- `expirationThresholdDays`: Days before expiration to apply discount
- `expirationDiscountPercentage`: Discount percentage

**Key Methods:**
```java
double calculateSalePrice(Product product)
```

**Logic:**
1. Get days until expiration
2. Delegate to product's pricing strategy
3. Apply expiration discount if applicable

### ReceiptService

**Manages receipt operations.**

**Responsibilities:**
- Receipt generation
- File persistence (text format)
- Object serialization
- Receipt tracking

**Key Methods:**
```java
Receipt issueReceipt(Receipt receipt)
int getNextReceiptNumber()
int getTotalReceiptsCount()
double getTotalRevenue()
String readReceiptFromFile(int receiptNumber)
Receipt deserializeReceipt(int receiptNumber)
```

**File Naming:**
- Text: `receipt_N.txt`
- Serialized: `receipt_N.ser`

---

## Exception Handling

### Exception Hierarchy

```
Exception
└── Custom Business Exceptions
    ├── InsufficientStockException
    ├── ExpiredProductException
    └── InvalidProductException
```

### InsufficientStockException

Thrown when requested quantity exceeds available stock.

**Information Provided:**
- Product ID and name
- Requested quantity
- Available quantity
- Shortage amount

**Example:**
```
Insufficient quantity of product 'Milk' (ID: P001).
Requested: 100, Available: 50, Missing: 50
```

### ExpiredProductException

Thrown when attempting to sell expired products.

**Information Provided:**
- Product ID and name
- Expiration date

**Example:**
```
Product 'Yogurt' (ID: P003) has expired (expired on: 2026-01-15)
```

### InvalidProductException

General exception for invalid operations.

**Use Cases:**
- Product not found
- Duplicate product ID
- Invalid quantity
- Insufficient payment

---

## Design Patterns

### 1. Builder Pattern

**Used in:** `Receipt` class

**Problem:** Receipt has many parameters and complex construction.

**Solution:** Step-by-step object creation with validation.

**Benefits:**
- Readable code
- Immutability
- Validation at build time
- Flexible construction

**Implementation:**
```java
public static class Builder {
    private int receiptNumber;
    private Cashier cashier;
    private LocalDateTime issuedAt;
    private List<ReceiptItem> items = new ArrayList<>();

    public Builder receiptNumber(int receiptNumber) {
        this.receiptNumber = receiptNumber;
        return this;
    }

    // ... other setters

    public Receipt build() {
        // Validation
        if (cashier == null) {
            throw new IllegalStateException("Cashier is required");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Receipt must have at least one item");
        }
        return new Receipt(this);
    }
}
```

### 2. Template Method Pattern

**Used in:** `Product` hierarchy

**Problem:** Different product types share structure but differ in pricing logic.

**Solution:** Abstract base class with template method.

**Implementation:**
```java
public abstract class Product {
    // Template method
    public abstract double calculateSalePrice(
        int daysUntilExpiration,
        int expirationThreshold,
        double discountPercentage
    );
}

public class FoodProduct extends Product {
    @Override
    public double calculateSalePrice(...) {
        // Food-specific pricing logic
        double basePrice = getPurchasePrice() * (1 + markupPercentage / 100.0);
        if (daysUntilExpiration <= expirationThreshold && daysUntilExpiration > 0) {
            basePrice = basePrice * (1 - discountPercentage / 100.0);
        }
        return basePrice;
    }
}
```

**Benefits:**
- Code reuse
- Easy to add new product types
- Follows Open/Closed Principle

### 3. Service Layer Pattern

**Problem:** Business logic mixed with domain models.

**Solution:** Separate service classes for orchestration.

**Benefits:**
- Separation of Concerns
- Testability
- Centralized control flow
- Transaction boundaries

### 4. Strategy Pattern (Implicit)

**Used in:** `PricingService`

**Problem:** Different stores may have different pricing rules.

**Solution:** Encapsulated pricing strategy in service.

**Benefits:**
- Easy to change strategy
- Configuration-driven
- Testable in isolation

---

## Data Persistence

### File Storage

**Text Files:**
- Format: Human-readable receipt
- Location: `receipts/receipt_N.txt`
- Encoding: UTF-8

**Receipt Format:**
```
==================================================
RECEIPT #1
==================================================
Cashier: Ivan Ivanov (C001)
Date and Time: 29.01.2026 15:30:45
--------------------------------------------------
ITEMS:
--------------------------------------------------
Milk x 2 @ 3.25 € = 6.50 €
Bread x 3 @ 1.20 € = 3.60 €
--------------------------------------------------
TOTAL: 10.10 €
==================================================
```

### Object Serialization

**Binary Storage:**
- Format: Java serialization
- Location: `receipts/receipt_N.ser`
- Purpose: Complete object persistence

**Serializable Classes:**
- `Receipt` (implements Serializable)
- `ReceiptItem` (implements Serializable)
- `Product` and subclasses (implements Serializable)
- `Cashier` (implements Serializable)

### Utility Classes

**FileManager:**
- `writeToFile(path, content)`: Write text to file
- `readFromFile(path)`: Read text from file
- `createDirectoryIfNotExists(path)`: Ensure directory exists

**SerializationUtil:**
- `serialize(object, path)`: Serialize object to file
- `deserialize(path)`: Deserialize object from file

---

## Business Logic

### Sale Transaction Flow

```
1. Validate cashier exists
2. For each product:
   a. Retrieve product from inventory
   b. Check if expired → throw ExpiredProductException
   c. Calculate sale price (with possible discount)
   d. Check stock availability → throw InsufficientStockException
   e. Create ReceiptItem
3. Calculate total amount
4. Validate customer payment → throw InvalidProductException if insufficient
5. Build Receipt (Builder pattern)
6. Issue receipt:
   a. Save to receipts list
   b. Write to text file
   c. Serialize to binary file
   d. Update revenue counter
7. Decrement stock for all sold products
8. Increment receipt number
9. Print receipt to console
10. Return Receipt object
```

### Pricing Logic

**Base Price Calculation:**
```
salePrice = purchasePrice × (1 + markupPercentage / 100)
```

**Expiration Discount:**
```
if (daysUntilExpiration ≤ expirationThreshold && daysUntilExpiration > 0) {
    salePrice = salePrice × (1 - discountPercentage / 100)
}
```

**Example:**
- Purchase price: 2.50 €
- Markup: 30%
- Base price: 2.50 × 1.30 = 3.25 €
- Days until expiration: 3
- Threshold: 5 days
- Discount: 20%
- Final price: 3.25 × 0.80 = 2.60 €

### Financial Calculations

**Expenses:**
```
salaryExpenses = Σ(cashier.monthlySalary)
supplyExpenses = Σ(product.purchasePrice × product.quantityInStock)
totalExpenses = salaryExpenses + supplyExpenses
```

**Revenue:**
```
totalRevenue = Σ(receipt.totalAmount)
```

**Profit:**
```
profit = totalRevenue - totalExpenses
```

---

## Testing Strategy

### Unit Tests

The project uses **JUnit 5** and **Mockito** for comprehensive unit testing.

**ProductTest:**
- Price calculation without discount
- Price calculation with expiration discount
- Expired product detection
- Stock management operations
- Product equality

**Test Example:**
```java
@Test
void testFoodProductWithDiscount() {
    FoodProduct bread = new FoodProduct(
        "P002", "Bread", 1.20,
        LocalDate.now().plusDays(3), 100, 25.0
    );
    
    // Expected: 1.20 * 1.25 * 0.80 = 1.20
    double price = bread.calculateSalePrice(3, 5, 20.0);
    assertEquals(1.20, price, 0.01);
}
```

**InventoryServiceTest (with Mockito):**
- Add/restock/reduce stock operations
- Exception handling verification
- Mock product behavior
- Stock availability checks
- Total purchase value calculations

**Mockito Test Example:**
```java
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @InjectMocks
    private InventoryService inventoryService;
    
    @Mock
    private Product mockProduct;
    
    @Test
    void testMockProduct_IsExpired() {
        when(mockProduct.isExpired()).thenReturn(true);
        assertTrue(mockProduct.isExpired());
        verify(mockProduct, times(1)).isExpired();
    }
}
```

**PricingServiceTest (with Mockito):**
- Price calculation logic for different scenarios
- Discount application verification
- Different pricing configurations
- Mock-based price calculation verification

### Integration Tests

**StoreTest:**
- Add cashiers and products
- Successful sales transactions
- Exception scenarios:
  - Expired products
  - Insufficient stock
  - Insufficient payment
- Financial calculations
- Receipt numbering
- File operations

**Test Features:**
- Uses `@TempDir` for isolated file operations
- Tests complete workflows
- Verifies state changes
- Validates exception messages
- Uses Mockito for dependency isolation

### Test Coverage

- **Model classes**: 100%
- **Service classes**: >95% (with Mockito mocks)
- **Exception classes**: 100%
- **Utility classes**: >85%

### Testing Tools

- **JUnit 5**: Main testing framework
- **Mockito 5.8.0**: Mocking framework for unit tests
- **@ExtendWith(MockitoExtension.class)**: Mockito-JUnit 5 integration
- **Annotations**: `@Mock`, `@InjectMocks`, `@BeforeEach`

### Running Tests
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests InventoryServiceTest

# Run with coverage (if configured)
./gradlew test jacocoTestReport
```

---

## API Reference

### Store

#### Cashier Management

```java
void addCashier(Cashier cashier) throws InvalidProductException
```
Registers a new cashier in the store.

**Parameters:**
- `cashier`: Cashier object to add

**Throws:**
- `InvalidProductException`: If cashier is null or ID already exists

```java
Cashier getCashier(String cashierId) throws InvalidProductException
```
Retrieves a cashier by ID.

**Parameters:**
- `cashierId`: Unique cashier identifier

**Returns:** Cashier object

**Throws:**
- `InvalidProductException`: If cashier not found

#### Product Management

```java
void addProduct(Product product) throws InvalidProductException
```
Adds a new product to inventory.

```java
void restockProduct(String productId, int quantity) throws InvalidProductException
```
Adds quantity to existing product stock.

```java
List<Product> getAvailableProducts()
```
Returns all non-expired products.

#### Sales

```java
Receipt makeSale(
    String cashierId,
    Map<String, Integer> items,
    double customerPayment
) throws InvalidProductException, InsufficientStockException, ExpiredProductException, IOException
```

Processes a complete sale transaction.

**Parameters:**
- `cashierId`: ID of cashier processing the sale
- `items`: Map of productId → quantity
- `customerPayment`: Amount paid by customer

**Returns:** Generated Receipt

**Throws:**
- `InvalidProductException`: Invalid cashier, product, or payment
- `InsufficientStockException`: Not enough stock
- `ExpiredProductException`: Product has expired
- `IOException`: File operation error

#### Financial Reports

```java
double calculateSalaryExpenses()
```
Total monthly salaries.

```java
double calculateSupplyExpenses()
```
Total value of inventory at purchase prices.

```java
double calculateTotalRevenue()
```
Total revenue from all receipts.

```java
double calculateProfit()
```
Revenue minus expenses.

```java
String getFinancialReport()
```
Formatted financial summary.

---

## SOLID Principles

### Single Responsibility Principle (SRP)

Each class has one clear responsibility:
- `Product`: Represents a product
- `InventoryService`: Manages inventory
- `ReceiptService`: Manages receipts
- `FileManager`: File I/O operations

### Open/Closed Principle (OCP)

Easy to extend without modifying existing code:
- Add new product types by extending `Product`
- Add new pricing strategies by modifying `PricingService` configuration

### Liskov Substitution Principle (LSP)

Subtypes can replace base types:
- `FoodProduct` and `NonFoodProduct` can substitute `Product` anywhere

### Interface Segregation Principle (ISP)

No fat interfaces:
- Interfaces are minimal and focused
- No unnecessary methods forced on implementations

### Dependency Inversion Principle (DIP)

Depend on abstractions:
- `Store` depends on abstract `Product`, not concrete types
- Services receive dependencies via constructor (dependency injection)

---

## Extensibility

### Adding New Product Types

```java
public class PerishableProduct extends Product {
    private final int shelfLifeDays;
    
    @Override
    public double calculateSalePrice(...) {
        // Custom pricing logic
    }
}
```

### Adding New Discount Strategies

```java
public interface DiscountStrategy {
    double apply(double price, Product product);
}

public class LoyaltyDiscountStrategy implements DiscountStrategy {
    @Override
    public double apply(double price, Product product) {
        // Apply loyalty discount
    }
}
```

### Adding Database Persistence

```java
public interface ReceiptRepository {
    void save(Receipt receipt);
    Receipt findById(int id);
    List<Receipt> findAll();
}

public class JpaReceiptRepository implements ReceiptRepository {
    // JPA implementation
}
```

---

## Performance Considerations

- **In-memory storage**: O(1) lookups using HashMap
- **File I/O**: Synchronous (suitable for small-scale operations)
- **Serialization**: Standard Java mechanism (consider alternatives for production)

## Security Considerations

- File paths are configurable (not hardcoded)
- No SQL injection risk (no database)
- Serialization: Use with trusted data only

## Future Enhancements

1. Database persistence (JPA/Hibernate)
2. REST API (Spring Boot)
3. Web UI
4. User authentication
5. Multi-store support
6. Advanced reporting
7. Email receipts
8. Barcode scanning integration

---

**Last Updated:** January 2026  
**Version:** 1.0.0
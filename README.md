# Store Management System

A comprehensive Java-based store management system developed as a project for CITB408 - Java Programming course at NBU.

## Overview

This application models the process of stocking and selling goods in a store, including inventory management, cashier operations, pricing strategies, and receipt generation.

## Features

- **Product Management**: Food and non-food products with different markup percentages
- **Dynamic Pricing**: Automatic discounts for products nearing expiration
- **Inventory Control**: Stock tracking with availability checks
- **Cashier Operations**: Employee management with salary tracking
- **Sales Processing**: Complete transaction handling with validation
- **Receipt Generation**: Detailed receipts with file storage and serialization
- **Financial Reports**: Revenue, expenses, and profit calculations
- **Exception Handling**: Custom exceptions for business logic errors

## Technology Stack

- **Java 21**
- **Gradle (Groovy DSL)** - Build automation
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for unit tests

## Project Structure
```
src/
├── main/java/org/informatics/store/
│   ├── Main.java                    # Application entry point
│   ├── model/                       # Domain models
│   │   ├── Product.java             # Abstract product class
│   │   ├── FoodProduct.java         # Food items
│   │   ├── NonFoodProduct.java      # Non-food items
│   │   ├── Cashier.java             # Cashier entity
│   │   ├── Receipt.java             # Receipt with Builder pattern
│   │   ├── ReceiptItem.java         # Receipt line item
│   │   └── ProductCategory.java     # Product category enum
│   │
│   ├── service/                     # Business logic layer
│   │   ├── Store.java               # Main store controller
│   │   ├── InventoryService.java    # Inventory management
│   │   ├── PricingService.java      # Price calculation
│   │   └── ReceiptService.java      # Receipt operations
│   │
│   ├── exception/                   # Custom exceptions
│   │   ├── InsufficientStockException.java
│   │   ├── ExpiredProductException.java
│   │   └── InvalidProductException.java
│   │
│   └── util/                        # Utility classes
│       ├── FileManager.java         # File I/O operations
│       └── SerializationUtil.java   # Object serialization
│
└── test/java/                       # JUnit tests
    ├── model/ProductTest.java
    └── service/
        ├── StoreTest.java
        ├── InventoryServiceTest.java    # with Mockito
        └── PricingServiceTest.java      # with Mockito
```

## Getting Started

### Prerequisites

- Java 21 or higher
- Gradle 8.x

### Building the Project
```bash
# Compile the project
./gradlew build

# Run tests
./gradlew test

# Run the application
./gradlew run
```

### Running the Application
```bash
# Using Gradle
./gradlew runApp

# Or using the JAR
./gradlew jar
java -jar build/libs/store-management-system-1.0.0.jar
```

## Key Design Patterns

- **Builder Pattern**: Used in `Receipt` class for flexible object creation
- **Template Method Pattern**: Implemented in `Product` hierarchy for price calculation
- **Service Layer Pattern**: Separation of business logic from domain models
- **Strategy Pattern**: Applied in `PricingService` for flexible pricing rules

## Architecture Highlights

The application follows a **layered architecture** with clear separation of concerns:

- **Model Layer**: Domain entities and business objects
- **Service Layer**: Business logic and orchestration
- **Exception Layer**: Custom exception hierarchy
- **Utility Layer**: Infrastructure and helper classes

All design follows **SOLID principles** for maintainability and extensibility.

## Testing

The project includes comprehensive unit and integration tests using JUnit 5 and Mockito:
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info
```

**Test Classes:**
- `ProductTest` - Model unit tests
- `InventoryServiceTest` - Service tests with Mockito mocks
- `PricingServiceTest` - Pricing logic with Mockito
- `StoreTest` - Integration tests

## Author

CITB408 Project - New Bulgarian University, Spring Semester 2024/2025
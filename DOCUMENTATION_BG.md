# Система за управление на магазин - Техническа документация

## Съдържание

1. [Архитектура на системата](#архитектура-на-системата)
2. [Доменен модел](#доменен-модел)
3. [Слой от услуги](#слой-от-услуги)
4. [Обработка на изключения](#обработка-на-изключения)
5. [Design Patterns](#design-patterns)
6. [Съхранение на данни](#съхранение-на-данни)
7. [Бизнес логика](#бизнес-логика)
8. [Стратегия за тестване](#стратегия-за-тестване)
9. [API Reference](#api-reference)

---

## Архитектура на системата

### Многослойна архитектура

Приложението следва стриктна многослойна архитектура:

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│     (Main.java)                     │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│     Service Layer                   │
│     (Бизнес логика)                 │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│     Domain Layer                    │
│     (Model класове)                 │
└─────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────┐
│     Infrastructure Layer            │
│     (Utilities)                     │
└─────────────────────────────────────┘
```

### Структура на пакетите

- `model`: Доменни обекти и enums
- `service`: Бизнес логика и координация
- `exception`: Йерархия от custom exceptions
- `util`: Инфраструктурни utilities (file I/O, serialization)

---

## Доменен модел

### Класова йерархия

```
Product (abstract)
├── FoodProduct
└── NonFoodProduct

Receipt
└── съдържа List<ReceiptItem>

Cashier (служител)

ProductCategory (enum: FOOD, NON_FOOD)
```

### Product

**Абстрактен базов клас** за всички продукти в магазина.

**Атрибути:**
- `id`: Уникален идентификатор на продукта
- `name`: Име на продукта
- `purchasePrice`: Доставна цена
- `category`: ProductCategory enum
- `expirationDate`: Срок на годност
- `quantityInStock`: Налично количество

**Абстрактни методи:**
- `calculateSalePrice()`: Template method за стратегия на ценообразуване

**Конкретни методи:**
- `isExpired()`: Проверява дали продуктът е изтекъл
- `getDaysUntilExpiration()`: Изчислява оставащите дни
- `reduceStock()`: Намалява количеството
- `addStock()`: Увеличава количеството

### FoodProduct / NonFoodProduct

Конкретни имплементации със специфични проценти надценка.

**Формула за изчисляване на цена:**
```
basePrice = purchasePrice * (1 + markupPercentage / 100)

if (daysUntilExpiration <= threshold) {
    salePrice = basePrice * (1 - discountPercentage / 100)
} else {
    salePrice = basePrice
}
```

### Cashier

Представлява служител в магазина.

**Атрибути:**
- `id`: Идентификатор на служителя
- `name`: Пълно име
- `monthlySalary`: Месечна заплата

### Receipt

Immutable касова бележка използваща **Builder Pattern**.

**Атрибути:**
- `receiptNumber`: Пореден номер
- `cashier`: Издаващ касиер
- `issuedAt`: Времеви печат
- `items`: Списък с артикули
- `totalAmount`: Изчислена обща сума

**Пример с Builder:**
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

Артикул в касовата бележка.

**Атрибути:**
- `product`: Референция към продукта
- `quantity`: Продадено количество
- `unitPrice`: Цена по време на продажба
- `totalPrice`: quantity × unitPrice

---

## Слой от услуги

### Store

**Главен контролер** координиращ всички операции.

**Отговорности:**
- Управлява касиери и продукти
- Оркестрира продажбени транзакции
- Генерира финансови отчети
- Делегира към специализирани услуги

**Ключови методи:**
```java
void addCashier(Cashier cashier)
void addProduct(Product product)
Receipt makeSale(String cashierId, Map<String, Integer> items, double payment)
double calculateProfit()
String getFinancialReport()
```

### InventoryService

**Управлява продуктовия инвентар.**

**Отговорности:**
- Регистрация на продукти
- Управление на наличности
- Проверки за наличност
- Оценка на инвентара

**Ключови методи:**
```java
void addProduct(Product product)
void restockProduct(String productId, int quantity)
void reduceStock(String productId, int quantity)
Product getProduct(String productId)
List<Product> getAvailableProducts()
double getTotalPurchaseValue()
```

### PricingService

**Обработва изчислението на цени.**

**Конфигурация:**
- `expirationThresholdDays`: Дни преди изтичане за прилагане на намаление
- `expirationDiscountPercentage`: Процент намаление

**Ключови методи:**
```java
double calculateSalePrice(Product product)
```

**Логика:**
1. Вземи дни до изтичане
2. Делегирай към pricing стратегията на продукта
3. Приложи намаление при изтичане ако е приложимо

### ReceiptService

**Управлява операциите с касови бележки.**

**Отговорности:**
- Генериране на бележки
- Файлово съхранение (текстов формат)
- Сериализация на обекти
- Проследяване на бележки

**Ключови методи:**
```java
Receipt issueReceipt(Receipt receipt)
int getNextReceiptNumber()
int getTotalReceiptsCount()
double getTotalRevenue()
String readReceiptFromFile(int receiptNumber)
Receipt deserializeReceipt(int receiptNumber)
```

**Наименуване на файлове:**
- Текст: `receipt_N.txt`
- Сериализирани: `receipt_N.ser`

---

## Обработка на изключения

### Йерархия на изключенията

```
Exception
└── Custom Business Exceptions
    ├── InsufficientStockException
    ├── ExpiredProductException
    └── InvalidProductException
```

### InsufficientStockException

Хвърля се когато поисканото количество превишава наличното.

**Предоставена информация:**
- ID и име на продукта
- Поискано количество
- Налично количество
- Липсващо количество

**Пример:**
```
Недостатъчно количество от продукт 'Мляко' (ID: P001).
Поискани: 100, Налични: 50, Липсват: 50
```

### ExpiredProductException

Хвърля се при опит за продажба на изтекъл продукт.

**Предоставена информация:**
- ID и име на продукта
- Дата на изтичане

**Пример:**
```
Продуктът 'Кисело мляко' (ID: P003) има изтекъл срок на годност (изтекъл на: 2026-01-15)
```

### InvalidProductException

Общо изключение за невалидни операции.

**Случаи на употреба:**
- Продуктът не е намерен
- Дублиран product ID
- Невалидно количество
- Недостатъчно плащане

---

## Design Patterns

### 1. Builder Pattern

**Използван в:** `Receipt` клас

**Проблем:** Receipt има много параметри и сложна конструкция.

**Решение:** Стъпка по стъпка създаване на обект с валидация.

**Предимства:**
- Четим код
- Immutability
- Валидация при build време
- Гъвкава конструкция

**Имплементация:**
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

    // ... други setters

    public Receipt build() {
        // Валидация
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

**Използван в:** `Product` йерархия

**Проблем:** Различните типове продукти споделят структура но се различават в логиката за ценообразуване.

**Решение:** Абстрактен базов клас с template method.

**Имплементация:**
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
        // Специфична логика за храни
        double basePrice = getPurchasePrice() * (1 + markupPercentage / 100.0);
        if (daysUntilExpiration <= expirationThreshold && daysUntilExpiration > 0) {
            basePrice = basePrice * (1 - discountPercentage / 100.0);
        }
        return basePrice;
    }
}
```

**Предимства:**
- Code reuse
- Лесно добавяне на нови типове продукти
- Следва Open/Closed Principle

### 3. Service Layer Pattern

**Проблем:** Бизнес логиката е смесена с domain моделите.

**Решение:** Отделни service класове за оркестрация.

**Предимства:**
- Separation of Concerns
- Testability
- Централизиран control flow
- Transaction boundaries

### 4. Strategy Pattern (Implicit)

**Използван в:** `PricingService`

**Проблем:** Различните магазини могат да имат различни правила за ценообразуване.

**Решение:** Енкапсулирана pricing стратегия в service.

**Предимства:**
- Лесна смяна на стратегия
- Configuration-driven
- Тестваем изолирано

---

## Съхранение на данни

### Файлово съхранение

**Текстови файлове:**
- Формат: Четима касова бележка
- Местоположение: `receipts/receipt_N.txt`
- Encoding: UTF-8

**Формат на касова бележка:**
```
==================================================
КАСОВА БЕЛЕЖКА #1
==================================================
Касиер: Иван Иванов (C001)
Дата и час: 29.01.2026 15:30:45
--------------------------------------------------
АРТИКУЛИ:
--------------------------------------------------
Мляко x 2 @ 3.25 € = 6.50 €
Хляб x 3 @ 1.20 € = 3.60 €
--------------------------------------------------
ОБЩО: 10.10 €
==================================================
```

### Сериализация на обекти

**Binary съхранение:**
- Формат: Java serialization
- Местоположение: `receipts/receipt_N.ser`
- Цел: Пълно съхранение на обекти

**Serializable класове:**
- `Receipt` (имплементира Serializable)
- `ReceiptItem` (имплементира Serializable)
- `Product` и подкласовете (имплементират Serializable)
- `Cashier` (имплементира Serializable)

### Utility класове

**FileManager:**
- `writeToFile(path, content)`: Записва текст във файл
- `readFromFile(path)`: Чете текст от файл
- `createDirectoryIfNotExists(path)`: Създава директория ако не съществува

**SerializationUtil:**
- `serialize(object, path)`: Сериализира обект във файл
- `deserialize(path)`: Десериализира обект от файл

---

## Бизнес логика

### Процес на продажбена транзакция

```
1. Валидирай че касиерът съществува
2. За всеки продукт:
   a. Извлечи продукта от инвентара
   b. Провери дали е изтекъл → хвърли ExpiredProductException
   c. Изчисли продажна цена (с възможно намаление)
   d. Провери наличност → хвърли InsufficientStockException
   e. Създай ReceiptItem
3. Изчисли обща сума
4. Валидирай плащането на клиента → хвърли InvalidProductException ако е недостатъчно
5. Построй Receipt (Builder pattern)
6. Издай бележка:
   a. Запази в списък с бележки
   b. Запиши в текстов файл
   c. Сериализирай в binary файл
   d. Обнови брояча на приходи
7. Намали наличностите за всички продадени продукти
8. Увеличи номера на бележката
9. Принтирай бележката в конзолата
10. Върни Receipt обекта
```

### Логика на ценообразуване

**Изчисляване на базова цена:**
```
salePrice = purchasePrice × (1 + markupPercentage / 100)
```

**Намаление при изтичане:**
```
if (daysUntilExpiration ≤ expirationThreshold && daysUntilExpiration > 0) {
    salePrice = salePrice × (1 - discountPercentage / 100)
}
```

**Пример:**
- Доставна цена: 2.50 €
- Надценка: 30%
- Базова цена: 2.50 × 1.30 = 3.25 €
- Дни до изтичане: 3
- Threshold: 5 дни
- Намаление: 20%
- Финална цена: 3.25 × 0.80 = 2.60 €

### Финансови изчисления

**Разходи:**
```
salaryExpenses = Σ(cashier.monthlySalary)
supplyExpenses = Σ(product.purchasePrice × product.quantityInStock)
totalExpenses = salaryExpenses + supplyExpenses
```

**Приходи:**
```
totalRevenue = Σ(receipt.totalAmount)
```

**Печалба:**
```
profit = totalRevenue - totalExpenses
```

---

## Стратегия за тестване

### Unit Tests

Проектът използва **JUnit 5** и **Mockito** за цялостно unit тестване.

**ProductTest:**
- Изчисление на цена без намаление
- Изчисление на цена с намаление при изтичане
- Детектиране на изтекъл продукт
- Операции за управление на наличности
- Равенство на продукти

**Пример за тест:**
```java
@Test
void testFoodProductWithDiscount() {
    FoodProduct bread = new FoodProduct(
        "P002", "Хляб", 1.20,
        LocalDate.now().plusDays(3), 100, 25.0
    );
    
    // Очаквано: 1.20 * 1.25 * 0.80 = 1.20
    double price = bread.calculateSalePrice(3, 5, 20.0);
    assertEquals(1.20, price, 0.01);
}
```

**InventoryServiceTest (с Mockito):**
- Операции за добавяне/зареждане/намаляване на стока
- Верификация на exception handling
- Mock на product поведение
- Проверки за наличност
- Изчисления на обща стойност на закупка

**Mockito тест пример:**
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

**PricingServiceTest (с Mockito):**
- Логика за изчисляване на цени при различни сценарии
- Верификация на прилагане на отстъпки
- Различни конфигурации на ценообразуване
- Mock-базирана верификация на изчисление на цени

### Integration Tests

**StoreTest:**
- Добавяне на касиери и продукти
- Успешни продажбени транзакции
- Сценарии с изключения:
  - Изтекли продукти
  - Недостатъчна наличност
  - Недостатъчно плащане
- Финансови изчисления
- Номериране на бележки
- Файлови операции

**Характеристики на тестовете:**
- Използва `@TempDir` за изолирани файлови операции
- Тества пълни workflow-и
- Верифицира промени в състоянието
- Валидира съобщения на изключения
- Използва Mockito за изолация на dependencies

### Test Coverage

- **Model класове**: 100%
- **Service класове**: >95% (с Mockito mocks)
- **Exception класове**: 100%
- **Utility класове**: >85%

### Инструменти за тестване

- **JUnit 5**: Основен testing framework
- **Mockito 5.8.0**: Mocking framework за unit tests
- **@ExtendWith(MockitoExtension.class)**: Mockito-JUnit 5 интеграция
- **Анотации**: `@Mock`, `@InjectMocks`, `@BeforeEach`

### Пускане на тестове
```bash
# Пускане на всички тестове
./gradlew test

# Пускане на тестове с детайлен output
./gradlew test --info

# Пускане на конкретен тест клас
./gradlew test --tests InventoryServiceTest

# Пускане с coverage (ако е конфигурирано)
./gradlew test jacocoTestReport
```

---

## API Reference

### Store

#### Управление на касиери

```java
void addCashier(Cashier cashier) throws InvalidProductException
```
Регистрира нов касиер в магазина.

**Параметри:**
- `cashier`: Cashier обект за добавяне

**Хвърля:**
- `InvalidProductException`: Ако касиерът е null или ID вече съществува

```java
Cashier getCashier(String cashierId) throws InvalidProductException
```
Извлича касиер по ID.

**Параметри:**
- `cashierId`: Уникален идентификатор на касиера

**Връща:** Cashier обект

**Хвърля:**
- `InvalidProductException`: Ако касиерът не е намерен

#### Управление на продукти

```java
void addProduct(Product product) throws InvalidProductException
```
Добавя нов продукт в инвентара.

```java
void restockProduct(String productId, int quantity) throws InvalidProductException
```
Добавя количество към съществуващ продукт.

```java
List<Product> getAvailableProducts()
```
Връща всички неизтекли продукти.

#### Продажби

```java
Receipt makeSale(
    String cashierId,
    Map<String, Integer> items,
    double customerPayment
) throws InvalidProductException, InsufficientStockException, ExpiredProductException, IOException
```

Обработва пълна продажбена транзакция.

**Параметри:**
- `cashierId`: ID на касиера обработващ продажбата
- `items`: Map от productId → количество
- `customerPayment`: Сума платена от клиента

**Връща:** Генерирана Receipt

**Хвърля:**
- `InvalidProductException`: Невалиден касиер, продукт или плащане
- `InsufficientStockException`: Недостатъчна наличност
- `ExpiredProductException`: Продуктът е изтекъл
- `IOException`: Грешка при файлова операция

#### Финансови отчети

```java
double calculateSalaryExpenses()
```
Общи месечни заплати.

```java
double calculateSupplyExpenses()
```
Обща стойност на инвентара по доставни цени.

```java
double calculateTotalRevenue()
```
Общи приходи от всички бележки.

```java
double calculateProfit()
```
Приходи минус разходи.

```java
String getFinancialReport()
```
Форматирано финансово обобщение.

---

## SOLID Principles

### Single Responsibility Principle (SRP)

Всеки клас има една ясна отговорност:
- `Product`: Представя продукт
- `InventoryService`: Управлява инвентар
- `ReceiptService`: Управлява бележки
- `FileManager`: Файлови I/O операции

### Open/Closed Principle (OCP)

Лесно разширяване без модифициране на съществуващ код:
- Добавяне на нови типове продукти чрез наследяване на `Product`
- Добавяне на нови pricing стратегии чрез модифициране на конфигурацията на `PricingService`

### Liskov Substitution Principle (LSP)

Подтиповете могат да заменят базовите типове:
- `FoodProduct` и `NonFoodProduct` могат да заместят `Product` навсякъде

### Interface Segregation Principle (ISP)

Няма "дебели" интерфейси:
- Интерфейсите са минимални и фокусирани
- Няма ненужни методи налагани върху имплементациите

### Dependency Inversion Principle (DIP)

Зависимост от абстракции:
- `Store` зависи от абстрактния `Product`, не от конкретни типове
- Services получават dependencies чрез конструктора (dependency injection)

---

## Разширяемост

### Добавяне на нови типове продукти

```java
public class PerishableProduct extends Product {
    private final int shelfLifeDays;
    
    @Override
    public double calculateSalePrice(...) {
        // Custom pricing логика
    }
}
```

### Добавяне на нови discount стратегии

```java
public interface DiscountStrategy {
    double apply(double price, Product product);
}

public class LoyaltyDiscountStrategy implements DiscountStrategy {
    @Override
    public double apply(double price, Product product) {
        // Прилагане на loyalty discount
    }
}
```

### Добавяне на Database persistence

```java
public interface ReceiptRepository {
    void save(Receipt receipt);
    Receipt findById(int id);
    List<Receipt> findAll();
}

public class JpaReceiptRepository implements ReceiptRepository {
    // JPA имплементация
}
```

---

## Съображения за производителност

- **In-memory съхранение**: O(1) lookups използвайки HashMap
- **File I/O**: Синхронно (подходящо за малък мащаб на операции)
- **Serialization**: Стандартен Java механизъм (разгледайте алтернативи за production)

## Съображения за сигурност

- Файловите пътища са конфигурируеми (не са hardcoded)
- Няма SQL injection риск (няма база данни)
- Serialization: Използвайте само с доверени данни

## Бъдещи подобрения

1. Database persistence (JPA/Hibernate)
2. REST API (Spring Boot)
3. Web UI
4. Потребителска автентикация
5. Multi-store поддръжка
6. Напреднали отчети
7. Email касови бележки
8. Интеграция със сканиране на баркод

---

**Последна актуализация:** Януари 2026  
**Версия:** 1.0.0
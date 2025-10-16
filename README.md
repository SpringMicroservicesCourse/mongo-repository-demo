# mongo-repository-demo

> Spring Data MongoDB with MongoRepository pattern for simplified data access

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-latest-green.svg)](https://www.mongodb.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive demonstration of Spring Data MongoDB using **MongoRepository** pattern, featuring query by method name, custom converters for multiple data formats, and complete CRUD workflow.

## Features

- MongoRepository pattern (high-level API)
- Query by method name (auto-implementation)
- Multiple custom converters (Document→Money, Long→Money, Money→Long)
- Complete CRUD operations demonstration
- Batch insert and update operations
- Sorting and pagination support
- @EnableMongoRepositories configuration
- Joda Money integration with backward compatibility

## Tech Stack

- Spring Boot 3.4.5
- Spring Data MongoDB
- Java 21
- MongoDB (Docker container)
- Joda Money 2.0.2
- Lombok
- Maven 3.8+

## Getting Started

### Prerequisites

- JDK 21 or higher
- Maven 3.8+ (or use included Maven Wrapper)
- Docker (for MongoDB)

### Quick Start

**Step 1: Start MongoDB**

```bash
docker run -d \
  --name mongo-spring-course \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=admin \
  mongo:latest
```

**Step 2: Create database and user**

```bash
# Connect to MongoDB
docker exec -it mongo-spring-course mongosh -u admin -p admin

# Create database and user
use springbucks
db.createUser({
  user: "springbucks",
  pwd: "springbucks",
  roles: [{ role: "readWrite", db: "springbucks" }]
})

# Verify
show users
exit
```

**Step 3: Run the application**

```bash
./mvnw spring-boot:run
```

## Usage

### Application Flow

```
1. Spring Boot starts
   ↓
2. @EnableMongoRepositories scans repositories
   ↓
3. CoffeeRepository injected
   ↓
4. CommandLineRunner executes CRUD operations:
   - CREATE: Batch insert espresso & latte
   - READ: Query all with sorting
   - UPDATE: Update latte price (TWD 150 → 175)
   - DELETE: Remove all documents
```

### Code Example

```java
@SpringBootApplication
@EnableMongoRepositories  // Enable MongoDB Repository support
public class MongoRepositoryDemoApplication implements CommandLineRunner {
    
    @Autowired
    private CoffeeRepository coffeeRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 1. CREATE - Batch insert
        Coffee espresso = Coffee.builder()
                .name("espresso")
                .price(Money.of(CurrencyUnit.of("TWD"), 100.0))
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        
        Coffee latte = Coffee.builder()
                .name("latte")
                .price(Money.of(CurrencyUnit.of("TWD"), 150.0))
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        
        coffeeRepository.insert(Arrays.asList(espresso, latte));
        
        // 2. READ - Query all with sorting
        coffeeRepository.findAll(Sort.by("name"))
                .forEach(c -> log.info("Saved Coffee {}", c));
        
        Thread.sleep(1000);  // Wait to see timestamp difference
        
        // 3. UPDATE - Update latte price
        latte.setPrice(Money.of(CurrencyUnit.of("TWD"), 175.0));
        latte.setUpdateTime(new Date());
        coffeeRepository.save(latte);
        
        // 4. Query by method name
        coffeeRepository.findByName("latte")
                .forEach(c -> log.info("Coffee {}", c));
        
        // 5. DELETE - Remove all
        coffeeRepository.deleteAll();
    }
}
```

### Sample Output

```
Saved Coffee Coffee(id=670a2345678901bcdef23456, name=espresso, price=TWD 100.00, createTime=..., updateTime=...)
Saved Coffee Coffee(id=670a3456789012cdef345678, name=latte, price=TWD 150.00, createTime=..., updateTime=...)
Coffee Coffee(id=670a3456789012cdef345678, name=latte, price=TWD 175.00, createTime=..., updateTime=...)
```

## Configuration

### Application Properties

```properties
# MongoDB connection URI
spring.data.mongodb.uri=mongodb://springbucks:springbucks@localhost:27017/springbucks

# Alternative: Separate properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=springbucks
spring.data.mongodb.username=springbucks
spring.data.mongodb.password=springbucks
```

## Key Components

### CoffeeRepository

```java
public interface CoffeeRepository extends MongoRepository<Coffee, String> {
    
    /**
     * Query by method name
     * Spring Data MongoDB auto-generates query from method name
     */
    List<Coffee> findByName(String name);
}
```

**Auto-provided Methods:**
- `save(T entity)` - Save or update
- `saveAll(Iterable<T> entities)` - Batch save
- `findById(ID id)` - Find by ID
- `findAll()` - Find all
- `findAll(Sort sort)` - Find all with sorting
- `findAll(Pageable pageable)` - Find all with pagination
- `count()` - Count documents
- `deleteById(ID id)` - Delete by ID
- `delete(T entity)` - Delete entity
- `deleteAll()` - Delete all
- `insert(T entity)` - Insert document
- `insert(Iterable<T> entities)` - Batch insert

### Query Method Naming

```java
public interface CoffeeRepository extends MongoRepository<Coffee, String> {
    
    // Single property query
    List<Coffee> findByName(String name);
    
    // Multi-condition AND query
    List<Coffee> findByNameAndPrice(String name, Money price);
    
    // Multi-condition OR query
    List<Coffee> findByNameOrPrice(String name, Money price);
    
    // Comparison queries
    List<Coffee> findByPriceLessThan(Money price);
    List<Coffee> findByPriceGreaterThanEqual(Money price);
    List<Coffee> findByPriceBetween(Money min, Money max);
    
    // Like queries
    List<Coffee> findByNameLike(String name);
    List<Coffee> findByNameStartingWith(String prefix);
    List<Coffee> findByNameContaining(String keyword);
    
    // Sorting
    List<Coffee> findByNameOrderByPriceDesc(String name);
    
    // Pagination
    Page<Coffee> findByName(String name, Pageable pageable);
    
    // Count
    long countByName(String name);
    
    // Exists
    boolean existsByName(String name);
    
    // Delete
    long deleteByName(String name);
}
```

### Custom Converters System

**Three converters handle different data formats:**

```java
@Configuration
public class MongoConfig {
    
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new MoneyReadConverter(),      // Document → Money (complex BSON)
            new MoneyLongReadConverter(),  // Long → Money (simple format)
            new MoneyWriteConverter()      // Money → Long (for storage)
        ));
    }
}
```

**Why Three Converters?**
- **Backward Compatibility**: Handle old data stored as Long
- **Forward Compatibility**: Handle new data stored as Document
- **Write Optimization**: Store as Long (more efficient)

**Conversion Flow:**

```
Read from MongoDB:
  BSON Document → MoneyReadConverter → Money object
  OR
  BSON Long → MoneyLongReadConverter → Money object

Write to MongoDB:
  Money object → MoneyWriteConverter → BSON Long
```

## MongoRepository vs MongoTemplate

| Feature | MongoRepository | MongoTemplate |
|---------|----------------|---------------|
| Abstraction Level | High | Low |
| Flexibility | Medium | High |
| Ease of Use | Easy (method naming) | Requires code |
| Complex Queries | ⚠️ Need @Query | ✅ Fully supported |
| Aggregation | ❌ Need MongoTemplate | ✅ Fully supported |
| Use Case | Simple CRUD | Complex business logic |
| Learning Curve | Gentle | Steep |

**Selection Guide:**
- **Simple operations**: Use MongoRepository
- **Complex queries**: Use MongoTemplate
- **Mixed approach**: Repository + custom implementation with MongoTemplate

## Testing

```bash
# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run
```

## Best Practices Demonstrated

1. **Repository Pattern**: Clean separation of data access layer
2. **Method Naming Convention**: Intuitive query method names
3. **Batch Operations**: Efficient bulk insert/update
4. **Custom Converters**: Handle complex type conversions
5. **Sorting Support**: Organize query results
6. **Clean Shutdown**: Remove test data after execution

## References

- [Spring Data MongoDB Documentation](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)
- [Spring Data Repository Query Methods](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongodb.repositories.queries)
- [Joda Money Documentation](https://www.joda.org/joda-money/)

## License

MIT License - see [LICENSE](LICENSE) file for details.

## About Us

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。近來也積極結合 AI 技術，推動自動化工作流，讓開發與運維更有效率、更智慧。持續學習與分享，希望能一起推動軟體開發的創新和進步。

## Contact

**風清雲談** - 專注於敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。

- 🌐 官方網站：[風清雲談部落格](https://blog.fengqing.tw/)
- 📘 Facebook：[風清雲談粉絲頁](https://www.facebook.com/profile.php?id=61576838896062)
- 💼 LinkedIn：[Chu Kuo-Lung](https://www.linkedin.com/in/chu-kuo-lung)
- 📺 YouTube：[雲談風清頻道](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- 📧 Email：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**⭐ 如果這個專案對您有幫助，歡迎給個 Star！**

# Spring Data/JPA Interview Questions

## Basic Concepts

### 1. What is Spring Data JPA?
Spring Data JPA is a part of the Spring Data family that provides an abstraction layer over JPA (Java Persistence API). It reduces the amount of boilerplate code required to implement data access layers by providing repository interfaces and implementations automatically.

### 2. What is the difference between JPA and Spring Data JPA?
- **JPA**: A specification that defines how to persist Java objects in a database. It's just a set of interfaces and annotations.
- **Spring Data JPA**: An implementation of JPA that provides additional features like:
  - Repository abstraction
  - Automatic query generation
  - Pagination and sorting support
  - Dynamic query creation
  - Integration with Spring's transaction management

### 3. What is a Repository in Spring Data JPA?
A Repository is an interface that extends `JpaRepository` or `CrudRepository`. It provides methods for common database operations like:
- `save()`
- `findById()`
- `findAll()`
- `delete()`
- `count()`

## Advanced Concepts

### 4. What are the different types of repositories in Spring Data JPA?
Spring Data JPA provides several repository interfaces that serve different purposes:

1. **CrudRepository**
   - Basic CRUD operations
   - Extends `Repository<T, ID>` interface
   - Provides methods like:
     ```java
     save(S entity)                    // Save a single entity
     saveAll(Iterable<S> entities)     // Save multiple entities
     findById(ID id)                   // Find entity by ID
     existsById(ID id)                 // Check if entity exists
     findAll()                         // Get all entities
     findAllById(Iterable<ID> ids)     // Get multiple entities by IDs
     count()                           // Count total entities
     deleteById(ID id)                 // Delete by ID
     delete(T entity)                  // Delete entity
     deleteAllById(Iterable<ID> ids)   // Delete multiple entities by IDs
     deleteAll(Iterable<T> entities)   // Delete multiple entities
     deleteAll()                       // Delete all entities
     ```

2. **PagingAndSortingRepository**
   - Extends `CrudRepository<T, ID>`
   - Adds pagination and sorting capabilities
   - Additional methods:
     ```java
     findAll(Sort sort)                // Get all entities with sorting
     findAll(Pageable pageable)        // Get paginated entities
     ```

3. **JpaRepository**
   - Extends `PagingAndSortingRepository<T, ID>`
   - Most commonly used repository interface
   - Adds JPA-specific methods:
     ```java
     flush()                          // Flush pending changes
     saveAndFlush(S entity)           // Save and flush immediately
     deleteInBatch(Iterable<T> entities) // Delete entities in batch
     deleteAllInBatch()               // Delete all entities in batch
     getOne(ID id)                    // Get reference to entity
     getById(ID id)                   // Get entity by ID
     findAll()                        // Get all entities
     findAll(Sort sort)               // Get all entities with sorting
     findAll(Pageable pageable)       // Get paginated entities
     ```

4. **QuerydslPredicateExecutor**
   - Adds support for Querydsl predicates
   - Enables type-safe queries
   - Additional methods:
     ```java
     findOne(Predicate predicate)     // Find single entity matching predicate
     findAll(Predicate predicate)     // Find all entities matching predicate
     findAll(Predicate predicate, Sort sort) // Find with sorting
     findAll(Predicate predicate, Pageable pageable) // Find with pagination
     count(Predicate predicate)       // Count matching entities
     exists(Predicate predicate)      // Check if any entity matches
     ```

5. **ReactiveCrudRepository** (for reactive programming)
   - Similar to CrudRepository but returns reactive types
   - Methods return `Mono<T>` or `Flux<T>`
   - Example:
     ```java
     Mono<T> save(T entity)
     Flux<T> findAll()
     Mono<T> findById(ID id)
     ```

6. **Custom Repository Interfaces**
   - Can create custom repository interfaces
   - Extend any of the above interfaces
   - Add custom methods
   - Example:
     ```java
     public interface CustomUserRepository extends JpaRepository<User, Long> {
         List<User> findByCustomCriteria(String criteria);
         @Query("SELECT u FROM User u WHERE u.status = :status")
         List<User> findByStatus(@Param("status") String status);
     }
     ```

Key Points to Remember:
- Choose the appropriate repository interface based on your needs
- JpaRepository is most commonly used as it provides all necessary methods
- Custom repositories can extend any of these interfaces
- Consider using QuerydslPredicateExecutor for complex queries
- Use ReactiveCrudRepository for reactive programming
- All repository interfaces are automatically implemented by Spring Data JPA

### 5. How does Spring Data JPA generate queries?
Spring Data JPA generates queries in several ways:
1. **Method name parsing**: Creates queries based on method names
   ```java
   List<User> findByEmailAndAge(String email, int age);
   ```
2. **@Query annotation**: Custom JPQL or native SQL queries
   ```java
   @Query("SELECT u FROM User u WHERE u.email = ?1")
   User findByEmail(String email);
   ```
3. **QueryDSL**: Type-safe queries using QueryDSL

### 6. What is the difference between @Entity and @Table?
- **@Entity**: Marks a class as a JPA entity
- **@Table**: Specifies the database table details
  ```java
  @Entity
  @Table(name = "users", schema = "public")
  public class User {
      // ...
  }
  ```

### 7. What is the difference between @Id and @GeneratedValue?
- **@Id**: Marks a field as the primary key
- **@GeneratedValue**: Specifies how the primary key should be generated

While `@GeneratedValue` is commonly used for primary keys, it's not always mandatory. Here are the different scenarios:

1. **When to use @GeneratedValue**:
   - For surrogate keys (artificial primary keys)
   - When you want the database to automatically generate unique IDs
   - For new entities where you don't want to manage ID generation manually

2. **When NOT to use @GeneratedValue**:
   - For natural keys (business keys that have meaning)
   - When you need to set IDs manually
   - When using composite primary keys
   - When using UUID as primary key (in some cases)

3. **Generation Strategies**:
   ```java
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;
   ```
   Available strategies:
   - **AUTO**: Let the persistence provider choose the strategy
   - **IDENTITY**: Uses database identity column (auto-increment)
   - **SEQUENCE**: Uses database sequence
   - **TABLE**: Uses a database table to generate values

4. **Examples of different strategies**:

   a. **IDENTITY** (Most common for MySQL):
   ```java
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   ```

   b. **SEQUENCE** (Common for Oracle, PostgreSQL):
   ```java
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
   @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
   private Long id;
   ```

   c. **TABLE** (Less common, works with any database):
   ```java
   @Id
   @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_generator")
   @TableGenerator(name = "user_generator", table = "id_generator", 
                  pkColumnName = "gen_name", valueColumnName = "gen_value",
                  pkColumnValue = "user_id", allocationSize = 1)
   private Long id;
   ```

5. **Best Practices**:
   - Use `IDENTITY` for MySQL
   - Use `SEQUENCE` for Oracle and PostgreSQL
   - Use `AUTO` when you want the JPA provider to choose the best strategy
   - Consider performance implications:
     - `IDENTITY`: Can cause performance issues with batch inserts
     - `SEQUENCE`: Better for batch operations
     - `TABLE`: Most flexible but least performant

6. **Common Issues and Solutions**:
   - Batch Insert Performance:
     ```java
     // For better batch insert performance with SEQUENCE
     @SequenceGenerator(name = "user_seq", allocationSize = 50)
     ```
   - UUID Generation:
     ```java
     @Id
     @GeneratedValue(generator = "uuid2")
     @GenericGenerator(name = "uuid2", strategy = "uuid2")
     @Column(columnDefinition = "VARCHAR(36)")
     private String id;
     ```

7. **Composite Primary Keys** (when not using @GeneratedValue):
   ```java
   @IdClass(UserId.class)
   public class User {
       @Id
       private String username;
       
       @Id
       private String email;
   }
   ```

Remember:
- Not all primary keys need to be generated
- Choose the generation strategy based on your database and requirements
- Consider performance implications for your specific use case
- For natural keys, you might not need @GeneratedValue at all
- For UUIDs, consider using a UUID generator instead of @GeneratedValue

### 8. What are the different types of fetch types in JPA?

Main Differences Between EAGER and LAZY Loading:

1. **Loading Behavior**:
   - **EAGER**: Loads the related entity immediately when the parent entity is loaded
   - **LAZY**: Loads the related entity only when it's accessed for the first time

2. **Database Queries**:
   - **EAGER**: Executes a JOIN query to fetch both parent and related entities in a single query
   - **LAZY**: Executes separate queries - one for parent entity and additional queries when related entities are accessed

Example to illustrate the difference:
```java
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    
    // EAGER loading
    @OneToOne(fetch = FetchType.EAGER)
    private Address address;  // Loaded immediately with User
    
    // LAZY loading
    @OneToMany(fetch = FetchType.LAZY)
    private List<Order> orders;  // Not loaded until accessed
}

// When you do:
User user = userRepository.findById(1L).orElseThrow();

// With EAGER loading:
// - Single query executed: SELECT u.*, a.* FROM user u LEFT JOIN address a ON u.id = a.user_id
// - user.getAddress() is immediately available
// - No additional queries needed

// With LAZY loading:
// - First query: SELECT * FROM user WHERE id = 1
// - When you do: user.getOrders().size()
// - Second query executed: SELECT * FROM orders WHERE user_id = 1
```

3. **Memory Usage**:
   - **EAGER**: Uses more memory as all related data is loaded at once
   - **LAZY**: Uses less memory initially, but may use more if many related entities are accessed

4. **Performance Impact**:
   - **EAGER**:
     - Better when you always need the related data
     - Can cause performance issues with large datasets
     - May lead to Cartesian product problems with multiple EAGER relationships
   - **LAZY**:
     - Better when you don't always need the related data
     - Can cause N+1 query problems if not handled properly
     - More efficient for large collections

5. **Use Case Examples**:

```java
// Good use of EAGER:
@Entity
public class User {
    @OneToOne(fetch = FetchType.EAGER)
    private UserProfile profile;  // Always needed with user
}

// Good use of LAZY:
@Entity
public class User {
    @OneToMany(fetch = FetchType.LAZY)
    private List<Order> orders;  // Not always needed
}

// Problematic EAGER:
@Entity
public class User {
    @OneToMany(fetch = FetchType.EAGER)
    private List<Order> orders;  // Could be thousands of orders!
    
    @OneToMany(fetch = FetchType.EAGER)
    private List<Comment> comments;  // Could be thousands of comments!
}

// Problematic LAZY:
@Entity
public class User {
    @OneToOne(fetch = FetchType.LAZY)
    private UserProfile profile;  // Always needed, causes extra query
}
```

6. **Common Issues and Solutions**:

```java
// Problem: N+1 queries with LAZY loading
List<User> users = userRepository.findAll();
for (User user : users) {
    System.out.println(user.getOrders().size());  // N+1 queries!
}

// Solution 1: Use JOIN FETCH
@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Solution 2: Use @EntityGraph
@EntityGraph(attributePaths = {"orders"})
List<User> findAll();

// Problem: LazyInitializationException
@Transactional
public UserDTO getUserDTO(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    return new UserDTO(user);  // Exception when accessing orders!
}

// Solution: Force initialization
@Transactional
public UserDTO getUserDTO(Long userId) {
    User user = userRepository.findById(userId).orElseThrow();
    user.getOrders().size();  // Force initialization
    return new UserDTO(user);
}
```

7. **When to Use Each**:

Use EAGER when:
- The related entity is always needed
- The relationship is small (one-to-one or few-to-one)
- The related data is small in size
- You want to avoid additional queries

Use LAZY when:
- The related entity is not always needed
- The relationship is large (one-to-many or many-to-many)
- The related data is large in size
- You want to optimize initial load time

### 9. What is the difference between @OneToMany and @ManyToOne?
- **@OneToMany**: One entity can have multiple related entities
- **@ManyToOne**: Multiple entities can be related to one entity
   ```java
   @OneToMany(mappedBy = "user")
   private List<Order> orders;

   @ManyToOne
   @JoinColumn(name = "user_id")
   private User user;
   ```

### 10. What is the purpose of @Transactional?
@Transactional is used to define the scope of a single database transaction. It can be applied at:
- Class level: All methods in the class are transactional
- Method level: Only the specific method is transactional

## Best Practices

### 11. What are some best practices when using Spring Data JPA?
1. Use appropriate fetch types (LAZY vs EAGER)
2. Implement proper transaction management
3. Use pagination for large datasets
4. Implement proper exception handling
5. Use DTOs for data transfer
6. Implement proper validation
7. Use appropriate indexes
8. Implement proper caching strategies

### 12. How do you handle N+1 query problem in Spring Data JPA?
1. Use JOIN FETCH in JPQL
   ```java
   @Query("SELECT u FROM User u JOIN FETCH u.orders")
   List<User> findAllWithOrders();
   ```
2. Use @EntityGraph
   ```java
   @EntityGraph(attributePaths = {"orders"})
   List<User> findAll();
   ```
3. Use batch fetching
   ```java
   @BatchSize(size = 100)
   @OneToMany(mappedBy = "user")
   private List<Order> orders;
   ```

### 13. How do you implement pagination in Spring Data JPA?
```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
Page<User> users = userRepository.findAll(pageable);
```

### 14. How do you implement custom queries in Spring Data JPA?
1. Using @Query
   ```java
   @Query("SELECT u FROM User u WHERE u.age > ?1")
   List<User> findByAgeGreaterThan(int age);
   ```
2. Using @Query with native SQL
   ```java
   @Query(value = "SELECT * FROM users WHERE age > ?1", nativeQuery = true)
   List<User> findByAgeGreaterThanNative(int age);
   ```

### 15. How do you handle transactions in Spring Data JPA?
1. Using @Transactional
   ```java
   @Transactional
   public void saveUser(User user) {
       userRepository.save(user);
   }
   ```
2. Using TransactionTemplate
   ```java
   transactionTemplate.execute(status -> {
       userRepository.save(user);
       return null;
   });
   ```

## Performance and Optimization

### 16. How do you optimize Spring Data JPA performance?
1. Use appropriate indexes
2. Implement proper caching
3. Use batch processing
4. Optimize fetch types
5. Use pagination
6. Implement proper transaction management
7. Use appropriate query methods
8. Monitor and analyze query performance

### 17. What is the difference between @Modifying and @Query?
- **@Query**: Defines a custom query
- **@Modifying**: Indicates that the query modifies the database
   ```java
   @Modifying
   @Query("UPDATE User u SET u.status = ?1 WHERE u.id = ?2")
   void updateStatus(String status, Long id);
   ```

### 18. How do you implement auditing in Spring Data JPA?
1. Enable auditing
   ```java
   @EnableJpaAuditing
   @Configuration
   public class JpaConfig {
       // ...
   }
   ```
2. Use auditing annotations
   ```java
   @Entity
   @EntityListeners(AuditingEntityListener.class)
   public class User {
       @CreatedDate
       private LocalDateTime createdDate;
       
       @LastModifiedDate
       private LocalDateTime lastModifiedDate;
   }
   ```

### 19. How do you handle database migrations in Spring Data JPA?
1. Use Flyway
2. Use Liquibase
3. Use Hibernate's schema generation
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

### 20. How do you implement soft delete in Spring Data JPA?
1. Use @SQLDelete
   ```java
   @SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
   @Where(clause = "deleted = false")
   @Entity
   public class User {
       // ...
   }
   ```
2. Use @Filter
   ```java
   @Filter(name = "deletedFilter", condition = "deleted = false")
   @Entity
   public class User {
       // ...
   }
   ```

### 21. How does @Transactional work internally in JPA/Spring?

1. **Basic Transaction Management**:
   ```java
   @Transactional
   public void saveUser(User user) {
       userRepository.save(user);
   }
   ```
   Internally, Spring:
   - Creates a new transaction
   - Gets a connection from the connection pool
   - Sets auto-commit to false
   - Executes the method
   - Commits or rolls back based on the result

2. **Transaction Propagation**:
   ```java
   @Service
   public class UserService {
       @Transactional(propagation = Propagation.REQUIRED) // Default
       public void method1() {
           // New transaction if none exists, or uses existing
       }
       
       @Transactional(propagation = Propagation.REQUIRES_NEW)
       public void method2() {
           // Always creates new transaction
       }
       
       @Transactional(propagation = Propagation.NESTED)
       public void method3() {
           // Creates nested transaction if supported
       }
   }
   ```

3. **Transaction Isolation Levels**:
   ```java
   @Transactional(isolation = Isolation.READ_COMMITTED)
   public void updateUser() {
       // Default in most databases
   }
   
   @Transactional(isolation = Isolation.REPEATABLE_READ)
   public void criticalUpdate() {
       // Higher isolation, prevents non-repeatable reads
   }
   ```

4. **Internal Working**:

   a. **Transaction Creation**:
   ```java
   // Simplified internal Spring code
   public class TransactionAspectSupport {
       protected Object invokeWithinTransaction(Method method, Class<?> targetClass, 
           final InvocationCallback invocation) throws Throwable {
           
           // 1. Get transaction manager
           PlatformTransactionManager ptm = getTransactionManager();
           
           // 2. Create transaction
           TransactionStatus status = ptm.getTransaction(txAttr);
           
           try {
               // 3. Execute method
               Object retVal = invocation.proceedWithInvocation();
               
               // 4. Commit transaction
               ptm.commit(status);
               return retVal;
           }
           catch (Throwable ex) {
               // 5. Rollback on exception
               ptm.rollback(status);
               throw ex;
           }
       }
   }
   ```

   b. **Connection Management**:
   ```java
   // Simplified JPA transaction management
   public class JpaTransactionManager {
       protected void doBegin(Object transaction, TransactionDefinition definition) {
           // 1. Get connection from pool
           Connection conn = dataSource.getConnection();
           
           // 2. Set auto-commit to false
           conn.setAutoCommit(false);
           
           // 3. Set isolation level
           conn.setTransactionIsolation(definition.getIsolationLevel());
           
           // 4. Store connection in transaction
           ((JpaTransactionObject) transaction).setConnection(conn);
       }
   }
   ```

5. **Transaction Synchronization**:
   ```java
   @Transactional
   public void complexOperation() {
       // 1. Transaction starts
       userRepository.save(user);
       
       // 2. Transaction synchronization
       TransactionSynchronizationManager.registerSynchronization(
           new TransactionSynchronizationAdapter() {
               @Override
               public void afterCommit() {
                   // Execute after commit
               }
               
               @Override
               public void afterRollback() {
                   // Execute after rollback
               }
           }
       );
       
       // 3. Transaction ends
   }
   ```

6. **Common Issues and Solutions**:

   a. **Self-Invocation Problem**:
   ```java
   @Service
   public class UserService {
       @Transactional
       public void method1() {
           method2(); // @Transactional won't work here!
       }
       
       @Transactional
       public void method2() {
           // This won't be in a transaction!
       }
   }
   
   // Solution: Use self-injection
   @Service
   public class UserService {
       @Autowired
       private UserService self;
       
       public void method1() {
           self.method2(); // Now @Transactional works
       }
       
       @Transactional
       public void method2() {
           // This will be in a transaction
       }
   }
   ```

   b. **Transaction Timeout**:
   ```java
   @Transactional(timeout = 30) // 30 seconds
   public void longRunningOperation() {
       // Will throw TransactionTimedOutException if takes longer
   }
   ```

7. **Best Practices**:

   a. **Transaction Scope**:
   ```java
   // Good: Transaction at service level
   @Service
   public class UserService {
       @Transactional
       public void createUser(UserDTO dto) {
           User user = convertToEntity(dto);
           userRepository.save(user);
           // All operations in one transaction
       }
   }
   
   // Bad: Too many small transactions
   @Service
   public class UserService {
       public void processUsers(List<UserDTO> dtos) {
           for (UserDTO dto : dtos) {
               @Transactional // Don't do this!
               public void processUser(UserDTO dto) {
                   // Each iteration creates new transaction
               }
           }
       }
   }
   ```

   b. **Exception Handling**:
   ```java
   @Transactional(rollbackFor = {CustomException.class})
   public void criticalOperation() {
       // Will rollback on CustomException
   }
   
   @Transactional(noRollbackFor = {BusinessException.class})
   public void businessOperation() {
       // Won't rollback on BusinessException
   }
   ```

8. **Performance Considerations**:
   - Keep transactions as short as possible
   - Avoid long-running transactions
   - Use appropriate isolation levels
   - Consider using read-only transactions when possible
   - Use batch operations for multiple updates

Remember:
- `@Transactional` uses AOP (Aspect-Oriented Programming)
- Transactions are thread-bound
- Self-invocation doesn't work with `@Transactional`
- Choose appropriate propagation and isolation levels
- Handle exceptions properly
- Consider performance implications

### 22. What are Silent Rollbacks in @Transactional and How to Handle Them?

1. **What is a Silent Rollback?**
   - A transaction that rolls back without throwing an exception
   - Can occur when an exception is caught and swallowed
   - Can happen when using `@Transactional` with incorrect exception handling

2. **Common Causes of Silent Rollbacks**:

   a. **Swallowed Exceptions**:
   ```java
   @Transactional
   public void problematicMethod() {
       try {
           userRepository.save(user);
           // Some operation that fails
       } catch (Exception e) {
           // Exception swallowed - transaction will roll back silently
           log.error("Error occurred", e);
       }
   }
   ```

   b. **Runtime Exceptions**:
   ```java
   @Transactional
   public void methodWithRuntimeException() {
       userRepository.save(user);
       if (someCondition) {
           throw new RuntimeException(); // Will cause rollback
       }
   }
   ```

3. **How to Detect Silent Rollbacks**:

   a. **Using TransactionSynchronization**:
   ```java
   @Transactional
   public void methodWithRollbackDetection() {
       TransactionSynchronizationManager.registerSynchronization(
           new TransactionSynchronizationAdapter() {
               @Override
               public void afterCompletion(int status) {
                   if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                       log.warn("Transaction was rolled back!");
                   }
               }
           }
       );
       // Your transaction code
   }
   ```

   b. **Using TransactionStatus**:
   ```java
   @Transactional
   public void methodWithTransactionStatus() {
       TransactionStatus status = TransactionAspectSupport.currentTransactionStatus();
       try {
           // Your transaction code
       } catch (Exception e) {
           if (status.isRollbackOnly()) {
               log.warn("Transaction marked for rollback");
           }
           throw e;
       }
   }
   ```

4. **Proper Exception Handling**:

   a. **Explicit Rollback**:
   ```java
   @Transactional
   public void methodWithExplicitRollback() {
       try {
           userRepository.save(user);
       } catch (Exception e) {
           TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
           throw new CustomException("Failed to save user", e);
       }
   }
   ```

   b. **Custom Exception Handling**:
   ```java
   @Transactional(rollbackFor = {CustomException.class})
   public void methodWithCustomException() {
       try {
           userRepository.save(user);
       } catch (Exception e) {
           throw new CustomException("Failed to save user", e);
       }
   }
   ```

5. **Best Practices to Avoid Silent Rollbacks**:

   a. **Proper Exception Propagation**:
   ```java
   @Transactional
   public void goodPractice() {
       try {
           userRepository.save(user);
       } catch (Exception e) {
           // Log the error
           log.error("Error saving user", e);
           // Re-throw or wrap in custom exception
           throw new CustomException("Failed to save user", e);
       }
   }
   ```

   b. **Using @Transactional with Specific Exceptions**:
   ```java
   @Transactional(
       rollbackFor = {CustomException.class},
       noRollbackFor = {BusinessException.class}
   )
   public void methodWithSpecificExceptions() {
       // Your code
   }
   ```

6. **Debugging Silent Rollbacks**:

   a. **Enable Transaction Logging**:
   ```properties
   # application.properties
   logging.level.org.springframework.transaction=DEBUG
   logging.level.org.hibernate.SQL=DEBUG
   ```

   b. **Using Transaction Debugging**:
   ```java
   @Transactional
   public void methodWithDebugging() {
       TransactionStatus status = TransactionAspectSupport.currentTransactionStatus();
       log.debug("Transaction status: {}", status);
       
       try {
           // Your code
       } catch (Exception e) {
           log.debug("Exception occurred, rollback status: {}", status.isRollbackOnly());
           throw e;
       }
   }
   ```

7. **Common Pitfalls and Solutions**:

   a. **Nested Transactions**:
   ```java
   @Service
   public class UserService {
       @Transactional
       public void outerMethod() {
           try {
               innerMethod(); // If this fails, outer transaction might roll back silently
           } catch (Exception e) {
               // Handle exception properly
               throw new CustomException("Inner method failed", e);
           }
       }
       
       @Transactional(propagation = Propagation.REQUIRES_NEW)
       public void innerMethod() {
           // This will have its own transaction
       }
   }
   ```

   b. **Async Methods**:
   ```java
   @Async
   @Transactional
   public void asyncMethod() {
       // This might cause issues with transaction management
       // Consider using TransactionTemplate instead
   }
   
   // Better approach
   @Async
   public void asyncMethod() {
       transactionTemplate.execute(status -> {
           // Your transaction code
           return null;
       });
   }
   ```

Remember:
- Always handle exceptions properly in `@Transactional` methods
- Don't swallow exceptions that should cause rollbacks
- Use appropriate exception types for rollback control
- Consider using transaction debugging in development
- Be careful with nested transactions
- Use `TransactionTemplate` for programmatic transaction management
- Monitor transaction logs for unexpected rollbacks 
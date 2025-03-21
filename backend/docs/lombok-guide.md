# Lombok Usage Guide for InfoLogic POS

Lombok is a Java library that helps reduce boilerplate code through the use of annotations. This guide explains how to use Lombok effectively in the InfoLogic POS project.

## Core Annotations

### Class-Level Annotations

| Annotation | Description | Use Case in Our Project |
|------------|-------------|-------------------------|
| `@Data` | Generates getters, setters, equals, hashCode, and toString methods | DTOs, simple entity classes |
| `@Getter` | Generates getters for all fields | When you only need getters |
| `@Setter` | Generates setters for all fields | When you only need setters |
| `@Builder` | Implements the Builder pattern | Complex objects with many fields |
| `@NoArgsConstructor` | Generates a no-args constructor | JPA entities, Spring components |
| `@AllArgsConstructor` | Generates a constructor with all fields | Immutable objects |
| `@RequiredArgsConstructor` | Generates a constructor for final/non-null fields | Services with dependencies |
| `@EqualsAndHashCode` | Generates equals and hashCode methods | When you need custom equality behavior |
| `@ToString` | Generates a toString method | For debugging and logging |

### Field-Level Annotations

| Annotation | Description | Use Case |
|------------|-------------|----------|
| `@NonNull` | Adds null check to parameters | To enforce non-null parameters |
| `@Singular` | Used with `@Builder` for collections | Adding items to collections in builders |
| `@UtilityClass` | Makes a class a utility class | Static utility classes |

### Log-Related Annotations

| Annotation | Description | Use in Our Project |
|------------|-------------|-------------------|
| `@Slf4j` | Creates a logger field named `log` | Preferred logging annotation |
| `@Log4j2` | Creates a Log4j 2 logger | Alternative logging option |

## Best Practices

### 1. Prefer Class-Level Annotations

```java
// Good
@Data
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
}

// Avoid
public class ProductDTO {
    @Getter @Setter private Long id;
    @Getter @Setter private String name;
    @Getter @Setter private BigDecimal price;
}
```

### 2. Combine Annotations Carefully

```java
// Good approach for services
@Service
@Slf4j
@RequiredArgsConstructor
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    
    public void processSale(Sale sale) {
        log.info("Processing sale: {}", sale.getId());
        // Implementation
    }
}
```

### 3. Use Builder Pattern for Complex Objects

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDTO {
    private Long id;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    @Singular
    private List<SaleItemDTO> items;
    private String paymentMethod;
}

// Usage
SaleDTO sale = SaleDTO.builder()
    .id(1L)
    .saleDate(LocalDateTime.now())
    .totalAmount(new BigDecimal("99.99"))
    .item(item1)
    .item(item2)
    .paymentMethod("CREDIT_CARD")
    .build();
```

### 4. Be Cautious with @EqualsAndHashCode

For JPA entities with relationships, customize `@EqualsAndHashCode`:

```java
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"id"}) // Only use ID for equality
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Other fields
}
```

### 5. Configure Logging with @Slf4j

```java
@Slf4j
public class AuthenticationService {
    public void authenticate(User user) {
        log.debug("Authenticating user: {}", user.getUsername());
        // Implementation
        log.info("User authenticated successfully: {}", user.getUsername());
    }
}
```

## Troubleshooting

### Common Issues and Solutions

1. **Lombok annotations aren't working**
   - Make sure Lombok is properly installed in your IDE
   - Run the `./scripts/fix-lombok.sh` script
   - Check that annotation processing is enabled in your IDE

2. **Log field is not recognized**
   - Make sure you're using the `@Slf4j` annotation correctly
   - Check imports (should be `import lombok.extern.slf4j.Slf4j;`)
   - Verify SLF4J is in your classpath

3. **Builder isn't working with inheritance**
   - Use `@SuperBuilder` instead of `@Builder` when extending classes

4. **Constructor conflicts**
   - Be careful when combining `@NoArgsConstructor`, `@RequiredArgsConstructor`, and `@AllArgsConstructor`
   - For complex cases, consider writing constructors manually

## IDE Setup

### IntelliJ IDEA

1. Install the Lombok plugin:
   - File → Settings → Plugins → Search for "Lombok" → Install
   
2. Enable annotation processing:
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### Eclipse

1. Download the Lombok installer JAR
2. Run the JAR file: `java -jar lombok.jar`
3. Select your Eclipse installation directory and install
4. Restart Eclipse

### VS Code

1. Install the "Language Support for Java" extension
2. Add Lombok to the classpath

## Further Resources

- [Project Lombok official documentation](https://projectlombok.org/features/all)
- [IntelliJ Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok)
- [Lombok configuration system](https://projectlombok.org/features/configuration) 
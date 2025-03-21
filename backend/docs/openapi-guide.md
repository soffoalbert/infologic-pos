# OpenAPI Annotations Guide

This guide explains the OpenAPI annotations used in the InfoLogic POS system for API documentation.

## Controller Annotations

### Class Level

```java
@Tag(name = "Authentication", description = "Authentication management APIs")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // ...
}
```

- `@Tag`: Groups operations by a tag name, used for organizing the Swagger UI

### Method Level

```java
@Operation(
    summary = "User login", 
    description = "Authenticates a user and returns a JWT token"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successful authentication"),
    @ApiResponse(responseCode = "401", description = "Authentication failed"),
    @ApiResponse(responseCode = "403", description = "Account locked or disabled")
})
@PostMapping("/login")
public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    // ...
}
```

- `@Operation`: Describes the operation, including summary and detailed description
- `@ApiResponses`: Documents possible response codes and their meanings
- `@ApiResponse`: Describes a specific response code with an optional schema

## Parameter Annotations

```java
@GetMapping("/product/{id}")
public ResponseEntity<ProductDTO> getProduct(
    @Parameter(description = "Product ID", example = "42", required = true)
    @PathVariable Long id,
    
    @Parameter(description = "Include deleted products", example = "false")
    @RequestParam(required = false) Boolean includeDeleted
) {
    // ...
}
```

- `@Parameter`: Provides additional information about operation parameters
  - `description`: Human-readable description
  - `example`: Example value
  - `required`: Whether the parameter is mandatory

## Schema Annotations

```java
@Schema(description = "Product information")
public class ProductDTO {
    
    @Schema(description = "Unique product identifier", example = "42")
    private Long id;
    
    @Schema(description = "Product name", example = "Organic Coffee Beans", required = true)
    private String name;
    
    @Schema(description = "Product price in USD", example = "12.99", minimum = "0")
    private BigDecimal price;
    
    // ...
}
```

- `@Schema`: Documents a model schema or property
  - Use at class level to document the entire model
  - Use at field level to document properties
  - Use `example` to provide example values
  - Use `required` to indicate mandatory fields

## Best Practices

1. **Be Consistent**: Use the same style and level of detail across all endpoints
2. **Provide Examples**: Include realistic example values for all parameters and fields
3. **Document Error Responses**: Include all possible error response codes and meanings
4. **Group Related Endpoints**: Use consistent tags to group related endpoints
5. **Describe Security Requirements**: Document authentication needs for endpoints
6. **Use Proper Data Types**: Ensure fields have appropriate data types (string, number, boolean, etc.)
7. **Include Validation Rules**: Document minimum/maximum values, patterns, etc.

## Common Annotations

| Annotation | Purpose | Used On |
|------------|---------|---------|
| `@Tag` | Groups operations | Controller class |
| `@Operation` | Describes an endpoint | Controller method |
| `@ApiResponses` | Documents possible responses | Controller method |
| `@Parameter` | Documents a parameter | Method parameter |
| `@Schema` | Documents a model or property | Class or field |
| `@Hidden` | Excludes from documentation | Class or method |

## Implementation Example

```java
@Tag(name = "Products", description = "Product management APIs")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Operation(
        summary = "Create a new product",
        description = "Creates a new product in the inventory"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Product created",
            content = @Content(schema = @Schema(implementation = ProductDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid product data"
        )
    })
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
        @RequestBody @Valid ProductDTO productDTO
    ) {
        // Implementation
    }
}
```

For more information, refer to the [SpringDoc OpenAPI documentation](https://springdoc.org/). 
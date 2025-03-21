# InfoLogic POS Backend

This is the backend API for the InfoLogic Point of Sale (POS) system.

## API Documentation

The API is documented using Swagger UI, which provides an interactive documentation interface.

### Accessing Swagger UI

Once the application is running, you can access the Swagger UI by navigating to:

```
http://localhost:8080/swagger-ui.html
```

### Features of the Documentation

1. **Interactive API Testing**: You can try out API endpoints directly from the browser
2. **Authentication**: The Swagger UI supports JWT authentication - use the `/api/auth/login` endpoint to get a token
3. **Detailed Schema Information**: All request and response models are fully documented
4. **Role-Based Access**: Endpoints are marked with their required roles (ADMIN, VENDOR, CASHIER)

### Authentication

1. First, use the `/api/auth/login` endpoint to authenticate
2. If MFA is not enabled, you'll receive a JWT token
3. Copy this token
4. Click on the "Authorize" button at the top of the page
5. Enter your token in the format: `Bearer your_token_here`
6. Now you're authenticated and can access secured endpoints

### Common Swagger UI Usage Scenarios

#### 1. Browsing Available Endpoints
- Navigate to the Swagger UI page
- Endpoints are grouped by controller/functionality
- Expand a controller section to see all available endpoints

#### 2. Testing Endpoints
- Click on an endpoint to expand it
- Click the "Try it out" button
- Fill in the required parameters or request body
- Click "Execute" to make the request
- View the response, including status code and body

#### 3. Viewing Models
- Scroll down to the "Schemas" section at the bottom of the page
- Click on any model to see its structure and properties
- All fields are documented with descriptions, examples, and validation rules

## Development

### Prerequisites

- Java 17 or higher
- PostgreSQL database
- Maven

### Running the Application

1. Configure your database in `application.properties`
2. Run with Maven:
   ```
   mvn spring-boot:run
   ```

### Development Setup

#### Lombok Configuration
This project uses Lombok to reduce boilerplate code. If you're experiencing issues with Lombok:

1. Make sure Lombok is properly installed in your IDE
   - IntelliJ IDEA: Install the Lombok plugin and enable annotation processing
   - Eclipse: Run the Lombok installer (lombok.jar) and restart Eclipse

2. Check Maven build configuration:
   ```xml
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <optional>true</optional>
   </dependency>
   ```

3. Use the included Lombok helper script to troubleshoot issues:
   ```
   ./scripts/fix-lombok.sh
   ```
   
   This script provides several helpful options:
   - Install Lombok in your IDE
   - Verify Lombok is working correctly
   - Clean and rebuild the project
   - Update .gitignore for Lombok files

4. Common Lombok annotations used:
   - `@Data`: Generates getters, setters, equals, hashCode, and toString
   - `@Builder`: Implements the Builder pattern
   - `@Slf4j`: Creates a logger instance named `log`
   - `@NoArgsConstructor`, `@AllArgsConstructor`: Generates constructors

5. A `lombok.config` file is included in the project root with helpful settings for this project.

### Multi-Tenancy

This API uses schema-based multi-tenancy. Each tenant's data is isolated in a separate schema.
When making API calls, include a header `X-Tenant-ID` with the tenant ID.

## API Organization

The API is organized into the following sections:

1. **Authentication**: User registration and authentication
2. **Products**: Inventory management
3. **Sales**: Sales transactions and orders
4. **Reports**: Analytics and reporting

## Troubleshooting

### Swagger UI Issues

- **Cannot access Swagger UI**: Verify the application is running and the URL is correct
- **Authorization not working**: Ensure your token format is correct with the "Bearer " prefix
- **Missing endpoints**: Check that controllers have proper annotations (`@RestController`, `@RequestMapping`)

### Development Issues

- **Lombok-related errors**: If getters/setters/builders are not recognized, ensure Lombok is properly configured
- **Database connectivity**: Verify PostgreSQL connection details in application.properties
- **Multi-tenancy issues**: Ensure X-Tenant-ID header is included in requests 
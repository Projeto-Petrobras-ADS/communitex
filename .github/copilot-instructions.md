# Java (Spring Boot) & PostgreSQL Project Guidelines

You are a Senior Backend Engineer specializing in Java 21, Spring Boot 3+, and PostgreSQL.
Follow these architectural guidelines and coding standards strictly.

## 1. Tech Stack & Versions
- **Java:** 21 (LTS). Use modern features: Records, `var`, Text Blocks, Switch Expressions.
- **Framework:** Spring Boot 3+.
- **Database:** PostgreSQL.
- **Persistence:** Spring Data JPA (Hibernate).
- **Utils:** Lombok (for boilerplate), MapStruct (for mapping).
- **Validation:** Jakarta Validation (`jakarta.validation.*`).

## 2. Architectural Rules
- **Layered Architecture:** Controller -> Service -> Repository -> Database.
- **DTO Pattern:** NEVER return Entities directly from Controllers. Use Request/Response DTOs.
- **Dependency Injection:**
    - ALWAYS use **Constructor Injection**.
    - Use Lombok's `@RequiredArgsConstructor` on classes.
    - NEVER use `@Autowired` on fields.
- **Exceptions:** Use a Global Exception Handler (`@RestControllerAdvice`) and custom `RuntimeException` classes.

## 3. Coding Standards (The "Modern Way")
- **DTOs:** Must be implemented as Java **`record`**.
- **Entities:** Use Lombok `@Data` or `@Getter/@Setter`. Always use `@Entity` and `@Table`.
- **Null Safety:** Use `Optional<T>` for return types that might be empty. Avoid explicit null checks if `Optional` can be used.
- **Logging:** Use `@Slf4j`.
- **JSON:** Use `@JsonProperty` only if the field name differs significantly. Use camelCase for JSON fields.

## 4. Database & JPA
- **Imports:** Use `jakarta.persistence.*` (NOT `javax.persistence`).
- **Repositories:** Extend `JpaRepository`.
- **Optimization:** Use `@Query` for complex fetches to avoid N+1 problems. Use Interface-based Projections for read-only data.
- **Spatial Data:** If handling Latitude/Longitude, prefer using explicit `double` columns or PostGIS `Geometry` types if configured.

## 5. "Gold Standard" Code Example
When asked to create a feature (e.g., Create Issue), follow this structure:

### Controller
```java
@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Community issue management")
public class IssueController {

    private final IssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IssueResponseDTO create(@RequestBody @Valid IssueRequestDTO request) {
        return issueService.create(request);
    }
}
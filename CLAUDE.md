# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
```bash
# Run the application (PostgreSQL - default)
./gradlew bootRun

# Run with H2 test database
./gradlew bootRun --args='--spring.profiles.active=test'

# Run all tests
./gradlew test

# Run specific test categories
./gradlew testS3      # S3 infrastructure tests
./gradlew testLambda  # Lambda infrastructure tests

# Build the application
./gradlew build
```

### Database Access
```bash
# H2 Console (test profile only)
curl http://localhost:8080/h2-console
```

## Project Architecture

### Tech Stack
- **Framework**: Spring Boot 3.5.0 with Java 17
- **Database**: PostgreSQL (production), H2 (test)
- **Cloud**: AWS Lambda (OCR/Analysis), S3 (file storage)
- **Auth**: JWT with Apple OAuth2

### Domain-Driven Structure
```
src/main/java/com/sbpb/ddobak/server/
├── common/                 # Shared utilities and components
│   ├── exception/         # Global exception handling (GlobalExceptionHandler)
│   ├── response/          # Standardized API responses (ApiResponse<T>)
│   └── utils/             # AWS utilities (S3Util, LambdaUtil)
├── config/                # Configuration classes
│   ├── AwsConfig.java     # Centralized AWS client configuration
│   └── SecurityConfig.java
└── domain/                # Business domains
    ├── auth/              # Authentication & authorization
    ├── documentProcess/   # Contract processing (OCR, analysis)
    ├── user/              # User management
    └── externalContent/   # External content handling
```

### Key Services Architecture
- **Document Processing Flow**: Image upload → S3 storage → OCR Lambda (parallel) → Analysis Lambda (async) → WebView results
- **AWS Integration**: Centralized through AwsConfig and utility classes
- **Database**: Multi-profile support (PostgreSQL for production, H2 for testing)

## Configuration Management

### Environment Files
- `.env` - Environment variables for AWS/DB credentials
- `application.yml` - PostgreSQL configuration (default)
- `application-test.yml` - H2 in-memory database configuration

### AWS Configuration
- All AWS settings centralized in `AwsConfig.java`
- Uses `ddobak` AWS profile by default
- S3 buckets: `service-bucket` (prod), `test-bucket` (test)
- Lambda functions: `ocr-lambda`, `analysis-lambda`

## API Development Guidelines

### Response Format
All APIs must use `ApiResponse<T>` wrapper:
```java
// Success response
return ResponseEntity.ok(ApiResponse.success(data));

// With custom success code
return ResponseEntity.ok(ApiResponse.success(data, SuccessCode.USER_CREATED));

// Error handling - use specific exceptions
throw ResourceNotFoundException.user(userId);
throw DuplicateResourceException.email(email);
```

### Exception Handling
- Use `common.exception` package classes
- Global exception handling via `GlobalExceptionHandler`
- Error codes: 2xxx (success), 4xxx (client), 5xxx (server)

### Data Processing
- OCR requests process multiple images in parallel
- Analysis requests are asynchronous via Lambda
- Results stored in database with status tracking

## Database Schema

### Core Tables
- `contracts` - Main contract records with S3 image keys
- `ocr_contents` - OCR results with HTML content and page indexes
- `contract_analyses` - Analysis results with DDOBAK commentary
- `toxic_clauses` - Identified problematic clauses with risk levels

### Relationships
- Contract → OCR Contents (1:N)
- Contract → Analyses (1:N)  
- Analysis → Toxic Clauses (1:N)

## Development Workflow

### Branch Strategy
- `main` - Production deployments
- `feat/#issue-feature` - Feature development
- `fix/#issue-bug` - Bug fixes

### Issue-Based Development
- All work starts from GitHub Issues
- Branch names include issue numbers
- Commit messages reference issues: `feat: login feature #12`

## Testing Strategy

### Test Profiles
- Default: PostgreSQL with full integration
- Test: H2 in-memory for fast unit tests
- Custom test tasks for specific components (S3, Lambda)

### Test Organization
- Controller tests for API endpoints
- Service tests for business logic
- Infrastructure tests for AWS integration

## Key Integration Points

### AWS Lambda Integration
- `LambdaUtil` provides async/sync invocation methods
- OCR Lambda processes images to HTML
- Analysis Lambda identifies toxic clauses

### S3 File Management
- `S3Util` handles upload/download/delete operations
- Presigned URLs for secure file access
- Organized by contract ID directories

## Development Guidelines

### Code Standards
- Follow existing domain structure
- Use standardized exception classes
- Implement proper logging for Lambda/S3 operations
- All API responses must use `ApiResponse<T>` format

### Security Considerations
- JWT-based authentication with Apple OAuth
- AWS profile-based access control
- Environment variable management for sensitive data
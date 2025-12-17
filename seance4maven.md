# Découpage en modules Maven

Le projet peut être découpé en modules maven cohérents.

Livrables : 
- Description de l'architecture en modules maven ci-aprés

Voici nos choix de découpage maven : 

### Vision de synthèse

```sh

learning-platform/
├── pom.xml (POM parent)
├── core/
│   └── pom.xml
├── user-module/
│   └── pom.xml
├── learning-module/
│   └── pom.xml
├── ai-module/
│   └── pom.xml
├── communication-module/
│   └── pom.xml
├── billing-module/
│   └── pom.xml
├── reporting-module/
│   └── pom.xml
└── api-gateway/
    └── pom.xml

```

### Vision global des dépendances

Le domaine définit des interfaces (ports > sorties), l'infrastructure les implémente (adapters). Le domaine ne dépend jamais de l'infrastructure.

```sh

┌─────────────┐
         │    CORE     │
         │ (Interfaces)│
         └─────────────┘
                ↑
                │ implements
                │
    ┌───────────┴───────────┐
    │                       │
┌───────────┐       ┌───────────┐
│USER-MODULE│       │   AUTRES  │
│           │       │  MODULES  │
└───────────┘       └───────────┘
    
Domain ───► (ne dépend que de core)
    ↑
    │ utilise
    │
Application ───► (ports in/out définis ici)
    ↑
    │ implémente
    │
Infrastructure ───► (adapters implémentent ports)

```

### Module core

```sh

core/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/learningplatform/core/
    │           ├── domain/
    │           │   ├── exceptions/
    │           │   │   ├── DomainException.java
    │           │   │   ├── EntityNotFoundException.java
    │           │   │   ├── ValidationException.java
    │           │   │   ├── UnauthorizedException.java
    │           │   │   └── BusinessRuleException.java
    │           │   │
    │           │   ├── valueobjects/
    │           │   │   ├── EntityId.java
    │           │   │   ├── Email.java
    │           │   │   ├── TenantId.java
    │           │   │   └── Timestamp.java
    │           │   │
    │           │   └── events/
    │           │       ├── DomainEvent.java
    │           │       └── DomainEventPublisher.java
    │           │
    │           ├── application/
    │           │   ├── ports/
    │           │   │   ├── in/
    │           │   │   │   ├── UseCase.java (interface marker)
    │           │   │   │   ├── Command.java
    │           │   │   │   └── Query.java
    │           │   │   │
    │           │   │   └── out/
    │           │   │       ├── Repository.java
    │           │   │       ├── EventPublisher.java
    │           │   │       └── ExternalService.java
    │           │   │
    │           │   └── dto/
    │           │       ├── PageRequest.java
    │           │       ├── PageResponse.java
    │           │       └── Response.java
    │           │
    │           └── infrastructure/
    │               ├── persistence/
    │               │   ├── BaseEntity.java
    │               │   └── AuditableEntity.java
    │               │
    │               └── security/
    │                   ├── SecurityContext.java
    │                   └── TenantContext.java
    │
    └── test/
        └── java/
            └── com/learningplatform/core/
                ├── valueobjects/
                │   ├── EmailTest.java
                │   └── ...
                └── ...
```

## Module User 

```sh

user-module/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/learningplatform/user/
    │   │       │
    │   │       ├── domain/
    │   │       │   ├── model/
    │   │       │   │   ├── User.java (aggregate root)
    │   │       │   │   ├── Role.java (enum ou entity)
    │   │       │   │   ├── Profile.java (entity)
    │   │       │   │   ├── School.java (aggregate root)
    │   │       │   │   └── Permission.java
    │   │       │   │
    │   │       │   ├── valueobjects/
    │   │       │   │   ├── UserId.java
    │   │       │   │   ├── UserEmail.java
    │   │       │   │   ├── HashedPassword.java
    │   │       │   │   └── SchoolId.java
    │   │       │   │
    │   │       │   ├── events/
    │   │       │   │   ├── UserCreatedEvent.java
    │   │       │   │   ├── UserDeletedEvent.java
    │   │       │   │   └── SchoolRegisteredEvent.java
    │   │       │   │
    │   │       │   └── services/
    │   │       │       ├── PasswordEncryptionService.java
    │   │       │       └── UserValidationService.java
    │   │       │
    │   │       ├── application/
    │   │       │   │
    │   │       │   ├── ports/
    │   │       │   │   ├── in/
    │   │       │   │   │   ├── commands/
    │   │       │   │   │   │   ├── CreateUserCommand.java
    │   │       │   │   │   │   ├── UpdateUserCommand.java
    │   │       │   │   │   │   ├── DeleteUserCommand.java
    │   │       │   │   │   │   ├── RegisterSchoolCommand.java
    │   │       │   │   │   │   └── AssignRoleCommand.java
    │   │       │   │   │   │
    │   │       │   │   │   ├── queries/
    │   │       │   │   │   │   ├── GetUserByIdQuery.java
    │   │       │   │   │   │   ├── GetUserByEmailQuery.java
    │   │       │   │   │   │   ├── ListUsersBySchoolQuery.java
    │   │       │   │   │   │   └── GetUserProfileQuery.java
    │   │       │   │   │   │
    │   │       │   │   │   └── usecases/
    │   │       │   │   │       ├── CreateUserUseCase.java (interface)
    │   │       │   │   │       ├── AuthenticateUserUseCase.java
    │   │       │   │   │       ├── GetUserUseCase.java
    │   │       │   │   │       └── ...
    │   │       │   │   │
    │   │       │   │   └── out/
    │   │       │   │       ├── UserRepository.java (port interface)
    │   │       │   │       ├── SchoolRepository.java
    │   │       │   │       ├── LoadUserPort.java
    │   │       │   │       ├── SaveUserPort.java
    │   │       │   │       └── NotificationPort.java
    │   │       │   │
    │   │       │   ├── services/
    │   │       │   │   ├── CreateUserService.java (implémente CreateUserUseCase)
    │   │       │   │   ├── AuthenticateUserService.java
    │   │       │   │   ├── GetUserService.java
    │   │       │   │   ├── DeleteUserService.java
    │   │       │   │   └── ...
    │   │       │   │
    │   │       │   └── dto/
    │   │       │       ├── UserDTO.java
    │   │       │       ├── CreateUserRequest.java
    │   │       │       ├── UserResponse.java
    │   │       │       ├── SchoolDTO.java
    │   │       │       └── ...
    │   │       │
    │   │       └── infrastructure/
    │   │           │
    │   │           ├── adapters/
    │   │           │   │
    │   │           │   ├── in/
    │   │           │   │   ├── rest/
    │   │           │   │   │   ├── UserController.java
    │   │           │   │   │   ├── SchoolController.java
    │   │           │   │   │   ├── AuthController.java
    │   │           │   │   │   └── mappers/
    │   │           │   │   │       ├── UserMapper.java
    │   │           │   │   │       └── SchoolMapper.java
    │   │           │   │   │
    │   │           │   │   └── events/
    │   │           │   │       └── UserEventListener.java
    │   │           │   │
    │   │           │   └── out/
    │   │           │       ├── persistence/
    │   │           │       │   ├── UserRepositoryAdapter.java
    │   │           │       │   ├── SchoolRepositoryAdapter.java
    │   │           │       │   ├── entities/
    │   │           │       │   │   ├── UserJpaEntity.java
    │   │           │       │   │   ├── SchoolJpaEntity.java
    │   │           │       │   │   ├── RoleJpaEntity.java
    │   │           │       │   │   └── ProfileJpaEntity.java
    │   │           │       │   │
    │   │           │       │   ├── repositories/
    │   │           │       │   │   ├── UserJpaRepository.java (Spring Data)
    │   │           │       │   │   └── SchoolJpaRepository.java
    │   │           │       │   │
    │   │           │       │   └── mappers/
    │   │           │       │       ├── UserEntityMapper.java
    │   │           │       │       └── SchoolEntityMapper.java
    │   │           │       │
    │   │           │       └── external/
    │   │           │           └── NotificationAdapter.java
    │   │           │
    │   │           └── config/
    │   │               ├── UserModuleConfiguration.java
    │   │               ├── SecurityConfiguration.java
    │   │               └── JpaConfiguration.java
    │   │
    │   └── resources/
    │       ├── application-user.yml
    │       └── db/
    │           └── migration/
    │               ├── V1__create_users_table.sql
    │               ├── V2__create_schools_table.sql
    │               └── ...
    │
    └── test/
        └── java/
            └── com/learningplatform/user/
                ├── domain/
                │   ├── UserTest.java
                │   ├── SchoolTest.java
                │   └── ...
                │
                ├── application/
                │   ├── CreateUserServiceTest.java
                │   ├── AuthenticateUserServiceTest.java
                │   └── ...
                │
                └── infrastructure/
                    ├── UserRepositoryAdapterTest.java
                    ├── UserControllerTest.java
                    └── ...
```

### Learning module

```sh

learning-module/
├── pom.xml
└── src/main/java/com/learningplatform/learning/
    ├── domain/
    │   ├── model/
    │   │   ├── LearningPath.java
    │   │   ├── PathStep.java
    │   │   ├── MemoryTool.java
    │   │   └── ProgressTracking.java
    │   ├── valueobjects/...
    │   ├── events/...
    │   └── services/...
    │
    ├── application/
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── commands/
    │   │   │   │   ├── CreatePathCommand.java
    │   │   │   │   └── ...
    │   │   │   ├── queries/
    │   │   │   │   ├── GetPathByIdQuery.java
    │   │   │   │   └── ...
    │   │   │   └── usecases/...
    │   │   └── out/
    │   │       ├── LearningPathRepository.java
    │   │       └── ...
    │   ├── services/...
    │   └── dto/...
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/rest/...
        │   └── out/persistence/...
        └── config/...

```

### AI Module

```sh

ai-module/
├── pom.xml
└── src/main/java/com/learningplatform/ai/
    ├── domain/
    │   ├── model/
    │   │   ├── Agent.java
    │   │   ├── DocumentSource.java
    │   │   ├── Prompt.java
    │   │   └── Conversation.java
    │   └── ...
    │
    ├── application/
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── commands/
    │   │   │   │   ├── ConfigureAgentCommand.java
    │   │   │   │   ├── AskQuestionCommand.java
    │   │   │   │   └── ...
    │   │   │   └── usecases/...
    │   │   └── out/
    │   │       ├── AgentRepository.java
    │   │       ├── LLMPort.java (appel modèles IA)
    │   │       └── RAGPort.java
    │   └── services/...
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/rest/...
        │   └── out/
        │       ├── persistence/...
        │       └── llm/
        │           ├── OpenAIAdapter.java
        │           ├── OllamaAdapter.java
        │           └── LangchainAdapter.java
        └── config/...

```

### Communication module

```sh

communication-module/
├── pom.xml
└── src/main/java/com/learningplatform/communication/
    ├── domain/
    │   ├── model/
    │   │   ├── Message.java
    │   │   ├── Conversation.java
    │   │   ├── VideoSession.java
    │   │   └── Notification.java
    │   └── ...
    │
    ├── application/
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── commands/
    │   │   │   │   ├── SendMessageCommand.java
    │   │   │   │   ├── StartVideoCommand.java
    │   │   │   │   └── ...
    │   │   │   └── usecases/...
    │   │   └── out/
    │   │       ├── MessageRepository.java
    │   │       ├── WebSocketPort.java
    │   │       └── EmailPort.java
    │   └── services/...
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   ├── websocket/
        │   │   │   ├── ChatWebSocketHandler.java
        │   │   │   └── VideoWebSocketHandler.java
        │   │   └── rest/...
        │   └── out/...
        └── config/...
```

### Billing module 

```sh

billing-module/
├── pom.xml
└── src/main/java/com/learningplatform/billing/
    ├── domain/
    │   ├── model/
    │   │   ├── Subscription.java
    │   │   ├── Invoice.java
    │   │   ├── Payment.java
    │   │   └── Contract.java
    │   └── ...
    │
    ├── application/
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── commands/
    │   │   │   │   ├── CreateSubscriptionCommand.java
    │   │   │   │   ├── GenerateInvoiceCommand.java
    │   │   │   │   └── ...
    │   │   │   └── usecases/...
    │   │   └── out/
    │   │       ├── SubscriptionRepository.java
    │   │       ├── PaymentGatewayPort.java
    │   │       └── DocumentStoragePort.java
    │   └── services/...
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/rest/...
        │   └── out/
        │       ├── persistence/...
        │       └── external/
        │           ├── StripeAdapter.java
        │           └── S3StorageAdapter.java
        └── config/...

```

### Reporting module

```sh

reporting-module/
├── pom.xml
└── src/main/java/com/learningplatform/reporting/
    ├── domain/
    │   ├── model/
    │   │   ├── Dashboard.java
    │   │   ├── Report.java
    │   │   └── Metric.java
    │   └── ...
    │
    ├── application/
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── queries/
    │   │   │   │   ├── GetDashboardQuery.java
    │   │   │   │   ├── GenerateReportQuery.java
    │   │   │   │   └── ...
    │   │   │   └── usecases/...
    │   │   └── out/
    │   │       ├── DataAggregationPort.java
    │   │       └── ExportPort.java
    │   └── services/...
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/rest/...
        │   └── out/
        │       ├── aggregation/
        │       │   ├── UserDataAdapter.java
        │       │   ├── LearningDataAdapter.java
        │       │   └── ...
        │       └── export/
        │           ├── PDFExportAdapter.java
        │           └── CSVExportAdapter.java
        └── config/...
```

### API Gateway

```sh

api-gateway/
├── pom.xml
└── src/main/java/com/learningplatform/gateway/
    ├── ApiGatewayApplication.java (main)
    │
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── CorsConfig.java
    │   └── ModulesConfig.java
    │
    ├── controllers/
    │   └── HealthController.java
    │
    ├── security/
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtTokenProvider.java
    │   └── SecurityContext.java
    │
    └── middleware/
        ├── RateLimitingFilter.java
        ├── TenantFilter.java
        └── LoggingFilter.java
```




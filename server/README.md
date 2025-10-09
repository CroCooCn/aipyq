# Friend App Server (Spring Boot)

Dev prerequisites
- Java 17+
- Maven 3.9+

Run
```bash
mvn spring-boot:run -f server/pom.xml
```

API base path: `/api/v1`

Endpoints (stubs)
- POST `/auth/anonymous` → create anonymous session
- POST `/auth/login` → phone+code login (stub)
- GET/PUT `/profile` → persona/audience
- POST `/generate/caption` → mock image insights
- POST `/generate/copy` → mock copy candidates
- POST `/generate/rewrite` → echo rewrite
- POST `/image/render` → mock render result
- GET `/history` → mock list
- GET `/hot-topics` → mock hot topics
- POST `/billing/orders`, GET `/billing/orders/{id}`, POST `/billing/callback/{channel}` → mock billing

Note: This is a scaffold aligned with `docs/api/openapi.yaml`.

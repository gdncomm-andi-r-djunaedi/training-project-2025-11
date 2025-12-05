This folder contains the API Gateway Maven project.

## Rate limiting
- Enabled by default with `rate-limiter.enabled=true`
- Requests are tracked per user (or IP fallback) via Redis counters with a 1-minute window
- Default quota is `rate-limiter.requests-per-minute=120`
- Paths listed under `rate-limiter.ignored-paths` bypass the limiter
- Update `src/main/resources/application.properties` (or env specific files) to change the quota without code changes

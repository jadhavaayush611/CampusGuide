# CampusGuide Backend Observability Review

This document reviews the production monitoring and observability audit and implementation details for the CampusGuide backend.

---

## 1. Spring Boot Actuator Audit
* **Configured Endpoints**: health, info, metrics.
* **Security & Details**:
  * Health (`/actuator/health`, `/actuator/health/**`) and Info (`/actuator/info`) are publicly accessible to allow orchestrator liveness/readiness polling without credentials.
  * Health details exposure is configured to `when-authorized` to prevent unauthorized callers from viewing system internal details (like disk space, DB connections, or AI sub-component status).
  * Metrics (`/actuator/metrics`) is protected under Spring Security and requires full authentication (`ROLE_SUPER_ADMIN`).

---

## 2. Health Probes Audit
* **Liveness Probe**: Monitored via `/actuator/health/liveness`. Indicates JVM process longevity.
* **Readiness Probe**: Monitored via `/actuator/health/readiness`. Indicates Spring context readiness to route HTTP traffic.
* **MongoDB Connectivity**: Integrates `MongoHealthIndicator` checking actual DB operations.
* **Disk Space Probe**: Checks storage boundaries using `DiskSpaceHealthIndicator`.
* **Application Startup**: Measured through application initialization events and reported via the Startup Diagnostics logging pipeline.

---

## 3. Metrics Audit
* **JVM, Memory & GC**: Micrometer bindings are configured to track memory allocations (heap/non-heap), garbage collection pauses, and class loading.
* **Thread Pools**: Metrics monitor execution threads, active counts, and thread pool exhaustion warnings.
* **HTTP Requests**: Tracks status code distributions, latency percentiles, and request counts per route.
* **MongoDB Commands**: Configured programmatically by registering a `MongoMetricsCommandListener` bean in `MongoConfig.java` to collect query count, latency, and connection pool metrics.

---

## 4. Logging Audit
* **Unified logback-spring.xml**: Implemented console logging with standardized ISO-8601 formatting, color-coded level tags, and thread names.
* **Correlation Metadata**: Every log message contains MDC context placeholders: `[correlationId=... requestPath=... httpMethod=...]`.
* **Duplicate Prevention**: Configured `additivity="false"` on the primary `com.campusguide` logger to prevent duplicate logs propagating to the root handler.
* **Log Privacy**: Audited security filters and auth controllers to ensure that no tokens, passwords, or keys are exposed to standard outputs.

---

## 5. Request Correlation & MDC Audit
* **Filter Implementation**: Added `CorrelationMdcFilter` with priority `Ordered.HIGHEST_PRECEDENCE`.
* **Correlation Flow**:
  * Extracts incoming `X-Correlation-ID` or `X-Request-ID` headers if present, otherwise generates a fresh UUID.
  * Assigns the ID to the outgoing `X-Correlation-ID` header.
  * Populates thread MDC with `correlationId`, `requestPath`, and `httpMethod`.
  * Guarantees context cleanup via a `finally { MDC.clear(); }` block to avoid thread-local leaking.
* **Request Latency Check**: Measures processing duration and prints a `WARN` log if a request exceeds `monitoring.slow-request-threshold-ms` (default: 1000ms).

---

## 6. Startup Diagnostics Audit
* **Diagnostics Runner**: Implemented `StartupDiagnostics` running post-initialization.
* **Safe Logged Info**:
  * Active profiles
  * JVM Java version
  * Spring Boot Framework version
  * Project version (dynamic lookup, defaulting to `1.0.0-MVP`)
  * Database connection ping result `{ping: 1}` to confirm database readiness.
* **Secret Safety**: No variables related to database credentials, mail hosts, or auth secrets are exposed.

---

## 7. Operational Readiness Audit
* **Graceful Shutdown**: Enabled standard `graceful` shutdown setting with a `30s` timeout-per-shutdown-phase.
* **Thread Executors**:
  * Configured async task executor `taskExecutor` in `AsyncConfig` to wait for active tasks on context destruction up to 30 seconds.
  * Configured `@PreDestroy` callbacks in `ExecutionDispatcher` and `AtlasStreamingServiceImpl` to gracefully terminate their respective internal thread executors.

---

## 8. Verification Results
* Execution of `mvn clean verify` completed successfully.
* All 700+ unit and integration tests passed, verifying that logging, metrics registration, and filter logic did not break any existing platform features or contract expectations.

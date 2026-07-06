# feature/my-solution — Change Log

This document summarises the two commits added on top of `prod` in the `feature/my-solution` branch.

---

## 1. Implement the liveness health check (`SystemLivenessCheck.java`)

**Commit:** `feat: implement the liveness health check`

### What changed

`SystemLivenessCheck.java` was created as a CDI `@ApplicationScoped` bean annotated with `@Liveness` that implements the MicroProfile `HealthCheck` interface:

```java
@Liveness
@ApplicationScoped
public class SystemLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memBean.getHeapMemoryUsage().getUsed();
        long memMax = memBean.getHeapMemoryUsage().getMax();

        return HealthCheckResponse.named(
            SystemResource.class.getSimpleName() + " Liveness Check")
                                  .status(memUsed < memMax * 0.9).build();
    }
}
```

### Why

MicroProfile Health requires a CDI bean that implements `HealthCheck` to contribute to the `/health/live` endpoint. The `@Liveness` annotation tells the Liberty runtime to include this check in liveness probes. The check reads heap memory usage from `MemoryMXBean` and reports DOWN when usage reaches or exceeds 90% of the JVM maximum, signalling the container orchestrator to restart the process before an `OutOfMemoryError` can occur.

---

## 2. Implement the readiness health check (`SystemReadinessCheck.java`)

**Commit:** `feat: implement the readiness health check`

### What changed

`SystemReadinessCheck.java` was created as a CDI `@ApplicationScoped` bean annotated with `@Readiness` that implements `HealthCheck`. It injects the `io_openliberty_guides_system_inMaintenance` MicroProfile Config property through a `Provider<String>`:

```java
@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "io_openliberty_guides_system_inMaintenance")
    Provider<String> inMaintenance;

    @Override
    public HealthCheckResponse call() {
        if (inMaintenance != null && inMaintenance.get().equalsIgnoreCase("true")) {
            return HealthCheckResponse.down(READINESS_CHECK);
        }
        return HealthCheckResponse.up(READINESS_CHECK);
    }
}
```

### Why

A readiness check controls whether the service receives traffic from the load balancer. When an operator sets `io_openliberty_guides_system_inMaintenance=true` in `server.xml`, Liberty propagates the value through MicroProfile Config and the check returns DOWN at `/health/ready`, causing the load balancer to stop routing requests during planned maintenance windows. The property is injected via a `Provider<String>` rather than a plain `String` so that runtime config changes are picked up immediately without restarting the server.

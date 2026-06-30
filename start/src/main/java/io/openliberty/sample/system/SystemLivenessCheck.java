package io.openliberty.sample.system;

import jakarta.enterprise.context.ApplicationScoped;

import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * MicroProfile Health liveness check that reports whether the JVM is healthy enough to continue
 * running. It reads current and maximum heap memory from {@link MemoryMXBean} and returns DOWN
 * when heap usage reaches or exceeds 90% of the JVM maximum, signalling that the process is under
 * severe memory pressure and should be restarted by the container orchestrator.
 *
 * <p>Exposed at {@code GET /health/live} by the {@code mpHealth} feature declared in
 * {@code server.xml}. No JAX-RS {@code @Path} is needed — Liberty scans for CDI beans annotated
 * with {@code @Liveness} that implement {@link HealthCheck} and registers them automatically.
 * The aggregate {@code GET /health} endpoint also includes this check alongside all others.
 */
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
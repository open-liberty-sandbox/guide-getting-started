package io.openliberty.sample.system;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

/**
 * MicroProfile Health readiness check that reports whether the service is ready to accept traffic.
 * It reads the {@code io_openliberty_guides_system_inMaintenance} MicroProfile Config property at
 * runtime (via a {@link Provider} so changes take effect without restarting the server) and returns
 * DOWN when the value is {@code "true"}, telling the load balancer to stop routing requests to this
 * instance while maintenance is in progress.
 *
 * <p>Exposed at {@code GET /health/ready} by the {@code mpHealth} feature declared in
 * {@code server.xml}. No JAX-RS {@code @Path} is needed — Liberty scans for CDI beans annotated
 * with {@code @Readiness} that implement {@link HealthCheck} and registers them automatically.
 * The aggregate {@code GET /health} endpoint also includes this check alongside all others.
 */
@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    private static final String READINESS_CHECK = SystemResource.class.getSimpleName()
                                                 + " Readiness Check";

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
package com.ciam.auth;

import java.net.SocketTimeoutException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Converts BFF proxy failures into a logged, structured JSON error instead of a bare 5xx.
 *
 * <p>Without this, a slow/unreachable resource server only shows up as Render's edge 502 with no
 * application log line, which made the proxy impossible to diagnose remotely. This handler logs the
 * full cause and returns an explicit status:
 * <ul>
 *   <li>504 — the upstream didn't answer in time ({@link ResourceAccessException} wrapping a
 *       {@link SocketTimeoutException}).</li>
 *   <li>502 — the upstream was unreachable, or answered with an error status
 *       ({@link RestClientResponseException}).</li>
 * </ul>
 */
@RestControllerAdvice
public class ProxyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ProxyExceptionHandler.class);

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> onResourceAccess(ResourceAccessException ex) {
        boolean timedOut = ex.getCause() instanceof SocketTimeoutException;
        log.error("BFF proxy: resource server {}", timedOut ? "timed out" : "unreachable", ex);
        HttpStatus status = timedOut ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(Map.of(
                "error", timedOut ? "upstream_timeout" : "upstream_unreachable",
                "message", timedOut
                        ? "The resource server did not respond in time (cold start?)."
                        : "The resource server could not be reached."));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> onUpstreamError(RestClientResponseException ex) {
        log.error("BFF proxy: resource server returned HTTP {}", ex.getStatusCode().value(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "error", "upstream_error",
                "upstream_status", ex.getStatusCode().value(),
                "message", "The resource server returned an error."));
    }
}

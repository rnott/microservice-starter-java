package org.rnott.example.feature;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.rnott.example.api.ServiceEntity;

/**
 * Feature providing automated support to HTTP caching.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching">HTTP Caching</a>
 */
@Provider
public class HttpCacheFeature implements DynamicFeature {

    static final DateTimeFormatter HTTP_TIME = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    static String toHttp(OffsetDateTime dt) {
        return dt.atZoneSameInstant(ZoneId.of("GMT"))
                .format(HTTP_TIME);
    }

    static class GeneralFilter implements ContainerResponseFilter {

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            EntityTag tag = null;
            if (responseContext.getStatus() >= 300) {
                // no change on error
                return;
            }

            if ("DELETE".equals((requestContext.getMethod()))) {
                // invalidate the cache
                jakarta.ws.rs.core.CacheControl cc = new jakarta.ws.rs.core.CacheControl();
                cc.setNoCache(true);
                cc.setNoStore(true);
                tag = new EntityTag("0");
                responseContext.getHeaders()
                        .add("Cache-Control", cc);
                responseContext.getHeaders()
                        .add("Last-Modified", toHttp(OffsetDateTime.now()));
            }

            if (responseContext.hasEntity()) {
                if (responseContext.getEntity() instanceof ServiceEntity) {
                    // get modified timestamp from metadata
                    OffsetDateTime dt = ((ServiceEntity) responseContext.getEntity())
                            .getMetadata().getModified();
                    responseContext.getHeaders()
                            .add("Last-Modified", toHttp(dt));
                } else {
                    // use current timestamp
                    responseContext.getHeaders()
                            .add("Last-Modified",
                                    toHttp(OffsetDateTime.now())
                            );
                }

                if (tag == null) {
                    tag = new EntityTag(String.valueOf(responseContext.getEntity().hashCode()));
                }
            }

            if (tag != null) {
                responseContext.getHeaders().add("Etag", tag);
            }
         }
    }

    /**
     * Resource filter to apply cache control headers to a response.
     *
     * NOTE: this class in not annotated with <code>@Provider</code> as it will be enabled on
     * a method by method basis by the corresponding feature type.
     *
     * @see CacheControl
     * @see Expires
     */
    static class DynamicFilter implements ContainerResponseFilter {

        private final Map<String, Object> headers;

        DynamicFilter(Map<String, Object> header) {
            this.headers = header;
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
                throws IOException {
            this.headers.forEach(((k, v) -> responseContext.getHeaders().add(k, v)));
        }
    }

    /**
     * Dynamically enables and configures a resource filter on a method by method basis. This method
     * is called once for each resource method. Enablement is determined using a combination of
     * application configuration and resource method annotations.
     *
     * @param resourceInfo resource metadata
     * @param featureContext feature context used for registration
     */
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        // general caching header support for all resource methods
        featureContext.register(new GeneralFilter());

        /*
         check for resource method annotations
         Order:
            Surrogate-Control
            Cache-Control
            Expires
         */
        Map<String, Object> headers = new LinkedHashMap<>();
        CacheControl cr = resourceInfo.getResourceMethod().getAnnotation(CacheControl.class);
        if (cr != null) {
            jakarta.ws.rs.core.CacheControl cc = new jakarta.ws.rs.core.CacheControl();
            cc.setNoCache(cr.isNoCache());
            cc.setMaxAge(cr.maxAge());
            cc.setSMaxAge(cr.sMaxAge());
            cc.setPrivate(cr.isPrivate());
            cc.setMustRevalidate(cr.isMustRevalidate());
            cc.setProxyRevalidate(cr.isProxyRevalidate());
            cc.setNoStore(cr.isNoStore());
            cc.setNoTransform(cr.isNoTransform());
            headers.put("Cache-Control", cc);
        }
        Expires expires = resourceInfo.getResourceMethod().getAnnotation(Expires.class);
        if (expires != null) {
            Duration d = Duration.parse(expires.value());
            ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("GMT")).plus(d);
            headers.put("expires", zdt.format(HTTP_TIME));
        }
        if (! headers.isEmpty()) {
            DynamicFilter filter = new DynamicFilter(headers);
            featureContext.register(filter);
        }
    }
}

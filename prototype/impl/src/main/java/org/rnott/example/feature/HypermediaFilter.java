package org.rnott.example.feature;

import io.undertow.util.Headers;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * RFC-5988 styled Web Links for HATEOAS support. This supports
 * two different approaches, which can be used individually or together:
 * <ol>
 * <li>Registered as a JAX-RS processing filter will supply default behavior</li>
 * <li>Injected into the REST controller for custom, programmatic control</li>
 * </ol>
 */
@Provider
@Component
public class HypermediaFilter implements ContainerResponseFilter {

    @Context
    private UriInfo uriInfo;

    // common relation names
    static final String RELATION_SELF = "self";
    static final String RELATION_MEMBER = "item";
    static final String RELATION_COLLECTION = "collection";
    static final String RELATION_FIRST = "first";
    static final String RELATION_LAST = "last";
    static final String RELATION_PREVIOUS = "prev";
    static final String RELATION_NEXT = "next";
    static final String PARAM_PAGE = "page";
    static final String PARAM_LIMIT = "limit";
    static final String HEADER_LINKS = "links";
    static final String HEADER_TOTAL_COUNT = "x-total-count";

    private Link relation(Class<?> resource, String method, String relation, String mediaType, Object ... values) {
        return Link.fromMethod(resource, method)
                .rel(relation)
                .type(mediaType)
                .build(values);
    }

    private Link relation(Link.Builder builder, String relation, String mediaType) {
        return builder
                .rel(relation)
                .type(mediaType == null ? MediaType.WILDCARD : mediaType)
                .build();
    }

    public Link self(String type) {
        return Link.fromUriBuilder(uriInfo.getRequestUriBuilder())
                .rel(RELATION_SELF)
                .type(type == null ? MediaType.WILDCARD : type)
                .build();
    }

    public Link identity(Class<?> resource, String mediaType, Function<Void, String> identifier) {
        return relation(resource, "GET", "TODO", mediaType, identifier.apply(null));
    }

    public List<Link> pagination(String type, int totalCount) {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        int page = params.containsKey(PARAM_PAGE) ?
                Integer.parseInt(params.getFirst(PARAM_PAGE)) : 1;
        int limit = params.containsKey(PARAM_LIMIT) ?
                Integer.parseInt(params.getFirst(PARAM_LIMIT)) : 1000;
        int pages = totalCount / limit + 1;

        List<Link> links = new LinkedList<>();
        links.add(
                self(type)
        );
        links.add(
                Link.fromUriBuilder(
                        uriInfo.getRequestUriBuilder()
                                .replaceQueryParam(PARAM_PAGE, 1)
                        )
                        .rel(RELATION_FIRST)
                        .type(type)
                        .build()
        );
        links.add(
                Link.fromUriBuilder(
                                uriInfo.getRequestUriBuilder()
                                        .replaceQueryParam(PARAM_PAGE, pages)
                        )
                        .rel(RELATION_LAST)
                        .type(type)
                        .build()
        );
        if (page > 1) {
            links.add(
                    relation(
                    Link.fromUriBuilder(uriInfo
                                    .getRequestUriBuilder()
                                    .replaceQueryParam(PARAM_PAGE, page - 1)
                    ), RELATION_PREVIOUS,
                    type)
            );
        }
        if (page < pages) {
            links.add(
                    relation(Link.fromUriBuilder(uriInfo
                                    .getRequestUriBuilder()
                                    .replaceQueryParam(PARAM_PAGE, page + 1)
                            ),
                            RELATION_NEXT,
                            type
                    )
            );
        }
        return links;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String type = responseContext.getHeaderString(Headers.CONTENT_TYPE_STRING);
        if (type != null && type.endsWith("+problem-details")) {
            type = requestContext.getMediaType().toString();
        }
        if (responseContext.getHeaders().containsKey(HEADER_TOTAL_COUNT)) {
            int totalCount = Integer.parseInt(
                    responseContext.getHeaderString(HEADER_TOTAL_COUNT)
            );
            pagination(type, totalCount).forEach(l -> responseContext.getHeaders().add(HEADER_LINKS, l));
        } else {
            responseContext.getHeaders().add(HEADER_LINKS, self(type));
        }
    }
}

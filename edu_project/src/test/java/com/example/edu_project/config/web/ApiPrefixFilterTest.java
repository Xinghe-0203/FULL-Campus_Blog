package com.example.edu_project.config.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiPrefixFilterTest {

    private final ApiPrefixFilter filter = new ApiPrefixFilter();

    @Test
    void apiPrefixIsRemovedOnlyForInternalDispatch() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/post/42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<HttpServletRequest> dispatchedRequest = new AtomicReference<>();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                dispatchedRequest.set((HttpServletRequest) servletRequest));

        assertNotNull(dispatchedRequest.get());
        assertEquals("/post/42", dispatchedRequest.get().getRequestURI());
        assertTrue(ApiPrefixFilter.isApiRequest(dispatchedRequest.get()));
        assertEquals("/api/post/42", dispatchedRequest.get().getAttribute(ApiPrefixFilter.ORIGINAL_URI_ATTRIBUTE));
    }

    @Test
    void apiRootRedirectsToTheSpaRoot() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new AssertionError("API root should redirect before controller dispatch");
        });

        assertEquals("/", response.getRedirectedUrl());
    }

    @Test
    void browserRouteDoesNotReachBackendControllerChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/post/42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Boolean> chainInvoked = new AtomicReference<>(false);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertFalse(chainInvoked.get());
        assertEquals(200, response.getStatus());
    }

    @Test
    void nonApiWritesCannotReachBackendControllerChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/message");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Boolean> chainInvoked = new AtomicReference<>(false);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertFalse(chainInvoked.get());
        assertEquals(404, response.getStatus());
    }
}

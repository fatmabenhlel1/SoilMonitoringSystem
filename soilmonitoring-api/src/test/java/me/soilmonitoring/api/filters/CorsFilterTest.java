package me.soilmonitoring.api.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorsFilter Tests")
class CorsFilterTest {

    private CorsFilter corsFilter;

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    private MultivaluedMap<String, Object> responseHeaders;

    @BeforeEach
    void setUp() {
        corsFilter = new CorsFilter();
        responseHeaders = new MultivaluedHashMap<>();
    }

    // ===== Tests pour filter(ContainerRequestContext) - Gestion OPTIONS =====

    @Test
    @DisplayName("Should abort with OK response for OPTIONS request")
    void testFilterOptionsRequest() {
        // Given
        when(requestContext.getMethod()).thenReturn("OPTIONS");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, times(1)).getMethod();
        verify(requestContext, times(1)).abortWith(any(Response.class));
    }

    @Test
    @DisplayName("Should handle OPTIONS request case-insensitively (lowercase)")
    void testFilterOptionsRequestLowercase() {
        // Given
        when(requestContext.getMethod()).thenReturn("options");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, times(1)).abortWith(any(Response.class));
    }

    @Test
    @DisplayName("Should handle OPTIONS request case-insensitively (mixed case)")
    void testFilterOptionsRequestMixedCase() {
        // Given
        when(requestContext.getMethod()).thenReturn("OpTiOnS");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, times(1)).abortWith(any(Response.class));
    }

    @Test
    @DisplayName("Should not abort for GET request")
    void testFilterGetRequest() {
        // Given
        when(requestContext.getMethod()).thenReturn("GET");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, times(1)).getMethod();
        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    @DisplayName("Should not abort for POST request")
    void testFilterPostRequest() {
        // Given
        when(requestContext.getMethod()).thenReturn("POST");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, times(1)).getMethod();
        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    @DisplayName("Should not abort for PUT request")
    void testFilterPutRequest() {
        // Given
        when(requestContext.getMethod()).thenReturn("PUT");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    @DisplayName("Should not abort for DELETE request")
    void testFilterDeleteRequest() {
        // Given
        when(requestContext.getMethod()).thenReturn("DELETE");

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, never()).abortWith(any(Response.class));
    }

    // ===== Tests pour filter(ContainerRequestContext, ContainerResponseContext) - Headers CORS =====

    @Test
    @DisplayName("Should add Access-Control-Allow-Origin header")
    void testAddAllowOriginHeader() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Origin"));
        assertEquals("*", responseHeaders.getFirst("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should add Access-Control-Allow-Methods header")
    void testAddAllowMethodsHeader() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Methods"));
        String methods = (String) responseHeaders.getFirst("Access-Control-Allow-Methods");
        assertNotNull(methods);
        assertTrue(methods.contains("GET"));
        assertTrue(methods.contains("POST"));
        assertTrue(methods.contains("PUT"));
        assertTrue(methods.contains("DELETE"));
        assertTrue(methods.contains("OPTIONS"));
        assertTrue(methods.contains("HEAD"));
    }

    @Test
    @DisplayName("Should add Access-Control-Allow-Headers header")
    void testAddAllowHeadersHeader() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Headers"));
        String headers = (String) responseHeaders.getFirst("Access-Control-Allow-Headers");
        assertNotNull(headers);
        assertTrue(headers.contains("Content-Type"));
        assertTrue(headers.contains("Authorization"));
        assertTrue(headers.contains("X-Requested-With"));
    }

    @Test
    @DisplayName("Should add Access-Control-Max-Age header")
    void testAddMaxAgeHeader() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertTrue(responseHeaders.containsKey("Access-Control-Max-Age"));
        assertEquals("3600", responseHeaders.getFirst("Access-Control-Max-Age"));
    }

    @Test
    @DisplayName("Should add Access-Control-Allow-Credentials header")
    void testAddAllowCredentialsHeader() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Credentials"));
        assertEquals("true", responseHeaders.getFirst("Access-Control-Allow-Credentials"));
    }

    @Test
    @DisplayName("Should add all CORS headers")
    void testAddAllCorsHeaders() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertEquals(5, responseHeaders.size(), "Should add exactly 5 CORS headers");

        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Origin"));
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Methods"));
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Headers"));
        assertTrue(responseHeaders.containsKey("Access-Control-Max-Age"));
        assertTrue(responseHeaders.containsKey("Access-Control-Allow-Credentials"));
    }

    @Test
    @DisplayName("Should verify exact values of all CORS headers")
    void testVerifyAllCorsHeaderValues() {
        // Given
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertEquals("*", responseHeaders.getFirst("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD",
                responseHeaders.getFirst("Access-Control-Allow-Methods"));
        assertEquals("Content-Type, Authorization, X-Requested-With",
                responseHeaders.getFirst("Access-Control-Allow-Headers"));
        assertEquals("3600", responseHeaders.getFirst("Access-Control-Max-Age"));
        assertEquals("true", responseHeaders.getFirst("Access-Control-Allow-Credentials"));
    }

    // ===== Tests d'intégration =====

    @Test
    @DisplayName("Should handle OPTIONS request and add CORS headers")
    void testOptionsRequestWithCorsHeaders() {
        // Given
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When - First filter (request)
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, times(1)).abortWith(any(Response.class));

        // When - Second filter (response)
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertEquals(5, responseHeaders.size());
    }

    @Test
    @DisplayName("Should not interfere with existing headers")
    void testDoesNotRemoveExistingHeaders() {
        // Given
        responseHeaders.add("Custom-Header", "CustomValue");
        responseHeaders.add("Another-Header", "AnotherValue");
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        // When
        corsFilter.filter(requestContext, responseContext);

        // Then
        assertTrue(responseHeaders.containsKey("Custom-Header"));
        assertTrue(responseHeaders.containsKey("Another-Header"));
        assertEquals(7, responseHeaders.size()); // 2 existing + 5 CORS
    }

    @Test
    @DisplayName("Should handle null method gracefully")
    void testNullMethod() {
        // Given
        when(requestContext.getMethod()).thenReturn(null);

        // When
        corsFilter.filter(requestContext);

        // Then
        verify(requestContext, never()).abortWith(any(Response.class));
    }
}
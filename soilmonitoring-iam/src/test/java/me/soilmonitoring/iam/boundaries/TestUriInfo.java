package me.soilmonitoring.iam.boundaries;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

class TestUriInfo implements UriInfo {
    private final MultivaluedMap<String, String> queryParameters;

    public TestUriInfo(MultivaluedMap<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public String getPath(boolean decode) {
        return "";
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return List.of();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        return List.of();
    }

    @Override
    public URI getRequestUri() {
        return URI.create("");
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri("");
    }

    @Override
    public URI getAbsolutePath() {
        return URI.create("");
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri("");
    }

    @Override
    public URI getBaseUri() {
        return URI.create("");
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri("");
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
        return new MultivaluedHashMap<>();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        return new MultivaluedHashMap<>();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return queryParameters;
    }

    @Override
    public List<String> getMatchedURIs() {
        return List.of();
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        return List.of();
    }

    @Override
    public List<Object> getMatchedResources() {
        return List.of();
    }

    @Override
    public MultivaluedMap<String, String> getMatrixParameters() {
        return new MultivaluedHashMap<>();
    }

    @Override
    public URI resolve(URI uri) {
        return uri;
    }

    @Override
    public URI relativize(URI uri) {
        return uri;
    }

    @Override
    public String getMatchedResourceTemplate() {
        return ""; // Return an empty string for testing purposes
    }
}
package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.security.Secured;


@Path("/test")

@RequestScoped
public class TestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response test() {
        return Response.ok("{\"status\":\"API is working!\",\"message\":\"Soil Monitoring API is running\"}").build();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response health() {
        return Response.ok("{\"status\":\"healthy\"}").build();
    }
}

package net.keksipurkki.petclinic.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

@OpenAPIDefinition(
    info = @Info(
        title = "Keksipurkki Pet Clinic",
        version = "1.0.0",
        description = """
                        
            Sample implementation of a REST API demonstrating
            the lessons learned over the years on microservice
            development.
            
            Powered by Eclipse Vert.x — https://vertx.io
                        
            """
    ),
    servers = {
        @Server(url = "http://localhost:8080/petclinic/v1")
    }
)
@Produces("application/json")
@Consumes("application/json")
public interface ApiContract {


}

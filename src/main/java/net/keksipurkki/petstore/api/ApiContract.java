package net.keksipurkki.petstore.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Future;
import net.keksipurkki.petstore.model.LoginToken;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.model.UserRecord;

import javax.ws.rs.*;
import java.util.List;

@OpenAPIDefinition(
    info = @Info(
        title = "Keksipurkki Pet Clinic",
        version = "1.0.0",
        description = """
                        
            Sample implementation of a REST API demonstrating
            lessons learned about microservice development.
                        
            Powered by Eclipse Vert.x — https://vertx.io
                        
            """
    ),
    servers = {
        @Server(url = "http://localhost:8080/petstore/v1")
    },
    tags = {
        @Tag(name = "pet", description = "Everything about your pets"),
        @Tag(name = "store", description = "Access to Petstore orders"),
        @Tag(name = "user", description = "Operations about user")
    }
)
@Produces("application/json")
@Consumes("application/json")
public interface ApiContract {

    /* User */

    @Path("/user")
    @POST
    @Operation(
        operationId = "CREATE_USER",
        tags = {"user"}
    )
    Future<ApiMessage> createUser(UserRecord user);

    @Path("/user/createWithList")
    @POST
    @Operation(
        operationId = "CREATE_USER_LIST",
        tags = {"user"}
    )
    Future<ApiMessage> createWithList(List<UserRecord> userList);

    @Path("/user/{username}")
    @GET
    @Operation(
        operationId = "GET_USER_BY_NAME",
        tags = {"user"}
    )
    Future<User> getUserByName(@PathParam("username") String username);

    @Path("/user/{username}")
    @PUT
    @Operation(
        operationId = "UPDATE_USER",
        tags = {"user"}
    )
    Future<User> updateUser(@PathParam("username") String username, UserRecord user);

    @Path("/user/{username}")
    @DELETE
    @Operation(
        operationId = "DELETE_USER",
        tags = {"user"}
    )
    Future<ApiMessage> deleteUser(@PathParam("username") String username);

    @Path("/user/login")
    @GET
    @Operation(
        operationId = "LOGIN_USER",
        tags = {"user"}
    )
    Future<LoginToken> login(@QueryParam("username") String username, @QueryParam("password") String password);

    @Path("/user/logout")
    @GET
    @Operation(
        operationId = "LOGOUT_USER",
        tags = {"user"}
    )
    Future<ApiMessage> logout(LoginToken token);

}

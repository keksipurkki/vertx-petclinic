package net.keksipurkki.petstore.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Future;
import net.keksipurkki.petstore.store.Inventory;
import net.keksipurkki.petstore.store.NewOrder;
import net.keksipurkki.petstore.store.Order;
import net.keksipurkki.petstore.user.AccessToken;
import net.keksipurkki.petstore.user.User;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

@OpenAPIDefinition(
    info = @Info(
        title = "Keksipurkki Pet Clinic",
        version = "1.0.0",
        description = """
                        
            Sample implementation of a REST API demonstrating
            lessons learned about backend development.
                        
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
@SecurityScheme(name = "LOGIN_SESSION", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public interface ApiContract {

    /* User */

    @Path("/user")
    @POST
    @Operation(
        operationId = "CREATE_USER",
        tags = {"user"},
        security = {@SecurityRequirement(name = "NONE")}
    )
    Future<ApiMessage> createUser(User user);

    @Path("/user/createWithList")
    @POST
    @Operation(
        operationId = "CREATE_USER_LIST",
        tags = {"user"},
        security = {@SecurityRequirement(name = "NONE")}
    )
    Future<ApiMessage> createWithList(List<User> userList);

    @Path("/user/{username}")
    @GET
    @Operation(
        operationId = "GET_USER_BY_NAME",
        tags = {"user"},
        security = {@SecurityRequirement(name = "NONE")}
    )
    Future<User> getUserByName(@PathParam("username") String username);

    @Path("/user/{username}")
    @PUT
    @Operation(
        operationId = "UPDATE_USER",
        tags = {"user"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<User> updateUser(@PathParam("username") String username, User user);

    @Path("/user/{username}")
    @DELETE
    @Operation(
        operationId = "DELETE_USER",
        tags = {"user"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<ApiMessage> deleteUser(@PathParam("username") String username);

    @Path("/user/login")
    @GET
    @Operation(
        operationId = "LOGIN_USER",
        tags = {"user"},
        security = {@SecurityRequirement(name = "NONE")}
    )
    Future<AccessToken> login(@QueryParam("username") String username, @QueryParam("password") String password);

    @Path("/user/logout")
    @GET
    @Operation(
        operationId = "LOGOUT_USER",
        tags = {"user"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<ApiMessage> logout();

    /* Store */

    @Path("/store/inventory")
    @GET
    @Operation(
        operationId = "GET_INVENTORY",
        tags = {"store"},
        security = {@SecurityRequirement(name = "NONE")}
    )
    Future<Map<Inventory, Integer>> getInventory();

    @Path("/store/order")
    @POST
    @Operation(
        operationId = "PLACE_ORDER",
        tags = {"store"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Order> placeOrder(NewOrder order);

    @Path("/store/order/{orderId}")
    @GET
    @Operation(
        operationId = "GET_ORDER",
        tags = {"store"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Order> getOrderById(@PathParam("orderId") int orderId);

    @Path("/store/order/{orderId}")
    @DELETE
    @Operation(
        operationId = "DELETE_ORDER",
        tags = {"store"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Order> deleteOrder(@PathParam("orderId") int orderId);

}

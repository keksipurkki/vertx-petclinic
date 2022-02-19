package net.keksipurkki.petstore.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.FileUpload;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import net.keksipurkki.petstore.pet.NewPet;
import net.keksipurkki.petstore.pet.Pet;
import net.keksipurkki.petstore.pet.Status;
import net.keksipurkki.petstore.store.NewOrder;
import net.keksipurkki.petstore.store.Order;
import net.keksipurkki.petstore.user.AccessToken;
import net.keksipurkki.petstore.user.User;

import java.util.List;
import java.util.Map;

@OpenAPIDefinition(
    info = @Info(
        title = "Keksipurkki Pet Store",
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
    Future<Map<Status, Integer>> getInventory();

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
    Future<Order> getOrderById(@Min(0) @PathParam("orderId") int orderId);

    @Path("/store/order/{orderId}")
    @DELETE
    @Operation(
        operationId = "DELETE_ORDER",
        tags = {"store"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Order> deleteOrder(@Min(0) @PathParam("orderId") int orderId);

    /* Pet */

    @Path("/pet")
    @POST
    @Operation(
        operationId = "ADD_PET",
        description = "Add a new pet to the store",
        tags = {"pet"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Pet> addPet(NewPet pet);

    @Path("/pet/{petId}")
    @POST
    @Operation(
        operationId = "UPDATE_PET",
        description = "Update an existing pet",
        tags = {"pet"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Pet> updatePet(@Min(0) @PathParam("petId") int petId, Pet pet);

    @Path("/pet/{petId}/uploadImage")
    @POST
    @Operation(
        operationId = "UPLOAD_IMAGE",
        description = "uploads an image",
        tags = {"pet"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    @Consumes("multipart/form-data")
    @Produces("application/json")
    Future<ApiMessage> uploadFile(@Min(0) @PathParam("petId") int petId,
                                  @Parameter(schema = @Schema(type = "string", format = "binary", description = "file"))
                                  @FormParam("file") FileUpload image,
                                  @Parameter(schema = @Schema(type = "string", description = "metadata"))
                                  @FormParam("additionalMetadata") String metadata
    );

    @Path("/pet/{petId}")
    @GET
    @Operation(
        operationId = "GET_PET",
        description = "Returns a single pet",
        tags = {"pet"},
        security = {@SecurityRequirement(name = "NONE")}

    )
    Future<Pet> getPetById(@Min(0) @PathParam("petId") int petId);


    @Path("/pet/{petId}")
    @DELETE
    @Operation(
        operationId = "DELETE_PET",
        description = "Deletes a pet",
        tags = {"pet"},
        security = {@SecurityRequirement(name = "LOGIN_SESSION")}
    )
    Future<Pet> deletePet(@Min(0) @PathParam("petId") int petId);


}

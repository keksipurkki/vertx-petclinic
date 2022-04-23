package it;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import net.keksipurkki.petstore.api.Api;
import net.keksipurkki.petstore.http.HttpVerticle;
import net.keksipurkki.petstore.pet.NewPet;
import net.keksipurkki.petstore.pet.Pet;
import net.keksipurkki.petstore.pet.Status;
import net.keksipurkki.petstore.security.JwtPrincipal;
import net.keksipurkki.petstore.support.Json;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static it.Tests.await;
import static it.Tests.randomPort;

@Timeout(
    value = 30,
    unit = TimeUnit.SECONDS
)
public class PetIT {

    private static final Vertx vertx = Vertx.vertx();
    private static final NewPet NEW_PET = new NewPet("doggone", "dogs");

    @BeforeAll
    @DisplayName("Server starts")
    public static void randomPort_deployServer_serverPortEquals() {

        var port = randomPort();
        var api = Api.create(vertx);

        var server = new HttpVerticle();
        server.withApi(api);

        var token = JwtPrincipal.from("test_user").getToken();

        await(vertx.deployVerticle(server));
        await(server.listen(port));

        Assertions.assertEquals(port, server.getPort());
        RestAssured.port = port;
        RestAssured.basePath = HttpVerticle.CONTEXT_PATH;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        RestAssured.requestSpecification = new RequestSpecBuilder()
            .addHeader("authorization", "Bearer " + token)
            .addHeader("x-request-id", UUID.randomUUID().toString())
            .addHeader("x-session-id", UUID.randomUUID().toString())
            .build();

    }

    @Test
    public void addPet_happyPath_ok() {

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(NEW_PET, true))
            .post("/pet");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());
        var actual = Json.parse(json, Pet.class);

        Assertions.assertEquals(Status.AVAILABLE, actual.status());

    }

    @Test
    public void getPetById_happyPath_ok() {
        var pet = pet();
        var id = pet.id();

        var resp = RestAssured
            .given()
            .accept("application/json")
            .get("/pet/{petId}", id);

        Assertions.assertEquals(200, resp.statusCode());
    }

    @Test
    public void getPetById_randomId_notFound() {

        var id = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);

        var resp = RestAssured
            .given()
            .accept("application/json")
            .get("/pet/{petId}", id);

        Assertions.assertEquals(404, resp.statusCode());


    }

    @Test
    public void getPetById_invalidId_badRequest() {

        var openApiSpecViolation = -1;

        var resp = RestAssured
            .given()
            .accept("application/json")
            .get("/pet/{petId}", openApiSpecViolation);

        Assertions.assertEquals(400, resp.statusCode());

    }

    @Test
    @DisplayName("Delete pet — invalid pet id — Bad Request")
    public void deleteOrder_invalidOrderId_badRequest() {

        var openApiSpecViolation = -1;

        var resp = RestAssured
            .given()
            .accept("application/json")
            .delete("/pet/{orderId}", openApiSpecViolation);

        Assertions.assertEquals(400, resp.statusCode());

    }

    @Test
    @DisplayName("Delete pet — retry")
    public void deleteOrder_twice_ok_then_notFound() {

        var pet = pet();
        var petId = pet.id();

        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .delete("/pet/{petId}", petId);

            Assertions.assertEquals(200, resp.statusCode());

        }

        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .delete("/pet/{petId}", petId);

            Assertions.assertEquals(404, resp.statusCode());

        }

    }

    @Test
    public void uploadImage_validImage_happyPath() {
        var pet = pet();
        var petId = pet.id();

        var fileUpload = new MultiPartSpecBuilder(dogImage())
            .fileName("snoopy.png")
            .mimeType("application/octet-stream")
            .controlName("file")
            .build();

        var metadata = new MultiPartSpecBuilder("Some description")
            .mimeType("text/plain")
            .controlName("additionalMetadata")
            .emptyFileName()
            .build();

        var resp = RestAssured
            .given()
            //.contentType("multipart/form-data")
            .multiPart(fileUpload)
            .multiPart(metadata)
            .post("/pet/{petId}/uploadImage", petId);

        Assertions.assertEquals(200, resp.statusCode());
    }

    @Test
    public void updatePet_noChanges_ok() {
        var pet = pet();

        var resp = RestAssured
            .given()
            .contentType("application/json")
            .body(Json.stringify(pet, true))
            .put("/pet/{petId}", pet.id());

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.body().asString());
        var updated = Json.parse(json, Pet.class);

        Assertions.assertEquals(updated, pet);

    }

    @Test
    public void updatePet_changeName_ok() {
        var pet = pet();
        var old = pet.name();
        var newName = "EXPECTED";

        var json = new JsonObject(Json.stringify(pet));
        json.put("name", newName);

        var resp = RestAssured
            .given()
            .contentType("application/json")
            .body(json.toString())
            .put("/pet/{petId}", pet.id());

        Assertions.assertEquals(200, resp.statusCode());

        var body = new JsonObject(resp.body().asString());
        var updated = Json.parse(body, Pet.class);

        Assertions.assertNotEquals(old, newName);
        Assertions.assertEquals(updated.name(), newName);


    }

    @Test
    public void updatePet_putAndGet_changeIsPersisted() {

        var pet = pet();
        var newName = "EXPECTED";

        {

            var json = new JsonObject(Json.stringify(pet));
            json.put("name", newName);

            var resp = RestAssured
                .given()
                .contentType("application/json")
                .body(json.toString())
                .put("/pet/{petId}", pet.id());

            Assertions.assertEquals(200, resp.statusCode());

        }

        {

            var resp = RestAssured
                .given()
                .contentType("application/json")
                .get("/pet/{petId}", pet.id());

            Assertions.assertEquals(200, resp.statusCode());

            var json = new JsonObject(resp.body().asString());

            Assertions.assertEquals(newName, json.getString("name"));

        }


    }

    @SneakyThrows
    private File dogImage() {
        var resource = PetIT.class.getResource("/snoopy.png");
        Objects.requireNonNull(resource, "Image not found");
        return new File(resource.toURI());
    }

    private Pet pet() {

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(NEW_PET, true))
            .post("/pet");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());
        return Json.parse(json, Pet.class);
    }

}

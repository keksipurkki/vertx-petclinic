package it;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
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

@Timeout(value = 30, unit = TimeUnit.SECONDS)
public class PetIT {

    private static final NewPet NEW_PET = new NewPet("doggone", "dogs");

    @BeforeAll
    @DisplayName("Server starts")
    public static void randomPort_deployServer_serverPortEquals() {

        var port = randomPort();
        var vertx = Vertx.vertx();
        var server = new HttpVerticle();
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
        var id = -1;

        {
            var resp = RestAssured
                .given()
                .header("content-type", "application/json")
                .body(Json.stringify(NEW_PET, true))
                .post("/pet");

            Assertions.assertEquals(200, resp.statusCode());

            var json = new JsonObject(resp.asString());
            var actual = Json.parse(json, Pet.class);
            id = actual.id();
            Assertions.assertEquals(Status.AVAILABLE, actual.status());
        }

        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .get("/pet/{petId}", id);

            Assertions.assertEquals(200, resp.statusCode());

        }


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

        var resp = RestAssured
            .given()
            .accept("application/json")
            .get("/pet/{petId}", -1);

        Assertions.assertEquals(400, resp.statusCode());

    }

    @Test
    @DisplayName("Delete pet — invalid pet id — Bad Request")
    public void deleteOrder_invalidOrderId_badRequest() {

        var petId = -1;

        var resp = RestAssured
            .given()
            .accept("application/json")
            .delete("/pet/{orderId}", petId);

        Assertions.assertEquals(400, resp.statusCode());

    }

    @Test
    @DisplayName("Delete pet — retry")
    public void deleteOrder_twice_ok_then_notFound() {

        var petId = -1;

        {

            var resp = RestAssured
                .given()
                .header("content-type", "application/json")
                .body(Json.stringify(NEW_PET, true))
                .post("/pet");

            Assertions.assertEquals(200, resp.statusCode());

            var json = new JsonObject(resp.asString());
            var actual = Json.parse(json, Pet.class);
            petId = actual.id();

        }

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

        var resp = RestAssured
            .given()
            .contentType("multipart/form-data")
            .multiPart("file", dogImage())
            .multiPart("additionalMetadata", "snoopy.png")
            .post("/pet/{petId}/uploadImage", petId);

        Assertions.assertEquals(200, resp.statusCode());
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

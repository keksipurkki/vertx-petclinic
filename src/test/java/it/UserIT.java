package it;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.keksipurkki.petstore.api.ApiMessage;
import net.keksipurkki.petstore.http.HttpVerticle;
import net.keksipurkki.petstore.model.UserRecord;
import net.keksipurkki.petstore.support.Json;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.Tests.await;
import static it.Tests.randomPort;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
public class UserIT {

    @BeforeAll
    @DisplayName("Server starts")
    public static void randomPort_deployServer_serverPortEquals() {

        var port = randomPort();
        var vertx = Vertx.vertx();
        var server = new HttpVerticle();

        await(vertx.deployVerticle(server));
        await(server.listen(port));

        Assertions.assertEquals(port, server.getPort());
        RestAssured.port = port;
        RestAssured.basePath = HttpVerticle.CONTEXT_PATH;
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    @DisplayName("Create a single user — happy path")
    public void createUser_validBody_responseMatchesSpec() {

        var expected = new UserRecord(
            "EXPECTED",
            "User",
            "Name",
            "email@email.com",
            "password",
            null
        );

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(expected, true))
            .post("/user");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());
        var actual = Json.parse(json, ApiMessage.class);

        Assertions.assertEquals("User EXPECTED created successfully", actual.message());

    }

    @Test
    @DisplayName("Create a single user — sad path — missing required data")
    public void createUser_invalidBody_expectedErrorMessage() {

        var user = new UserRecord(
            "username",
            "User",
            "Name",
            "email@email.com",
            null,
            null
        );

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(user, true))
            .post("/user");

        Assertions.assertEquals(400, resp.statusCode());

        var json = new JsonObject(resp.asString());

        var expected = "provided object should contain property password";
        assertThat(json.getString("detail"), containsString(expected));

    }

    @Test
    @DisplayName("Create multiple users — happy path")
    public void createWithList_validInput_expectedApiMessage() {

        var alice = new UserRecord(
            "alice",
            "User",
            "Name",
            "email@email.com",
            "password",
            null
        );

        var bob = new UserRecord(
            "bob",
            "User",
            "Name",
            "email@email.com",
            "password",
            null
        );

        var list = List.of(alice, bob);

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(list, true))
            .post("/user/createWithList");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());
        var actual = Json.parse(json, ApiMessage.class);

        Assertions.assertEquals("Created 2 users successfully", actual.message());

    }

    @Test
    @DisplayName("Create multiple users — sad path — duplicate data")
    public void createWithList_duplicateInput_expectedError() {

        var alice = new UserRecord(
            "alice",
            "User",
            "Name",
            "email@email.com",
            "password",
            null
        );

        var list = List.of(alice, alice);

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(list, true))
            .post("/user/createWithList");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());

        var expected = "Input contains duplicate users";
        assertThat(json.getString("detail"), is(expected));

    }
}

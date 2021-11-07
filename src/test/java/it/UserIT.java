package it;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.keksipurkki.petstore.api.ApiMessage;
import net.keksipurkki.petstore.http.HttpVerticle;
import net.keksipurkki.petstore.model.User;
import net.keksipurkki.petstore.service.UsersImpl;
import net.keksipurkki.petstore.support.Json;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.Tests.await;
import static it.Tests.randomPort;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
public class UserIT {

    private static final User ALICE = new User(
        0,
        "Alice",
        "Alice",
        "Johnson",
        "email@email.com",
        "password",
        null
    );

    private static final User BOB = new User(
        1,
        "Bob",
        "Bob",
        "Builder",
        "email@email.com",
        "password",
        null
    );

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

    @AfterEach
    public void cleanUp() {
        // :-/
        UsersImpl.clear();
    }

    @Test
    @DisplayName("Create a single user — happy path")
    public void createUser_validBody_responseMatchesSpec() {

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(ALICE, true))
            .post("/user");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());
        var actual = Json.parse(json, ApiMessage.class);

        Assertions.assertEquals("User Alice created successfully", actual.message());

    }

    @Test
    @DisplayName("User response does not contain password")
    public void createUser_thenGet_passwordIsRedacted() {

        var user = ALICE;

        var creation = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(user, true))
            .post("/user");

        Assertions.assertEquals(200, creation.statusCode());

        var retrieval = RestAssured
            .given()
            .accept("application/json")
            .get("/user/{username}", user.username());

        assertThat(retrieval.statusCode(), equalTo(200));

        var json = new JsonObject(retrieval.asString());

        assertThat(json.getString("password"), is(not(equalTo(user.password()))));
        assertThat(json.getString("password"), is(nullValue()));

    }

    @Test
    @DisplayName("Create a single user — sad path — missing required data")
    public void createUser_invalidBody_expectedErrorMessage() {

        var passwordMissing = new User(
            0,
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
            .body(Json.stringify(passwordMissing, true))
            .post("/user");

        Assertions.assertEquals(400, resp.statusCode());

        var json = new JsonObject(resp.asString());

        var expected = "provided object should contain property password";
        assertThat(json.getString("detail"), containsString(expected));

    }

    @Test
    @DisplayName("Create multiple users — happy path")
    public void createWithList_validInput_expectedApiMessage() {

        var list = List.of(ALICE, BOB);

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

        var list = List.of(ALICE, ALICE);

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(list, true))
            .post("/user/createWithList");

        Assertions.assertEquals(400, resp.statusCode());

        var json = new JsonObject(resp.asString());

        var expected = "Input contains duplicate users";
        assertThat(json.getString("detail"), is(expected));

    }

    @Test
    @DisplayName("User lifecycle — happy path")
    public void crud_happyPath_expectedOutcome() {
        var user = ALICE;

        // Create
        {
            var resp = RestAssured
                .given()
                .header("content-type", "application/json")
                .body(Json.stringify(user, true))
                .post("/user");

            assertThat(resp.statusCode(), equalTo(200));
        }

        // Read
        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .get("/user/{username}", user.username());

            assertThat(resp.statusCode(), equalTo(200));

            var actual = new JsonObject(resp.asString());
            var expected = JsonObject.mapFrom(user);

            assertThat(expected.getString("username"), equalTo(actual.getString("username")));

        }

        // Login
        String token;
        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .queryParam("username", user.username())
                .queryParam("password", user.password())
                .get("/user/login/");

            assertThat(resp.statusCode(), equalTo(200));

            var json = new JsonObject(resp.asString());
            token = json.getString("token");
            assertThat(token, is(not(nullValue())));

        }

        // Update
        {

            var update = new User(
                0,
                "Alice",
                "Alice",
                "Bronson",
                "email@email.com",
                "password",
                null
            );

            var resp = RestAssured
                .given()
                .contentType("application/json")
                .header("authorization", "Bearer " + token)
                .body(Json.stringify(update, true))
                .put("/user/{username}", user.username());

            assertThat(resp.statusCode(), equalTo(200));

            var actual = new JsonObject(resp.asString());
            var expected = JsonObject.mapFrom(user);

            assertThat(expected, not(equalTo(actual.getJsonObject("data"))));
            assertThat("Bronson", equalTo(actual.getString("lastName")));

        }

        // Delete
        {

            var resp = RestAssured
                .given()
                .contentType("application/json")
                .header("authorization", "Bearer " + token)
                .delete("/user/{username}", user.username());

            assertThat(resp.statusCode(), equalTo(200));

        }

        // End
        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .get("/user/{username}", user.username());

            assertThat(resp.statusCode(), equalTo(404));

        }

    }

}

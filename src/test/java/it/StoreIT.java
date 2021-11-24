package it;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.keksipurkki.petstore.http.HttpVerticle;
import net.keksipurkki.petstore.security.JwtPrincipal;
import net.keksipurkki.petstore.store.NewOrder;
import net.keksipurkki.petstore.store.Order;
import net.keksipurkki.petstore.support.Json;
import org.junit.jupiter.api.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static it.Tests.await;
import static it.Tests.randomPort;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
public class StoreIT {

    private static final NewOrder NEW_ORDER = new NewOrder(0, 1);

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
            .build().header("authorization", "Bearer " + token);

    }

    @Test
    @DisplayName("Place a new order — happy path")
    public void placeOrder_validBody_responseMatchesSpec() {

        var resp = RestAssured
            .given()
            .header("content-type", "application/json")
            .body(Json.stringify(NEW_ORDER, true))
            .post("/store/order");

        Assertions.assertEquals(200, resp.statusCode());

        var json = new JsonObject(resp.asString());
        var actual = Json.parse(json, Order.class);

        Assertions.assertEquals("placed", actual.status());

    }

    @Test
    @DisplayName("Get order by id — Not Found")
    public void getOrderById_someRandomOrderId_notFound() {

        var random = ThreadLocalRandom.current().nextInt();

        var resp = RestAssured
            .given()
            .accept("application/json")
            .get("/store/order/{orderId}", random);

        Assertions.assertEquals(404, resp.statusCode());

    }

    @Test
    @DisplayName("Get order by id — happy path")
    public void getOrderById_validId_found() {

        int orderId = -1;

        {

            var resp = RestAssured
                .given()
                .header("content-type", "application/json")
                .body(Json.stringify(NEW_ORDER, true))
                .post("/store/order");

            Assertions.assertEquals(200, resp.statusCode());

            var json = new JsonObject(resp.asString());
            var actual = Json.parse(json, Order.class);
            orderId = actual.orderId();

        }
        {

            var resp = RestAssured
                .given()
                .accept("application/json")
                .get("/store/order/{orderId}", orderId);

            Assertions.assertEquals(200, resp.statusCode());

            var json = new JsonObject(resp.asString());
            var actual = Json.parse(json, Order.class);

            Assertions.assertEquals(actual.orderId(), orderId);


        }


    }

}

import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;

public class OrderListTests {

    private String baseUri = "https://qa-scooter.praktikum-services.ru";

    @Before
    public void setUp() {
        RestAssured.baseURI = baseUri;
    }

    @Test
    @DisplayName("Проверка списка заказов с использованием POJO")
    public void testOrderListWithPojoValidation() {
        JsonPath response = given()
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .extract().jsonPath();

        List<Order> orders = response.getList("orders", Order.class);

        // Проверяем что список не пустой
        assertThat(orders, not(empty()));

        // Проверяем структуру каждого заказа
        for (Order order : orders) {
            assertThat(order.getId(), notNullValue());
            assertThat(order.getFirstName(), notNullValue());
            assertThat(order.getLastName(), notNullValue());
            assertThat(order.getAddress(), notNullValue());
            assertThat(order.getMetroStation(), notNullValue());
            assertThat(order.getPhone(), notNullValue());
            assertThat(order.getRentTime(), notNullValue());
            assertThat(order.getDeliveryDate(), notNullValue());
            assertThat(order.getTrack(), notNullValue());
            assertThat(order.getStatus(), notNullValue());
        }
    }

}

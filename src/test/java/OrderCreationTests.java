import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.InputStream;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreationTests {
    private final String orderFile;
    private String baseUri = "https://qa-scooter.praktikum-services.ru";

    public OrderCreationTests(String testName, String orderFile) {
        this.orderFile = orderFile;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] getTestData() {
        return new Object[][] {
                {"Создание заказа с цветом BLACK", "/orders/order_black.json"},
                {"Создание заказа с цветом GREY", "/orders/order_grey.json"},
                {"Создание заказа с обоими цветами", "/orders/order_both_colors.json"},
                {"Создание заказа без указания цвета", "/orders/order_no_color.json"}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = baseUri;
    }

    @Test
    @DisplayName("Проверка создания заказа с разными вариантами цветов из файла")
    public void testOrderCreationFromFiles() {
        // Читаем JSON из файла
        InputStream inputStream = getClass().getResourceAsStream(orderFile);

        given()
                .contentType("application/json")
                .body(inputStream)  // Передаем содержимое файла напрямую
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }

}
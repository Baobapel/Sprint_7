import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierLoginTests {
    private Courier courier;
    private String baseUri = "https://qa-scooter.praktikum-services.ru";

    @Before
    public void setUp() {
        RestAssured.baseURI = baseUri;
        // Создаем курьера перед тестами авторизации
        courier = new Courier(
                RandomUtils.randomString(10),
                RandomUtils.randomString(10),
                RandomUtils.randomString(10)
        );

        // Регистрируем курьера для тестов авторизации
        given()
                .contentType("application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }

    @After
    public void tearDown() {
        // Удаление созданного курьера после тестов
        Response loginResponse = given()
                .contentType("application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}",
                        courier.getLogin(), courier.getPassword()))
                .when()
                .post("/api/v1/courier/login");

        if (loginResponse.statusCode() == 200) {
            String id = loginResponse.then().extract().path("id").toString();
            given()
                    .when()
                    .delete("/api/v1/courier/" + id)
                    .then()
                    .statusCode(200);
        }
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void testCourierLoginSuccessfully() {
        given()
                .contentType("application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}",
                        courier.getLogin(), courier.getPassword()))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("Авторизация без логина")
    public void testCourierLoginWithoutLogin() {
        given()
                .contentType("application/json")
                .body(String.format("{\"password\":\"%s\"}", courier.getPassword()))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация без пароля должна возвращать ошибку 400")
    public void testCourierLoginWithoutPassword() {
        try {
            Response response = given()
                    .contentType("application/json")
                    .body(String.format("{\"login\":\"%s\"}", courier.getLogin()))
                    .post("/api/v1/courier/login");

            // Проверяем статус код и тело ответа в одном месте
            if (response.statusCode() != 400) {
                throw new AssertionError(String.format(
                        "Ожидался статус код 400, но получен %d. Тело ответа: %s",
                        response.statusCode(),
                        response.getBody().asString()
                ));
            }

            String actualMessage = response.jsonPath().getString("message");
            if (!"Недостаточно данных для входа".equals(actualMessage)) {
                throw new AssertionError(String.format(
                        "Ожидалось сообщение 'Недостаточно данных для входа', но получено: '%s'",
                        actualMessage
                ));
            }
        } catch (Exception e) {
            throw new AssertionError("Ошибка при выполнении теста авторизации без пароля: " + e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("Авторизация с неверным логином")
    public void testCourierLoginWithWrongLogin() {
        given()
                .contentType("application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}",
                        "wrong_" + courier.getLogin(), courier.getPassword()))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    public void testCourierLoginWithWrongPassword() {
        given()
                .contentType("application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}",
                        courier.getLogin(), "wrong_" + courier.getPassword()))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    public void testNonExistentCourierLogin() {
        Courier nonExistentCourier = new Courier(
                "nonexistent_" + RandomUtils.randomString(10),
                RandomUtils.randomString(10),
                null
        );

        given()
                .contentType("application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}",
                        nonExistentCourier.getLogin(), nonExistentCourier.getPassword()))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}
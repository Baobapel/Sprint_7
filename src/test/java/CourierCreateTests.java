import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierCreateTests {
    private Courier courier;
    private String baseUri = "https://qa-scooter.praktikum-services.ru";

    @Before
    public void setUp() {
        RestAssured.baseURI = baseUri;
        // Генерация случайных данных для курьера перед каждым тестом
        courier = new Courier(
                RandomUtils.randomString(10),
                RandomUtils.randomString(10),
                RandomUtils.randomString(10)
        );

    }

    @After
    public void tearDown() {
        // Удаление созданного курьера после каждого теста, если он был создан
        if (courier != null && courier.getLogin() != null) {
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
    }

    @Test
    @DisplayName("Успешное создание курьера")
    public void testCreateCourierSuccessfully() {
        given()
                .contentType("application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        // Сначала создаем курьера
        given()
                .contentType("application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);

        // Пытаемся создать такого же курьера еще раз
        given().log().all()
                .contentType("application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Создание курьера без логина")
    public void testCreateCourierWithoutLogin() {
        Courier courierWithoutLogin = new Courier(null, courier.getPassword(), courier.getFirstName());

        given()
                .contentType("application/json")
                .body(courierWithoutLogin)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        Courier courierWithoutPassword = new Courier(courier.getLogin(), null, courier.getFirstName());

        given().log().all()
                .contentType("application/json")
                .body(courierWithoutPassword)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        // Имя не является обязательным полем, поэтому тест проверяет успешное создание
        Courier courierWithoutFirstName = new Courier(courier.getLogin(), courier.getPassword(), null);

        given()
                .contentType("application/json")
                .body(courierWithoutFirstName)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    }
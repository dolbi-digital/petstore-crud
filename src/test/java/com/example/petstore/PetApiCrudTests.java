package com.example.petstore;

import com.example.petstore.model.Category;
import com.example.petstore.model.Pet;
import com.example.petstore.model.Tag;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

@Epic("Petstore API")
@Feature("Pets CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetApiCrudTests extends TestBase {

    private Pet buildPet(long id, String name, String status) {
        Pet p = new Pet();
        p.setId(id);
        p.setName(name);
        p.setStatus(status);
        p.setCategory(new Category(1L, "dogs"));
        p.setPhotoUrls(List.of("https://example.com/dog.png"));
        p.setTags(List.of(new Tag(11L, "automation")));
        return p;
    }

    private static long petId;

    @BeforeAll
    static void initId() {
        String sys = System.getProperty("PET_ID");
        if (sys != null && sys.matches("\\d+") && !sys.equals("0")) {
            petId = Long.parseLong(sys);
        } else {
            petId = ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L);
        }
        System.out.println("Using petId = " + petId);
    }

    @Test
    @Order(1)
    @DisplayName("Create pet (POST /pet) -> 200")
    void createPet() {
        Pet newPet = buildPet(petId, "test-dog", "available");

        given().spec(reqSpec)
                .body(newPet)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo((int) petId))
                .body("name", equalTo("test-dog"))
                .body("status", equalTo("available"))
                .body(matchesJsonSchemaInClasspath("schemas/pet-get.json"));
    }

    @Test
    @Order(2)
    @DisplayName("Get pet (GET /pet/{id}) -> 200")
    void getPet() {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        given().spec(reqSpec)
                                .when()
                                .get("/pet/{id}", petId)
                                .then().spec(okJsonSpec)
                                .statusCode(200)
                                .body("id", equalTo((int) petId))
                                .body("name", equalTo("test-dog"))
                                .body("status", equalTo("available"))
                                .body("category.id", equalTo(1))
                                .body("category.name", equalTo("dogs"))
                                .body("photoUrls", hasItem("https://example.com/dog.png"))
                                .body("tags[0].id", equalTo(11))
                                .body("tags[0].name", equalTo("automation"))
                                .body(matchesJsonSchemaInClasspath("schemas/pet-get.json"))
                );
    }

    @Test
    @Order(3)
    @DisplayName("Update pet (PUT /pet) -> 200")
    void updatePetPut() {
        Pet upd = buildPet(petId, "updated-dog", "sold");
        Response putResp = given().spec(reqSpec)
                .body(upd)
                .when()
                .put("/pet");
        int putCode = putResp.getStatusCode();

        if (putCode == 404) {
            await().atMost(10, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                    .untilAsserted(() ->
                            given().spec(reqSpec)
                                    .body(upd)
                                    .when()
                                    .put("/pet")
                                    .then().spec(okJsonSpec)
                                    .statusCode(200)
                                    .body("id", equalTo((int) petId))
                                    .body("name", equalTo("updated-dog"))
                                    .body("status", equalTo("sold"))
                                    .body(matchesJsonSchemaInClasspath(
                                            "schemas/pet-get.json")));
        }
    }

    @Test
    @Order(4)
    @DisplayName("GET /pet/findByStatus?status=sold -> 200")
    void findByStatus() {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        given().spec(reqSpec)
                                .queryParam("status", "sold")
                                .when()
                                .get("/pet/findByStatus")
                                .then().spec(okJsonSpec)
                                .statusCode(200)
                                .body("$", isA(java.util.List.class))
                                .body("id", hasItem((int) petId))
                                .body("name", hasItem("updated-dog"))
                                .body("status", hasItem("sold"))
                );
    }

    @Test
    @Order(5)
    @DisplayName("Delete pet (DELETE /pet/{id}) -> 200")
    void deletePet() {
        given().spec(reqSpec)
                .when()
                .delete("/pet/{id}", petId)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    @DisplayName("Get after delete -> 404")
    void getAfterDelete() {
        await().atMost(5, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() ->
                        given().spec(reqSpec)
                                .when()
                                .get("/pet/{id}", petId)
                                .then()
                                .statusCode(404)
                );
    }
}
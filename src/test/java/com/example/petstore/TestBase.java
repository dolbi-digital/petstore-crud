package com.example.petstore;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

import static io.restassured.RestAssured.*;

public abstract class TestBase {

    protected static RequestSpecification reqSpec;
    protected static ResponseSpecification okJsonSpec;

    @BeforeAll
    static void setup() {
        baseURI = "https://petstore3.swagger.io";
        basePath = "/api/v3";

        reqSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.URI)
                .log(LogDetail.METHOD)
                .log(LogDetail.BODY)
                .build();

        okJsonSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }
}
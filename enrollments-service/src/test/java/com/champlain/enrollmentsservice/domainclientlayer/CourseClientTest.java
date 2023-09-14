package com.champlain.enrollmentsservice.domainclientlayer;


import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(CourseClientTest.class)
class CourseClientTest {


    @MockBean
    private ConnectionFactoryInitializer initializer;

    @MockBean
    private CourseClient courseClient;

    private ObjectMapper objectMapper = new ObjectMapper();


    private static MockWebServer webServer;


    @BeforeAll
    static void setup() throws IOException {
        webServer = new MockWebServer();
        webServer.start();

    }

    @BeforeEach
    void initialize() {
        courseClient = new CourseClient("localhost",String.valueOf(webServer.getPort()));

    }

    @AfterAll
    static void tearDown() throws IOException {
        webServer.shutdown();
    }

    @Test
    void getCourseByCourseId_shouldSucceed() throws IOException{

        CourseResponseDTO courseResponseDTO = new CourseResponseDTO("courseId", "courseNumber", "courseName", 1, 1.0, "department");
        webServer.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(courseResponseDTO))
                .addHeader("Content-type", "application/json"));

        Mono<CourseResponseDTO> courseResponseDTOMono = courseClient.getCourseByCourseId("courseId");
        StepVerifier.create(courseResponseDTOMono)
                .expectNextMatches(courseResponseDTO1 ->
                        courseResponseDTO1.getCourseId().equals("courseId")
                ).verifyComplete();

    }

    @Test
    void getCourseByCourseId_invalidId_shouldThrowNotFoundException() throws IOException {
        webServer.enqueue(new MockResponse().setResponseCode(404));
        Mono<CourseResponseDTO> courseResponseDTOMono = courseClient.getCourseByCourseId("courseId");
        StepVerifier.create(courseResponseDTOMono)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("CourseId not found: courseId"))
                .verify();
    }


    @Test
    void getCourseByCourseId_badRequest_throwsIllegalArgumentException() throws IOException {

        webServer.enqueue(new MockResponse().setResponseCode(400));

        Mono<CourseResponseDTO> courseResponseDTOMono = courseClient.getCourseByCourseId("courseId");
        StepVerifier.create(courseResponseDTOMono)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().equals("Something went wrong"))
                .verify();
    }


}
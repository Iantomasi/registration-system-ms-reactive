package com.champlain.enrollmentsservice.domainclientlayer;

import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(com.champlain.enrollmentsservice.domainclientlayer.StudentClientTest.class)
class StudentClientTest {

        @MockBean
        private ConnectionFactoryInitializer initializer;

        @MockBean
        private StudentClient studentClient;

        private ObjectMapper objectMapper = new ObjectMapper();



        private static MockWebServer webServer;


        @BeforeAll
        static void setup() throws IOException {
            webServer = new MockWebServer();
            webServer.start();

        }

        @BeforeEach
        void initialize() {
            studentClient = new StudentClient("localhost",String.valueOf(webServer.getPort()));

        }

        @AfterAll
        static void tearDown() throws IOException {
            webServer.shutdown();
        }

        @Test
        void getStudentByStudentId_shouldSucceed() throws IOException{

            StudentResponseDTO studentResponseDTO = new StudentResponseDTO("studentId", "firstName", "lastName", "department");
            webServer.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(objectMapper.writeValueAsString(studentResponseDTO))
                    .addHeader("Content-type", "application/json"));

            Mono<StudentResponseDTO> studentResponseDTO01 = studentClient.getStudentByStudentId("studentId");
            StepVerifier.create(studentResponseDTO01)
                    .expectNextMatches(studentResponseDTO1 ->
                            studentResponseDTO1.getStudentId().equals("studentId"))
                    .verifyComplete();


        }

        //NotFound
        @Test
        void getStudentByStudentId_invalidId_shouldThrowNotFoundException() throws IOException {
            webServer.enqueue(new MockResponse().setResponseCode(404));

            Mono<StudentResponseDTO> studentResponseDTO01 = studentClient.getStudentByStudentId("studentId");

            StepVerifier.create(studentResponseDTO01)
                    .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                            && throwable.getMessage().equals("StudentId not found: studentId"))
                    .verify();
        }


        @Test
        void getStudentByStudentId_invalidRequest_shouldThrowIllegalArgumentException() throws IOException {
            webServer.enqueue(new MockResponse().setResponseCode(400));

            Mono<StudentResponseDTO> studentResponseDTO01 = studentClient.getStudentByStudentId("studentId");
            StepVerifier.create(studentResponseDTO01)
                    .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                            && throwable.getMessage().equals("Something went wrong"))
                    .verify();
        }


}
package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(controllers = CourseController.class)
class CourseControllerUnitTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    CourseService courseService;

    String uuid1 = UUID.randomUUID().toString();

    @Test
    void getCourseByCourseId_validId_shouldSucceed(){
        //arrange
        CourseResponseDTO courseResponseDTO = CourseResponseDTO.builder()
                .courseId(uuid1)
                .courseName("Final Project 1")
                .courseNumber("420-N45-LA")
                .department("Computer Science")
                .numCredits(2.0)
                .numHours(60)
                .build();

        when(courseService.getCourseById(courseResponseDTO.getCourseId()))
                .thenReturn(Mono.just(courseResponseDTO));

        webTestClient
                .get()
                .uri("/courses/{courseId}", courseResponseDTO.getCourseId())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(CourseResponseDTO.class)
                .value(dto ->{
                    assertNotNull(dto);
                    assertEquals(courseResponseDTO.getCourseId(), dto.getCourseId());
                    assertEquals(courseResponseDTO.getCourseName(), dto.getCourseName());

                });

        verify(courseService, times(1))
                .getCourseById(courseResponseDTO.getCourseId());

    }

    @Test
    void deleteCourseByCourseId_validId_shouldSucceed() {
        //arrange
        String courseId = uuid1;

        when(courseService.deleteCourseById(courseId))
                .thenReturn(Mono.empty());

        //act & assert
        webTestClient
                .delete()
                .uri("/courses/{courseId}", courseId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(courseService, times(1))
                .deleteCourseById(courseId);
    }

    @Test
    void getCourseByCourseId_invalidId_shouldThrowNotFoundException() {
        //arrange
        String notFoundId = "12345";

        when(courseService.getCourseById(notFoundId))
                .thenReturn(Mono.empty());

        //act and assert
        webTestClient
                .get()
                .uri("/courses/{courseId}", notFoundId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(courseService, times(1))
                .getCourseById(notFoundId);
    }

}
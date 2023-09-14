package com.champlain.studentsservice.presentationlayer;


import com.champlain.studentsservice.businesslayer.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(controllers = StudentController.class)
class StudentControllerUnitTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    StudentService studentService;

    @Test
    void getStudentByStudentId_validId_shouldSucceed(){
        //arrange
        StudentResponseDTO studentResponseDTO = StudentResponseDTO.builder()
                .firstName("John")
                .lastName("low")
                .studentId("123")
                .program("History")
                .build();

        when(studentService.getStudentById(studentResponseDTO.getStudentId()))
                .thenReturn(Mono.just(studentResponseDTO));

        webTestClient
                .get()
                .uri("/students/{studentId}", studentResponseDTO.getStudentId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(StudentResponseDTO.class)
                .value(dto ->{
                    assertNotNull(dto);
                    assertEquals(studentResponseDTO.getStudentId(), dto.getStudentId());
                    assertEquals(studentResponseDTO.getFirstName(), dto.getFirstName());

                });

        verify(studentService, times(1))
                .getStudentById(studentResponseDTO.getStudentId());

    }

    @Test
    void removeStudentByStudentId_validId_shouldSucceed() {
        //arrange
        String studentId = "c2db7b50-26b5-43f0-ab03-8dc5dab937fb";

        when(studentService.deleteStudentById(studentId))
                .thenReturn(Mono.empty());

        //act & assert
        webTestClient
                .delete()
                .uri("/students/{studentId}", studentId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(studentService, times(1))
                .deleteStudentById(studentId);
    }

    @Test
    void getStudentByStudentId_invalidId_shouldReturnNotFound() {
        //arrange
        String notFoundId = "12345";

        when(studentService.getStudentById(notFoundId))
                .thenReturn(Mono.empty());

        //act and assert
        webTestClient
                .get()
                .uri("/students/{studentId}", notFoundId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }



}
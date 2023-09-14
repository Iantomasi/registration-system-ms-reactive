package com.champlain.enrollmentsservice.businesslayer;

import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.presentationlayer.EnrollmentResponseDTO;
import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.champlain.enrollmentsservice.dataaccesslayer.Semester.SPRING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class EnrollmentServiceUnitTest {


    @Autowired
    EnrollmentService enrollmentService;

    @MockBean
    private EnrollmentRepository enrollmentRepository;


    String uuidStudent = UUID.randomUUID().toString();
    String uuidCourse = UUID.randomUUID().toString();
    String uuidEnrollment = UUID.randomUUID().toString();


    Enrollment enrollment = Enrollment.builder()

            .enrollmentId(uuidEnrollment)
            .enrollmentYear(2023)
            .semester(SPRING)
            .studentId(uuidStudent)
            .studentFirstName("studentFirstName")
            .studentLastName("studentLastName")
            .courseId(uuidCourse)
            .courseName("CourseName")
            .courseNumber("420-NA")
            .build();


    @Test
    void getAllEnrollments_shouldSucceed() {

        //arrange
        when(enrollmentRepository.findAll()).thenReturn(Flux.just(enrollment));

        when(enrollmentRepository.findAllEnrollmentsByStudentId(anyString())).thenReturn(Flux.just(enrollment));

        //act
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("studentId", "someStudentIdValue"); // Just an example

        Flux<EnrollmentResponseDTO> enrollmentResponseDTOFlux = enrollmentService.getAllEnrollments(queryParameters);

        //assert
        StepVerifier
                .create(enrollmentResponseDTOFlux)
                .consumeNextWith(foundEnrollment ->{

                    assertNotNull(foundEnrollment);
                    assertEquals(enrollment.getEnrollmentId(), foundEnrollment.getEnrollmentId());
                    assertEquals(enrollment.getEnrollmentYear(), foundEnrollment.getEnrollmentYear());
                    assertEquals(enrollment.getSemester(), foundEnrollment.getSemester());
                    assertEquals(enrollment.getStudentId(), foundEnrollment.getStudentId());
                    assertEquals(enrollment.getStudentFirstName(), foundEnrollment.getStudentFirstName());
                    assertEquals(enrollment.getStudentLastName(), foundEnrollment.getStudentLastName());
                    assertEquals(enrollment.getCourseId(), foundEnrollment.getCourseId());
                    assertEquals(enrollment.getCourseNumber(), foundEnrollment.getCourseNumber());
                    assertEquals(enrollment.getCourseName(), foundEnrollment.getCourseName());
                })
                .verifyComplete();
    }

    @Test
    void getEnrollmentByEnrollmentId_validId_shouldSucceed(){
        //arrange
        when(enrollmentRepository.findEnrollmentByEnrollmentId(anyString()))
                .thenReturn(Mono.just(enrollment));

        //act
        Mono<EnrollmentResponseDTO> enrollmentResponseDTOMono = enrollmentService
                .getEnrollmentById(enrollment.getEnrollmentId());

        //assert
        StepVerifier
                .create(enrollmentResponseDTOMono)
                .consumeNextWith(foundEnrollment ->{
                    assertNotNull(foundEnrollment);
                    assertEquals(enrollment.getEnrollmentId(), foundEnrollment.getEnrollmentId());
                    assertEquals(enrollment.getEnrollmentYear(), foundEnrollment.getEnrollmentYear());
                    assertEquals(enrollment.getSemester(), foundEnrollment.getSemester());
                    assertEquals(enrollment.getStudentId(), foundEnrollment.getStudentId());
                    assertEquals(enrollment.getStudentFirstName(), foundEnrollment.getStudentFirstName());
                    assertEquals(enrollment.getStudentLastName(), foundEnrollment.getStudentLastName());
                    assertEquals(enrollment.getCourseId(), foundEnrollment.getCourseId());
                    assertEquals(enrollment.getCourseName(), foundEnrollment.getCourseName());
                    assertEquals(enrollment.getCourseNumber(), foundEnrollment.getCourseNumber());

                })
                .verifyComplete();
    }

    @Test
    void deleteEnrollmentById_validId_shouldSucceed() {
        //arrange
        String enrollmentId = uuidEnrollment;

        when(enrollmentRepository.delete(any(Enrollment.class)))
                .thenReturn(Mono.empty());

        when(enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId))
                .thenReturn(Mono.just(new Enrollment()));

        when(enrollmentRepository.delete(any(Enrollment.class)))
                .thenReturn(Mono.empty());

        //act
        Mono<Void> enrollmentDelete = enrollmentService.deleteEnrollmentById(enrollmentId);

        //assert
        StepVerifier
                .create(enrollmentDelete)
                .verifyComplete();
    }

    @Test
    void deleteEnrollmentById_invalidId_shouldThrowInvalidInputException() {
        //arrange
        String invalidEnrollmentId = "invalidId";

        //act and Assert
        StepVerifier
                .create(enrollmentService.deleteEnrollmentById(invalidEnrollmentId))
                .expectErrorMatches(exception -> exception instanceof InvalidInputException &&
                        exception.getMessage().contains("Invalid enrollmentId, length must be 36 characters"))
                .verify();
    }



}
package com.champlain.enrollmentsservice.dataaccesslayer;

import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.UUID;

import static com.champlain.enrollmentsservice.dataaccesslayer.Semester.SPRING;
import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
class EnrollmentRepositoryIntegrationTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @BeforeEach
    public void setupDB(){
        StepVerifier
                .create(enrollmentRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findEnrollmentsByEnrollmentId_validId_shouldSucceed(){
        //arrange
        Enrollment enrollment = Enrollment.builder()
                .enrollmentId("enrollmentId01")
                .enrollmentYear(2023)
                .semester(SPRING)
                .studentId("studentId01")
                .studentFirstName("studentFirstName01")
                .studentLastName("studentLastName01")
                .courseId("courseId01")
                .courseName("courseName01")
                .courseNumber("courseNumber01")
                .build();

        Mono<Enrollment> setup = enrollmentRepository.save(enrollment);

        StepVerifier
                .create(setup)
                .consumeNextWith(testEnrollment -> {
                    assertNotNull(testEnrollment);
                    assertEquals(enrollment.getEnrollmentId(), testEnrollment.getEnrollmentId());

                })
                .verifyComplete();

        Mono<Enrollment> addedEnrollment = enrollmentRepository
                .findEnrollmentByEnrollmentId(enrollment.getEnrollmentId());

        StepVerifier
                .create(addedEnrollment)
                .consumeNextWith(foundEnrollment -> {
                    assertNotNull(foundEnrollment);
                    assertEquals(enrollment.getEnrollmentId(), foundEnrollment.getEnrollmentId());
                })
                .verifyComplete();
    }

    @Test
    void findAllEnrollmentsByStudentId_validId_shouldSucceed() {
        // Arrange
        String studentId = "studentId01";

        Enrollment enrollment1 = Enrollment.builder()
                .enrollmentId("enrollmentId02")
                .enrollmentYear(2023)
                .semester(SPRING)
                .studentId(studentId)
                .studentFirstName("studentFirstName01")
                .studentLastName("studentLastName01")
                .courseId("courseId02")
                .courseName("courseName02")
                .courseNumber("courseNumber02")
                .build();

        Enrollment enrollment2 = Enrollment.builder()
                .enrollmentId("enrollmentId03")
                .enrollmentYear(2023)
                .semester(SPRING)
                .studentId(studentId)
                .studentFirstName("studentFirstName01")
                .studentLastName("studentLastName01")
                .courseId("courseId03")
                .courseName("courseName03")
                .courseNumber("courseNumber03")
                .build();

        Flux<Enrollment> saveAll = enrollmentRepository.saveAll(Arrays.asList(enrollment1, enrollment2));

        StepVerifier.create(saveAll).expectNextCount(2).verifyComplete();

        // Act
        Flux<Enrollment> getByStudentId = enrollmentRepository.findAllEnrollmentsByStudentId(studentId);

        // Assert
        StepVerifier.create(getByStudentId)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllEnrollmentByCourseId_validId_shouldSucceed() {
        // Arrange
        String courseId = "courseId04";

        Enrollment enrollment1 = Enrollment.builder()
                .enrollmentId("enrollmentId04")
                .enrollmentYear(2023)
                .semester(SPRING)
                .studentId("studentId04")
                .studentFirstName("studentFirstName04")
                .studentLastName("studentLastName04")
                .courseId(courseId)
                .courseName("courseName04")
                .courseNumber("courseNumber04")
                .build();

        Enrollment enrollment2 = Enrollment.builder()
                .enrollmentId("enrollmentId05")
                .enrollmentYear(2023)
                .semester(SPRING)
                .studentId("studentId05")
                .studentFirstName("studentFirstName05")
                .studentLastName("studentLastName05")
                .courseId(courseId)
                .courseName("courseName05")
                .courseNumber("courseNumber05")
                .build();

        Flux<Enrollment> saveAll = enrollmentRepository.saveAll(Arrays.asList(enrollment1, enrollment2));

        StepVerifier.create(saveAll).expectNextCount(2).verifyComplete();

        // Act
        Flux<Enrollment> getByCourseId = enrollmentRepository.findAllEnrollmentsByCourseId(courseId);

        // Assert
        StepVerifier.create(getByCourseId)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllEnrollmentByEnrollmentYear_validYear_shouldSucceed() {
        // Arrange
        Integer enrollmentYear = 2023;

        Enrollment enrollment1 = Enrollment.builder()
                .enrollmentId("enrollmentId06")
                .enrollmentYear(enrollmentYear)
                .semester(SPRING)
                .studentId("studentId06")
                .studentFirstName("studentFirstName06")
                .studentLastName("studentLastName06")
                .courseId("courseId06")
                .courseName("courseName06")
                .courseNumber("courseNumber06")
                .build();

        Enrollment enrollment2 = Enrollment.builder()
                .enrollmentId("enrollmentId07")
                .enrollmentYear(enrollmentYear)
                .semester(SPRING)
                .studentId("studentId07")
                .studentFirstName("studentFirstName07")
                .studentLastName("studentLastName07")
                .courseId("courseId07")
                .courseName("courseName07")
                .courseNumber("courseNumber07")
                .build();

        Flux<Enrollment> saveAll = enrollmentRepository.saveAll(Arrays.asList(enrollment1, enrollment2));

        StepVerifier.create(saveAll).expectNextCount(2).verifyComplete();

        // Act
        Flux<Enrollment> getByEnrollmentYear = enrollmentRepository.findAllEnrollmentsByEnrollmentYear(enrollmentYear);

        // Assert
        StepVerifier.create(getByEnrollmentYear)
                .expectNextCount(2)
                .verifyComplete();
    }

}
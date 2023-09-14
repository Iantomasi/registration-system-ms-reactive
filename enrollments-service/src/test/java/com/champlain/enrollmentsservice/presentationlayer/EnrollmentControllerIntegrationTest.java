package com.champlain.enrollmentsservice.presentationlayer;
import static org.junit.jupiter.api.Assertions.*;

import com.champlain.enrollmentsservice.businesslayer.EnrollmentService;
import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.domainclientlayer.CourseClient;
import com.champlain.enrollmentsservice.domainclientlayer.CourseResponseDTO;
import com.champlain.enrollmentsservice.domainclientlayer.StudentClient;
import com.champlain.enrollmentsservice.domainclientlayer.StudentResponseDTO;
import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.champlain.enrollmentsservice.dataaccesslayer.Semester.FALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient

class EnrollmentControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private StudentClient studentClient;

    @MockBean
    private CourseClient courseClient;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    EnrollmentService enrollmentService;

    private final Long DB_SIZE = 5L;


    StudentResponseDTO studentResponseDTO = StudentResponseDTO.builder()
            .studentId("student123")
            .firstName("fistName")
            .lastName("lastName")
            .program("History")
            .build();

    CourseResponseDTO courseResponseDTO = CourseResponseDTO.builder()
            .courseId("course123")
            .courseName("Web Services")
            .courseNumber("420-N45-LA")
            .department("Computer Science")
            .numCredits(2.0)
            .numHours(60)
            .build();

    EnrollmentRequestDTO enrollmentRequestDTO = EnrollmentRequestDTO.builder()
            .enrollmentYear(2023)
            .semester(FALL)
            .studentId(studentResponseDTO.getStudentId())
            .courseId(courseResponseDTO.getCourseId())
            .build();

    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    String uuid4 = UUID.randomUUID().toString();
    String uuid5 = UUID.randomUUID().toString();

    Enrollment enrollment1 = buildEnrollment(2022,uuid1);
    Enrollment enrollment2 = buildEnrollment(2023,uuid2);
    Enrollment enrollment3 = buildEnrollment(2024,uuid3);
    Enrollment enrollment4 = buildEnrollment(2025,uuid4);
    Enrollment enrollment5 = buildEnrollment(2026,uuid5);

    @BeforeEach
    public void dbSetUp(){

        Publisher<Enrollment> setup = enrollmentRepository.deleteAll()
                .thenMany(enrollmentRepository.save(enrollment1))
                .thenMany(enrollmentRepository.save(enrollment2))
                .thenMany(enrollmentRepository.save(enrollment3))
                .thenMany(enrollmentRepository.save(enrollment4))
                .thenMany(enrollmentRepository.save(enrollment5));

        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllEnrollments_expected5(){
        webTestClient.get()
                .uri("/enrollments")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(StudentResponseDTO.class).value((list) -> {
                    assertNotNull(list);
                    assertEquals(DB_SIZE, list.size());});
    }

    @Test
    public void getEnrollmentByEnrollmentId_validId_shouldSucceed(){
        webTestClient.get()
                .uri("/enrollments/{enrollmentId}", enrollment1.getEnrollmentId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.enrollmentId").isEqualTo(enrollment1.getEnrollmentId());
    }

    @Test
    public void getEnrollmentByEnrollmentId_invalidId_throwsNotFoundException(){
        UUID uuidTest= UUID.randomUUID();
        webTestClient.get()
                .uri("/enrollments/{enrollmentId}", uuidTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("No enrollment with this enrollmentId was found: " + uuidTest);
    }

    @Test
    void getAllEnrollmentsByStudentId_validId_shouldSucceed() {
        String studentId = "123";
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments")
                        .queryParam("studentId", studentId)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(EnrollmentResponseDTO.class)
                .consumeWith(response -> {
                    Flux<EnrollmentResponseDTO> responseBody = Flux.fromIterable(response.getResponseBody());
                    StepVerifier.create(responseBody)
                            .expectNextCount(0)
                            .thenConsumeWhile(enrollment -> enrollment.getStudentId().equals(studentId))
                            .expectComplete()
                            .verify();
                });
    }

    @Test
    void getAllEnrollmentsByCourseId_validId_shouldSucceed() {
        String courseId = "course123";
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments")
                        .queryParam("courseId", courseId)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(EnrollmentResponseDTO.class)
                .consumeWith(response -> {
                    Flux<EnrollmentResponseDTO> responseBody = Flux.fromIterable(response.getResponseBody());
                    StepVerifier.create(responseBody)
                            .expectNextCount(2)
                            .thenConsumeWhile(enrollment -> enrollment.getCourseId().equals(courseId))
                            .expectComplete()
                            .verify();
                });
    }

    @Test
    void getAllEnrollmentsByEnrollmentYear_validYear_shouldSucceed() {
        String enrollmentYear = "2023";
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments")
                        .queryParam("enrollmentYear", enrollmentYear)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(EnrollmentResponseDTO.class)
                .consumeWith(response -> {
                    Flux<EnrollmentResponseDTO> responseBody = Flux.fromIterable(response.getResponseBody());

                    StepVerifier.create(responseBody)
                            .expectNextCount(1)
                            .thenConsumeWhile(enrollment -> enrollment.getEnrollmentYear().equals(enrollmentYear))
                            .expectComplete()
                            .verify();
                });
    }

    @Test
    public void getEnrollmentByEnrollmentId_invalidId_throwsInvalidInputException(){
        String invalidIdTest = "12345";
        webTestClient.get()
                .uri("/enrollments/{enrollmentId}", invalidIdTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid enrollmentId, length must be 36 characters");
    }


    @Test
    void addEnrollment_validRequest_shouldSucceed() {
        //arrange
        when(studentClient.getStudentByStudentId(enrollmentRequestDTO.
                getStudentId()))
                .thenReturn(Mono.just(studentResponseDTO));

        when(courseClient.getCourseByCourseId(enrollmentRequestDTO.getCourseId()))
                .thenReturn(Mono.just(courseResponseDTO));

        //act and assert
        webTestClient
                .post()
                .uri("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(enrollmentRequestDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseDTO.class)
                .value((enrollmentResponseDTO) -> {
                    assertNotNull(enrollmentResponseDTO);
                    assertNotNull(enrollmentResponseDTO.getEnrollmentId());
                    assertEquals(enrollmentRequestDTO.getEnrollmentYear(), enrollmentResponseDTO.getEnrollmentYear());
                    assertEquals(enrollmentRequestDTO.getSemester(), enrollmentResponseDTO.getSemester());
                    assertEquals(enrollmentRequestDTO.getStudentId(), enrollmentResponseDTO.getStudentId());
                    assertEquals(studentResponseDTO.getFirstName(), enrollmentResponseDTO.getStudentFirstName());
                    assertEquals(studentResponseDTO.getLastName(), enrollmentResponseDTO.getStudentLastName());
                    assertEquals(enrollmentRequestDTO.getCourseId(), enrollmentResponseDTO.getCourseId());
                    assertEquals(courseResponseDTO.getCourseName(), enrollmentResponseDTO.getCourseName());
                    assertEquals(courseResponseDTO.getCourseNumber(), enrollmentResponseDTO.getCourseNumber());
                });

    }

    @Test
    public void updateEnrollment_validId_shouldSucceed() {

        EnrollmentRequestDTO enrollmentRequestDTO = EnrollmentRequestDTO.builder()
                .enrollmentYear(2023)
                .semester(FALL)
                .studentId(studentResponseDTO.getStudentId())
                .courseId(courseResponseDTO.getCourseId())
                .build();

        when(studentClient.getStudentByStudentId(enrollmentRequestDTO.
                getStudentId()))
                .thenReturn(Mono.just(studentResponseDTO));

        when(courseClient.getCourseByCourseId(enrollmentRequestDTO.getCourseId()))
                .thenReturn(Mono.just(courseResponseDTO));

        webTestClient.put()
                .uri("/enrollments/{enrollmentId}", enrollment2.getEnrollmentId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(enrollmentRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseDTO.class)
                .value((enrollmentResponseDTO -> {
                    assertNotNull(enrollmentResponseDTO);
                    assertNotNull(enrollmentResponseDTO.getEnrollmentId());
                    assertEquals(enrollmentRequestDTO.getEnrollmentYear(), enrollmentResponseDTO.getEnrollmentYear());
                    assertEquals(enrollmentRequestDTO.getSemester(), enrollmentResponseDTO.getSemester());
                    assertEquals(enrollmentRequestDTO.getStudentId(), enrollmentResponseDTO.getStudentId());
                    assertEquals(studentResponseDTO.getFirstName(), enrollmentResponseDTO.getStudentFirstName());
                    assertEquals(studentResponseDTO.getLastName(), enrollmentResponseDTO.getStudentLastName());
                    assertEquals(enrollmentRequestDTO.getCourseId(), enrollmentResponseDTO.getCourseId());
                    assertEquals(courseResponseDTO.getCourseName(), enrollmentResponseDTO.getCourseName());
                    assertEquals(courseResponseDTO.getCourseNumber(), enrollmentResponseDTO.getCourseNumber());
                }));;
    }


    @Test
    public void updateEnrollment_invalidId_shouldThrowInvalidInputException() {
        String invalidEnrollmentId = "invalidId";

        EnrollmentRequestDTO enrollmentRequestDTO = EnrollmentRequestDTO.builder()
                .enrollmentYear(2023)
                .semester(FALL)
                .studentId(studentResponseDTO.getStudentId())
                .courseId(courseResponseDTO.getCourseId())
                .build();

        // Mock student and course clients
        when(studentClient.getStudentByStudentId(enrollmentRequestDTO.getStudentId()))
                .thenReturn(Mono.just(studentResponseDTO));

        when(courseClient.getCourseByCourseId(enrollmentRequestDTO.getCourseId()))
                .thenReturn(Mono.just(courseResponseDTO));


        Mono<EnrollmentResponseDTO> resultMono = enrollmentService.updateEnrollment(
                Mono.just(enrollmentRequestDTO),
                invalidEnrollmentId
        );

        StepVerifier.create(resultMono)
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    public void deleteEnrollmentById_validId_shouldSucceed(){
        webTestClient.delete()
                .uri("/enrollments/{enrollmentId}", enrollment3.getEnrollmentId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteEnrollmentById_badRequest_throwsInvalidInputException(){
        String invalidIdTest = "12345";
        webTestClient.delete()
                .uri("/enrollments/{enrollmentId}",invalidIdTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid enrollmentId, length must be 36 characters");
    }


    private Enrollment buildEnrollment(Integer enrollmentYear, String enrollmentId){
        return Enrollment.builder()
                .enrollmentId(enrollmentId)
                .enrollmentYear(enrollmentYear)
                .semester(FALL)
                .studentId(studentResponseDTO.getStudentId())
                .courseId(courseResponseDTO.getCourseId())
                .build();
    }

}
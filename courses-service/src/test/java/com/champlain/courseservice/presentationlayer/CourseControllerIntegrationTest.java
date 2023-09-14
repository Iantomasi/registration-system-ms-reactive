package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port= 0"})
@AutoConfigureWebTestClient
class CourseControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    CourseRepository courseRepository;

    private final Long DB_SIZE = 5L;

    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    String uuid4 = UUID.randomUUID().toString();
    String uuid5 = UUID.randomUUID().toString();


    Course course1 = buildCourse("courseName01", uuid1);
    Course course2 = buildCourse("courseName02", uuid2);
    Course course3 = buildCourse("courseName03", uuid3);
    Course course4 = buildCourse("courseName04", uuid4);
    Course course5 = buildCourse("courseName05", uuid5);


    @BeforeEach
    public void dbSetUp(){

        Publisher<Course> setup = courseRepository.deleteAll()
                .thenMany(courseRepository.save(course1))
                .thenMany(courseRepository.save(course2))
                .thenMany(courseRepository.save(course3))
                .thenMany(courseRepository.save(course4))
                .thenMany(courseRepository.save(course5));

        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllCourses_expected5(){
        webTestClient.get()
                .uri("/courses")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(CourseResponseDTO.class).value((list) -> {
                    assertNotNull(list);
                    assertEquals(DB_SIZE, list.size());});
    }

    @Test
    public void getCourseByCourseId_validId_shouldSucceed(){
        webTestClient.get()
                .uri("/courses/{courseId}", course1.getCourseId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.courseId").isEqualTo(course1.getCourseId());
    }

    @Test
    public void getCourseByCourseId_invalidId_throwsNotFoundException(){
        UUID uuidTest= UUID.randomUUID();
        webTestClient.get()
                .uri("/courses/{courseId}", uuidTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("No course with this courseId was found: " + uuidTest);
    }

    @Test
    public void getCourseByCourseId_invalidId_throwsInvalidInputException(){
        String invalidIdTest = "invalidCourseId";
        webTestClient.get()
                .uri("/courses/{courseId}", invalidIdTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid courseId, length must be 36 characters");
    }

    @Test
    public void addCourse_validRequest_shouldSucceed(){
        CourseRequestDTO courseRequestDTO = CourseRequestDTO.builder()
                .courseName("validCourseName")
                .courseNumber("validCourseNumber")
                .numHours(25)
                .numCredits(2.5)
                .department("validDepartment")
                .build();

        webTestClient.post()
                .uri("/courses")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CourseResponseDTO.class)
                .value(courseResponseDTO -> {
                    assertNotNull(courseResponseDTO);
                    assertNotNull(courseResponseDTO.getCourseId());
                    assertThat(courseResponseDTO.getCourseName()).isEqualTo(courseResponseDTO.getCourseName());
                    assertThat(courseResponseDTO.getCourseNumber()).isEqualTo(courseResponseDTO.getCourseNumber());
                    assertThat(courseResponseDTO.getDepartment()).isEqualTo(courseResponseDTO.getDepartment());
                });
    }

    @Test
    public void updateCourse_validId_shouldSucceed() {
        String validCourseNumber = "validCourseNumber";

        CourseRequestDTO courseRequestDTO = CourseRequestDTO.builder()
                .courseName(course2.getCourseName())
                .courseNumber(validCourseNumber)
                .department(course2.getDepartment()).build();

        webTestClient.put()
                .uri("/courses/{courseId}", course2.getCourseId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(CourseResponseDTO.class)
                .value((courseResponseDTO -> {
                    assertNotNull(courseResponseDTO);
                    assertThat(courseResponseDTO.getCourseId()).isEqualTo(course2.getCourseId());
                    assertThat(courseResponseDTO.getCourseName()).isEqualTo(course2.getCourseName());
                    assertThat(courseResponseDTO.getCourseNumber()).isEqualTo(validCourseNumber);
                    assertThat(courseResponseDTO.getDepartment()).isEqualTo(course2.getDepartment());
                }));;
    }

    @Test
    public void deleteCourseById_validId_shouldSucceed(){
        webTestClient.delete()
                .uri("/courses/{courseId}", course1.getCourseId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteCourse_invalidId_throwsInvalidInputException(){
        String invalidIdTest = "invalidCourseId";
        webTestClient.delete()
                .uri("/courses/{courseId}",invalidIdTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid courseId, length must be 36 characters");
    }

    private Course buildCourse(String courseName, String courseId){
        return Course.builder()
                .courseName(courseName)
                .department("CDEPARTMENT")
                .courseId(courseId)
                .build();
    }
}
package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.presentationlayer.CourseRequestDTO;
import com.champlain.courseservice.presentationlayer.CourseResponseDTO;
import com.champlain.courseservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class CourseServiceUnitTest {

    @Autowired
    CourseService courseService;

    @MockBean
    private CourseRepository courseRepository;

    String uuid1 = UUID.randomUUID().toString();

    Course course = Course.builder()
            .courseId(uuid1)
            .courseName("Final Project 1")
            .courseNumber("420-N45-LA")
            .department("Computer Science")
            .numCredits(2.0)
            .numHours(60)
            .build();

    @Test
    void getAllCourses_shouldSucceed() {
        //arrange
        when(courseRepository.findAll())
                .thenReturn(Flux.just(course));

        //act
        Flux<CourseResponseDTO> courseResponseDTOFlux = courseService
                .getAllCourses();

        //assert
        StepVerifier
                .create(courseResponseDTOFlux)
                .consumeNextWith(foundCourse -> {
                    assertNotNull(foundCourse);
                    assertEquals(course.getCourseId(), foundCourse.getCourseId());
                    assertEquals(course.getCourseName(), foundCourse.getCourseName());
                    assertEquals(course.getCourseNumber(), foundCourse.getCourseNumber());
                    assertEquals(course.getDepartment(), foundCourse.getDepartment());
                    assertEquals(course.getNumCredits(), foundCourse.getNumCredits());
                    assertEquals(course.getNumHours(), foundCourse.getNumHours());
                })
                .verifyComplete();
    }

    @Test
    void getCourseByCourseId_validId_shouldSucceed(){
        //arrange
        when(courseRepository.findCourseByCourseId(anyString()))
                .thenReturn(Mono.just(course));

        //act
        Mono<CourseResponseDTO> courseResponseDTOMono = courseService
                .getCourseById(course.getCourseId());

        //assert
        StepVerifier
                .create(courseResponseDTOMono)
                .consumeNextWith(foundCourse ->{
                    assertNotNull(foundCourse);
                    assertEquals(course.getCourseId(), foundCourse.getCourseId());
                    assertEquals(course.getCourseName(), foundCourse.getCourseName());
                    assertEquals(course.getCourseNumber(), foundCourse.getCourseNumber());
                    assertEquals(course.getDepartment(), foundCourse.getDepartment());
                    assertEquals(course.getNumCredits(), foundCourse.getNumCredits());
                    assertEquals(course.getNumHours(), foundCourse.getNumHours());
                })
                .verifyComplete();
    }


    @Test
    void addCourse_validRequest_shouldSucceed() {
        //arrange
        CourseRequestDTO courseRequestDTO = CourseRequestDTO.builder()
                .courseName("Final Project 1")
                .courseNumber("420-N45-LA")
                .department("Computer Science")
                .numCredits(2.0)
                .numHours(60)
                .build();

        Course courseEntity = Course.builder()
                .courseId(uuid1)
                .courseName("Final Project 1")
                .courseNumber("420-N45-LA")
                .department("Computer Science")
                .numCredits(2.0)
                .numHours(60)
                .build();

        when(courseRepository.insert(any(Course.class)))
                .thenReturn(Mono.just(courseEntity));

        //act
        Mono<CourseResponseDTO> courseResponseDTO = courseService.addCourse(Mono.just(courseRequestDTO));

        //assert
        StepVerifier
                .create(courseResponseDTO)
                .expectNextMatches(foundCourse -> {
                    assertNotNull(foundCourse);
                    assertEquals(course.getCourseId(), foundCourse.getCourseId());
                    assertEquals(course.getCourseName(), foundCourse.getCourseName());
                    assertEquals(course.getCourseNumber(), foundCourse.getCourseNumber());
                    assertEquals(course.getDepartment(), foundCourse.getDepartment());
                    assertEquals(course.getNumCredits(), foundCourse.getNumCredits());
                    assertEquals(course.getNumHours(), foundCourse.getNumHours());

                    return true;
                })
                .verifyComplete();
    }


    @Test
    void updateCourseById_validRequest_shouldSucceed() {
        //arrange
        String validCourseId = uuid1;
        CourseRequestDTO courseRequestDTO = CourseRequestDTO.builder()
                .build();

        Course existingCourse = Course.builder()
                .courseId(validCourseId)
                .id("123")
                .build();

        Course updatedCourseEntity = Course.builder()
                .build();

        when(courseRepository.findCourseByCourseId(validCourseId)).thenReturn(Mono.just(existingCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(Mono.just(updatedCourseEntity));

        //act and assert
        StepVerifier
                .create(courseService.updateCourseById(Mono.just(courseRequestDTO), validCourseId))
                .expectNextMatches(updatedCourse -> { return true; })
                .verifyComplete();
    }


    @Test
    void updateCourse_invalidId_shouldThrowInvalidInputException() {
        //arrange
        String invalidCourseId = "invalidId";
        CourseRequestDTO courseRequestDTO = CourseRequestDTO.builder().build();

        //act and assert
        StepVerifier
                .create(courseService.updateCourseById(Mono.just(courseRequestDTO), invalidCourseId))
                .expectErrorMatches(exception -> exception instanceof InvalidInputException &&
                        exception.getMessage().contains("Invalid courseId, length must be 36 characters"))
                .verify();
    }

    @Test
    void deleteCourse_validId_shouldSucceed() {
        //arrange
        String courseIdToDelete = uuid1;

        when(courseRepository.delete(any(Course.class)))
                .thenReturn(Mono.empty());

        when(courseRepository.findCourseByCourseId(courseIdToDelete))
                .thenReturn(Mono.just(new Course()));

        when(courseRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        //act
        Mono<Void> courseDelete = courseService.deleteCourseById(courseIdToDelete);

        //assert
        StepVerifier
                .create(courseDelete)
                .verifyComplete();
    }


}
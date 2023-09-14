package com.champlain.courseservice.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class CourseRepositoryIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    Course course1;
    Course course2;

    @BeforeEach()
    public void setUp(){

        course1 = buildCourse("courseName01", "courseId01");

        //arrange
        Publisher<Course> setUp1 = courseRepository.deleteAll()
                .thenMany(courseRepository.save(course1));

        StepVerifier.create(setUp1).expectNextCount(1).verifyComplete();


        course2 = buildCourse("courseName02", "courseId02");

        Publisher<Course> setUp2 = courseRepository.save(course2);

        //act & assert

        StepVerifier.create(setUp2)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    public void addCourse_shouldCreateOne(){
        //arrange
        Course course = buildCourse("courseName03", "courseId03");

        Publisher<Course> setUp = courseRepository.save(course);

        //act & assert
        StepVerifier.create(setUp)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void getAllCourse_shouldReturnAll(){
        //arrange
        Publisher<Course> setUp = courseRepository.findAll();
        //act & assert
        StepVerifier.create(setUp)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void getCourseByCourseId_validId_shouldReturnOne(){
        //arrange
        Publisher<Course> setUp = courseRepository.findCourseByCourseId(course2.getCourseId());

        //act and assert
        StepVerifier.create(setUp)
                .assertNext(course ->{
                    assertThat(course.getCourseId()).isEqualTo(course2.getCourseId());
                    assertThat(course.getCourseName()).isEqualTo(course2.getCourseName());
                    assertThat(course.getDepartment()).isEqualTo(course2.getDepartment());
                })
                .verifyComplete();
    }

    @Test
    public void getCourseByCourseId_invalidId_shouldReturnNone(){
        //act and assert
        StepVerifier.create(courseRepository.findCourseByCourseId("fakeId")).expectNextCount(0).verifyComplete();
    }

    private Course buildCourse(String courseName, String courseId){
        return Course.builder()
                .courseName(courseName)
                .department("CDEPARTMENT")
                .courseId(courseId)
                .build();
    }

}
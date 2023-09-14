package com.champlain.studentsservice.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class StudentRepositoryIntegrationTest {


    @Autowired
    private StudentRepository studentRepository;

    Student student1;
    Student student2;

    @BeforeEach()
    public void setUp(){

        student1 = buildStudent("lastName01", "studentId01");

        Publisher<Student> setUp1 = studentRepository.deleteAll()
                .thenMany(studentRepository.save(student1));

        StepVerifier.create(setUp1).expectNextCount(1).verifyComplete();


        student2 = buildStudent("lastName02", "studentId02");

        Publisher<Student> setUp2 = studentRepository.save(student2);

        StepVerifier.create(setUp2)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    public void addStudent_shouldCreateOne(){
        //arrange
        Student student = buildStudent("lastName03", "studentId03");

        Publisher<Student> setUp = studentRepository.save(student);

        //act & assert
        StepVerifier.create(setUp)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void getAllStudents_shouldReturnAll(){
        //arrange
        Publisher<Student> setUp = studentRepository.findAll();
        //act & assert
        StepVerifier.create(setUp)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void getStudentByStudentId_validID_shouldReturnOne(){
        //arrange
        Publisher<Student> setUp = studentRepository.findStudentByStudentId(student2.getStudentId());

        //act and assert
        StepVerifier.create(setUp)
                .assertNext(student ->{
                    assertThat(student.getStudentId()).isEqualTo(student2.getStudentId());
                    assertThat(student.getLastName()).isEqualTo(student2.getLastName());
                    assertThat(student.getFirstName()).isEqualTo(student2.getFirstName());
                })
                .verifyComplete();
    }

    @Test
    public void getStudentByStudentId_invalidId_shouldReturnNone(){
        StepVerifier.create(studentRepository.findStudentByStudentId("studentId04")).expectNextCount(0).verifyComplete();
    }

    private Student buildStudent(String lastName, String studentId){
        return Student.builder()
                .lastName(lastName)
                .firstName("FNAME")
                .studentId(studentId)
                .build();
    }







}
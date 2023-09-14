package com.champlain.studentsservice.businesslayer;
import com.champlain.studentsservice.dataaccesslayer.Student;
import com.champlain.studentsservice.dataaccesslayer.StudentRepository;
import com.champlain.studentsservice.presentationlayer.StudentRequestDTO;
import com.champlain.studentsservice.presentationlayer.StudentResponseDTO;
import com.champlain.studentsservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@SpringBootTest
class StudentServiceUnitTest {

    @Autowired
    StudentService studentService;

    @MockBean
    private StudentRepository studentRepository;

    String uuid1 = UUID.randomUUID().toString();

    Student student = Student.builder()
            .studentId(uuid1)
            .firstName("studentFirstName02")
            .lastName("studentLastName02")
            .program("program02")
            .build();


    @Test
    void getAllStudents_shouldSucceed() {
        // Arrange
        when(studentRepository.findAll())
                .thenReturn(Flux.just(student));

        // Act
        Flux<StudentResponseDTO> studentResponseDTOFlux = studentService
                .getAllStudents();

        // Assert
        StepVerifier
                .create(studentResponseDTOFlux)
                .consumeNextWith(foundStudent -> {
                    assertNotNull(foundStudent);
                    assertEquals(student.getStudentId(), foundStudent.getStudentId());
                    assertEquals(student.getFirstName(), foundStudent.getFirstName());
                    assertEquals(student.getLastName(), foundStudent.getLastName());
                    assertEquals(student.getProgram(), foundStudent.getProgram());
                })
                .verifyComplete();
    }

    @Test
    void getStudentByStudentId_validId_shouldSucceed(){
        //arrange
        when(studentRepository.findStudentByStudentId(anyString()))
                .thenReturn(Mono.just(student));

        //act
        Mono<StudentResponseDTO> studentResponseDTOMono = studentService
                .getStudentById(student.getStudentId());

        //assert
        StepVerifier
                .create(studentResponseDTOMono)
                .consumeNextWith(foundStudent ->{
                    assertNotNull(foundStudent);
                    assertEquals(student.getStudentId(), foundStudent.getStudentId());
                    assertEquals(student.getFirstName(), foundStudent.getFirstName());
                    assertEquals(student.getLastName(), foundStudent.getLastName());
                    assertEquals(student.getProgram(), foundStudent.getProgram());
                })
                .verifyComplete();
    }
    @Test
    void addStudent_validRequest_shouldSucceed() {
        // Arrange
        StudentRequestDTO studentRequestDTO = StudentRequestDTO.builder()
                .firstName("studentFirstName03")
                .lastName("studentLastName03")
                .program("program03")
                .build();

        Student student = Student.builder()
                .studentId(uuid1)
                .firstName("studentFirstName03")
                .lastName("studentLastName03")
                .program("program03")
                .build();

        when(studentRepository.insert(any(Student.class)))
                .thenReturn(Mono.just(student));

        // Act
        Mono<StudentResponseDTO> studentResponseDTOMono = studentService.addStudent(Mono.just(studentRequestDTO));

        // Assert
        StepVerifier
                .create(studentResponseDTOMono)
                .expectNextMatches(foundStudent -> {
                    assertNotNull(foundStudent);
                    assertEquals(student.getStudentId(), foundStudent.getStudentId());
                    assertEquals(student.getFirstName(), foundStudent.getFirstName());
                    assertEquals(student.getLastName(), foundStudent.getLastName());
                    assertEquals(student.getProgram(), foundStudent.getProgram());

                    return true;
                })
                .verifyComplete();
    }

    @Test
    void updateStudentById_validRequest_shouldSucceed() {
        // Arrange
        String validStudentId = uuid1;
        StudentRequestDTO studentRequestDTO = StudentRequestDTO.builder()
                .build();

        Student existingStudent = Student.builder()
                .studentId(uuid1)
                .id("123")
                .build();

        Student updatedStudentEntity = Student.builder()
                .build();

        when(studentRepository.findStudentByStudentId(validStudentId)).thenReturn(Mono.just(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(Mono.just(updatedStudentEntity));

        // Act and Assert
        StepVerifier
                .create(studentService.updateStudentById(Mono.just(studentRequestDTO), validStudentId))
                .expectNextMatches(updatedStudent -> {
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void updateStudent_invalidId_shouldThrowInvalidInputException() {
        // Arrange
        String invalidStudentId = "invalidId";
        StudentRequestDTO studentRequestDTO = StudentRequestDTO.builder().build();

        // Act and Assert
        StepVerifier
                .create(studentService.updateStudentById(Mono.just(studentRequestDTO), invalidStudentId))
                .expectErrorMatches(exception -> exception instanceof InvalidInputException &&
                        exception.getMessage().contains("Invalid studentId, length must be 36 characters"))
                .verify();
    }




    @Test
    void deleteStudentById_validId_shouldSucceed() {
        // Arrange
        String studentIdToDelete = uuid1;

        when(studentRepository.delete(any(Student.class)))
                .thenReturn(Mono.empty());

        when(studentRepository.findStudentByStudentId(studentIdToDelete))
                .thenReturn(Mono.just(new Student()));

        when(studentRepository.deleteById(anyString()))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> studentDelete = studentService.deleteStudentById(studentIdToDelete);

        // Assert
        StepVerifier
                .create(studentDelete)
                .verifyComplete();
    }

}
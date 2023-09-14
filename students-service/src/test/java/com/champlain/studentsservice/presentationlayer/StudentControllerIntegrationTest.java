package com.champlain.studentsservice.presentationlayer;

import com.champlain.studentsservice.dataaccesslayer.Student;
import com.champlain.studentsservice.dataaccesslayer.StudentRepository;
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
class StudentControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    StudentRepository studentRepository;


    private final Long DB_SIZE = 5L;

    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    String uuid4 = UUID.randomUUID().toString();
    String uuid5 = UUID.randomUUID().toString();


    Student student1 = buildStudent("Seinfeld", uuid1);
    Student student2 = buildStudent("Jerry", uuid2);
    Student student3 = buildStudent("Ronaldo", uuid3);
    Student student4 = buildStudent("Cristiano", uuid4);
    Student student5 = buildStudent("Mbappe", uuid5);


    @BeforeEach
    public void dbSetUp(){

        Publisher<Student> setup = studentRepository.deleteAll()
                .thenMany(studentRepository.save(student1))
                .thenMany(studentRepository.save(student2))
                .thenMany(studentRepository.save(student3))
                .thenMany(studentRepository.save(student4))
                .thenMany(studentRepository.save(student5));

        StepVerifier.create(setup).expectNextCount(1).verifyComplete();
    }

    @Test
    void getAllStudents_expected5(){
        webTestClient.get()
                .uri("/students")
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
    public void getStudentByStudentId_validId_shouldSucceed(){
        webTestClient.get()
                .uri("/students/{studentId}", student1.getStudentId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.studentId").isEqualTo(student1.getStudentId());
    }

    @Test
    public void getStudentByStudentId_invalidId_throwsNotFoundException(){
        UUID uuidTest= UUID.randomUUID();
        webTestClient.get()
                .uri("/students/{studentId}", uuidTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("No student with this studentId was found: " + uuidTest);
    }

    @Test
    public void getStudentByStudentId_invalidId_throwsInvalidInputException(){
        String invalidIdTest = "1234";
        webTestClient.get()
                .uri("/students/{studentId}", invalidIdTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid studentId, length must be 36 characters");
    }

    @Test
    public void addNewStudent_validRequest_shouldSucceed(){
        StudentRequestDTO studentRequestDTO = StudentRequestDTO.builder()
                .firstName("Giuliano")
                .lastName("Iantomasi")
                .program("STEM")
                .build();

        webTestClient.post()
                .uri("/students")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(studentRequestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(StudentResponseDTO.class)
                .value(studentResponseDTO -> {
                    assertNotNull(studentResponseDTO);
                    assertNotNull(studentResponseDTO.getStudentId());
                    assertThat(studentResponseDTO.getFirstName()).isEqualTo(studentResponseDTO.getFirstName());
                    assertThat(studentResponseDTO.getLastName()).isEqualTo(studentResponseDTO.getLastName());
                    assertThat(studentResponseDTO.getProgram()).isEqualTo(studentResponseDTO.getProgram());
                });
    }

    @Test
    public void updateStudent_validId_shouldSucceed() {
        String validLastName = "Giuliano";

        StudentRequestDTO studentRequestDTO = StudentRequestDTO.builder()
                .firstName(student2.getFirstName())
                .lastName(validLastName)
                .program(student2.getProgram()).build();

        webTestClient.put()
                .uri("/students/{studentId}",student2.getStudentId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(studentRequestDTO)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody(StudentResponseDTO.class)
                .value((studentResponseDTO -> {
                    assertNotNull(studentResponseDTO);
                    assertThat(studentResponseDTO.getStudentId()).isEqualTo(student2.getStudentId());
                    assertThat(studentResponseDTO.getFirstName()).isEqualTo(student2.getFirstName());
                    assertThat(studentResponseDTO.getLastName()).isEqualTo(validLastName);
                    assertThat(studentResponseDTO.getProgram()).isEqualTo(student2.getProgram());
                }));;
    }


    @Test
    public void deleteStudent_validId_shouldSucceed(){
        webTestClient.delete()
                .uri("/students/{studentId}",student1.getStudentId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteStudent_badRequest_throwsInvalidInputException(){
        String invalidIdTest = "1234";
        webTestClient.delete()
                .uri("/students/{studentId}",invalidIdTest)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid studentId, length must be 36 characters");
    }

    private Student buildStudent(String lastName, String studentId) {
        return Student.builder()
                .lastName(lastName)
                .firstName("FNAME")
                .studentId(studentId)
                .build();
    }
}
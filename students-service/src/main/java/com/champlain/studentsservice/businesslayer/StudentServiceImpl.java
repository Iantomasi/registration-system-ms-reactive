package com.champlain.studentsservice.businesslayer;

import com.champlain.studentsservice.dataaccesslayer.StudentRepository;
import com.champlain.studentsservice.presentationlayer.StudentRequestDTO;
import com.champlain.studentsservice.presentationlayer.StudentResponseDTO;
import com.champlain.studentsservice.utils.EntityDTOUtils;
import com.champlain.studentsservice.utils.exceptions.InvalidInputException;
import com.champlain.studentsservice.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService{

    private final StudentRepository studentRepository;

    @Override
    public Flux<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll()
                .map(EntityDTOUtils::toStudentResponseDTO);

    }

    @Override
    public Mono<StudentResponseDTO> getStudentById(String studentId) {
        if(studentId.length() != 36){
            return Mono.error(new InvalidInputException("Invalid studentId, length must be 36 characters"));
        }
        return studentRepository.findStudentByStudentId(studentId)
                .switchIfEmpty(Mono.error(new NotFoundException("No student with this studentId was found: " + studentId)))
                .map(EntityDTOUtils::toStudentResponseDTO);
    }

    @Override
    public Mono<StudentResponseDTO> addStudent(Mono<StudentRequestDTO> studentRequestDTO) {
        return studentRequestDTO
                .map(EntityDTOUtils::toStudentEntity)
                .doOnNext(e -> e.setStudentId(EntityDTOUtils.generateUUIDString()))
                .flatMap(studentRepository::insert)
                .map(EntityDTOUtils::toStudentResponseDTO);
    }

    @Override
    public Mono<StudentResponseDTO> updateStudentById(Mono<StudentRequestDTO> studentRequestDTO,String studentId) {
        if(studentId.length() != 36){
            return Mono.error(new InvalidInputException("Invalid studentId, length must be 36 characters"));
        }
        return studentRepository.findStudentByStudentId(studentId)
                .switchIfEmpty(Mono.error(new NotFoundException("No student with this studentId was found: " + studentId)))
                .flatMap(student ->
                        studentRequestDTO
                                .map(EntityDTOUtils::toStudentEntity)
                                .doOnNext(e -> {
                                    e.setStudentId(student.getStudentId());
                                    e.setId(student.getId());
                                }))
                .flatMap(studentRepository::save)
                .map(EntityDTOUtils::toStudentResponseDTO);
    }

    @Override
    public Mono<Void> deleteStudentById(String studentId) {
        if(studentId.length() != 36){
            return Mono.error(new InvalidInputException("Invalid studentId, length must be 36 characters"));
        }
        return studentRepository.findStudentByStudentId(studentId)
                .switchIfEmpty(Mono.error(new NotFoundException("No student with this studentId was found: " + studentId)))
                .flatMap(studentRepository::delete);
    }

}

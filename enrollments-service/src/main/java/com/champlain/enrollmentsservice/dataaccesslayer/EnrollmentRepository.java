package com.champlain.enrollmentsservice.dataaccesslayer;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrollmentRepository extends ReactiveCrudRepository<Enrollment, Integer> {

    Mono<Enrollment> findEnrollmentByEnrollmentId(String enrollmentId);

    Flux<Enrollment> findAllEnrollmentsByStudentId(String studentId);


    Flux<Enrollment> findAllEnrollmentsByCourseId(String courseId);


    Flux<Enrollment> findAllEnrollmentsByEnrollmentYear(Integer enrollmentYear);


}

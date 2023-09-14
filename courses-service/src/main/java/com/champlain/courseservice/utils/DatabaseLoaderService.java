package com.champlain.courseservice.utils;


import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DatabaseLoaderService implements CommandLineRunner {

    @Autowired
    CourseRepository courseRepository;

    @Override
    public void run(String... args) throws Exception{
        Course course1 = Course
                .builder()
                .courseId(UUID.randomUUID().toString())
                .courseNumber("G21")
                .courseName("CST")
                .department("Department")
                .numHours(70)
                .numCredits(5.00)
                .build();

        Course course2 = Course
                .builder()
                .courseId(UUID.randomUUID().toString())
                .courseNumber("I99")
                .courseName("Business")
                .department("Department")
                .numHours(40)
                .numCredits(3.00)
                .build();




        Flux.just(course1, course2)
                .flatMap(c -> courseRepository.insert(Mono.just(c))
                        .log(c.toString())).subscribe();



    }

}

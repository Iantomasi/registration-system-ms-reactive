package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

@RestController
@RequestMapping("courses")
@Slf4j
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CourseResponseDTO> getAllCourses(){
        return courseService.getAllCourses();
    }

    @GetMapping("/{courseId}")
    public Mono<ResponseEntity<CourseResponseDTO>> getCourseById(@PathVariable String courseId){
        return courseService.getCourseById(courseId)
                .map(c -> ResponseEntity.ok().body(c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public Mono<ResponseEntity<CourseResponseDTO>> addCourse(@RequestBody Mono<CourseRequestDTO> courseRequestBody) throws URISyntaxException {
        return courseService.addCourse(courseRequestBody).map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @PutMapping("/{courseId}")
    public Mono<ResponseEntity<CourseResponseDTO>> updateCourse(@PathVariable String courseId, @RequestBody Mono<CourseRequestDTO> courseRequestBody){
        return this.courseService.updateCourseById(courseRequestBody, courseId)
                .map(course -> ResponseEntity.ok().body(course))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{courseId}")
    public Mono<ResponseEntity<Void>> deleteCourse(@PathVariable String courseId){
        return courseService.deleteCourseById(courseId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

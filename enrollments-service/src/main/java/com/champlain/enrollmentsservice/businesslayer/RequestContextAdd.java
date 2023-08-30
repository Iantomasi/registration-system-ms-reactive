package com.champlain.enrollmentsservice.businesslayer;

import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.domainclientlayer.CourseResponseDTO;
import com.champlain.enrollmentsservice.domainclientlayer.StudentResponseDTO;
import com.champlain.enrollmentsservice.presentationlayer.EnrollmentRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContextAdd {

    private EnrollmentRequestDTO enrollmentRequestDTO;
    private Enrollment enrollment;
    private StudentResponseDTO studentResponseDTO;
    private CourseResponseDTO courseResponseDTO;

    public RequestContextAdd(EnrollmentRequestDTO enrollmentRequestDTO){
        this.enrollmentRequestDTO = enrollmentRequestDTO;
    }




}

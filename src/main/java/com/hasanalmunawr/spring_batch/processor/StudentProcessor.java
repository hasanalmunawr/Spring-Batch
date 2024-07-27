package com.hasanalmunawr.spring_batch.processor;

import com.hasanalmunawr.spring_batch.entity.Student;
import jakarta.annotation.Nullable;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student, Student> {

    @Override
    public Student process(@Nullable Student student) throws Exception {
//        All The Logic Business here
        return null;
    }
}

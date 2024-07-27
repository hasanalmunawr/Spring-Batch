package com.hasanalmunawr.spring_batch.repository;

import com.hasanalmunawr.spring_batch.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
}

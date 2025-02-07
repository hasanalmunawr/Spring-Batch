package com.hasanalmunawr.spring_batch.repository;

import com.hasanalmunawr.spring_batch.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}

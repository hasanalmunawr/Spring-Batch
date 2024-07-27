package com.hasanalmunawr.spring_batch.config;

import com.hasanalmunawr.spring_batch.entity.Customer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(@NonNull Customer customer) throws Exception {
        if (customer.getCountry().equals("United States")) {
            log.info("[CustomerItemProcessor:process] Processing Customer : {}", customer.getFirstName());
            return customer;
        } else {
            log.info("[CustomerItemProcessor:process] No Processing Customer ");
            return null;
        }
    }
}

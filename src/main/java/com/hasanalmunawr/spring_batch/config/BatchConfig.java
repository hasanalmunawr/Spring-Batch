package com.hasanalmunawr.spring_batch.config;

import com.hasanalmunawr.spring_batch.entity.Customer;
import com.hasanalmunawr.spring_batch.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

    private final CustomerRepository customerRepository;
    private final JobRepository jobRepository;
//    private final JobBuilder jobRepository;


    @Bean
    public FlatFileItemReader<Customer> reader() {
       return new FlatFileItemReaderBuilder<Customer>()
               .name("CustomerItemReader")
               .resource(new ClassPathResource("customer.csv"))
               .linesToSkip(1)
               .lineMapper(lineMapper())
               .build();
    }

    @Bean
    public CustomerItemProcessor processor() {
        return new CustomerItemProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer() {
      RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
      writer.setRepository(customerRepository);
      writer.setMethodName("save");
      return writer;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setStrict(false);
        tokenizer.setNames("id", "firstname", "lastname", "email", "gender", "contact", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setFieldSetMapper(fieldSetMapper);
        lineMapper.setLineTokenizer(tokenizer);

        return lineMapper;
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<Customer, Customer> chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository,Step step1, JobCompletionNotificationListener listener) {
        JobBuilder jobBuilder = new JobBuilder("importUserJob", jobRepository);
        SimpleJobBuilder simpleJobBuilder = jobBuilder.start(step1);
        return simpleJobBuilder.listener(listener).build();
    }
}

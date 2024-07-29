package com.hasanalmunawr.spring_batch.config;

import com.hasanalmunawr.spring_batch.entity.Customer;
import com.hasanalmunawr.spring_batch.processor.CustomerItemProcessor;
import com.hasanalmunawr.spring_batch.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

    private final CustomerRepository customerRepository;
    private final JobRepository jobRepository;
//    private final JobBuilder jobRepository;


//    TO READ AND STORE INTO DATABASE
    @Bean
    public FlatFileItemReader<Customer> reader() {
       return new FlatFileItemReaderBuilder<Customer>()
               .name("CustomerItemReader")
               .resource(new ClassPathResource("customers.csv"))
               .linesToSkip(1)
               .lineMapper(lineMapper())
               .build();
    }

//    TO WRITE FROM DATABASE AND RESULT A FILE
    @Bean
    public FlatFileItemWriter<Customer> writerOut() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("CustomerItemWriter")
                .resource(new FileSystemResource("output.csv"))
                .delimited()
                .delimiter(",")
                .names("id", "firstname", "lastname", "email", "gender", "contact", "country", "dob")
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

    public RepositoryItemReader<Customer> readerOut() {
        RepositoryItemReader<Customer> reader = new RepositoryItemReader<>();
        reader.setRepository(customerRepository);
        reader.setMethodName("findAll");
        return reader;
    }

    @Bean
    public LineMapper<Customer> lineMapper() {
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
    public Step step2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
       return new StepBuilder("step2", jobRepository)
               .<Customer, Customer> chunk(10, transactionManager)
               .reader(readerOut())
               .writer(writerOut())
               .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository,Step step1, JobCompletionNotificationListener listener) {
        JobBuilder jobBuilder = new JobBuilder("importUserJob", jobRepository);
        SimpleJobBuilder simpleJobBuilder = jobBuilder.start(step1);
        return simpleJobBuilder.listener(listener).build();
    }

}

package com.wisesupport.batch;

import com.wisesupport.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReader<User> reader() {
        return new SimpleItemReader();
    }

    @Bean
    public ItemProcessor<User, User> itemProcessor() {
        return new SimpleProcessor();
    }

    @Bean
    public ItemWriter<User> itemWriter() {
        return new SimpleItemWriter();
    }

    @Bean
    public Job importJob(Step stepOne, Step stepTwo) {
        return jobBuilderFactory.get("importJob")
                .incrementer(new RunIdIncrementer())
                .listener(new JobExecutionListenerSupport() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("afterJob {}", jobExecution.getStatus());
                    }
                })
                .flow(stepOne)
                .next(stepTwo)
                .end()
                .build();
    }

    @Bean
    public Step stepOne() {
        return stepBuilderFactory.get("stepOne")
                .<User, User>chunk(10)
                .reader(reader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .faultTolerant()
                .retry(IllegalArgumentException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step stepTwo() {
        return stepBuilderFactory
                .get("stepTow")
                .tasklet((contribution, chunkContext) -> {
                    log.info("tasklet run");
                    return RepeatStatus.FINISHED;})
                .build();
    }

}

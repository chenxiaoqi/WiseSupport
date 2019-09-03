package com.wisesupport.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Author chenxiaoqi on 2019-06-01.
 */

@Slf4j
public class SimpleTasklet implements Tasklet, StepListener {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("simple tasklet running");
        return RepeatStatus.FINISHED;
    }

}

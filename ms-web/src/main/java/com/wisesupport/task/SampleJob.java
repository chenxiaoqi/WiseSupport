package com.wisesupport.task;

import com.wisesupport.user.User;
import com.wisesupport.user.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author c00286900
 */
@Slf4j
public class SampleJob extends QuartzJobBean {


    private UserMapper userMapper;

    private int timeout;

    public SampleJob(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        for (User user : userMapper.findAll()) {
            log.debug("account {}, password {} timeout {}", user.getAccount(), user.getPassword(), timeout);
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

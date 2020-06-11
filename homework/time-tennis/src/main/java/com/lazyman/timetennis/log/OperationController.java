package com.lazyman.timetennis.log;

import com.lazyman.timetennis.user.User;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Date;
import java.util.List;

@RestController
public class OperationController {

    private OperationMapper mapper;

    public OperationController(OperationMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping("/operations")
    public List<Operation> operations(@SessionAttribute User user,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) Date start) {
        return mapper.selectAll(start);
    }
}

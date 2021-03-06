package com.lazyman.timetennis.log;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@RestController
public class OperationController {

    private OperationMapper mapper;

    public OperationController(OperationMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping("/operations")
    public List<Operation> operations(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) @Size(max = 32) Date start) {
        return mapper.selectAll(start);
    }
}

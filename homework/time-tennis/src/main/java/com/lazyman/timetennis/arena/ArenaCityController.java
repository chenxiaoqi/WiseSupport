package com.lazyman.timetennis.arena;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/arena")
public class ArenaCityController {
    private ArenaDao arenaDao;

    public ArenaCityController(ArenaDao arenaDao) {
        this.arenaDao = arenaDao;
    }

    @GetMapping("/cities")
    public List<String> cities() {
        return arenaDao.cities();
    }

}

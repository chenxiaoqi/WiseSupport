package com.lazyman.timetennis.arena;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class ArenaUserController {

    private final ArenaDao arenaDao;

    public ArenaUserController(ArenaDao arenaDao) {
        this.arenaDao = arenaDao;
    }

    @GetMapping("/arenas")
    public List<Arena> arenas(@RequestParam(required = false) String city,
                              @RequestParam(required = false) Integer type,
                              @RequestParam(required = false) String name) {
        return arenaDao.searchArena(city, type, name);
    }

    @GetMapping("/arena/detail")
    public Arena arenaDetail(@RequestParam int arenaId) {
        return arenaDao.loadFull(arenaId);
    }
}


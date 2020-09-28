package com.lazyman.timetennis.arena;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class ArenaUserController {

    private ArenaDao arenaDao;

    private CourtDao courtDao;

    private RuleDao ruleDao;

    public ArenaUserController(ArenaDao arenaDao, CourtDao courtDao, RuleDao ruleDao) {
        this.arenaDao = arenaDao;
        this.courtDao = courtDao;
        this.ruleDao = ruleDao;
    }

    @GetMapping("/arenas")
    public List<Arena> arenas(@RequestParam String city,
                              @RequestParam(required = false) Integer type,
                              @RequestParam(required = false) String name) {
        return arenaDao.searchArena(city, type, name);
    }

    @GetMapping("/arena/{id}")
    public Arena arena(@PathVariable int id) {
        return arenaDao.load(id);
    }

    @GetMapping("/arena/{id}/detail")
    public Arena arenaDetail(@PathVariable int id) {
        Arena arena = arenaDao.load(id);
        List<Court> courts = courtDao.onLineCourts(arena.getId());
        if (!courts.isEmpty()) {
            Object[] courtIds = courts.stream().map(Court::getId).toArray();
            List<Rule> rules = ruleDao.courtRules(courtIds);
            for (Court court : courts) {
                for (Rule rule : rules) {
                    if (court.getId() == rule.getCourtId()) {
                        if (rule.getType() == 1) {
                            court.getDisableRules().add(rule);
                        } else {
                            court.getFeeRules().add(rule);
                        }
                    }
                }
            }
            arena.setCourts(courts);
        }

        return arena;
    }
}


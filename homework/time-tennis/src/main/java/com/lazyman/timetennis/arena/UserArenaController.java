package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.user.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mine")
public class UserArenaController {

    private ArenaDao arenaDao;

    private RuleDao ruleDao;

    public UserArenaController(ArenaDao arenaDao, RuleDao ruleDao) {
        this.arenaDao = arenaDao;
        this.ruleDao = ruleDao;
    }

    @GetMapping("/arenas")
    public List<Arena> arenas(@SessionAttribute User user) {
        return arenaDao.arenas(user.getOpenId());
    }

    @GetMapping("/rules")
    public List<Rule> rules(@SessionAttribute User user, int type, int arenaId) {
        //todo 检查用户是不是有权限
        return ruleDao.rules(arenaId, type);
    }

    @DeleteMapping("/rule")
    public void deleteRule(@SessionAttribute User user, int id) {
        //todo 检查用户权限
        if (ruleDao.used(id)) {
            throw new BusinessException("该规则已经被使用,请先到场地中删除");
        }
        ruleDao.delete(id);
    }

    @GetMapping("/rule/{id}")
    public Rule rule(@SessionAttribute User user, @PathVariable int id) {
        return ruleDao.load(id);
    }

    @PutMapping("/rule")
    public void updateRule(@SessionAttribute User user, Rule rule) {
        //todo 权限
        ruleDao.update(rule);
    }

    @PostMapping("/rule")
    public void insertRule(@SessionAttribute User user, Rule rule) {
        //todo 权限
        ruleDao.insert(rule);
    }
}

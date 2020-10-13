package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.user.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class FavoriteController {
    private FavoriteDao dao;

    public FavoriteController(FavoriteDao dao) {
        this.dao = dao;
    }

    @GetMapping("/favorites")
    public List<Arena> favorites(User user) {
        return dao.byOpenId(user.getOpenId());
    }

    @DeleteMapping("/favorite")
    public void delete(User user, @RequestParam int arenaId) {
        dao.delete(user.getOpenId(), arenaId);
    }

    @PostMapping("/favorite")
    public void add(User user, @RequestParam int arenaId) {
        try {
            dao.add(user.getOpenId(), arenaId);
        } catch (DuplicateKeyException e) {
            //do nothing
        }
    }
}

package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.BusinessException;
import com.lazyman.timetennis.user.User;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/mine")
public class UserArenaController {
    private ArenaDao arenaDao;

    private RuleDao ruleDao;

    private CourtDao courtDao;

    public UserArenaController(ArenaDao arenaDao, RuleDao ruleDao, CourtDao courtDao) {
        this.arenaDao = arenaDao;
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
    }

    @GetMapping("/arenas")
    public List<Arena> arenas(@SessionAttribute User user) {
        return arenaDao.arenas(user.getOpenId());
    }

    @GetMapping("/rules")
    public List<Rule> rules(@SessionAttribute User user, int arenaId, Integer type) {
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

    @DeleteMapping("/court")
    @Transactional
    public void deleteCourt(@SessionAttribute User user, int id) {
        //todo 权限
        ruleDao.deleteCourtRelation(id);
        courtDao.delete(id);
    }

    @GetMapping("/court/{id}")
    public Court court(@SessionAttribute User user, @PathVariable int id) {
        Court court = courtDao.load(id);
        List<Rule> rules = ruleDao.courtRules(new Object[]{id});
        for (Rule rule : rules) {
            if (rule.getType() == 1) {
                court.getDisableRules().add(rule);
            } else {
                court.getFeeRules().add(rule);
            }
        }
        return court;
    }

    @PostMapping("/court")
    @Transactional
    public void addCourt(@SessionAttribute User user, int arenaId, String name, int fee, String ruleIds) {
        //todo 权限
        int courtId = courtDao.insert(arenaId, name, fee);

        insertCourtRuleRelation(courtId, ruleIds);
    }

    @PutMapping("/court")
    @Transactional
    public void updateCourt(@SessionAttribute User user, int id, String name, int fee, String ruleIds) {
        //todo 权限
        courtDao.update(id, name, fee);
        ruleDao.deleteCourtRelation(id);
        insertCourtRuleRelation(id, ruleIds);
    }

    @PostMapping("/upload")
    public String upload(MultipartFile image) throws IOException {
        String tn = "tmp_" + System.currentTimeMillis() + "." + MimeType.valueOf(Objects.requireNonNull(image.getContentType())).getSubtype();
        try (FileOutputStream out = new FileOutputStream("./images/" + tn)) {
            IOUtils.copy(image.getInputStream(), out);
        }
        return tn;
    }

    @PostMapping("/arena")
    @Transactional
    public void addArena(@SessionAttribute User user, Arena arena) {
        int id = arenaDao.insert(arena);
        String[] images = StringUtils.split(arena.getImages()[0]);
        String[] names = new String[images.length];
        for (int i = 0; i < images.length; i++) {
            String image = images[i];
            names[i] = id + "_" + i + "." + FilenameUtils.getExtension(image);
            Assert.isTrue(new File("images", image).renameTo(new File("images", names[i])), "rename file failed");
        }
        arenaDao.updateImages(id, StringUtils.join(names, ','));
        arenaDao.setRole(id, user.getOpenId(), "admin");
    }

    private void insertCourtRuleRelation(int id, String ruleIds) {
        if (!StringUtils.isEmpty(ruleIds)) {
            String[] rIds = StringUtils.split(ruleIds, ',');
            for (int i = 0; i < rIds.length; i++) {
                String rId = rIds[i];
                ruleDao.insertCourtRelation(id, Integer.parseInt(rId), i);
            }
        }
    }
}

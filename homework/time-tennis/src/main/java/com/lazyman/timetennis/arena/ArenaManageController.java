package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/manage")
public class ArenaManageController {
    private static final String TEMP_FILE_PREFIX = "tmp_";
    private ArenaDao arenaDao;

    private RuleDao ruleDao;

    private CourtDao courtDao;

    private File imagesDir;

    public ArenaManageController(ArenaDao arenaDao, RuleDao ruleDao, CourtDao courtDao, @Value("${wx.images-path}") String imagesPath) {
        this.arenaDao = arenaDao;
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
        this.imagesDir = new File(imagesPath);
    }

    @GetMapping("/arena/{id}")
    public Arena arena(@PathVariable int id) {
        return arenaDao.load(id);
    }

    @GetMapping("/arenas")
    public List<Arena> arenas(@SessionAttribute User user) {
        checkPrivileges(user);
        return arenaDao.arenas(user.getOpenId());
    }

    @GetMapping("/rules")
    public List<Rule> rules(@SessionAttribute User user, int arenaId, Integer type) {
        checkPrivileges(user);
        checkArenaPrivileges(user, arenaId);
        return ruleDao.rules(arenaId, type);
    }

    @DeleteMapping("/rule")
    public void deleteRule(@SessionAttribute User user, int id) {
        Rule rule = ruleDao.load(id);
        Validate.notNull(rule);
        checkArenaPrivileges(user, rule.getArenaId());
        if (ruleDao.used(id)) {
            throw new BusinessException("该规则已经被使用,请先到场地中删除");
        }
        ruleDao.delete(id);
    }

    @GetMapping("/rule/{id}")
    public Rule rule(@SessionAttribute User user, @PathVariable int id) {
        checkPrivileges(user);
        return ruleDao.load(id);
    }

    @PutMapping("/rule")
    public void updateRule(@SessionAttribute User user, Rule rule) {
        Rule dbRule = ruleDao.load(rule.getId());
        Validate.notNull(dbRule);
        checkArenaPrivileges(user, dbRule.getArenaId());
        ruleDao.update(rule);
    }

    @PostMapping("/rule")
    public void insertRule(@SessionAttribute User user, Rule rule) {
        checkArenaPrivileges(user, rule.getArenaId());
        ruleDao.insert(rule);
    }

    @DeleteMapping("/court")
    @Transactional
    public void deleteCourt(@SessionAttribute User user, int id) {
        Court court = courtDao.load(id);
        Validate.notNull(court);
        checkArenaPrivileges(user, court.getArenaId());
        ruleDao.deleteCourtRelation(id);
        courtDao.delete(id);
    }

    @GetMapping("/court/{id}")
    public Court court(@SessionAttribute User user, @PathVariable int id) {
        checkPrivileges(user);
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
        checkArenaPrivileges(user, arenaId);
        int courtId = courtDao.insert(arenaId, name, fee);
        insertCourtRuleRelation(courtId, ruleIds);
    }

    @PutMapping("/court")
    @Transactional
    public void updateCourt(@SessionAttribute User user, int id, String name, int fee, String ruleIds) {
        Court court = courtDao.load(id);
        Validate.notNull(court);
        checkArenaPrivileges(user, court.getArenaId());
        courtDao.update(id, name, fee);
        ruleDao.deleteCourtRelation(id);
        insertCourtRuleRelation(id, ruleIds);
    }

    @PostMapping("/upload")
    public String upload(MultipartFile image) throws IOException {
//        checkPrivileges(user);
        String tn = TEMP_FILE_PREFIX + System.currentTimeMillis() + "." + MimeType.valueOf(Objects.requireNonNull(image.getContentType())).getSubtype();
        try (FileOutputStream out = new FileOutputStream(new File(imagesDir, tn))) {
            IOUtils.copy(image.getInputStream(), out);
        }
        return tn;
    }

    @PostMapping("/arena")
    @Transactional(rollbackFor = {IOException.class, RuntimeException.class})
    @CacheEvict("arena.cities")
    public void addArena(@SessionAttribute User user, Arena arena) throws IOException {
        checkPrivileges(user);
        int id = arenaDao.insert(arena);
        String[] images = arena.getImages();
        String[] names = new String[images.length];
        for (int i = 0; i < images.length; i++) {
            String image = images[i];
            names[i] = id + "_" + i + "." + FilenameUtils.getExtension(image);
            moveFile(image, names[i]);
        }
        arenaDao.updateImages(id, StringUtils.join(names, ','));
        arenaDao.setRole(id, user.getOpenId(), "admin");
    }

    @PutMapping("/arena")
    @Transactional(rollbackFor = {IOException.class, RuntimeException.class})
    @CacheEvict(value = "arena.cities", allEntries = true)
    public void updateArena(@SessionAttribute User user, Arena arena) throws IOException {
        checkArenaPrivileges(user, arena.getId());
        String[] images = arena.getImages();
        String[] names = new String[images.length];
        for (int i = 0; i < images.length; i++) {
            String image = images[i];
            names[i] = arena.getId() + "_" + i + "." + FilenameUtils.getExtension(image);
        }
        arena.setImages(names);
        arenaDao.update(arena);

        //先把原来的文件换个名字
        for (int i = 0; i < images.length; i++) {
            String image = images[i];
            long now = System.currentTimeMillis();
            if (!image.startsWith(TEMP_FILE_PREFIX)) {
                images[i] = "tmp_m" + now + i;
                moveFile(image, images[i]);
            }
        }
        for (int i = 0; i < images.length; i++) {
            String image = images[i];
            moveFile(image, names[i]);
        }
    }

    @DeleteMapping("/arena")
    @Transactional
    @CacheEvict("arena.cities")
    public void deleteArena(@SessionAttribute User user, int id) {
        checkArenaPrivileges(user, id);
        arenaDao.delete(id);

        File[] files = imagesDir.listFiles((dir, name) -> name.startsWith(id + "_"));
        if (files != null) {
            for (File file : files) {
                FileUtils.deleteQuietly(file);
            }
        }
    }

    private void moveFile(String image, String name) throws IOException {
        File destFile = new File(imagesDir, name);
        FileUtils.deleteQuietly(destFile);
        FileUtils.moveFile(new File(imagesDir, image), destFile);
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

    private void checkPrivileges(User user) {
        if (!user.isArenaAdmin()) {
            throw new BusinessException("对不起,您没有权限没有权限");
        }
    }

    private void checkArenaPrivileges(User user, int arenaId) {
        checkPrivileges(user);
        if (!arenaDao.isArenaAdmin(user.getOpenId(), arenaId)) {
            throw new BusinessException("对不起,您不是该场馆管理员");
        }
    }
}

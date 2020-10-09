package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
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

    @GetMapping("/arena/query")
    public List<Arena> getArenaById(@RequestParam @NotEmpty String idOrName) {
        List<Arena> arenas;
        if (StringUtils.isNumeric(idOrName)) {
            try {
                arenas = Collections.singletonList(arenaDao.load(Integer.parseInt(idOrName)));
            } catch (EmptyResultDataAccessException e) {
                arenas = arenaDao.byName(idOrName);
            }
        } else {
            arenas = arenaDao.byName(idOrName);
        }
        return arenas;
    }

    @GetMapping("/arena/{id}/detail")
    public Arena arenaDetail(@PathVariable int id) {
        Arena arena = arenaDao.load(id);
        arena.setCourts(courtDao.courts(arena.getId()));
        return arena;
    }

    @GetMapping("/arenas")
    public List<Arena> arenas(User user) {
        checkPrivileges(user);
        return arenaDao.arenas(user.getOpenId());
    }

    @GetMapping("/rules")
    public List<Rule> rules(User user,
                            @RequestParam int arenaId,
                            Integer type) {
        checkPrivileges(user);
        checkArenaPrivileges(user, arenaId);
        return ruleDao.rules(arenaId, type);
    }

    @DeleteMapping("/rule")
    public void deleteRule(User user,
                           @RequestParam int id) {
        Rule rule = ruleDao.load(id);
        Validate.notNull(rule);
        checkArenaPrivileges(user, rule.getArenaId());
        if (ruleDao.used(id)) {
            throw new BusinessException("该规则已经被使用,请先到场地中删除");
        }
        ruleDao.delete(id);
    }

    @GetMapping("/rule/{id}")
    public Rule rule(User user, @PathVariable int id) {
        checkPrivileges(user);
        return ruleDao.load(id);
    }

    @PutMapping("/rule")
    public void updateRule(User user,
                           Rule rule) {
        Rule dbRule = ruleDao.load(rule.getId());
        Validate.notNull(dbRule);
        checkArenaPrivileges(user, dbRule.getArenaId());
        ruleDao.update(rule);
    }

    @PostMapping("/rule")
    public void insertRule(User user, Rule rule) {
        checkArenaPrivileges(user, rule.getArenaId());
        ruleDao.insert(rule);
    }

    @GetMapping("/court/{id}")
    public Court court(User user, @PathVariable int id) {
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
    public void addCourt(User user, @RequestParam int arenaId,
                         @RequestParam @NotEmpty String name,
                         @RequestParam int fee,
                         @RequestParam(required = false) String ruleIds) {
        checkArenaPrivileges(user, arenaId);
        int courtId = courtDao.insert(arenaId, name, fee);
        insertCourtRuleRelation(courtId, ruleIds);
    }

    @PutMapping("/court")
    @Transactional
    public void updateCourt(User user,
                            @RequestParam int id,
                            @RequestParam @NotEmpty String name,
                            @RequestParam int fee,
                            @RequestParam(required = false) String ruleIds) {
        Court court = courtDao.load(id);
        Validate.notNull(court);
        checkArenaPrivileges(user, court.getArenaId());
        courtDao.update(id, court.getArenaId(), name, fee);
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
    public void addArena(User user, Arena arena) throws IOException {
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
    public void updateArena(User user, Arena arena) throws IOException {
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

    @PostMapping("/arena/status")
    public void updateArenaStatus(User user, int arenaId, boolean online) {
        checkArenaPrivileges(user, arenaId);
        if (online && courtDao.onLineCourts(arenaId).isEmpty()) {
            throw new BuilderException("该场馆还没有上线的场地");
        }
        Arena arena = arenaDao.load(arenaId);
        Validate.notNull(arena);

        String status;
        if (online) {
            status = "ol";
            //如果是被管理员下线的需要管理员权限
            if (arena.getStatus().equals("sofl")) {
                if (!user.isSuperAdmin()) {
                    throw new BusinessException("请联系管理员,执行上线");
                }
            }
        } else {
            if (user.isSuperAdmin()) {
                status = "sofl";
            } else {
                status = "ofl";
            }
        }
        arenaDao.updateArenaStatus(arenaId, status);
    }

    @PutMapping("/arena/super_manage")
    public void superManage(User user, @RequestParam @NotEmpty int arenaId, @RequestParam(required = false) String mchId) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要系统管理员权限");
        }
        if (StringUtils.isEmpty(mchId)) {
            mchId = null;
        }
        Validate.isTrue(arenaDao.updateMchId(arenaId, mchId) == 1);
    }

    @GetMapping("/arena/admins")
    public List<User> admins(User user, @RequestParam @NotEmpty int arenaId) {
        return arenaDao.admins(arenaId);
    }

    @PostMapping("/arena/role")
    public void addRole(User user, @RequestParam @NotEmpty int arenaId,
                        @RequestParam @NotEmpty String openId,
                        @RequestParam @NotEmpty String role) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要管理员权限");
        }
        try {
            arenaDao.setRole(arenaId, openId, role);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该用户已有权限");
        }
    }

    @DeleteMapping("/arena/role")
    public void deleteRole(User user, @RequestParam @NotEmpty int arenaId,
                           @RequestParam @NotEmpty String openId,
                           @RequestParam @NotEmpty String role) {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要管理员权限");
        }
        arenaDao.deleteRole(arenaId, openId, role);
    }

    @PostMapping("/court/status")
    public void updateCourtStatus(User user, int courtId, int arenaId, boolean online) {
        checkArenaPrivileges(user, arenaId);
        Arena arena = arenaDao.load(arenaId);
        if (!online && arena.getStatus().equals("ol") && courtDao.onLineCourts(arenaId).size() <= 1) {
            throw new BuilderException("上线的场馆请至少保留一个在线的场地");
        }
        arenaDao.updateCourtStatus(courtId, arenaId, online ? "ol" : "ofl");
    }

//    @Transactional
//    public void deleteCourt(User user, int id) {
//        Court court = courtDao.load(id);
//        Validate.notNull(court);
//        checkArenaPrivileges(user, court.getArenaId());
//        ruleDao.deleteCourtRelation(id);
//        courtDao.delete(id);
//    }

//    @Transactional
//    @CacheEvict("arena.cities")
//    public void deleteArena(User user, int id) {
//        checkArenaPrivileges(user, id);
//        arenaDao.delete(id);
//
//        File[] files = imagesDir.listFiles((dir, name) -> name.startsWith(id + "_"));
//        if (files != null) {
//            for (File file : files) {
//                FileUtils.deleteQuietly(file);
//            }
//        }
//    }

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
        if (!user.isSuperAdmin()) {
            checkPrivileges(user);
            if (!arenaDao.isArenaAdmin(user.getOpenId(), arenaId)) {
                throw new BusinessException("对不起,您不是该场馆管理员");
            }
        }
    }
}

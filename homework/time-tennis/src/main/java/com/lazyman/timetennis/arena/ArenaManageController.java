package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.booking.BookSchedulerRepository;
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

    private ArenaPrivilege privilege;

    private File imagesDir;

    private BookSchedulerRepository repository;

    public ArenaManageController(ArenaDao arenaDao, RuleDao ruleDao, CourtDao courtDao, ArenaPrivilege privilege, @Value("${wx.images-path}") String imagesPath, BookSchedulerRepository repository) {
        this.arenaDao = arenaDao;
        this.ruleDao = ruleDao;
        this.courtDao = courtDao;
        this.privilege = privilege;
        this.imagesDir = new File(imagesPath);
        this.repository = repository;
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
        return arenaDao.arenas(user.getOpenId());
    }

    @GetMapping("/rules")
    public List<Rule> rules(User user,
                            @RequestParam int arenaId,
                            Integer type) {
        privilege.requireAdministrator(user.getOpenId(), arenaId);
        return ruleDao.rules(arenaId, type);
    }

    @DeleteMapping("/rule")
    public void deleteRule(User user,
                           @RequestParam int id) {
        Rule rule = ruleDao.load(id);
        Validate.notNull(rule);
        privilege.requireAdministrator(user.getOpenId(), rule.getArenaId());
        if (ruleDao.used(id)) {
            throw new BusinessException("该规则已经被使用,请先到场地中删除");
        }
        ruleDao.delete(id);
    }

    @GetMapping("/rule/{id}")
    public Rule rule(User user, @PathVariable int id) {
        Rule rule = ruleDao.load(id);
        privilege.requireAdministrator(user.getOpenId(), rule.getArenaId());
        return rule;
    }

    @PutMapping("/rule")
    public void updateRule(User user,
                           Rule rule) {
        Rule dbRule = ruleDao.load(rule.getId());
        Validate.notNull(dbRule);
        privilege.requireAdministrator(user.getOpenId(), dbRule.getArenaId());
        ruleDao.update(rule);
    }

    @PostMapping("/rule")
    public void insertRule(User user, Rule rule) {
        privilege.requireAdministrator(user.getOpenId(), rule.getArenaId());
        ruleDao.insert(rule);
    }

    @GetMapping("/court/{id}")
    public Court court(User user, @PathVariable int id) {
        Court court = courtDao.load(id);
        privilege.requireAdministrator(user.getOpenId(), court.getArenaId());
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
        privilege.requireAdministrator(user.getOpenId(), arenaId);
        int courtId = courtDao.insert(arenaId, name, fee);
        insertCourtRuleRelation(courtId, ruleIds);
        repository.invalidate(arenaId);
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
        privilege.requireAdministrator(user.getOpenId(), court.getArenaId());
        courtDao.update(id, court.getArenaId(), name, fee);
        ruleDao.deleteCourtRelation(id);
        insertCourtRuleRelation(id, ruleIds);
    }

    @PostMapping("/upload")
    public String upload(MultipartFile image) throws IOException {
        String tn = TEMP_FILE_PREFIX + System.currentTimeMillis() + "." + MimeType.valueOf(Objects.requireNonNull(image.getContentType())).getSubtype();
        try (FileOutputStream out = new FileOutputStream(new File(imagesDir, tn))) {
            IOUtils.copy(image.getInputStream(), out);
        }
        return tn;
    }

    @PostMapping("/arena")
    @Transactional(rollbackFor = {IOException.class, RuntimeException.class})
    public void addArena(User user, Arena arena) throws IOException {
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要系统管理员权限");
        }
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
        if (!user.isSuperAdmin()) {
            throw new BusinessException("需要系统管理员权限");
        }

        Arena dbArena = arenaDao.load(arena.getId());
        String[] ons = dbArena.getImages();
        String[] nns = arena.getImages();
        if (!Objects.deepEquals(ons, nns)) {
            String lastName = ons[ons.length - 1];
            int idx = lastName.indexOf('_');
            int start = Integer.parseInt(lastName.substring(idx + 1, lastName.indexOf('.')), 36) + 1;
            String[] names = new String[nns.length];
            for (int i = 0; i < nns.length; i++) {
                String name = nns[i];
                names[i] = arena.getId() + "_" + Integer.toString((start + i), 36) + "." + FilenameUtils.getExtension(name);
            }
            arena.setImages(names);

            //先把原来的文件改个名字
            for (int i = 0; i < nns.length; i++) {
                String name = nns[i];
                long now = System.currentTimeMillis();
                if (!name.startsWith(TEMP_FILE_PREFIX)) {
                    nns[i] = "tmp_m" + now + i;
                    moveFile(name, nns[i]);
                }
            }
            //把所有文件改成新名称
            for (int i = 0; i < nns.length; i++) {
                String name = nns[i];
                moveFile(name, names[i]);
            }
        }
        arenaDao.update(arena);
    }

    @PostMapping("/arena/status")
    public void updateArenaStatus(User user, int arenaId, boolean online) {
        privilege.requireAdministrator(user.getOpenId(), arenaId);
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
        if (!user.isSuperAdmin()) {
            privilege.requireAdministrator(user.getOpenId(), arenaId);
        }
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
        privilege.requireAdministrator(user.getOpenId(), arenaId);
        Arena arena = arenaDao.load(arenaId);
        if (!online && arena.getStatus().equals("ol") && courtDao.onLineCourts(arenaId).size() <= 1) {
            throw new BuilderException("上线的场馆请至少保留一个在线的场地");
        }
        arenaDao.updateCourtStatus(courtId, arenaId, online ? "ol" : "ofl");
        repository.invalidate(arenaId);
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
}

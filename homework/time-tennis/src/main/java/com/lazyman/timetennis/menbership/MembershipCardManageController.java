package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.user.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mine/mc")
public class MembershipCardManageController {
    private MembershipCardDao mcDao;

    public MembershipCardManageController(MembershipCardDao mcDao) {
        this.mcDao = mcDao;
    }

    @GetMapping("/metas")
    public List<MembershipCardMeta> metas(@SessionAttribute User user) {
        return mcDao.byOpenId(user.getOpenId());
    }

    @DeleteMapping("/meta/{id}")
    @Transactional
    public void deleteMeta(@SessionAttribute User user, @PathVariable int id) {
        //todo 会员卡是否有人购买,已有购买的就不让删除？
        mcDao.deleteMeta(id, user.getOpenId());
    }

    @PutMapping("/meta/{id}")
    @Transactional
    public void updateMeta(@SessionAttribute User user, @PathVariable int id, String name, int initialBalance, int discount, int price, int extendMonth, String[] arenaIds) {
        //todo 有用户购买就不能修改
        Validate.isTrue(mcDao.updateMeta(user.getOpenId(), id, name, initialBalance, discount, price, extendMonth) == 1);
        mcDao.deleteMetaArenaRelation(id);
        if (!ArrayUtils.isEmpty(arenaIds)) {
            for (String arenaId : arenaIds) {
                mcDao.addMetaArenaRelation(id, arenaId);
            }
        }
    }

    @PostMapping("/meta")
    @Transactional
    public void addMeta(@SessionAttribute User user, String name, int initialBalance, int discount, int price, int extendMonth, String[] arenaIds) {
        int metaId = mcDao.createMeta(user.getOpenId(), name, initialBalance, discount, price, extendMonth);
        if (!ArrayUtils.isEmpty(arenaIds)) {
            for (String arenaId : arenaIds) {
                mcDao.addMetaArenaRelation(metaId, arenaId);
            }
        }
    }

    @GetMapping("/meta/{id}")
    public MembershipCardMeta loadMeta(@PathVariable int id) {
        MembershipCardMeta meta = mcDao.loadMeta(id);
        meta.setArenas(mcDao.arenas(id));
        return meta;
    }

    @PostMapping("/meta/status")
    @Transactional
    public void status(@SessionAttribute User user, boolean online, int id) {
        if (online) {
            Validate.isTrue(mcDao.metaOnline(user.getOpenId(), id) == 1, "没有权限");
        } else {
            Validate.isTrue(mcDao.metaOffline(user.getOpenId(), id) == 1, "没有权限");
        }
    }
}

package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/manage/mc")
public class MembershipCardManageController {
    private MembershipCardDao mcDao;

    public MembershipCardManageController(MembershipCardDao mcDao) {
        this.mcDao = mcDao;
    }

    @GetMapping("/metas")
    public List<MembershipCardMeta> metas(User user) {
        return mcDao.byOpenId(user.getOpenId());
    }

    @DeleteMapping("/meta/{id}")
    @Transactional
    public void deleteMeta(User user, @PathVariable int id) {
        if (mcDao.hasMember(id)) {
            throw new BusinessException("该会员卡已经有会员,无法删除!");
        }
        mcDao.deleteMeta(id, user.getOpenId());
    }

    @PutMapping("/meta/{id}")
    @Transactional
    public void updateMeta(User user,
                           @PathVariable int id,
                           @RequestParam @NotEmpty String name,
                           @RequestParam int initialBalance,
                           @RequestParam int discount,
                           @RequestParam int price,
                           @RequestParam int extendMonth,
                           @RequestParam String[] arenaIds) {
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
    public void addMeta(User user, @RequestParam @NotEmpty String name,
                        @RequestParam int initialBalance,
                        @RequestParam int discount,
                        @RequestParam int price,
                        @RequestParam int extendMonth,
                        @RequestParam String[] arenaIds) {
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
    public void status(User user,
                       @RequestParam boolean online,
                       @RequestParam int id) {
        if (online) {
            Validate.isTrue(mcDao.metaOnline(user.getOpenId(), id) == 1, "没有权限");
        } else {
            Validate.isTrue(mcDao.metaOffline(user.getOpenId(), id) == 1, "没有权限");
        }
    }
}

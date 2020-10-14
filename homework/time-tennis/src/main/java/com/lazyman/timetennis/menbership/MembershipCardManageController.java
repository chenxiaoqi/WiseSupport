package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.privilege.PrivilegeTool;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
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
    public List<MembershipCardMeta> metas(User user, int arenaId) {
        return mcDao.byArenaId(arenaId);
    }

    @DeleteMapping("/meta/{id}")
    @Transactional
    public void deleteMeta(User user, @PathVariable int id) {
        MembershipCardMeta meta = mcDao.loadMeta(id);
        Validate.notNull(meta);

        if (mcDao.hasMember(id)) {
            throw new BusinessException("该会员卡已经有会员,无法删除!");
        }
        //todo 判断是不是场馆管的理员
        mcDao.deleteMeta(id, meta.getArena().getId());
    }

    @PutMapping("/meta/{id}")
    @Transactional
    public void updateMeta(User user,
                           @PathVariable int id,
                           @RequestParam @NotEmpty String name,
                           @RequestParam int initialBalance,
                           @RequestParam int discount,
                           @RequestParam int price,
                           @RequestParam int extendMonth) {
        //todo 权限
        MembershipCardMeta meta = mcDao.loadMeta(id);
        Validate.notNull(meta);
        Validate.isTrue(mcDao.updateMeta(id, meta.getArena().getId(), name, initialBalance, discount, price, extendMonth) == 1);
    }

    @PostMapping("/meta")
    @Transactional
    public void addMeta(User user,
                        @RequestParam int arenaId,
                        @RequestParam @NotEmpty String name,
                        @RequestParam int initialBalance,
                        @RequestParam int discount,
                        @RequestParam int price,
                        @RequestParam int extendMonth) {
        mcDao.createMeta(arenaId, name, initialBalance, discount, price, extendMonth);
    }

    @GetMapping("/meta/{id}")
    public MembershipCardMeta loadMeta(@PathVariable int id) {
        return mcDao.loadMeta(id);
    }

    @PostMapping("/meta/status")
    @Transactional
    public void status(User user,
                       @RequestParam boolean online,
                       @RequestParam int id) {

        //todo 权限
        MembershipCardMeta meta = mcDao.loadMeta(id);
        Validate.notNull(meta);
        if (online) {
            mcDao.metaOnline(meta.getArena().getId(), id);
        } else {
            mcDao.metaOffline(meta.getArena().getId(), id);
        }
    }

    @GetMapping("/members")
    public List<MembershipCard> members(User user, int metaId,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String openId) {
        //todo 权限
        PrivilegeTool.assertHasArenaManagerRole(user);
        return mcDao.members(metaId, name, openId);
    }
}

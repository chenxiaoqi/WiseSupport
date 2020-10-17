package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.ArenaPrivilege;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.user.UserMapper;
import com.lazyman.timetennis.wp.WePayService;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.validator.constraints.Length;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/manage/mc")
public class MembershipCardManageController {
    private MembershipCardDao mcDao;

    private MembershipCardBillDao billDao;

    private ArenaPrivilege privilege;

    private UserMapper userMapper;

    private MembershipCardService cardService;

    public MembershipCardManageController(MembershipCardDao mcDao, MembershipCardBillDao billDao, ArenaPrivilege privilege, UserMapper userMapper, MembershipCardService cardService) {
        this.mcDao = mcDao;
        this.billDao = billDao;
        this.privilege = privilege;
        this.userMapper = userMapper;
        this.cardService = cardService;
    }

    @GetMapping("/metas")
    public List<MembershipCardMeta> metas(int arenaId) {
        return mcDao.byArenaId(arenaId);
    }

    @DeleteMapping("/meta/{id}")
    @Transactional
    public void deleteMeta(User user, @PathVariable int id) {
        MembershipCardMeta meta = mcDao.loadMeta(id);

        privilege.requireAdministrator(user.getOpenId(), meta.getArena().getId());

        if (mcDao.hasMember(id)) {
            throw new BusinessException("该会员卡已经有会员,无法删除!");
        }
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

        MembershipCardMeta meta = mcDao.loadMeta(id);
        privilege.requireAdministrator(user.getOpenId(), meta.getArena().getId());
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
        privilege.requireAdministrator(user.getOpenId(), arenaId);
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
        MembershipCardMeta meta = mcDao.loadMeta(id);

        privilege.requireAdministrator(user.getOpenId(), meta.getArena().getId());

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
        MembershipCardMeta meta = mcDao.loadMeta(metaId);
        privilege.requireAdministrator(user.getOpenId(), meta.getArena().getId());

        if (StringUtils.isNumeric(name)) {
            return mcDao.members(metaId, null, name, openId);
        } else {
            return mcDao.members(metaId, name, null, openId);
        }
    }

    @PostMapping("/add_member")
    @Transactional
    public void addMember(User user,
                          @RequestParam @NotEmpty String openId,
                          @RequestParam int metaId) {
        MembershipCardMeta meta = mcDao.loadMeta(metaId);
        privilege.requireAdministrator(user.getOpenId(), meta.getArena().getId());

        Validate.notNull(userMapper.selectByPrimaryKey(openId), "用户ID[%s]不存在", openId);
        if (mcDao.hasMeta(openId, meta.getId())) {
            throw new BusinessException("用户已经购买了该会员卡");
        }
        cardService.create(meta, openId);
    }

    @PutMapping("/recharge")
    @Transactional
    public void recharge(User user,
                         @RequestParam @Length(min = 10, max = 10) String code,
                         @RequestParam int fee) {
        MembershipCard card = mcDao.loadCard(code);
        MembershipCardMeta meta = mcDao.loadMeta(card.getMeta().getId());
        privilege.requireAccountant(user.getOpenId(), meta.getArena().getId());

        int balance = mcDao.recharge(code, fee);
        mcDao.extendExpireDate(meta.getExtendMonth(), code);
        String tradeNo = WePayService.creatTradeNo(Constant.PRODUCT_RECHARGE);
        billDao.add(tradeNo, user.getOpenId(), code, Constant.PRODUCT_RECHARGE, fee, balance, new Date());
    }
}

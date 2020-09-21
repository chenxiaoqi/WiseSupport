package com.lazyman.timetennis.user.membership;

import com.lazyman.timetennis.menbership.MembershipCard;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.menbership.MembershipCardMeta;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mc")
public class MembershipCardController {
    private MembershipCardDao mcDao;

    public MembershipCardController(MembershipCardDao mcDao) {
        this.mcDao = mcDao;
    }

    @GetMapping("/metas")
    public List<MembershipCardMeta> metas(String arenaId) {
        return mcDao.byArenaId(arenaId).stream().filter(meta -> meta.getStatus().equals("ol")).collect(Collectors.toList());
    }

    @GetMapping("/user/cards")
    public List<MembershipCard> useCards(@SessionAttribute User user) {
        List<MembershipCard> cards = mcDao.userCards(user.getOpenId());
        for (MembershipCard card : cards) {
            card.getMeta().setArenas(mcDao.arenas(card.getMeta().getId()));
        }
        return cards;
    }

    @PostMapping("/purchase")
    public synchronized void purchase(@SessionAttribute User user, int metaId) {
        //todo 支付
        MembershipCardMeta meta = mcDao.loadMeta(metaId);
        Validate.notNull(meta);

        String code = StringUtils.leftPad(String.valueOf(metaId), 5, '0');
        String maxCode = mcDao.maxMembershipCardCode(metaId);
        if (maxCode == null) {
            code = code + StringUtils.leftPad("1", 5, '0');
        } else {
            code = code + StringUtils.leftPad(String.valueOf(Integer.parseInt(maxCode.substring(5)) + 1), 5, '0');
        }
        try {
            mcDao.createMembershipCard(user.getOpenId(), meta, code, DateUtils.addMonths(new Date(), meta.getExtendMonth()));
        } catch (DuplicateKeyException e) {
            throw new BusinessException("你已经购买了该会员卡");
        }
    }

    @GetMapping("/user/arena/cards")
    public List<MembershipCard> userCardsInArena(@SessionAttribute User user, @RequestParam int arenaId) {
        return mcDao.userCardsInArena(user.getOpenId(), arenaId);
    }
}

package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.BasePayController;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeEvent;
import com.wisesupport.commons.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/mc")
@Slf4j
public class MembershipCardUserController extends BasePayController implements ApplicationListener<TradeEvent> {
    private MembershipCardDao mcDao;

    public MembershipCardUserController(MembershipCardDao mcDao) {
        this.mcDao = mcDao;
    }

    @GetMapping("/metas")
    public List<MembershipCardMeta> metas(String arenaId) {
        return mcDao.byArenaId(arenaId).stream().filter(meta -> meta.getStatus().equals("ol")).collect(Collectors.toList());
    }

    @GetMapping("/cards")
    public List<MembershipCard> useCards(@SessionAttribute User user) {
        List<MembershipCard> cards = mcDao.userCards(user.getOpenId());
        for (MembershipCard card : cards) {
            card.getMeta().setArenas(mcDao.arenas(card.getMeta().getId()));
        }
        return cards;
    }

    @PostMapping("/purchase")
    @Transactional
    public synchronized Map<String, String> purchase(@SessionAttribute User user, int metaId) {
        if (mcDao.hasMeta(user.getOpenId(), metaId)) {
            throw new BusinessException("你已经有该会员卡,无需购买");
        }
        if (mcDao.hasWaitPay(user.getOpenId(), metaId)) {
            throw new BusinessException("您的购买订单确认中,请稍等");
        }
        MembershipCardMeta meta = mcDao.loadMeta(metaId);
        Validate.notNull(meta);

        return preparePay(user.getOpenId(), Constant.PRODUCT_CARD, meta.getPrice(), "购买会员卡[" + meta.getName() + "]", tradeNo -> mcDao.createTradeMembershipCardRelation(tradeNo, metaId));
    }

    @GetMapping("/arena/cards")
    public List<MembershipCard> userCardsInArena(@SessionAttribute User user, @RequestParam int arenaId) {
        return mcDao.userCardsInArena(user.getOpenId(), arenaId);
    }

    @Override
    public void onApplicationEvent(TradeEvent event) {
        Trade trade = event.getTrade();
        if (!Constant.PRODUCT_CARD.equals(trade.getProductType())) {
            return;
        }
        log.info("receive membership card trade[{}] event, in status {}", trade.getTradeNo(), trade.getStatus());
        if ("ok".equals(trade.getStatus())) {

            MembershipCardMeta meta = mcDao.getMetaByTradeNo(trade.getTradeNo());
            if (meta == null) {
                log.error("could not found meta for trade {}", trade.getTradeNo());
                return;
            }
            if (mcDao.hasMeta(trade.getOpenId(), meta.getId())) {
                log.warn("user {} already has meta {},maybe duplicate notify", trade.getOpenId(), meta.getId());
                return;
            }
            String code = StringUtils.leftPad(String.valueOf(meta.getId()), 5, '0');
            String maxCode = mcDao.maxMembershipCardCode(meta.getId());
            if (maxCode == null) {
                code = code + StringUtils.leftPad("1", 5, '0');
            } else {
                code = code + StringUtils.leftPad(String.valueOf(Integer.parseInt(maxCode.substring(5)) + 1), 5, '0');
            }
            mcDao.createMembershipCard(trade.getOpenId(), meta, code, DateUtils.addMonths(new Date(), meta.getExtendMonth()));
        }
    }
}

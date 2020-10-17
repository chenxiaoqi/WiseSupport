package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.user.User;
import com.lazyman.timetennis.wp.BasePayController;
import com.lazyman.timetennis.wp.Trade;
import com.lazyman.timetennis.wp.TradeEvent;
import com.lazyman.timetennis.wp.WePayService;
import com.wisesupport.commons.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/mc")
@Slf4j
public class MembershipCardUserController extends BasePayController implements ApplicationListener<TradeEvent> {
    private MembershipCardDao mcDao;

    private MembershipCardBillDao billDao;

    private MembershipCardService cardService;

    public MembershipCardUserController(MembershipCardDao mcDao, MembershipCardBillDao billDao, MembershipCardService cardService) {
        this.mcDao = mcDao;
        this.billDao = billDao;
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    public List<MembershipCard> useCards(User user) {
        return mcDao.userCards(user.getOpenId());
    }

    @PostMapping("/purchase")
    @Transactional
    public synchronized Map<String, String> purchase(User user, @RequestParam int metaId) {
        if (mcDao.hasMeta(user.getOpenId(), metaId)) {
            throw new BusinessException("你已经有该会员卡,无需购买");
        }
        if (payDao.hasWaitForPay(user.getOpenId())) {
            throw new BusinessException("您的购买订单确认中,请稍等");
        }
        MembershipCardMeta meta = mcDao.loadMeta(metaId);
        if (!meta.getStatus().equals("ol")) {
            throw new BusinessException("对不起,该会员卡已经下线,不能购买");
        }

        String mchId = meta.getArena().getMchId();
        String tradeNo = WePayService.creatTradeNo(Constant.PRODUCT_CARD);
        return preparePay(tradeNo, mchId, user.getOpenId(), Constant.PRODUCT_CARD, meta.getPrice(), "购买会员卡[" + meta.getName() + "]", () -> mcDao.createTradeMembershipCardRelation(tradeNo, metaId));
    }

    @PostMapping("/recharge")
    @Transactional
    public synchronized Map<String, String> recharge(User user,
                                                     @RequestParam @NotEmpty String code) {

        if (payDao.hasWaitForPay(user.getOpenId())) {
            throw new BusinessException("您的购买订单确认中,请稍等");
        }
        MembershipCard card = mcDao.loadCard(code);
        MembershipCardMeta meta = mcDao.loadMeta(card.getMeta().getId());
        String mchId = meta.getArena().getMchId();
        String tradeNo = WePayService.creatTradeNo(Constant.PRODUCT_RECHARGE);
        return preparePay(tradeNo, mchId, user.getOpenId(),
                Constant.PRODUCT_RECHARGE,
                card.getMeta().getInitialBalance(),
                "会员卡[" + meta.getName() + "]充值",
                () -> mcDao.createTradeMembershipCardChargeRelation(tradeNo, code));
    }

    @GetMapping("/arena/cards")
    public List<MembershipCard> userCardsInArena(User user,
                                                 @RequestParam int arenaId) {
        return mcDao.userCardsInArena(user.getOpenId(), arenaId);
    }

    @Override
    @Transactional
    public void onApplicationEvent(TradeEvent event) {
        Trade trade = event.getTrade();
        if (Constant.PRODUCT_CARD.equals(trade.getProductType())) {
            onPurchase(trade);
        } else if (Constant.PRODUCT_RECHARGE.equals(trade.getProductType())) {
            onRecharge(trade);
        }
    }

    private void onRecharge(Trade trade) {
        log.info("receive membership card trade[{}] event, in status {}", trade.getTradeNo(), trade.getStatus());
        if ("ok".equals(trade.getStatus())) {
            if (mcDao.changeToFinished(trade.getTradeNo()) == 1) {
                MembershipCard card = mcDao.loadCardByTradeNo(trade.getTradeNo());
                int balance = mcDao.recharge(card.getCode(), trade.getFee());
                mcDao.extendExpireDate(card.getMeta().getExtendMonth(), card.getCode());
                billDao.add(trade.getTradeNo(), trade.getOpenId(), card.getCode(), Constant.PRODUCT_RECHARGE, trade.getFee(), balance, new Date());
            } else {
                log.info("duplicate recharge event trade {}", trade.getTradeNo());
            }
        }
    }

    private void onPurchase(Trade trade) {
        log.info("receive membership card trade[{}] event, in status {}", trade.getTradeNo(), trade.getStatus());
        if ("ok".equals(trade.getStatus())) {
            MembershipCardMeta meta = mcDao.getMetaByTradeNo(trade.getTradeNo());
            if (meta == null) {
                log.error("could not found meta for trade {}", trade.getTradeNo());
                return;
            }
            cardService.create(meta, trade.getOpenId());
        }
    }
}

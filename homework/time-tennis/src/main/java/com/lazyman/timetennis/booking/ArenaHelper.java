package com.lazyman.timetennis.booking;

import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.arena.Court;
import com.lazyman.timetennis.menbership.MembershipCardMeta;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.Validate;
import org.springframework.util.CollectionUtils;

import java.util.Date;

final class ArenaHelper {
    private ArenaHelper() {
    }

    static void verifyStatus(Arena arena) {
        if (!arena.getStatus().equals("ol")) {
            throw new BusinessException("对不起,该场馆暂时下线,不能预定!");
        }
    }

    static void verifyRules(Arena arena, int countId, Date date, int startTime) {
        Court find = findCourt(arena, countId);

        if (!CollectionUtils.isEmpty(find.getDisableRules())) {
            if (!BookingTool.isBookable(find.getDisableRules(), date, startTime, startTime + 1)) {
                throw new BusinessException("时间段不可预定");
            }
        }
    }

    public static int calcFee(Arena dbArena, Date date, int startTime, int endTime, int courtId) {
        Court court = findCourt(dbArena, courtId);
        if (dbArena.getBookStyle() == 2) {
            return BookingTool.calcFeeV2(court.getFeeRules(), date, startTime, court.getFee(), courtId);
        } else {
            return BookingTool.calcFee(court.getFeeRules(), date, startTime, endTime, court.getFee(), courtId);
        }
    }

    private static Court findCourt(Arena arena, int courtId) {
        Court find = null;
        for (Court court : arena.getCourts()) {
            if (courtId == court.getId()) {
                find = court;
                break;
            }
        }
        Validate.notNull(find, "court %s not found in arena %s", courtId, arena.getId());
        return find;
    }

    public static void verifyHasMeta(Arena arena, int id) {
        for (MembershipCardMeta meta : arena.getMetas()) {
            if (meta.getId() == id) {
                return;
            }
        }
        throw new BusinessException("会员卡不能在该场馆使用");
    }
}


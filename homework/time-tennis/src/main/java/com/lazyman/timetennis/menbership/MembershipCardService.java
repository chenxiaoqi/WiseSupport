package com.lazyman.timetennis.menbership;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class MembershipCardService {
    private MembershipCardDao mcDao;

    public MembershipCardService(MembershipCardDao mcDao) {
        this.mcDao = mcDao;
    }

    public void create(MembershipCardMeta meta, String openId) {
        if (mcDao.hasMeta(openId, meta.getId())) {
            log.warn("user {} already has meta {},maybe duplicate notify", openId, meta.getId());
            return;
        }
        String code = StringUtils.leftPad(String.valueOf(meta.getId()), 5, '0');
        String maxCode = mcDao.maxMembershipCardCode(meta.getId());
        if (maxCode == null) {
            code = code + StringUtils.leftPad("1", 5, '0');
        } else {
            code = code + StringUtils.leftPad(String.valueOf(Integer.parseInt(maxCode.substring(5)) + 1), 5, '0');
        }
        mcDao.createMembershipCard(openId, meta, code, DateUtils.addMonths(new Date(), meta.getExtendMonth()));
    }
}

package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/user/mc")
public class UserBillController {
    private MembershipCardDao mcDao;
    private MembershipCardBillDao dao;

    public UserBillController(MembershipCardDao mcDao, MembershipCardBillDao dao) {
        this.mcDao = mcDao;
        this.dao = dao;
    }

    @GetMapping("/bills")
    public List<MembershipCardBill> bills(User user, @RequestParam @NotEmpty String code) {
        MembershipCard card = mcDao.loadCard(code);
        if (!user.isArenaAdmin() && !card.getOpenId().equals(user.getOpenId())) {
            throw new BusinessException("没有权限");
        }
        return dao.bills(code);
    }
}


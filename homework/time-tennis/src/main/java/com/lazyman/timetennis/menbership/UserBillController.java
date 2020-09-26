package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/mc")
public class UserBillController {
    private MembershipCardBillDao dao;

    public UserBillController(MembershipCardBillDao dao) {
        this.dao = dao;
    }

    @GetMapping("/bills")
    public List<MembershipCardBill> bills(User user, @RequestParam String code) {
        return dao.userBill(user.getOpenId(), code);
    }
}


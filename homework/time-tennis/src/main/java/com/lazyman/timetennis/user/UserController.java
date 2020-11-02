package com.lazyman.timetennis.user;

import com.alibaba.fastjson.JSON;
import com.lazyman.timetennis.core.SecurityUtils;
import com.lazyman.timetennis.core.WeXinService;
import com.lazyman.timetennis.core.WeXinToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class UserController {

    private UserMapper userMapper;

    private WeXinService weXinService;

    public UserController(UserMapper userMapper, WeXinService weXinService) {
        this.userMapper = userMapper;
        this.weXinService = weXinService;
    }

    @GetMapping("/users")
    public List<User> users(String nickname) {
        return userMapper.query(nickname);
    }

    @GetMapping("/user/{openId}")
    public User user(@PathVariable @Size(min = 4, max = 64) String openId) {
        User u = userMapper.selectByPrimaryKey(openId);
        Validate.notNull(u);
        return u;
    }

    @PostMapping("/user/decode_phone_number")
    public String decodePhoneNumber(@RequestParam @NotEmpty String jsCode,
                                    @RequestParam @NotEmpty String encData,
                                    @RequestParam @NotEmpty String iv) throws IOException {
        WeXinToken token = weXinService.getWeXinToken(jsCode);
        String json = new String(SecurityUtils.aesDecrypt(Base64.decodeBase64(encData), Base64.decodeBase64(token.getSessionKey()), Base64.decodeBase64(iv)), StandardCharsets.UTF_8);
        String phoneNumber = JSON.parseObject(json).getString("purePhoneNumber");
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setOpenId(token.getOpenId());
        userMapper.updateByPrimaryKey(user);
        return phoneNumber;
    }
}

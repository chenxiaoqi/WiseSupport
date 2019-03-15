package com.wisesupport.test.api;

import com.wisesupport.Swagger2Configuration;
import com.wisesupport.user.User;
import com.wisesupport.user.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *  * Author chenxiaoqi on 2019-03-02.
 *   */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {Swagger2Configuration.class})
public class ServiceSideTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMapper userMapper;

    @Test
    public void test() throws Exception {
        User user = new User();
        user.setAccount("ryan");
        user.setPassword("ryan");

        given(userMapper.findByAccount("ryan")).willReturn(user);
        mockMvc.perform(post("/login/sign_in")
                .param("account", "ryan")
                .param("password", "ryan"))
                .andExpect(view()
                        .name("redirect:/user/user_list"));

        mockMvc.perform(post("/login/sign_in")
                .param("account", "ryan")
                .param("password", "ryan1"))
                .andExpect(view()
                        .name("sign_in"));
    }

    @Test
    public void testJson() throws Exception {
        mockMvc.perform(get("/validate/binding")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .param("name", "cxq")
                .param("address", "LTO street 3")
                .param("age", "5")
                .param("birthDate", "2019-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.name").value("cxq"));
    }

    @Test
    @WithMockUser("ROLE_ADMIN")
    public void testAdd() throws Exception {
        this.mockMvc.perform(get("/user/user_list")).andExpect(status().isOk());
    }
}


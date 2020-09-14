package com.lazyman.timetennis.admin;

import com.lazyman.timetennis.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

@Component
public class RoleDao {

    private JdbcTemplate template;

    public RoleDao(JdbcTemplate template) {
        this.template = template;
    }

    void grant(String openId, String roleName) {
        template.update("insert into user_role (open_id, role_name) values (?,?)", openId, roleName);
    }

    public boolean isSuperAdmin(String openId) {
        return isRoleOf(openId, "super");
    }

    public boolean isAreaAdmin(String openId) {
        return isRoleOf(openId, "arena_admin");
    }

    public boolean isAccountant(String openId) {
        return isRoleOf(openId, "account");
    }

    int revoke(String openId, String roleName) {
        return template.update("delete  from user_role where open_id=? and role_name=?", openId, roleName);
    }

    List<User> arenaAdmins() {
        return template.query("select a.open_id,a.wx_nickname,a.avatar from user_role b, tt_user a where a.open_id = b.open_id and b.role_name='arena_admin'", (rs, rowNum) -> {
            User user = new User();
            user.setOpenId(rs.getString("open_id"));
            user.setWxNickname(rs.getString("wx_nickname"));
            user.setAvatar(rs.getString("avatar"));
            return user;
        });
    }

    private boolean isRoleOf(String openId, String role) {
        return Objects.requireNonNull(template.query("select 1 from user_role where open_id=? and role_name=?", ResultSet::next, openId, role));
    }
}

package com.lazyman.timetennis.arena;

import com.wisesupport.commons.exceptions.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.Objects;

@Component
public class ArenaPrivilege {

    private JdbcTemplate template;

    public ArenaPrivilege(JdbcTemplate template) {
        this.template = template;
    }

    public void requireAdministrator(String openId, int arenaId) {
        if (!isRoleOf(openId, arenaId, "admin")) {
            throw new BusinessException("你不是该场馆管理员");
        }
    }

    public void requireAccountant(String openId, int arenaId) {
        if (!isRoleOf(openId, arenaId, "account")) {
            throw new BusinessException("你不是该场馆会计");
        }
    }

    private boolean isRoleOf(String openId, int arenaId, String role) {
        return Objects.requireNonNull(template.query("select 1 from arena_role where open_id=? and arena_id=? and role=?", ResultSet::next, openId, arenaId, role));
    }
}

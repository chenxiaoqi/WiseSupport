package com.lazyman.timetennis.arena;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FavoriteDao {

    private JdbcTemplate template;

    public FavoriteDao(JdbcTemplate template) {
        this.template = template;
    }

    public void add(String openId, int arenaId) {
        template.update("insert into arena_favorite (open_id, arena_id) values (?,?)", openId, arenaId);
    }

    void delete(String openId, int arenaId) {
        template.update("delete from arena_favorite where open_id=? and arena_id=?", openId, arenaId);
    }

    List<Arena> byOpenId(String openId) {
        return template.query("select a.name,a.id,a.images,a.district from arena_favorite b,arena a where b.arena_id=a.id and b.open_id=? limit 25", (rs, rowNum) -> {
            Arena arena = new Arena();
            arena.setId(rs.getInt("id"));
            arena.setDistrict(rs.getString("district"));
            arena.setName(rs.getString("name"));
            arena.setImages(StringUtils.split(rs.getString("images"), ','));
            return arena;
        }, openId);
    }
}

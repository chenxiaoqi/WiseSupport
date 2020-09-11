package com.lazyman.timetennis.arena;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArenaDao {
    private JdbcTemplate template;

    public ArenaDao(JdbcTemplate template) {
        this.template = template;
    }

    public List<Arena> arenas() {
        return template.query("select id, name,location,images,book_style from arena", (rs, rowNum) -> {
            Arena arena = new Arena();
            arena.setId(rs.getInt("id"));
            arena.setName(rs.getString("name"));
            arena.setLocation(rs.getString("location"));
            arena.setBookStyle(rs.getInt("book_style"));
            arena.setImages(StringUtils.split(rs.getString("images"), ','));
            return arena;
        });
    }

    public Arena load(int id) {
        return template.queryForObject("select id,name,location,address,images,book_style,phone,introduction,advance_book_days,book_start_hour,book_end_hour from arena where id=?", (rs, rowNum) -> {
            Arena result = new Arena();
            result.setId(rs.getInt("id"));
            result.setName(rs.getString("name"));
            result.setLocation(rs.getString("location"));
            result.setAddress(rs.getString("address"));
            result.setImages(StringUtils.split(rs.getString("images"), ','));
            result.setBookStyle(rs.getInt("book_style"));
            result.setPhone(rs.getString("phone"));
            result.setIntroduction(rs.getString("introduction"));
            result.setAdvanceBookDays(rs.getInt("advance_book_days"));
            result.setBookStartHour(rs.getInt("book_start_hour"));
            result.setBookEndHour(rs.getInt("book_end_hour"));
            return result;
        }, id);
    }


}

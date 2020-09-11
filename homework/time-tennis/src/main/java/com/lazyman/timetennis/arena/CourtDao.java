package com.lazyman.timetennis.arena;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class CourtDao {
    private JdbcTemplate template;

    public CourtDao(JdbcTemplate template) {
        this.template = template;
    }

    public List<Court> courts(int id) {
        return template.query("select id,name,fee from court where arena_id=?", new RowMapper<Court>() {
            @Override
            public Court mapRow(ResultSet rs, int rowNum) throws SQLException {
                Court court = new Court();
                court.setId(rs.getInt("id"));
                court.setName(rs.getString("name"));
                court.setFee(rs.getInt("fee"));
                return court;
            }
        }, id);
    }
}

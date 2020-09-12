package com.lazyman.timetennis.arena;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;
import java.util.Objects;

@Component
public class CourtDao {
    private JdbcTemplate template;

    public CourtDao(JdbcTemplate template) {
        this.template = template;
    }

    List<Court> courts(int id) {
        return template.query("select id,name,fee from court where arena_id=?", (rs, rowNum) -> {
            Court court = new Court();
            court.setId(rs.getInt("id"));
            court.setName(rs.getString("name"));
            court.setFee(rs.getInt("fee"));
            return court;
        }, id);
    }

    void delete(int id) {
        template.update("delete from court where id=?", id);
    }

    public int insert(int arenaId, String name, int fee) {
        PreparedStatementCreatorFactory psf =
                new PreparedStatementCreatorFactory("insert into court(arena_id,name,fee) values (?,?,?)", Types.INTEGER, Types.VARCHAR, Types.INTEGER);
        psf.setReturnGeneratedKeys(true);
        KeyHolder holder = new GeneratedKeyHolder();
        template.update(psf.newPreparedStatementCreator(new Object[]{arenaId, name, fee}), holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    void update(int id, String name, int fee) {
        template.update("update court set name=?,fee=? where id=?", name, fee, id);
    }

    Court load(int id) {
        return template.queryForObject("select id,name,fee from court where id=?", (rs, rowNum) -> {
            Court court = new Court();
            court.setId(rs.getInt("id"));
            court.setFee(rs.getInt("fee"));
            court.setName(rs.getString("name"));
            return court;
        }, id);
    }
}

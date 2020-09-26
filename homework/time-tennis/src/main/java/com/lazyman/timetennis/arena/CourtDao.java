package com.lazyman.timetennis.arena;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Objects;

@Component
public class CourtDao {
    private JdbcTemplate template;

    public CourtDao(JdbcTemplate template) {
        this.template = template;
    }

    public List<Court> courts(int id) {
        return template.query("select id,name,fee,status from court where arena_id=?", (rs, rowNum) -> {
            Court court = new Court();
            populateCourt(court, rs);
            return court;
        }, id);
    }

    public List<Court> onLineCourts(int id) {
        return template.query("select id,name,fee,status from court where arena_id=? and status='ol'", (rs, rowNum) -> {
            Court court = new Court();
            populateCourt(court, rs);
            return court;
        }, id);
    }

    private void populateCourt(Court court, ResultSet rs) throws SQLException {
        court.setId(rs.getInt("id"));
        court.setName(rs.getString("name"));
        court.setFee(rs.getInt("fee"));
        court.setStatus(rs.getString("status"));
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

    public Court load(int id) {
        return template.queryForObject("select id,name,fee,arena_id from court where id=?", (rs, rowNum) -> {
            Court court = new Court();
            court.setId(rs.getInt("id"));
            court.setFee(rs.getInt("fee"));
            court.setName(rs.getString("name"));
            court.setArenaId(rs.getInt("arena_id"));
            return court;
        }, id);
    }
}

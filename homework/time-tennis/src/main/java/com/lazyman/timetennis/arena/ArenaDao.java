package com.lazyman.timetennis.arena;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
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
public class ArenaDao {
    private JdbcTemplate template;

    public ArenaDao(JdbcTemplate template) {
        this.template = template;
    }

    public List<Arena> searchArena(String city, String name) {
        Object[] params = new Object[StringUtils.isEmpty(name) ? 1 : 2];
        params[0] = city;
        String sql = "select id, name,district,images,book_style from arena where city=? ";
        if (!StringUtils.isEmpty(name)) {
            sql = sql + "and name like concat('%',?,'%')";
            params[1] = name;
        }
        return template.query(sql, (rs, rowNum) -> {
            Arena arena = new Arena();
            arena.setId(rs.getInt("id"));
            arena.setName(rs.getString("name"));
            arena.setDistrict(rs.getString("district"));
            arena.setBookStyle(rs.getInt("book_style"));
            arena.setImages(StringUtils.split(rs.getString("images"), ','));
            return arena;
        }, params);
    }

    public Arena load(int id) {
        return template.queryForObject("select id,name,type,province,city,district,address,images,book_style,phone,introduction,advance_book_days,book_start_hour,book_end_hour from arena where id=?", (rs, rowNum) -> {
            Arena result = new Arena();
            populateArenaProperties(rs, result);
            result.setType(rs.getInt("type"));
            result.setAddress(rs.getString("address"));
            result.setImages(StringUtils.split(rs.getString("images"), ','));
            result.setBookStyle(rs.getInt("book_style"));
            result.setPhone(rs.getString("phone"));
            result.setIntroduction(rs.getString("introduction"));
            return result;
        }, id);
    }

    public List<Arena> arenas(String openId) {
        return template.query("select b.id,b.name,b.province,b.city,b.district,b.advance_book_days,b.book_start_hour,b.book_end_hour from arena_role a,arena b where a.arena_id=b.id and a.role='admin' and a.open_id=?", (rs, rowNum) -> {
                    Arena arena = new Arena();
                    populateArenaProperties(rs, arena);
                    return arena;
                },
                openId);
    }

    private void populateArenaProperties(ResultSet rs, Arena result) throws SQLException {
        result.setId(rs.getInt("id"));
        result.setName(rs.getString("name"));
        result.setProvince(rs.getString("province"));
        result.setCity(rs.getString("city"));
        result.setDistrict(rs.getString("district"));
        result.setAdvanceBookDays(rs.getInt("advance_book_days"));
        result.setBookStartHour(rs.getInt("book_start_hour"));
        result.setBookEndHour(rs.getInt("book_end_hour"));
    }

    public int insert(Arena arena) {
        String sql = "insert into arena (name, type, address, province, city, district, phone, introduction, advance_book_days, book_start_hour, book_end_hour) values (?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatementCreatorFactory psf =
                new PreparedStatementCreatorFactory(sql,
                        Types.VARCHAR,
                        Types.VARCHAR,
                        Types.NVARCHAR,
                        Types.NVARCHAR,
                        Types.NVARCHAR,
                        Types.NVARCHAR,
                        Types.NVARCHAR,
                        Types.NVARCHAR,
                        Types.INTEGER,
                        Types.INTEGER,
                        Types.INTEGER);
        psf.setReturnGeneratedKeys(true);
        KeyHolder holder = new GeneratedKeyHolder();
        template.update(psf.newPreparedStatementCreator(
                new Object[]{
                        arena.getName(),
                        arena.getType(),
                        arena.getAddress(),
                        arena.getProvince(),
                        arena.getCity(),
                        arena.getDistrict(),
                        arena.getPhone(),
                        arena.getIntroduction(),
                        arena.getAdvanceBookDays(),
                        arena.getBookStartHour(),
                        arena.getBookEndHour()
                }
        ), holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    public void updateImages(int id, String images) {
        template.update("update arena set images=? where id=?", images, id);
    }

    public void setRole(int id, String openId, String role) {
        template.update("insert into arena_role (arena_id, open_id, role) value (?,?,?)", id, openId, role);
    }

    public void update(Arena arena) {
        String sql = "update arena set name=?,type=?,province=?,city=?,district=?,address=?,phone=?,advance_book_days=?,book_start_hour=?,book_end_hour=?,introduction=?,images=? where id=?";
        template.update(sql,
                arena.getName(),
                arena.getType(),
                arena.getProvince(),
                arena.getCity(),
                arena.getDistrict(),
                arena.getAddress(),
                arena.getPhone(),
                arena.getAdvanceBookDays(),
                arena.getBookStartHour(),
                arena.getBookEndHour(),
                arena.getIntroduction(),
                StringUtils.join(arena.getImages(), ','),
                arena.getId());
    }

    public void delete(int id) {
        template.update("delete from arena_role where arena_id=?", id);
        template.update("delete from rule where arena_id=?", id);
        template.update("delete from arena where id=?", id);
        template.update("delete from court_rule_r where court_id in(select id from court where arena_id=?)", id);
        template.update("delete from court where arena_id=?", id);
        //todo 会员卡,会员卡用户 场馆上线后就不允许删除？
    }

    public boolean isArenaAdmin(String openId, int arenaId) {
        return Objects.requireNonNull(template.query("select 1 from arena_role where open_id=? and arena_id=? and role='admin'", ResultSet::next, openId, arenaId));
    }

    @Cacheable("arena.cities")
    public List<String> cities() {
        return template.query("select distinct(city) as city from arena order by city", (rs, rowNum) -> rs.getString(1));
    }
}

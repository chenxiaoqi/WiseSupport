package com.lazyman.timetennis.arena;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.menbership.MembershipCardDao;
import com.lazyman.timetennis.user.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ArenaDao {

    private final JdbcTemplate template;

    private final CourtDao courtDao;

    private final RuleDao ruleDao;

    private final MembershipCardDao mcDao;

    public ArenaDao(JdbcTemplate template, CourtDao courtDao, RuleDao ruleDao, MembershipCardDao mcDao) {
        this.template = template;
        this.courtDao = courtDao;
        this.ruleDao = ruleDao;
        this.mcDao = mcDao;
    }

    public Arena load(int id) {
        return template.queryForObject("select id,name,type,province,city,district,address,images,book_style,phone,introduction,advance_book_days,book_start_hour,book_end_hour,status,receiver_id,receiver_type,allow_half_hour,book_at_least,refund_advance_hours,refund_times_limit,charge_strategy,book_hours_limit,latitude,longitude from arena where id=?", (rs, rowNum) -> {
            Arena result = new Arena();
            populateArenaProperties(rs, result);
            result.setType(rs.getInt("type"));
            result.setAddress(rs.getString("address"));
            result.setImages(StringUtils.split(rs.getString("images"), ','));
            result.setBookStyle(rs.getInt("book_style"));
            result.setPhone(rs.getString("phone"));
            result.setIntroduction(rs.getString("introduction"));
            result.setAllowHalfHour(rs.getBoolean("allow_half_hour"));
            result.setBookAtLeast(rs.getInt("book_at_least"));
            result.setRefundAdvanceHours(rs.getInt("refund_advance_hours"));
            result.setRefundTimesLimit(rs.getInt("refund_times_limit"));
            result.setChargeStrategy(rs.getInt("charge_strategy"));
            result.setBookHoursLimit(rs.getInt("book_hours_limit"));
            result.setLatitude(rs.getFloat("latitude"));
            result.setLongitude(rs.getFloat("longitude"));
            return result;
        }, id);
    }

    @Cacheable(value = Constant.CK_ARENA, key = "#id")
    public Arena loadFull(int id) {
        Arena arena = load(id);
        List<Court> courts = courtDao.onLineCourts(arena.getId());
        if (!courts.isEmpty()) {
            Object[] courtIds = courts.stream().map(Court::getId).toArray();
            List<Rule> rules = ruleDao.courtRules(courtIds);
            for (Court court : courts) {
                for (Rule rule : rules) {
                    if (court.getId() == rule.getCourtId()) {
                        if (rule.getType() == 1) {
                            court.getDisableRules().add(rule);
                        } else {
                            court.getFeeRules().add(rule);
                        }
                    }
                }
            }
            arena.setCourts(courts);
        }
        arena.setMetas(mcDao.byArenaId(id));
        return arena;
    }

    List<Arena> searchArena(String city, Integer type, String name) {
        List<Object> params = new ArrayList<>(3);
        String sql = "select id, name,district,images,book_style from arena where status='ol'";
        if (!StringUtils.isEmpty(city)) {
            sql = sql + " and city=?";
            params.add(city);
        }
        if (type != null) {
            sql = sql + " and type=?";
            params.add(type);
        }
        if (!StringUtils.isEmpty(name)) {
            sql = sql + " and name like concat('%',?,'%')";
            params.add(name);
        }
        return template.query(sql, (rs, rowNum) -> {
            Arena arena = new Arena();
            arena.setId(rs.getInt("id"));
            arena.setName(rs.getString("name"));
            arena.setDistrict(rs.getString("district"));
            arena.setBookStyle(rs.getInt("book_style"));
            arena.setImages(StringUtils.split(rs.getString("images"), ','));
            return arena;
        }, params.toArray());
    }

    List<Arena> arenas(String openId) {
        return template.query("select b.id,b.name,b.province,b.city,b.district,b.advance_book_days,b.book_start_hour,b.book_end_hour,b.status,b.receiver_id,b.receiver_type from arena_role a,arena b where a.arena_id=b.id and a.role='admin' and a.open_id=?", (rs, rowNum) -> {
                    Arena arena = new Arena();
                    populateArenaProperties(rs, arena);
                    return arena;
                },
                openId);
    }

    @CacheEvict(value = Constant.CK_CITIES, allEntries = true)
    public int insert(Arena arena) {
        String sql = "insert into arena (name, type, address, province, city, district, phone, introduction, advance_book_days, book_start_hour, book_end_hour,allow_half_hour,book_at_least,refund_times_limit,refund_advance_hours,charge_strategy,book_hours_limit,latitude,longitude) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                        Types.INTEGER,
                        Types.BOOLEAN,
                        Types.TINYINT,
                        Types.TINYINT,
                        Types.TINYINT,
                        Types.TINYINT,
                        Types.TINYINT,
                        Types.NUMERIC,
                        Types.NUMERIC);
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
                        arena.getBookEndHour(),
                        arena.getAllowHalfHour(),
                        arena.getBookAtLeast(),
                        arena.getRefundTimesLimit(),
                        arena.getRefundAdvanceHours(),
                        arena.getChargeStrategy(),
                        arena.getBookHoursLimit(),
                        arena.getLatitude(),
                        arena.getLongitude()
                }
        ), holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    void updateImages(int id, String images) {
        template.update("update arena set images=? where id=?", images, id);
    }

    void setRole(int id, String openId, String role) {
        template.update("insert into arena_role (arena_id, open_id, role) value (?,?,?)", id, openId, role);
    }

    @Caching(evict = {
            @CacheEvict(value = Constant.CK_CITIES, allEntries = true),
            @CacheEvict(value = Constant.CK_ARENA, key = "#arena.id")
    })
    public void update(Arena arena) {
        String sql = "update arena set name=?,type=?,province=?,city=?,district=?,address=?,phone=?,advance_book_days=?,book_start_hour=?,book_end_hour=?,introduction=?,images=?,allow_half_hour=?,book_at_least=?,refund_advance_hours=?,refund_times_limit=?,charge_strategy=?,book_hours_limit=?,latitude=?,longitude=?,receiver_id=?,receiver_type=? where id=?";
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
                arena.getAllowHalfHour(),
                arena.getBookAtLeast(),
                arena.getRefundAdvanceHours(),
                arena.getRefundTimesLimit(),
                arena.getChargeStrategy(),
                arena.getBookHoursLimit(),
                arena.getLatitude(),
                arena.getLongitude(),
                arena.getReceiverId(),
                arena.getReceiverType(),
                arena.getId());
    }

    @Cacheable(Constant.CK_CITIES)
    public List<String> cities() {
        return template.query("select distinct(city) as city from arena order by city", (rs, rowNum) -> rs.getString(1));
    }

    @CacheEvict(cacheNames = Constant.CK_ARENA, key = "#arenaId")
    public void updateArenaStatus(int arenaId, String status) {
        template.update("update arena set status=? where id=?", status, arenaId);
    }

    @CacheEvict(cacheNames = Constant.CK_ARENA, key = "#arenaId")
    public void updateCourtStatus(int courtId, int arenaId, String status) {
        template.update("update court set status=? where id=? and arena_id=?", status, courtId, arenaId);
    }

    List<Arena> byName(String name) {
        return template.query("select id,name,city,district,status,receiver_id,receiver_type from arena where name like concat('%',?,'%') limit 20", (rs, rowNum) -> {
            Arena result = new Arena();
            result.setId(rs.getInt("id"));
            result.setName(rs.getString("name"));
            result.setDistrict(rs.getString("district"));
            result.setCity(rs.getString("city"));
            result.setStatus(rs.getString("status"));
            result.setReceiverId(rs.getString("receiver_id"));
            result.setReceiverType(rs.getObject("receiver_type", Integer.class));
            return result;
        }, name);
    }

    public List<Integer> arenaIds() {
        return template.query("select id from arena", (rs, rowNum) -> rs.getInt("id"));
    }

    List<User> admins(int arenaId) {
        return template.query("select a.open_id,a.wx_nickname,a.avatar from arena_role b, tt_user a where a.open_id = b.open_id and b.role='admin' and b.arena_id=?", (rs, rowNum) -> {
            User user = new User();
            user.setOpenId(rs.getString("open_id"));
            user.setWxNickname(rs.getString("wx_nickname"));
            user.setAvatar(rs.getString("avatar"));
            return user;
        }, arenaId);
    }

    void deleteRole(int arenaId, String openId, String role) {
        template.update("delete from arena_role where arena_id=? and open_id=? and role=?", arenaId, openId, role);
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
        result.setStatus(rs.getString("status"));
        result.setReceiverId(rs.getString("receiver_id"));
        result.setReceiverType(rs.getObject("receiver_type", Integer.class));
    }

    public List<Integer> arenaIdsByChargeStrategy(int chargeStrategy) {
        return template.query("select id from arena where charge_strategy=?", (rs, rowNum) -> rs.getInt("id"), chargeStrategy);
    }
}

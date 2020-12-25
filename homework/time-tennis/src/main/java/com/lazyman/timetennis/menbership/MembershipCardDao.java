package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.Constant;
import com.lazyman.timetennis.arena.Arena;
import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class MembershipCardDao {

    private final JdbcTemplate template;

    public MembershipCardDao(JdbcTemplate template) {
        this.template = template;
    }

    void createMeta(int arenaId, String name, int initialBalance, int discount, int price, int extendMonth) {
        template.update("insert into membership_card_meta(arena_id,name,initial_balance,discount,price,extend_month) values (?,?,?,?,?,?)",
                arenaId, name, initialBalance, discount, price, extendMonth);
    }

    @CacheEvict(cacheNames = Constant.CK_ARENA, key = "#arenaId")
    public void deleteMeta(int id, int arenaId) {
        template.update("delete from membership_card_meta where id=?", id);
    }

    @CacheEvict(cacheNames = Constant.CK_ARENA, key = "#arenaId")
    public int updateMeta(int id, int arenaId, String name, int initialBalance, int discount, int price, int extendMonth) {
        return template.update("update membership_card_meta set name=?,initial_balance=?,discount=?,price=?,extend_month=? where id=?", name, initialBalance, discount, price, extendMonth, id);
    }

    @CacheEvict(cacheNames = Constant.CK_ARENA, key = "#arenaId")
    public void metaOffline(int arenaId, int id) {
        this.changeMetaStatus(id, "ofl");
    }

    @CacheEvict(cacheNames = Constant.CK_ARENA, key = "#arenaId")
    public void metaOnline(int arenaId, int id) {
        this.changeMetaStatus(id, "ol");
    }

    public List<MembershipCardMeta> byArenaId(int arenaId) {
        return template.query("select b.id,b.name,b.arena_id,b.initial_balance,b.discount,b.price,b.extend_month,b.status from membership_card_meta b,arena a where a.id=b.arena_id and b.arena_id =?", (rs, rowNum) -> {
            MembershipCardMeta meta = new MembershipCardMeta();
            populateMeta(rs, meta);
            return meta;
        }, arenaId);
    }

    MembershipCardMeta getMetaByTradeNo(String tradeNo) {
        return template.query("select a.id,a.name,a.initial_balance,a.discount,a.price,a.extend_month,a.status from trade_membership_card_meta_r b, membership_card_meta a where b.meta_id=a.id and  b.trade_no=? limit 1", rs -> {
            if (rs.next()) {
                MembershipCardMeta meta = new MembershipCardMeta();
                populateMeta(rs, meta);
                return meta;
            } else {
                return null;
            }
        }, tradeNo);
    }

    MembershipCardMeta loadMeta(int id) {
        return template.queryForObject("select a.id,a.name,a.arena_id,a.initial_balance,a.discount,a.price,a.extend_month,a.status,b.name as arena_name,b.receiver_id,b.receiver_type from membership_card_meta a,arena b where a.arena_id=b.id and a.id=?", (rs, rowNum) -> {
            MembershipCardMeta meta = new MembershipCardMeta();
            populateMeta(rs, meta);
            Arena arena = new Arena();
            arena.setId(rs.getInt("arena_id"));
            arena.setName(rs.getString("arena_name"));
            arena.setReceiverId(rs.getString("receiver_id"));
            arena.setReceiverType(rs.getObject("receiver_type", Integer.class));
            meta.setArena(arena);
            return meta;
        }, id);
    }

    private void populateMeta(ResultSet rs, MembershipCardMeta meta) throws SQLException {
        meta.setId(rs.getInt("id"));
        meta.setName(rs.getString("name"));
        meta.setDiscount(rs.getInt("discount"));
        meta.setInitialBalance(rs.getInt("initial_balance"));
        meta.setPrice(rs.getInt("price"));
        meta.setExtendMonth(rs.getInt("extend_month"));
        meta.setStatus(rs.getString("status"));
    }

    void createMembershipCard(String openId, MembershipCardMeta meta, String code, Date expireDate) {
        template.update("insert into membership_card(code, open_id, balance, meta_id,expire_date) values (?,?,?,?,?)", code, openId, meta.getInitialBalance(), meta.getId(), expireDate);
    }

    String maxMembershipCardCode(int metaId) {
        return template.query("select max(code) from membership_card where meta_id=?", rs -> {
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        }, metaId);
    }

    public List<MembershipCard> userCardsInArena(String openId, int arenaId) {
        return template.query("select a.code,a.open_id,a.balance,a.expire_date,c.id as meta_id, c.name,c.discount,c.initial_balance,c.extend_month from membership_card a,membership_card_meta c where a.meta_id=c.id and a.open_id=? and c.arena_id=?", (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            populateCard(card, rs);
            return card;
        }, openId, arenaId);
    }

    List<MembershipCard> userCards(String openId) {
        return template.query("select a.code,a.open_id,a.balance,a.expire_date,a.meta_id, b.name,b.discount,b.id as meta_id,b.initial_balance,b.extend_month,c.id as arena_id,c.name as arena_name from membership_card a,membership_card_meta b,arena c where a.meta_id=b.id and b.arena_id=c.id and a.open_id=?", (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            populateCard(card, rs);

            Arena arena = new Arena();
            arena.setId(rs.getInt("arena_id"));
            arena.setName(rs.getString("arena_name"));
            card.getMeta().setArena(arena);
            return card;
        }, openId);
    }

    public int chargeFee(String code, int fee, boolean ignoreLowerBalance) {
        int balance = Objects.requireNonNull(template.queryForObject("select balance from membership_card where code=?", Integer.class, code));
        int result = balance - fee;
        if (result < 0 && !ignoreLowerBalance) {
            throw new BusinessException("账户余额不足");
        }
        Validate.isTrue(template.update("update membership_card set balance=? where balance=? and code=?", result, balance, code) == 1, "扣费并发冲突");
        return result;
    }

    int recharge(String code, int fee) {
        int balance = Objects.requireNonNull(template.queryForObject("select balance from membership_card where code=?", Integer.class, code));
        int result = balance + fee;
        Validate.isTrue(template.update("update membership_card set balance=? where code=? and balance=?", result, code, balance) == 1, "充值并发冲突");
        return result;
    }

    public MembershipCard loadCard(String code) {
        return template.queryForObject("select a.code,a.meta_id,a.open_id,a.balance,a.expire_date,b.name,b.discount,b.initial_balance,b.extend_month from membership_card a,membership_card_meta b where a.meta_id=b.id and a.code=?", (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            populateCard(card, rs);
            return card;
        }, code);
    }

    boolean hasMeta(String openId, int metaId) {
        return Objects.requireNonNull(template.query("select 1 from membership_card where open_id=? and meta_id=? limit 1", ResultSet::next, openId, metaId));
    }

    void createTradeMembershipCardRelation(String tradeNo, int metaId) {
        template.update("insert into trade_membership_card_meta_r(trade_no, meta_id) values (?,?)", tradeNo, metaId);
    }

    void createTradeMembershipCardChargeRelation(String tradeNo, String code) {
        template.update("insert into trade_membership_card_recharge_r(trade_no, mc_code) values (?,?)", tradeNo, code);
    }

    private void populateCard(MembershipCard card, ResultSet rs) throws SQLException {
        card.setCode(rs.getString("code"));
        card.setBalance(rs.getInt("balance"));
        card.setExpireDate(rs.getDate("expire_date"));
        card.setOpenId(rs.getString("open_id"));

        MembershipCardMeta meta = new MembershipCardMeta();
        meta.setName(rs.getString("name"));
        meta.setDiscount(rs.getInt("discount"));
        meta.setId(rs.getInt("meta_id"));
        meta.setInitialBalance(rs.getInt("initial_balance"));
        meta.setExtendMonth(rs.getInt("extend_month"));
        card.setMeta(meta);
    }

    int changeToFinished(String tradeNo) {
        return template.update("update trade_membership_card_recharge_r set finished=1 where trade_no=? and finished=0", tradeNo);
    }

    MembershipCard loadCardByTradeNo(String tradeNo) {
        return template.queryForObject("select a.code,a.meta_id,a.open_id,a.balance,a.expire_date,b.name,b.discount,b.initial_balance,b.extend_month from trade_membership_card_recharge_r c, membership_card a,membership_card_meta b where c.mc_code=a.code and  a.meta_id=b.id and c.trade_no=?", (rs, rowNum) -> {
                    MembershipCard card = new MembershipCard();
                    populateCard(card, rs);
                    return card;
                },
                tradeNo);
    }

    boolean hasMember(int metaId) {
        return Objects.requireNonNull(template.query("select 1 from membership_card where meta_id=? limit 1", ResultSet::next, metaId));
    }

    private void changeMetaStatus(int id, String status) {
        template.update("update membership_card_meta set status=? where id=?", status, id);
    }

    List<MembershipCard> members(int metaId, String name, String code, String openId) {
        String sql = "select b.open_id,b.avatar,b.wx_nickname,a.code,a.balance,a.expire_date,a.create_time from membership_card a, tt_user b where a.open_id=b.open_id and a.meta_id=?";
        List<Object> args = new ArrayList<>();
        args.add(metaId);
        if (!StringUtils.isEmpty(name)) {
            args.add(name);
            sql = sql + " and b.wx_nickname like concat('%',?,'%')";
        }
        if (!StringUtils.isEmpty(code)) {
            args.add(code);
            sql = sql + " and a.code=?";
        }
        if (!StringUtils.isEmpty(openId)) {
            args.add(openId);
            sql = sql + " and a.open_id>?";
        }
        sql = sql + " order by a.open_id limit 50";
        return template.query(sql, (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            card.setBalance(rs.getInt("balance"));
            card.setCode(rs.getString("code"));
            card.setExpireDate(rs.getDate("expire_date"));
            card.setCreateTime(rs.getTimestamp("create_time"));
            User user = new User();
            user.setOpenId(rs.getString("open_id"));
            user.setAvatar(rs.getString("avatar"));
            user.setWxNickname(rs.getString("wx_nickname"));
            card.setUser(user);
            return card;
        }, args.toArray());
    }

    void extendExpireDate(int extendMonth, String code) {
        template.update("update membership_card set expire_date=? where code=?", DateUtils.addMonths(new Date(), extendMonth), code);
    }
}


package com.lazyman.timetennis.menbership;

import com.lazyman.timetennis.arena.Arena;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class MembershipCardDao {

    private JdbcTemplate template;

    public MembershipCardDao(JdbcTemplate template) {
        this.template = template;
    }

    int createMeta(String openId, String name, int initialBalance, int discount, int price, int extendMonth) {
        PreparedStatementCreatorFactory psf =
                new PreparedStatementCreatorFactory("insert into membership_card_meta(open_id,name,initial_balance,discount,price,extend_month) values (?,?,?,?,?,?)",
                        Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER);
        psf.setReturnGeneratedKeys(true);
        KeyHolder holder = new GeneratedKeyHolder();
        template.update(psf.newPreparedStatementCreator(new Object[]{openId, name, initialBalance, discount, price, extendMonth}), holder);
        return Objects.requireNonNull(holder.getKey()).intValue();
    }

    void deleteMeta(int id, String openId) {
        Validate.isTrue(template.update("delete from membership_card_meta where id=? and open_id=?", id, openId) == 1, "会员看元数据不存在");
        template.update("delete from membership_card_meta_arena_r where meta_id=?", id);
    }

    int updateMeta(String openId, int id, String name, int initialBalance, int discount, int price, int extendMonth) {
        return template.update("update membership_card_meta set name=?,initial_balance=?,discount=?,price=?,extend_month=? where id=? and open_id=?", name, initialBalance, discount, price, extendMonth, id, openId);
    }

    int metaOffline(String openId, int id) {
        return this.changeMetaStatus(openId, id, "ofl");
    }

    int metaOnline(String openId, int id) {
        return this.changeMetaStatus(openId, id, "ol");
    }

    List<MembershipCardMeta> byOpenId(String openId) {
        return template.query("select id,name,open_id,initial_balance,discount,price,extend_month,status from membership_card_meta where open_id=?", (rs, rowNum) -> {
            MembershipCardMeta meta = new MembershipCardMeta();
            populateMeta(rs, meta);
            return meta;
        }, openId);
    }

    public List<MembershipCardMeta> byArenaId(String arenaId) {
        return template.query("select b.id,b.name,b.open_id,b.initial_balance,b.discount,b.price,b.extend_month,b.status from membership_card_meta_arena_r a,membership_card_meta b where a.meta_id=b.id and a.arena_id =?", (rs, rowNum) -> {
            MembershipCardMeta meta = new MembershipCardMeta();
            populateMeta(rs, meta);
            return meta;
        }, arenaId);
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

    public MembershipCardMeta loadMeta(int id) {
        return template.queryForObject("select id,name,open_id,initial_balance,discount,price,extend_month,status from membership_card_meta a where id=?", (rs, rowNum) -> {
            MembershipCardMeta meta = new MembershipCardMeta();
            populateMeta(rs, meta);
            return meta;
        }, id);
    }

    public List<Arena> arenas(int metaId) {
        return template.query("select b.id,b.name from membership_card_meta_arena_r a,arena b where a.arena_id=b.id and a.meta_id=?", (rs, rowNum) -> {
            Arena arena = new Arena();
            arena.setId(rs.getInt("id"));
            arena.setName(rs.getString("name"));
            return arena;
        }, metaId);
    }

    private int changeMetaStatus(String openId, int id, String status) {
        return template.update("update membership_card_meta set status=? where id=? and open_id=?", status, id, openId);
    }

    void addMetaArenaRelation(int metaId, String arenaId) {
        template.update("insert into membership_card_meta_arena_r (arena_id, meta_id) value (?,?)", arenaId, metaId);
    }

    void deleteMetaArenaRelation(int metaId) {
        template.update("delete from membership_card_meta_arena_r where meta_id=?", metaId);
    }

    public void createMembershipCard(String openId, MembershipCardMeta meta, String code, Date expireDate) {
        template.update("insert into membership_card(code, open_id, balance, meta_id,expire_date) values (?,?,?,?,?)", code, openId, meta.getInitialBalance(), meta.getId(), expireDate);
    }

    public String maxMembershipCardCode(int metaId) {
        return template.query("select max(code) from membership_card where meta_id=?", rs -> {
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        }, metaId);
    }

    public List<MembershipCard> userCardsInArena(String openId, int arenaId) {
        return template.query("select a.code,a.open_id,a.balance,a.expire_date,c.id as meta_id, c.name,c.discount from membership_card a,membership_card_meta_arena_r b,membership_card_meta c where a.meta_id=b.meta_id and a.meta_id=c.id and a.open_id=? and b.arena_id=?", (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            populateCard(card, rs);
            return card;
        }, openId, arenaId);
    }

    public List<MembershipCard> userCards(String openId) {
        return template.query("select a.code,a.open_id,a.balance,a.expire_date,a.meta_id, b.name,b.discount,b.id as meta_id from membership_card a,membership_card_meta b where a.meta_id=b.id and a.open_id=?", (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            populateCard(card, rs);
            return card;
        }, openId);
    }

    public int chargeFee(String code, int fee) {
        return template.update("update membership_card set balance=balance-? where balance>=? and code=?", fee, fee, code);
    }

    public MembershipCard loadCard(String code) {
        return template.queryForObject("select a.code,a.meta_id,a.open_id,a.balance,a.expire_date,b.name,b.discount from membership_card a,membership_card_meta b where a.meta_id=b.id and a.code=?", (rs, rowNum) -> {
            MembershipCard card = new MembershipCard();
            populateCard(card, rs);
            return card;
        }, code);
    }

    private void populateCard(MembershipCard card, ResultSet rs) throws SQLException {
        card.setCode(rs.getString("code"));
        card.setBalance(rs.getInt("balance"));
        card.setExpireDate(rs.getDate("expire_date"));

        MembershipCardMeta meta = new MembershipCardMeta();
        meta.setName(rs.getString("name"));
        meta.setDiscount(rs.getInt("discount"));
        meta.setId(rs.getInt("meta_id"));
        card.setMeta(meta);
    }
}


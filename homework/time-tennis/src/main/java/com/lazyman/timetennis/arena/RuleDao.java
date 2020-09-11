package com.lazyman.timetennis.arena;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class RuleDao {
    private JdbcTemplate template;

    public RuleDao(JdbcTemplate template) {
        this.template = template;
    }

    public List<Rule> courtRules(Object[] courtIds) {
        StringBuilder sql = new StringBuilder("select a.court_id,b.id,b.name,b.fee,b.type,b.start_date,b.end_date,b.week,b.start_hour,b.end_hour from court_rule_r a,rule b where a.rule_id=b.id and a.court_id in(");
        sql.append("?");
        for (int i = 1; i < courtIds.length; i++) {
            sql.append(",?");
        }
        sql.append(") order by seq asc");
        return template.query(sql.toString(), (rs, rowNum) -> {
            Rule rule = new Rule();
            populateRuleProperties(rs, rule);
            rule.setCourtId(rs.getInt("court_id"));
            return rule;
        }, courtIds);
    }

    private void populateRuleProperties(ResultSet rs, Rule rule) throws SQLException {
        rule.setId(rs.getInt("id"));
        rule.setName(rs.getString("name"));
        rule.setFee((Integer) rs.getObject("fee"));
        rule.setType(rs.getInt("type"));
        rule.setStartDate(rs.getString("start_date"));
        rule.setEndDate(rs.getString("end_date"));
        rule.setWeek((Integer) rs.getObject("week"));
        rule.setStartHour((Integer) rs.getObject("start_hour"));
        rule.setEndHour((Integer) rs.getObject("end_hour"));
    }

    public List<Rule> rules(int arenaId, int type) {

        return template.query("select b.id,b.name,b.fee,b.type,b.start_date,b.end_date,b.week,b.start_hour,b.end_hour from rule b where arena_id=? and type=?", new RowMapper<Rule>() {
            @Override
            public Rule mapRow(ResultSet rs, int rowNum) throws SQLException {
                Rule rule = new Rule();
                populateRuleProperties(rs, rule);
                return rule;
            }
        }, arenaId, type);
    }

    public Boolean used(int id) {
        return template.query("select 1 from court_rule_r where  rule_id=? limit 1", ResultSet::next, id);
    }

    public void delete(int id) {
        template.update("delete from rule where id=?", id);
    }

    public Rule load(int id) {
        return template.queryForObject("select b.id,b.name,b.fee,b.type,b.start_date,b.end_date,b.week,b.start_hour,b.end_hour from rule b where b.id=?", new RowMapper<Rule>() {
            @Override
            public Rule mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                Rule rule = new Rule();
                populateRuleProperties(rs, rule);
                return rule;
            }
        }, id);
    }

    public void update(Rule rule) {
        template.update("update rule set name=?,fee=?,start_date=?,end_date=?,week=?,start_hour=?,end_hour=? where id=?",
                rule.getName(),
                rule.getFee(),
                rule.getStartDate(),
                rule.getEndDate(),
                rule.getWeek(),
                rule.getStartHour(),
                rule.getEndHour(),
                rule.getId());
    }

    public void insert(Rule rule) {
        template.update("insert into rule (arena_id,name,fee,type,start_date,end_date,week,start_hour,end_hour)values (?,?,?,?,?,?,?,?,?)",
                rule.getArenaId(),
                rule.getName(),
                rule.getFee(),
                rule.getType(),
                rule.getStartDate(),
                rule.getEndDate(),
                rule.getWeek(),
                rule.getStartHour(),
                rule.getEndHour());
    }
}

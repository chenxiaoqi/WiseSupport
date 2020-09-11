package com.lazyman.timetennis.arena;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
            rule.setId(rs.getInt("id"));
            rule.setCourtId(rs.getInt("court_id"));
            rule.setName(rs.getString("name"));
            rule.setFee((Integer) rs.getObject("fee"));
            rule.setType(rs.getInt("type"));
            rule.setStartDate(rs.getString("start_date"));
            rule.setEndDate(rs.getString("end_date"));
            rule.setWeek((Integer) rs.getObject("week"));
            rule.setStartHour((Integer) rs.getObject("start_hour"));
            rule.setEndHour((Integer) rs.getObject("end_hour"));
            return rule;
        }, courtIds);
    }
}

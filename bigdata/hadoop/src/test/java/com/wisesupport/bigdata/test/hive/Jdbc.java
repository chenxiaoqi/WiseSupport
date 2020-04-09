package com.wisesupport.bigdata.test.hive;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Jdbc {

    private static Connection connection;

    @BeforeClass
    public static void beforeClass() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        connection = DriverManager.getConnection("jdbc:hive2://192.168.1.20:10000", "chenxiaoqi", null);
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        connection.close();
    }

    @Test
    public void test() throws SQLException {
        try (ResultSet rs = connection.createStatement().executeQuery("select year,max(temperature) from records group by year")) {
            while (rs.next()) {
                System.out.printf("%s %s\n",rs.getString(1),rs.getString(2));
            }
        }
    }
}

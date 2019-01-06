package com.wisesupport.mybatis;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

import java.util.List;

/**
 * Author chenxiaoqi on 2019/1/4.
 */
public class ValidationXmlPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);

        for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {

            System.out.println(introspectedColumn.getActualColumnName());
        }
    }
}

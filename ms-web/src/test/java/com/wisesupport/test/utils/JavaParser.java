package com.wisesupport.test.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 功能描述
 *
 * @author c00286900
 * @since 2019-08-15
 */
public class JavaParser {

    @Test
    public void test() throws FileNotFoundException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(new File("D:\\projects\\WiseSupport\\ms-web\\src\\test\\java\\com\\wisesupport\\test\\utils\\CommonLangTest.java"));
        compilationUnit.accept(new VoidVisitorAdapter<Object>() {
            @Override
            public void visit(MethodDeclaration n, Object arg) {

                System.out.println(n.getJavadocComment());

            }
        },null);
    }

}

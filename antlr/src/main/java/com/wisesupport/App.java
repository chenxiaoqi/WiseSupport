package com.wisesupport;

import com.wisesupport.antlr.ExprBaseVisitor;
import com.wisesupport.antlr.ExprLexer;
import com.wisesupport.antlr.ExprParser;
import com.wisesupport.antlr.ExprVisitor;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

        CharStream cs = CharStreams.fromString("a=1\nb=2\na+b\n1*a\n4/a\n");

        ExprLexer lexer = new ExprLexer(cs);

        CommonTokenStream cts = new CommonTokenStream(lexer);

        ExprParser ep = new ExprParser(cts);

        ExprParser.ProgContext context = ep.prog();


        final ExprVisitor<Integer> visitor = new ExprBaseVisitor<Integer>() {
            Map<String, Integer> memory = new HashMap<>();

            @Override
            public Integer visitAssign(ExprParser.AssignContext ctx) {
                String id = ctx.ID().getText();
                int value = visit(ctx.expr());
                memory.put(id, value);
                return 0;
            }

            @Override
            public Integer visitPrintExpr(ExprParser.PrintExprContext ctx) {
                int value = visit(ctx.expr());
                System.out.println(value);
                return 0;
            }

            @Override
            public Integer visitInt(ExprParser.IntContext ctx) {
                return Integer.parseInt(ctx.INT().getText());
            }

            @Override
            public Integer visitId(ExprParser.IdContext ctx) {
                String id = ctx.ID().getText();
                if (memory.containsKey(id)) {
                    return memory.get(id);
                } else {
                    return -1;
                }
            }

            @Override
            public Integer visitMulDiv(ExprParser.MulDivContext ctx) {
                int left = visit(ctx.expr(0));
                int right = visit(ctx.expr(1));
                if (ExprLexer.MUL == ctx.op.getType()) {
                    return left * right;
                } else {
                    return left / right;
                }
            }

            @Override
            public Integer visitAddSub(ExprParser.AddSubContext ctx) {
                int left = visit(ctx.expr(0));
                int right = visit(ctx.expr(1));
                if (ExprLexer.ADD == ctx.op.getType()) {
                    return left + right;
                } else {
                    return left - right;
                }
            }

            @Override
            public Integer visitParens(ExprParser.ParensContext ctx) {
                return visit(ctx.expr());
            }
        };
        visitor.visit(context);
    }
}


package com.wisesupport.antlr.calc;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class Calc {
    public static void main(String[] args) throws IOException {

        CharStream cs = CharStreams.fromStream(System.in);

        CalcLexer lexer = new CalcLexer(cs);

        CommonTokenStream cts = new CommonTokenStream(lexer);

        CalcParser ep = new CalcParser(cts);

        CalcParser.ProgContext context = ep.prog();

        final CalcVisitor<Integer> visitor = new CalcBaseVisitor<Integer>() {
            Map<String, Integer> memory = new HashMap<>();

            @Override
            public Integer visitAssign(CalcParser.AssignContext ctx) {
                String id = ctx.ID().getText();
                int value = visit(ctx.expr());
                memory.put(id, value);
                return 0;
            }

            @Override
            public Integer visitPrintExpr(CalcParser.PrintExprContext ctx) {
                int value = visit(ctx.expr());
                System.out.println(value);
                return 0;
            }

            @Override
            public Integer visitInt(CalcParser.IntContext ctx) {
                return Integer.parseInt(ctx.INT().getText());
            }

            @Override
            public Integer visitId(CalcParser.IdContext ctx) {
                String id = ctx.ID().getText();
                if (memory.containsKey(id)) {
                    return memory.get(id);
                } else {
                    return -1;
                }
            }

            @Override
            public Integer visitMulDiv(CalcParser.MulDivContext ctx) {
                int left = visit(ctx.expr(0));
                int right = visit(ctx.expr(1));
                if (CalcLexer.MUL == ctx.op.getType()) {
                    return left * right;
                } else {
                    return left / right;
                }
            }

            @Override
            public Integer visitAddSub(CalcParser.AddSubContext ctx) {
                int left = visit(ctx.expr(0));
                int right = visit(ctx.expr(1));
                if (CalcLexer.ADD == ctx.op.getType()) {
                    return left + right;
                } else {
                    return left - right;
                }
            }

            @Override
            public Integer visitParens(CalcParser.ParensContext ctx) {
                return visit(ctx.expr());
            }

            @Override
            public Integer visitClear(CalcParser.ClearContext ctx) {
                memory.clear();
                return 0;
            }

        };
        visitor.visit(context);
    }
}


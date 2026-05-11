package com.extfro.extfrocore.api.cover.filter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TagExpressionFilter {

    private TagExpressionFilter() {}

    public static @Nullable MatchExpr parseExpression(String expression) {
        return new Parser().parse(expression);
    }

    public static boolean tagsMatch(@Nullable MatchExpr expr, ItemStack stack) {
        Set<String> tags = stack.getTags()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
        return expr != null && expr.matches(tags);
    }

    public static boolean tagsMatch(@Nullable MatchExpr expr, FluidStack stack) {
        Set<String> tags = stack.getFluid().defaultFluidState().getTags()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
        return expr != null && expr.matches(tags);
    }

    public abstract static class MatchExpr {

        public abstract boolean matches(Set<String> input);
    }

    private enum TokenType {
        L_PAREN,
        R_PAREN,
        AND,
        OR,
        NOT,
        XOR,
        STRING
    }

    private record Token(TokenType type, @Nullable String lexeme) {

        private Token(TokenType type) {
            this(type, null);
        }
    }

    private static final class BinExpr extends MatchExpr {

        private final Token op;
        private final @Nullable MatchExpr left;
        private final @Nullable MatchExpr right;

        private BinExpr(Token op, @Nullable MatchExpr left, @Nullable MatchExpr right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean matches(Set<String> input) {
            if (left == null || right == null) {
                return false;
            }
            return switch (op.type()) {
                case AND -> left.matches(input) && right.matches(input);
                case OR -> left.matches(input) || right.matches(input);
                case XOR -> left.matches(input) ^ right.matches(input);
                default -> false;
            };
        }
    }

    private static final class UnaryExpr extends MatchExpr {

        private final Token token;
        private final @Nullable MatchExpr expr;

        private UnaryExpr(Token token, @Nullable MatchExpr expr) {
            this.token = token;
            this.expr = expr;
        }

        @Override
        public boolean matches(Set<String> input) {
            return token.type() == TokenType.NOT && expr != null && !expr.matches(input);
        }
    }

    private static final class StringExpr extends MatchExpr {

        private @Nullable String value;

        private StringExpr(@Nullable String value) {
            this.value = value;
        }

        @Override
        public boolean matches(Set<String> input) {
            if (value == null || value.isEmpty()) {
                return false;
            }
            if (value.equals("$") && input.isEmpty()) {
                return true;
            }
            if (!value.contains(":") && !value.startsWith("*")) {
                value = "c:" + value;
            }
            String pattern = quote(value);
            return input.stream().anyMatch(tag -> Pattern.matches(pattern, tag));
        }

        private String quote(String str) {
            int idx = str.indexOf("*");
            if (idx >= 0) {
                if (idx == str.length() - 1) {
                    return quote(str.substring(0, idx)) + ".*";
                }
                return quote(str.substring(0, idx)) + ".*" + quote(str.substring(idx + 1));
            }
            return Pattern.quote(str);
        }
    }

    private static final class GroupingExpr extends MatchExpr {

        private final @Nullable MatchExpr inner;

        private GroupingExpr(@Nullable MatchExpr inner) {
            this.inner = inner;
        }

        @Override
        public boolean matches(Set<String> input) {
            return inner != null && inner.matches(input);
        }
    }

    private static final class Parser {

        private List<Token> tokens = Collections.emptyList();
        private int idx = 0;
        private @Nullable Token prev = null;

        private @Nullable MatchExpr parse(String expr) {
            tokens = tokenize(expr);
            idx = 0;
            prev = null;
            return expression();
        }

        private boolean match(TokenType type) {
            if (idx >= tokens.size() || tokens.get(idx).type() != type) {
                return false;
            }
            prev = tokens.get(idx);
            idx++;
            return true;
        }

        private @Nullable MatchExpr expression() {
            return term();
        }

        private @Nullable MatchExpr term() {
            MatchExpr lhs = unary();
            BinExpr result = null;
            while (match(TokenType.AND) || match(TokenType.OR) || match(TokenType.XOR)) {
                if (result == null) {
                    result = new BinExpr(prev, lhs, unary());
                } else {
                    result = new BinExpr(prev, result, unary());
                }
            }
            return result != null ? result : lhs;
        }

        private @Nullable MatchExpr unary() {
            if (match(TokenType.NOT)) {
                return new UnaryExpr(prev, id());
            }
            return id();
        }

        private @Nullable MatchExpr id() {
            if (match(TokenType.L_PAREN)) {
                MatchExpr inner = expression();
                match(TokenType.R_PAREN);
                return new GroupingExpr(inner);
            }
            if (match(TokenType.STRING)) {
                return new StringExpr(prev == null ? null : prev.lexeme());
            }
            return null;
        }

        private List<Token> tokenize(String expr) {
            List<Token> result = new ArrayList<>();
            int pos = 0;
            while (pos < expr.length()) {
                char cur = expr.charAt(pos);
                if (Character.isWhitespace(cur)) {
                    pos++;
                    continue;
                }

                int stringLen = 0;
                while (cur != '(' && cur != ')' && cur != '!' && cur != '&' && cur != '|' && cur != '^' &&
                        cur != ' ') {
                    stringLen++;
                    if (stringLen + pos == expr.length()) {
                        break;
                    }
                    cur = expr.charAt(pos + stringLen);
                }
                if (stringLen > 0) {
                    result.add(new Token(TokenType.STRING, expr.substring(pos, pos + stringLen)));
                    pos += stringLen;
                    continue;
                }

                switch (cur) {
                    case '!' -> result.add(new Token(TokenType.NOT));
                    case '&' -> result.add(new Token(TokenType.AND));
                    case '|' -> result.add(new Token(TokenType.OR));
                    case '^' -> result.add(new Token(TokenType.XOR));
                    case '(' -> result.add(new Token(TokenType.L_PAREN));
                    case ')' -> result.add(new Token(TokenType.R_PAREN));
                    default -> {}
                }
                pos++;
            }
            return result;
        }
    }
}

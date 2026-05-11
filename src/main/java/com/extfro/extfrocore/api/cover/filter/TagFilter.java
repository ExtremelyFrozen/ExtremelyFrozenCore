package com.extfro.extfrocore.api.cover.filter;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class TagFilter<T, S extends Filter<T, S>> implements Filter<T, S> {

    private static final Pattern DOUBLE_WILDCARD = Pattern.compile("\\*{2,}");
    private static final Pattern DOUBLE_AND = Pattern.compile("&{2,}");
    private static final Pattern DOUBLE_OR = Pattern.compile("\\|{2,}");
    private static final Pattern DOUBLE_NOT = Pattern.compile("!{2,}");
    private static final Pattern DOUBLE_XOR = Pattern.compile("\\^{2,}");
    private static final Pattern DOUBLE_SPACE = Pattern.compile(" {2,}");

    @Getter
    protected String tagFilterExpression = "";

    protected Consumer<S> itemWriter = filter -> {};
    protected Consumer<S> onUpdated = filter -> itemWriter.accept(filter);

    @Nullable
    protected TagExpressionFilter.MatchExpr matchExpr = null;

    protected TagFilter() {}

    @Override
    public boolean isBlank() {
        return tagFilterExpression.isBlank();
    }

    public void setFilterExpr(String filterExpr) {
        this.tagFilterExpression = sanitizeExpression(filterExpr);
        matchExpr = TagExpressionFilter.parseExpression(tagFilterExpression);
        onUpdated.accept(self());
    }

    @Override
    public UIElement openConfigurator(int x, int y) {
        return new UIElement().layout(layout -> {
            layout.width(18 * 3 + 25);
            layout.height(18 * 3);
        });
    }

    @Override
    public void setOnUpdated(Consumer<S> onUpdated) {
        this.onUpdated = filter -> {
            this.itemWriter.accept(filter);
            onUpdated.accept(filter);
        };
    }

    @SuppressWarnings("unchecked")
    private S self() {
        return (S) this;
    }

    protected static String sanitizeExpression(String input) {
        input = DOUBLE_WILDCARD.matcher(input).replaceAll("*");
        input = DOUBLE_AND.matcher(input).replaceAll("&");
        input = DOUBLE_OR.matcher(input).replaceAll("|");
        input = DOUBLE_NOT.matcher(input).replaceAll("!");
        input = DOUBLE_XOR.matcher(input).replaceAll("^");
        input = DOUBLE_SPACE.matcher(input).replaceAll(" ");

        StringBuilder builder = new StringBuilder();
        int unclosed = 0;
        char last = ' ';
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ' ') {
                if (last != '(') {
                    builder.append(" ");
                }
                continue;
            }
            if (c == '(') {
                unclosed++;
            } else if (c == ')') {
                unclosed--;
                if (last == '&' || last == '|' || last == '^') {
                    int l = builder.lastIndexOf(" " + last);
                    int l2 = builder.lastIndexOf(String.valueOf(last));
                    builder.insert(l == l2 - 1 ? l : l2, ")");
                    continue;
                }
                if (i > 0 && !builder.isEmpty() && builder.charAt(builder.length() - 1) == ' ') {
                    builder.deleteCharAt(builder.length() - 1);
                }
            } else if ((c == '&' || c == '|' || c == '^') && last == '(') {
                builder.deleteCharAt(builder.lastIndexOf("("));
                builder.append(c).append(" (");
                continue;
            }

            builder.append(c);
            last = c;
        }
        if (unclosed > 0) {
            builder.append(")".repeat(unclosed));
        } else if (unclosed < 0) {
            for (int i = 0; i < -unclosed; i++) {
                builder.insert(0, "(");
            }
        }
        return DOUBLE_SPACE.matcher(builder.toString()).replaceAll(" ");
    }
}

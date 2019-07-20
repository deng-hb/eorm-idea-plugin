package com.denghb.eorm.plugin;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SQL Highlight
 *
 * @author denghb
 * @since 2019-06-23 17:21
 */
public class MultiLineSQLHighlighter implements Annotator {

    private static TextAttributes DEFAULT_TEXT_ATTR = new TextAttributes(new Color(0, 61, 191), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes KEYWORD_TEXT_ATTR = new TextAttributes(new Color(0, 128, 0), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes FUNCTION_TEXT_ATTR = new TextAttributes(new Color(255, 128, 0), null, null, null, Font.TRUETYPE_FONT);

    private static TextAttributes EXPRESSION_TEXT_ATTR = new TextAttributes(new Color(200, 67, 109), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes BRACKET_TEXT_ATTR = new TextAttributes(new Color(0, 115, 191), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes VARCHAR_TEXT_ATTR = new TextAttributes(new Color(195, 43, 24), null, null, null, Font.TRUETYPE_FONT);

    private static TextAttributes COMMA_TEXT_ATTR = new TextAttributes(new Color(128, 128, 128), null, null, null, Font.TRUETYPE_FONT);

    private static Set<String> KEYWORDS = new HashSet<String>();
    private static Set<String> FUNCTIONS = new HashSet<String>();
    private static Set<String> EXPRESSIONS = new HashSet<String>();

    static {
        // TODO 可能列得不全
        KEYWORDS.addAll(Arrays.asList("select ", "update ", "create ", "delete ", "truncate ", "insert ", " into ", " values ", " set "));
        KEYWORDS.addAll(Arrays.asList(" from ", " on ", " by ", " where ", " left ", " inner ", " join ", " right ", " as ", " group ", " order ", " having ", " distinct "));
        KEYWORDS.addAll(Arrays.asList(" and ", " or ", " > ", " < ", " <= ", " >= ", " = ", " != ", " not ", " desc", " asc", " between ", " union ", " is ", " null"));
        KEYWORDS.addAll(Arrays.asList(" limit ", " like ", " case ", "(case", " when ", " then ", " else ", " end ", "end)"));

        FUNCTIONS.addAll(Arrays.asList("count(", "sum(", "avg(", "min(", "max(", "avg(", "concat(", "date_format(", "date_sub(", "date_add(", "now(", "curdate("));

        EXPRESSIONS.addAll(Arrays.asList("#if", "#elseif", "#else", "#end"));

    }

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        String code = psiElement.getText();
        if (!code.startsWith("/*{")) {
            return;
        }
        int originStart = psiElement.getNode().getTextRange().getStartOffset();

        code = code.toLowerCase();
        // /*{}*/
        annotationHolder.createInfoAnnotation(new TextRange(originStart + 3, originStart + 3 + code.length() - 6), null).setEnforcedTextAttributes(DEFAULT_TEXT_ATTR);

        for (String keyword : KEYWORDS) {
            doColor(originStart, code, keyword, annotationHolder, KEYWORD_TEXT_ATTR);
        }

        for (String function : FUNCTIONS) {
            doColor(originStart, code, function, annotationHolder, FUNCTION_TEXT_ATTR);
        }

        for (String expression : EXPRESSIONS) {
            doColor(originStart, code, expression, annotationHolder, EXPRESSION_TEXT_ATTR);
        }
        // 字符串红色
        int nextStart = -1;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if ('(' == c || ')' == c) {
                int start = originStart + i;
                TextRange textRange = new TextRange(start, start + 1);
                annotationHolder.createInfoAnnotation(textRange, null).setEnforcedTextAttributes(BRACKET_TEXT_ATTR);
            } else if (',' == c) {
                int start = originStart + i;
                TextRange textRange = new TextRange(start, start + 1);
                annotationHolder.createInfoAnnotation(textRange, null).setEnforcedTextAttributes(COMMA_TEXT_ATTR);
            }

            if ('\'' != c) {
                continue;
            }

            if (-1 == nextStart) {
                nextStart = i;
            } else {
                int start = originStart + nextStart;
                TextRange textRange = new TextRange(start, start + (i - nextStart) + 1);
                annotationHolder.createInfoAnnotation(textRange, null).setEnforcedTextAttributes(VARCHAR_TEXT_ATTR);
                nextStart = -1;
            }
        }


//        Notifications.Bus.notify(new Notification("", "", code, NotificationType.INFORMATION));
    }

    private void doColor(int originStart, String code, String key, AnnotationHolder annotationHolder, TextAttributes textAttributes) {
        int start = code.indexOf(key);
        if (-1 == start) {
            return;
        }
        int newStart = originStart + start;
        int newEnd = newStart + key.length();
        TextRange textRange = new TextRange(newStart, newEnd);

        annotationHolder.createInfoAnnotation(textRange, null).setEnforcedTextAttributes(textAttributes);

        doColor(newStart + key.length(), code.substring(start + key.length()), key, annotationHolder, textAttributes);
    }
}

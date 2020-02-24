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

    private static TextAttributes TEXT_ATTR_DEFAULT = new TextAttributes(new Color(0, 61, 191), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes TEXT_ATTR_KEYWORD = new TextAttributes(new Color(0, 128, 0), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes TEXT_ATTR_FUNCTION = new TextAttributes(new Color(255, 128, 0), null, null, null, Font.TRUETYPE_FONT);

    private static TextAttributes TEXT_ATTR_EXPRESSION = new TextAttributes(new Color(200, 67, 109), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes TEXT_ATTR_SYMBOL = new TextAttributes(new Color(128, 128, 128), null, null, null, Font.TRUETYPE_FONT);
    private static TextAttributes TEXT_ATTR_VARCHAR = new TextAttributes(new Color(195, 43, 24), null, null, null, Font.TRUETYPE_FONT);

    // MySQL所有关键字
    private static final String ALL_KEYWORDS = ",ADD,ALL,ALTER,ANALYZE,AND,AS,ASC,ASENSITIVE,BEFORE,BETWEEN,BIGINT,BINARY,BLOB,BOTH,BY,CALL,CASCADE,CASE,CHANGE,CHAR,CHARACTER,CHECK,COLLATE,COLUMN,CONDITION,CONNECTION,CONSTRAINT,CONTINUE,CONVERT,CREATE,CROSS,CURRENT_DATE,CURRENT_TIME,CURRENT_TIMESTAMP,CURRENT_USER,CURSOR,DATABASE,DATABASES,DAY_HOUR,DAY_MICROSECOND,DAY_MINUTE,DAY_SECOND,DEC,DECIMAL,DECLARE,DEFAULT,DELAYED,DELETE,DESC,DESCRIBE,DETERMINISTIC,DISTINCT,DISTINCTROW,DIV,DOUBLE,DROP,DUAL,EACH,ELSE,ELSEIF,END,ENCLOSED,ESCAPED,EXISTS,EXIT,EXPLAIN,FALSE,FETCH,FLOAT,FLOAT4,FLOAT8,FOR,FORCE,FOREIGN,FROM,FULLTEXT,GOTO,GRANT,GROUP,HAVING,HIGH_PRIORITY,HOUR_MICROSECOND,HOUR_MINUTE,HOUR_SECOND,IF,IGNORE,IN,INDEX,INFILE,INNER,INOUT,INSENSITIVE,INSERT,INT,INT1,INT2,INT3,INT4,INT8,INTEGER,INTERVAL,INTO,IS,ITERATE,JOIN,KEY,KEYS,KILL,LABEL,LEADING,LEAVE,LEFT,LIKE,LIMIT,LINEAR,LINES,LOAD,LOCALTIME,LOCALTIMESTAMP,LOCK,LONG,LONGBLOB,LONGTEXT,LOOP,LOW_PRIORITY,MATCH,MEDIUMBLOB,MEDIUMINT,MEDIUMTEXT,MIDDLEINT,MINUTE_MICROSECOND,MINUTE_SECOND,MOD,MODIFIES,NATURAL,NOT,NO_WRITE_TO_BINLOG,NULL,NUMERIC,ON,OPTIMIZE,OPTION,OPTIONALLY,OR,ORDER,OUT,OUTER,OUTFILE,PRECISION,PRIMARY,PROCEDURE,PURGE,RAID0,RANGE,READ,READS,REAL,REFERENCES,REGEXP,RELEASE,RENAME,REPEAT,REPLACE,REQUIRE,RESTRICT,RETURN,REVOKE,RIGHT,RLIKE,SCHEMA,SCHEMAS,SECOND_MICROSECOND,SELECT,SENSITIVE,SEPARATOR,SET,SHOW,SMALLINT,SPATIAL,SPECIFIC,SQL,SQLEXCEPTION,SQLSTATE,SQLWARNING,SQL_BIG_RESULT,SQL_CALC_FOUND_ROWS,SQL_SMALL_RESULT,SSL,STARTING,STRAIGHT_JOIN,TABLE,TERMINATED,THEN,TINYBLOB,TINYINT,TINYTEXT,TO,TRAILING,TRIGGER,TRUE,UNDO,UNION,UNIQUE,UNLOCK,UNSIGNED,UPDATE,USAGE,USE,USING,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,VALUES,VARBINARY,VARCHAR,VARCHARACTER,VARYING,WHEN,WHERE,WHILE,WITH,WRITE,X509,XOR,YEAR_MONTH,ZEROFILL,";

    private static final String ALL_FUNCTION = "(ASCII(CHAR_LENGTH(CHARACTER_LENGTH(CONCAT(CONCAT_WS(FIELD(FIND_IN_SET(FORMAT(INSERT(LOCATE(LCASE(LEFT(LOWER(LPAD(LTRIM(MID(POSITION(REPEAT(REPLACE(REVERSE(RIGHT(RPAD(RTRIM(SPACE(STRCMP(SUBSTR(SUBSTRING(SUBSTRING_INDEX(TRIM(UCASE(UPPER(ABS(ACOS(ASIN(ATAN(ATAN2(AVG(CEIL(CEILING(COS(COT(COUNT(DEGREES(EXP(FLOOR(GREATEST(LEAST(LN(LOG(LOG10(LOG2(MAX(MIN(MOD(PI(POW(POWER(RADIANS(RAND(ROUND(SIGN(SIN(SQRT(SUM(TAN(TRUNCATE(ADDDATE(ADDTIME(CURDATE(CURRENT_DATE(CURRENT_TIME(CURRENT_TIMESTAMP(CURTIME(DATE(DATEDIFF(DATE_ADD(DATE_FORMAT(DATE_SUB(DAY(DAYNAME(DAYOFMONTH(DAYOFWEEK(DAYOFYEAR(EXTRACT(FROM_DAYS(HOUR(LAST_DAY(LOCALTIME(LOCALTIMESTAMP(MAKEDATE(MAKETIME(MICROSECOND(MINUTE(MONTHNAME(MONTH(NOW(PERIOD_ADD(PERIOD_DIFF(QUARTER(SECOND(SEC_TO_TIME(STR_TO_DATE(SUBDATE(SUBTIME(SYSDATE(TIME(TIME_FORMAT(TIME_TO_SEC(TIMEDIFF(TIMESTAMP(TO_DAYS(WEEK(WEEKDAY(WEEKOFYEAR(YEAR(YEARWEEK(BIN(BINARY(CAST(COALESCE(CONNECTION_ID(CONV(CONVERT(CURRENT_USER(DATABASE(IF(IFNULL(ISNULL(LAST_INSERT_ID(NULLIF(SESSION_USER(SYSTEM_USER(USER(VERSION(";

    private static final String ALL_EXPRESSION = "#IF#ELSEIF#ELSE#END#";

    // 符号
    private static final String ALL_SYMBOL = "=-*/><(),.%|&";

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        String code = psiElement.getText();
        if (!code.startsWith("/*{")) {
            return;
        }
        int originStart = psiElement.getNode().getTextRange().getStartOffset();

        code = code.toUpperCase();
        // /*{}*/
        annotationHolder.createInfoAnnotation(new TextRange(originStart + 3, originStart + 3 + code.length() - 6), null)
                .setEnforcedTextAttributes(TEXT_ATTR_DEFAULT);

        StringBuilder sb = new StringBuilder();
        int codeLength = code.length() - 2;
        boolean singleQuote = false;

        for (int i = 3; i < codeLength; i++) {
            char c = code.charAt(i);
            if ((' ' == c || '\n' == c || '\r' == c || '\t' == c) && !singleQuote || i == codeLength - 1) {
                if (sb.length() > 0) {
                    // 是否关键字
                    showColor(originStart, i, sb, annotationHolder);
                    sb = new StringBuilder();
                }
            } else if (ALL_SYMBOL.contains(String.valueOf(c)) && !singleQuote) {
                // 是否关键字
                showColor(originStart, i, sb, annotationHolder);
                //
                sb = new StringBuilder();
                sb.append(c);
                showColor(originStart, i + 1, sb, annotationHolder);
                sb = new StringBuilder();
            } else if ('(' == c && sb.length() > 0) {
                // 函数
                showColor(originStart, i, sb, annotationHolder);

                // (
                sb = new StringBuilder();
                sb.append(c);
                showColor(originStart, i + 1, sb, annotationHolder);

                sb = new StringBuilder();
            } else if ('\\' == c && '\'' == code.charAt(i + 1)) {
                // \' 转译
                sb.append(c);
                sb.append(code.charAt(i + 1));
                i++;
            } else {
                sb.append(c);
                if ('\'' == c) {
                    singleQuote = !singleQuote;
                    if (!singleQuote) {
                        // 字符串
                        showColor(originStart, i + 1, sb, annotationHolder);
                        sb = new StringBuilder();
                    }
                }
            }
        }
//        Notifications.Bus.notify(new Notification("", "", code, NotificationType.INFORMATION));
    }

    private void showColor(int originStart, int i, StringBuilder sb, AnnotationHolder annotationHolder) {
        System.out.println(sb);

        int sbLength = sb.length();
        int start = originStart + i - sbLength;
        TextAttributes attributes = null;

        if (sb.length() > 1) {
            if (sb.indexOf("'") == 0) {
                attributes = TEXT_ATTR_VARCHAR;
            } else if (ALL_KEYWORDS.contains("," + sb + ",")) {
                attributes = TEXT_ATTR_KEYWORD;
            } else if (ALL_FUNCTION.contains("(" + sb)) {
                attributes = TEXT_ATTR_FUNCTION;
            } else if (ALL_EXPRESSION.contains(sb + "#")) {
                attributes = TEXT_ATTR_EXPRESSION;
            }
        } else {
            if (ALL_SYMBOL.contains(sb)) {
                attributes = TEXT_ATTR_SYMBOL;
            }
        }

        if (null != attributes) {
            annotationHolder.createInfoAnnotation(new TextRange(start, start + sbLength), null).setEnforcedTextAttributes(attributes);
        }
    }

}

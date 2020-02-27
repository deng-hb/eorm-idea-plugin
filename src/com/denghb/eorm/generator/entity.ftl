package ${config.packageName};

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;

/**
 * ${table.tableComment}
 *
 * @author ${config.author}
<#if config.since > * @since ${generateTime}
</#if> */
<#if config.lombok >@lombok.Data()
@Etable(name = "${table.tableName}"<#if config.schema >, database="${config.database}"</#if>)<#else>@Etable(name="${table.tableName}"<#if config.schema >, database="${config.database}"</#if>)</#if>
public class ${className} implements java.io.Serializable {

    <#list table.columns as column >
    /** ${column.columnComment} */
    @Ecolumn(name = "${column.columnName}"<#if column.columnKey = "PRI">, primaryKey = true</#if>)
    private ${column.javaType} ${column.objectName};

    </#list>
    <#if !config.lombok ><#list table.columns as column >
    public ${column.javaType} get${column.methodName}() {
        return ${column.objectName};
    }

    public void set${column.methodName}(${column.javaType} ${column.objectName}) {
        this.${column.objectName} = ${column.objectName};
    }

    </#list>
    @Override
    public String toString() {
        return "${className} {" +
        <#list table.columns as column >        "<#if (0 < column_index) >, </#if>${column.objectName}=<#if column.javaType = "String">'</#if>" + ${column.objectName} + <#if column.javaType = "String">'\'' +</#if>
        </#list>        "}";
    }</#if>
}
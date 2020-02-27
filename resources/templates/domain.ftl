package ${packageName};

import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.annotation.Etable;

/**
 * ${tableComment}
<#if gen.writeDDL > * DDL
 * 
 <pre>
${tableDdl}
 <pre> <#else> *</#if>
 * @author denghb
<#if gen.writeGenerateTime > * @generateTime ${generateTime}
</#if> */
<#if gen.lombok >@lombok.Data()
@Etable(name = "${tableName}"<#if gen.writeDatabase >, database="${databaseName}"</#if>)<#else>@Etable(name="${tableName}"<#if gen.writeDatabase >, database="${databaseName}"</#if>)</#if>
public class ${className} implements java.io.Serializable {

    <#list list as table>
    @Ecolumn(name = "${table.columnName}"<#if table.columnKey = "PRI">, primaryKey = true</#if>)
    private ${table.dataType} ${table.objectName};
    
    </#list>
    <#if !gen.lombok><#list list as table>
    public ${table.dataType} get${table.methodName}() {
        return ${table.objectName};
    }

    public void set${table.methodName}(${table.dataType} ${table.objectName}) {
        this.${table.objectName} = ${table.objectName};
    }

    </#list>
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("${className} {");
        <#list list as table>
        str.append("\"${table.objectName}\":\"");
        str.append(${table.objectName});
        str.append("\"");
        <#if (list?size - 1 > table_index) >str.append(",");</#if>
        </#list>
        return str.append("}").toString();
    }</#if>
}
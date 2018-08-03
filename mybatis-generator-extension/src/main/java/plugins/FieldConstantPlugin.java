package plugins;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//



import com.mysql.jdbc.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;

public class FieldConstantPlugin extends PluginAdapter {
    private String targetProject;
    private String targetPackage;
    private String fileName;

    private boolean generateEOFields;

    private final String EO = "EO";
    private final String BO = "BO";
    private Map<String, Map<String, List<String>>> tableNameColumns = new TreeMap<String, Map<String, List<String>>>();

    public FieldConstantPlugin() {
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String generateEOFieldsStr = this.properties.getProperty("generateEOFields");
        this.generateEOFields = StringUtils.isNullOrEmpty(generateEOFieldsStr)? false: Boolean.parseBoolean(generateEOFieldsStr);
        String targetProject = this.properties.getProperty("targetProject");
        if (StringUtility.stringHasValue(targetProject)) {
            this.targetProject = targetProject;
            String targetPackage = this.properties.getProperty("targetPackage");
            if (StringUtility.stringHasValue(targetPackage)) {
                this.targetPackage = targetPackage;
                String fileName = this.properties.getProperty("fileName");
                if (StringUtility.stringHasValue(fileName)) {
                    this.fileName = fileName;
                } else {
                    throw new RuntimeException("fileName 属性不能为空！");
                }
            } else {
                throw new RuntimeException("targetPackage 属性不能为空！");
            }
        } else {
            throw new RuntimeException("targetProject 属性不能为空！");
        }
    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        String domainName = introspectedTable.getTableConfiguration().getDomainObjectName();
        List<String> realNames = new ArrayList<>();
        List<String> camelNames = new ArrayList<>();

        for(IntrospectedColumn column : introspectedTable.getAllColumns()){
            String columnName = column.getActualColumnName();
            String camelName = camelName(columnName);
            realNames.add(columnName);
            camelNames.add(camelName);
        }
        Collections.sort(realNames);
        Collections.sort(camelNames);
        Map<String, List<String>> typeColumns = new TreeMap<String, List<String>>();
        typeColumns.put(EO, realNames);
        typeColumns.put(BO, camelNames);
        tableNameColumns.put(domainName, typeColumns);
        return null;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
        return Arrays.asList(this.generateConstant());
    }

    private GeneratedJavaFile generateConstant() {
        if(tableNameColumns.isEmpty()){
            return null;
        }
        String constant = this.targetPackage + "." + this.fileName;
        TopLevelClass topClass = new TopLevelClass(new FullyQualifiedJavaType(constant));
        topClass.setVisibility(JavaVisibility.PUBLIC);

        StringBuilder sb = new StringBuilder();
        for(String tableName : tableNameColumns.keySet()){
            String className = firstCapitalName(tableName);
            InnerClass innerClass = new InnerClass(className);
            innerClass.setVisibility(JavaVisibility.PUBLIC);
            innerClass.setStatic(true);
            Map<String, List<String>> columns = tableNameColumns.get(tableName);
            if(this.generateEOFields){
                generateEOText(columns, innerClass);
            }
            generateBOText(columns, innerClass);
            topClass.addInnerClass(innerClass);
        }
        GeneratedJavaFile javaFile = new GeneratedJavaFile(topClass, this.targetProject, new DefaultJavaFormatter());
        return javaFile;
    }

    private void generateEOText(Map<String, List<String>> columns, InnerClass innerClass){
        List<String> underLineColumns = columns.get(EO);
        for(String realColumn : underLineColumns){
            String fieldName = allCapitalName(realColumn);
            String fieldValue = realColumn;
            Field field = new Field(fieldName, FullyQualifiedJavaType.getStringInstance());
            field.setInitializationString("\"" + fieldValue + "\"");
            field.setVisibility(JavaVisibility.PUBLIC);
            field.setFinal(true);
            field.setStatic(true);
            innerClass.addField(field);
        }
    }
    private void generateBOText(Map<String, List<String>> columns, InnerClass innerClass){
        List<String> boColumns = columns.get(BO);
        for(String boColumn : boColumns){
            String fieldName = boColumn;
            String fieldValue = boColumn;
            Field field = new Field(fieldName, FullyQualifiedJavaType.getStringInstance());
            field.setInitializationString("\"" + fieldValue + "\"");
            field.setVisibility(JavaVisibility.PUBLIC);
            field.setFinal(true);
            field.setStatic(true);
            innerClass.addField(field);
        }
    }

    //驼峰转换
    private String camelName(String columnName) {
        StringBuilder camelBuilder = new StringBuilder();
        int i = 0;
        for(String partName : columnName.split("_")){
            if(i == 0){
                i++;
                camelBuilder.append(partName);
            }else{
                camelBuilder.append(partName.substring(0, 1).toUpperCase() + partName.substring(1));
            }
        }
        return camelBuilder.toString();
    }
    //首字母大写
    private String firstCapitalName(String tableName) {
        StringBuilder camelBuilder = new StringBuilder();
        int i = 0;
        for(String partName : tableName.split("_")){
            camelBuilder.append(partName.substring(0, 1).toUpperCase() + partName.substring(1));
        }
        return camelBuilder.toString();
    }
    //所有字母大写
    private String allCapitalName(String tableName) {
        StringBuilder capitalBuilder = new StringBuilder();
        int i = 0;
        for(char ch : tableName.toCharArray()){
            capitalBuilder.append(Character.toUpperCase(ch));
        }
        return capitalBuilder.toString();
    }
}


package com.simple.dao.parser;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simple.base.parser.BeanParser;
import com.simple.dao.error.DaoException;


/**
 * SQL执行结果解析器，负责将执行结果解析成要求的数据对象。
 *
 * @since 1.0
 */
public class ResultSetParser {

    private static final Pattern COLUMN_TO_PROPERTY_PARTTERN = Pattern.compile("(_)([a-zA-Z])");
    private static Map<String, String> convertMap = new ConcurrentHashMap<String, String>();

    /**
     * 解析ResultSet，并返回要求的数据类型。
     *
     * @param rs SQL执行结果ResultSet
     * @param returnType 返回值类型
     * @param rawType 如果返回值为集合类型，此参数指定实际集合元素类型
     * @return 返回指定类型的结果对象
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Object parseRs(ResultSet rs, Class<?> returnType, Class<?> rawType)
            throws SQLException, InstantiationException, IllegalAccessException {
        Object ret;
        if (returnType == List.class) {
            ret = parseListReturn(rs, rawType);
        } else {
            ret = parseObjectReturn(rs, returnType);
        }
        return ret;
    }

    /**
     * 解析集合类型的数据。
     * @param <T> 实际返回的集合类型
     * @param rs SQL执行结果
     * @param rType 集合类型的class
     * @return 返回解析出来的集合类型实例
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseListReturn(ResultSet rs, Class<T> rType)
            throws SQLException, InstantiationException, IllegalAccessException {
        List<T> ret = new ArrayList<T>();
        while (rs.next()) {
            ret.add((T) getValue(rs, rType));
        }
        return ret;
    }

    /**
     * 解析bean对象。
     * @param rs SQL执行结果
     * @param rType 实际要返回的bean对象class
     * @return 返回解析后的bean对象
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Object parseObjectReturn(ResultSet rs, Class<?> rType)
            throws SQLException, InstantiationException, IllegalAccessException {
        return (rs.next()) ? getValue(rs, rType) : null;
    }

    /**
     * 获得特定类型数据的值。
     * @param rs SQL执行结果
     * @param type 要获得的数据类型
     * @return 返回取到的值
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static Object getValue(ResultSet rs, Class<?> type)
            throws SQLException, InstantiationException, IllegalAccessException {
        Object ret;
        if (type == int.class || type == Integer.class) {
            ret = rs.getInt(1);
        } else if (type == short.class || type == Short.class) {
            ret = rs.getShort(1);
        } else if (type == byte.class || type == Byte.class) {
            ret = rs.getBytes(1);
        } else if (type == long.class || type == Long.class) {
            ret = rs.getLong(1);
        } else if (type == float.class || type == Float.class) {
            ret = rs.getFloat(1);
        } else if (type == double.class || type == Double.class) {
            ret = rs.getDouble(1);
        } else if (type == boolean.class || type == Boolean.class) {
            ret = rs.getBoolean(1);
        } else if (type == String.class) {
            ret = rs.getString(1);
        } else if (Map.class.isAssignableFrom(type)) {
            ret = parseMapValue(rs);
        } else {
            ret = parseBeanValue(rs, type);
        }
        return ret;
    }

    /**
     * 解析bean对象的数值，并返回实例对象。
     * @param <T> bean对象类型
     * @param rs SQL执行结果
     * @param beanClass bean对象class
     * @return 返回解析出来的bean对象
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException
     */
    private static <T> T parseBeanValue(ResultSet rs, Class<T> beanClass)
            throws InstantiationException, IllegalAccessException, SQLException {
        T ret = (T)beanClass.newInstance();
        BeanParser parser = BeanParser.getBeanParser(beanClass);
        ResultSetMetaData metaData = rs.getMetaData();
        String name = null;
        BeanParser.FieldStruct bfi = null;

        for (int i = 1, j = metaData.getColumnCount(); i <= j; i++) {
            name = metaData.getColumnLabel(i);
            bfi = parser.getFieldStruct(convertColumnNameToPropertyName(name));

            if (null != bfi) {
                bfi.set(ret, getNameValue(rs, bfi.getType(), name));
            }
        }
        return ret;
    }

    /**
     * 解析Map对象，并返回。
     * 当前只支持将所有的列以列名为key，值为value组装到map中再返回。
     * @param rs SQL执行结果
     * @return 返回解析出来的结果集map
     * @throws SQLException
     */
    private static Map<String, Object> parseMapValue(ResultSet rs) throws SQLException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String name = null;

        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1, j = metaData.getColumnCount(); i <= j; i++) {
             name = metaData.getColumnLabel(i);
             resultMap.put(name, rs.getObject(i));
        }

        return resultMap;
    }

    /**
     * 将数据表的字段名映射到bean的属性名。
     * 例如：
     *   user_id 映射为 userId
     * @param columnName 数据表的字段名
     * @return 返回bean的属性名
     */
    private static String convertColumnNameToPropertyName(String columnName) {
        columnName = columnName.toLowerCase();

        if (convertMap.containsKey(columnName)) {
            return convertMap.get(columnName);
        }

        Matcher m = COLUMN_TO_PROPERTY_PARTTERN.matcher(columnName);
        StringBuilder s = new StringBuilder();
        int start = 0;

        while (m.find()) {
            s.append(columnName.substring(start, m.start()));
            s.append(m.group(2).toUpperCase());
            start = m.end();
        }

        s.append(columnName.substring(start));

        convertMap.put(columnName, s.toString());
        return s.toString();
    }

    /**
     * 用结果列名来获得数值。
     * @param rs SQL执行结果
     * @param type 实际数据类型
     * @param name 数据名称
     * @return 返回获得的数据值
     * @throws SQLException
     */
    private static Object getNameValue(ResultSet rs, Class<?> type, String name) throws SQLException {
        Object ret;
        if (type == int.class || type == Integer.class) {
            ret = rs.getInt(name);
        } else if (type == short.class || type == Short.class) {
            ret = rs.getShort(name);
        } else if (type == byte.class || type == Byte.class) {
            ret = rs.getBytes(name);
        } else if(type == byte[].class){
        	 ret = rs.getBytes(name);
        } else if (type == long.class || type == Long.class) {
            ret = rs.getLong(name);
        } else if (type == float.class || type == Float.class) {
            ret = rs.getFloat(name);
        } else if (type == double.class || type == Double.class) {
            ret = rs.getDouble(name);
        } else if (type == boolean.class || type == Boolean.class) {
            ret = rs.getBoolean(name);
        } else if (type == String.class) {
            ret = rs.getString(name);
        } else if (Date.class.isAssignableFrom(type)) {
            ret = rs.getTimestamp(name);
           
        } else {
            throw new DaoException("Bean属性中不能有特殊类型[" + type + "]");
        }
        return ret;
    }
}

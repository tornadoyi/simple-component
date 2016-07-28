package com.simple.base.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.simple.base.Consts;

/**
 * 数值工具类
 */
public class NumberUtil {

	/**
	 * 将字符串转化为int类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为Java的整数时，返回转化后数值</li> <li>其它，返回HeConsts.DFT_INTEGER_VAL返回值</li>
	 * @see #getInteger(String, int)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_INTEGER_VAL
	 */
	public static int getInteger(String input) {
		return getInteger(input, Consts.DFT_INTEGER_VAL);
	}

	/**
	 * 将字符串转化为int类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return
	 *         <li>字符串可以转化为Java的整数时，返回转化后数值</li> <li>其它，返回参数defVal的值</li>
	 */
	public static int getInteger(String input, int defVal) {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return  defVal;
		}
	}

	/**
	 * 将字符串转化为short类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为Java的整数时，返回转化后数值</li> <li>其它，返回HeConsts.DFT_SHORT_VAL</li>
	 * @see #getShort(String, short)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_SHORT_VAL
	 */
	public static short getShort(String input) {
		return getShort(input, Consts.DFT_SHORT_VAL);
	}

	/**
	 * 将字符串转化为short类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return
	 *         <li>字符串可以转化为Java的整数时，返回转化后数值</li> <li>其它，返回参数defVal的值</li>
	 */
	public static short getShort(String input, short defVal) {
		try {
			return Short.parseShort(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * 将字符串转化为byte类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为byte值时，返回转化后数值</li> <li>其它，返回HeConsts.DFT_BYTE_VAL</li>
	 * @see #getByte(String, byte)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_BYTE_VAL
	 */
	public static byte getByte(String input) {
		return getByte(input, Consts.DFT_BYTE_VAL);
	}

	/**
	 * 将字符串转化为byte类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return
	 *         <li>字符串可以转化为byte值时，返回转化后数</li> <li>其它，返回参数defVal的值</li>
	 */
	public static byte getByte(String input, byte defVal) {
		try {
			return Byte.parseByte(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * 将字符串转化为long类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为long值时，返回转化后数值</li> <li>其它，返回HeConsts.DFT_LONG_VAL</li>
	 * @see #getLong(String, long)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_LONG_VAL
	 */
	public static long getLong(String input) {
		return getLong(input, Consts.DFT_LONG_VAL);
	}

	/**
	 * 将字符串转化为long类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return
	 *         <li>字符串可以转化为long值时，返回转化后数值</li> <li>其它，返回参数defVal的值</li>
	 */
	public static long getLong(String input, long defVal) {
		try {
			return Long.parseLong(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * 将字符串转化为float类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为float值时，返回转化后数值</li> <li>其它，返回HeConsts.DFT_FLOAT_VAL</li>
	 * @see #getFloat(String, float)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_FLOAT_VAL
	 */
	public static float getFloat(String input) {
		return getFloat(input, Consts.DFT_FLOAT_VAL);
	}

	/**
	 * 将字符串转化为float类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return
	 *         <li>字符串可以转化为float值时，返回转化后数值</li> <li>其它，返回参数defVal的值</li>
	 */
	public static float getFloat(String input, float defVal) {
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * 将字符串转化为double类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为double值时，返回转化后数值</li> <li>其它，返HeConsts.DFT_DOUBLE_VAL</li>
	 * @see #getDouble(String, double)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_DOUBLE_VAL
	 */
	public static double getDouble(String input) {
		return getDouble(input, Consts.DFT_DOUBLE_VAL);
	}

	/**
	 * 将字符串转化为double类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return
	 *         <li>字符串可以转化为double值时，返回转化后数值</li> <li>其它，返回参数defVal的值</li>
	 */
	public static double getDouble(String input, double defVal) {
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}

	/**
	 * 将字符串转化为boolean类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串可以转化为boolean值时，返回转化后布尔值</li> <li>其它，返回HeConsts.DFT_BOOLEAN_VAL</li>
	 * @see #getBoolean(String, boolean)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_BOOLEAN_VAL
	 */
	public static boolean getBoolean(String input) {
		return getBoolean(input, Consts.DFT_BOOLEAN_VAL);
	}

	/**
	 * 将字符串转化为boolean类型的值
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转换时的默认值
	 * @return <br/>
	 *         <li>字符串为"true"时(不区分大小写)，返回true</li> <li>字符串为整数，且不为1个或多个"0"时，返回true</li> <li>字符串为"false"时(不区分大小写)，返回false</li> <li>字符串为1个或多个"0"时，返回false</li> <li>其它，返回defVal指定的值</li>
	 */
	public static boolean getBoolean(String input, boolean defVal) {
		if (input == null)
			return defVal;
		if (input.equalsIgnoreCase("true"))
			return true;
		if (input.equalsIgnoreCase("false"))
			return false;
		if(isDigit(input)) {
			return (getInteger(input) != 0);
		}
		return defVal;
	}

	/**
	 * 检测字符串是否可以是整数值
	 * <B>效率是正则表达式检测的20-60倍</B>
	 * 
	 * @param input
	 *            待检测字符串
	 * @return
	 *         <li>true：是整数</li> <li>false：不是整数</li>
	 */
	public static boolean isDigit(String input) {
		if (input == null)
			return false;
		int len = input.length();
		if (len == 0)
			return false;
		char ch = input.charAt(0);
		if (ch != '-' && (ch < '0' || ch > '9'))
			return false;
		for (int i = 1; i < len; i++) {
			ch = input.charAt(i);
			if (ch < '0' && ch > '9')
				return false;
		}
		return true;
	}


	/**
	 * 将字符串转化为BigDecimal类型对象
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串内容为数值时，返回转化后对象</li> <li>其它，返回HeConsts.DFT_BIGDECIMAL_VAL</li>
	 * @see #getBigDecimal(String, BigDecimal)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_BIGDECIMAL_VAL
	 */
	public static BigDecimal getBigDecimal(String input) {
		return getBigDecimal(input, Consts.DFT_BIGDECIMAL_VAL);
	}

	/**
	 * 将字符串传话为BigDecimal类型对象
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转化时默认返回值
	 * @return
	 *         <li>字符串内容是数值时，返回转化后对象</li> <li>其它，返回null</li>
	 * @see #getBigDecimal(String)
	 */
	public static BigDecimal getBigDecimal(String input, BigDecimal defVal) {
		try {
			return new BigDecimal(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}
	
	/**
	 * 将字符串转化为BigInteger类型对象
	 * 
	 * @param input
	 *            待转化字符串
	 * @return
	 *         <li>字符串内容为数值时，返回转化后对象</li> <li>其它，返回HeConsts.DFT_BIGINTEGER_VAL</li>
	 * @see #getBigInteger(String, BigInteger)
	 * @see com.Consts.rdcenter.commons.HeConsts#DFT_BIGINTEGER_VAL
	 */
	public static BigInteger getBigInteger(String input) {
		return getBigInteger(input, Consts.DFT_BIGINTEGER_VAL);
	}

	/**
	 * 将字符串传话为BigInteger类型对象
	 * 
	 * @param input
	 *            待转化字符串
	 * @param defVal
	 *            无法转化时默认返回值
	 * @return
	 *         <li>字符串内容是数值时，返回转化后对象</li> <li>其它，返回null</li>
	 * @see #getBigInteger(String)
	 */
	public static BigInteger getBigInteger(String input, BigInteger defVal) {
		try {
			return new BigInteger(input);
		} catch (NumberFormatException e) {
			return defVal;
		}
	}
	
}

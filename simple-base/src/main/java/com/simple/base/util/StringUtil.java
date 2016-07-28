package com.simple.base.util;

import java.util.Random;

/**
 * 字符串工具类
 */
public class StringUtil {
	/** 随机数对象 */
	private static final Random random = new Random();
	/** 数字与字母字典 */
	private static final char[] LETTER_AND_DIGIT = ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
	/** 数字与字母字典长度 */
	private static final int LETTER_AND_DIGIT_LENGTH = LETTER_AND_DIGIT.length;
	
	public static final String SIGN_COMMER = ",";
	public static final String SIGN_EQUAL = "=";
	public static final String SIGN_SEMICOLON = ":";
	
	private StringUtil() {}
	
	/**
	 * 检测字符串是否为空字符串
	 * 字符串为空的标准：null或全部由空字符组成的字符串
	 * @param input 待检测字符串
	 * @return
	 * <li>true：字符串是空字符串</li>
	 * <li>false：字符串不是空字符串</li>
	 */
	public static boolean isEmpty(String input) {
		return (input == null || input.trim().length() == 0);
	}
	
	/**
	 * 生成固定长度的随机字符串
	 * @param len 随机字符串长度
	 * @return 生成的随机字符串
	 */
	public static String getRandomString(final int len) {
		if (len < 1) return "";
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(LETTER_AND_DIGIT[random.nextInt(LETTER_AND_DIGIT_LENGTH)]);
		}
		return sb.toString();
	}
	
	/**
	 * 生成固定长度的随机字符串
	 * @param len 随机字符串长度
	 * @param dictionary 字符串字典
	 * @return 生成的随机字符串
	 */
	public static String getRandomString(final int len, char[] dictionary) {
		if (len < 1) return "";
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(dictionary[random.nextInt(dictionary.length)]);
		}
		return sb.toString();
	}
	
	/**
	 * 字符串是否由可见的ascii字符组成
	 * @param str
	 * @return
	 */
	public static boolean isAsciiPrintable(String str) {
		if (str == null) {
			return false;
		}
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (isAsciiPrintable(str.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 字符是否为可见的ascii字符
	 * @param ch
	 * @return
	 */
	public static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}
	
	/**
	 * 将字符串转换为unicode的表示形式
	 * @param s
	 * @return
	 */
	public static String str2unicode(String s) {
		StringBuilder uStr = new StringBuilder();
		int size = s.length();
		for (int i = 0; i < size; i++) {
			int iValue = s.codePointAt(i);
			if (iValue < 16) {
				uStr.append("\\u000").append(Integer.toHexString(iValue));
			} else if (iValue < 256) {
				uStr.append("\\u00").append(Integer.toHexString(iValue));
			} else if (iValue < 4096) {
				uStr.append("\\u0").append(Integer.toHexString(iValue));
			} else {
				uStr.append("\\u").append(Integer.toHexString(iValue));
			}
		}
		return uStr.toString();
	}
	
	/**
	 * html转义
	 * @param str
	 * @return
	 */
	public static String htmlEscape(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder sb = null;
		int len = str.length();
		char ch;
		try {
			for (int i = 0; i < len; i++) {
				ch = str.charAt(i);
				switch (ch) {
				// 过滤一些恶意代码,这些字符会让utf-8的页面混乱，倒排
				case '\'':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&#39;");
					break;
				case '\"':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&quot;");
					break;
				case '\\':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("\\&shy;");
					break;
				case '>':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&gt;");
					break;
				case '<':
					if (sb == null) {
						sb = new StringBuilder(str.length() << 1);
						sb.append(str, 0, i);
					}
					sb.append("&lt;");
					break;
				case '&':
					int in = str.indexOf(';', i + 1);
					if (in != -1 && in - i < 9 && (str.substring(i + 1, in).indexOf('&') == -1)) {// 防止2次转义,同时防止&..&这种状况出现。
						if (sb != null) {
							sb.append(ch);
						}
					} else {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&amp;");
					}
					break;

				// 以下两个是为了转换/*xxxx*/这种注释的，这种注释会让代码乱掉
				case '/':
					if ((i + 1) < str.length() && str.charAt(i + 1) == '*') {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&#47;&#42;");
						++i;
						break;
					}

					if (sb != null) {
						sb.append(ch);
					}
					break;
				case '*':
					if ((i + 1) < str.length() && str.charAt(i + 1) == '/') {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
						sb.append("&#42;&#47;");
						++i;
						break;
					}

					if (sb != null) {
						sb.append(ch);
					}
					break;
				default:
					// 防止几个从左到右的字符影响utf-8页面
					if (Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
							|| Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
							|| Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE) {
						if (sb == null) {
							sb = new StringBuilder(str.length() << 1);
							sb.append(str, 0, i);
						}
					} else if (sb != null) {
						sb.append(ch);
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(str);
		}
		if (null != sb) {
			return sb.toString();
		} else {
			return str;
		}
	}
	
	/**
	 * html反转义
	 * @param str
	 * @return
	 */
	public static String htmlUnescape(String str) {
		if (str == null) {
			return null;
		}
		int firstAmp = str.indexOf('&');
		if (firstAmp == -1) {
			return str;
		} else {
			StringBuilder sb = new StringBuilder(str.length() << 1);
			sb.append(str, 0, firstAmp);
			char c;
			for (int i = firstAmp; i < str.length(); i++) {
				c = str.charAt(i);
				if (c == '&') {
					int nextIdx = i + 1;
					int semiColonIdx = str.indexOf(';', nextIdx);
					if (semiColonIdx == -1) {
						sb.append(c);
						continue;
					}
					int amphersandIdx = str.indexOf('&', nextIdx);
					// 防止&...&..;这种被转换
					if (amphersandIdx != -1 && amphersandIdx < semiColonIdx) {
						sb.append(c);
						continue;
					}
					String entityContent = str.substring(nextIdx, semiColonIdx);

					if (entityContent.equalsIgnoreCase("#39")) {
						sb.append('\'');
					} else if (entityContent.equalsIgnoreCase("#40")) {
						sb.append('(');
					} else if (entityContent.equalsIgnoreCase("#41")) {
						sb.append(')');
					} else if (entityContent.equalsIgnoreCase("lt")) {
						sb.append('<');
					} else if (entityContent.equalsIgnoreCase("gt")) {
						sb.append('>');
					} else if (entityContent.equalsIgnoreCase("amp")) {
						sb.append('&');
					} else if (entityContent.equalsIgnoreCase("quot")) {
						sb.append('"');
					} else {
						sb.append(c);
					}
					i = semiColonIdx;
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		}
	}
	
	/**
     * <p>Capitalizes a String changing the first letter to title case as
     * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
     *
     * <pre>
     * StringUtils.capitalize(null)  = null
     * StringUtils.capitalize("")    = ""
     * StringUtils.capitalize("cat") = "Cat"
     * StringUtils.capitalize("cAt") = "CAt"
     * </pre>
     *
     * @param str  the String to capitalize, may be null
     * @return the capitalized String, <code>null</code> if null String input
     */
    public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
    }
    
    /**
     * <p>Uncapitalizes a String changing the first letter to title case as
     * per {@link Character#toLowerCase(char)}. No other letters are changed.</p>
     *
     * <pre>
     * StringUtils.uncapitalize(null)  = null
     * StringUtils.uncapitalize("")    = ""
     * StringUtils.uncapitalize("Cat") = "cat"
     * StringUtils.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param str  the String to uncapitalize, may be null
     * @return the uncapitalized String, <code>null</code> if null String input
     */
    public static String uncapitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen)
            .append(Character.toLowerCase(str.charAt(0)))
            .append(str.substring(1))
            .toString();
    }
    
    /**
     * 将驼峰式命名的转换为下划线命名的
     * eg.   aaaBbbCcc ->  aaa_bbb_ccc
     * @param str
     * @return
     */
    public static String convertHumpNameToUnderlineName(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(Character.isUpperCase(c)) {
				sb.append("_").append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		String s = "<embed src=\"http://player.youku.com/player.php/sid/XMjE5NTc1NDQ=/v.swf\"quality=\"high\"width=\"480\"height=\"400\"align=\"middle\"allowScriptAccess=\"sameDomain\"type=\"application/x-shockwave-flash\"></embed>";
		System.out.println(htmlEscape(s));
		System.out.println(htmlUnescape(htmlEscape(s)));
	}
	
}

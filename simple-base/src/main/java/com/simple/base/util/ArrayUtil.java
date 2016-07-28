package com.simple.base.util;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * 数组相关工具类
 */
public class ArrayUtil {

	/**
	 * 将一个数组或Collection转换为字符串连接
	 * 
	 * @param array
	 *            必须是数组或Collection
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public static String join(Object array, String p) {
		if (array != null) {
			if (array instanceof Collection) {
				array = ((Collection) array).toArray();
			}
			StringBuilder sb = new StringBuilder();
			int len = Array.getLength(array);
			for (int index = 0; index < len; index++) {
				if (index != 0) {
					sb.append(p);
				}
				sb.append(Array.get(array, index));
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 * 将一个数组或Collection转换为字符串连接
	 * 
	 * @param array
	 * @return
	 */
	public static String join(Object array) {
		return join(array, ",");
	}

	/**
	 * 判断2个数组中元素是否相同
	 * 
	 * @param src
	 * @param dest
	 * @return 只有当长度相同且每个元素都相同时，才返回true
	 */
	public static boolean sameArray(Object src, Object dest) {
		int srcLen = Array.getLength(src);
		int descLen = Array.getLength(dest);
		if (srcLen != descLen) {
			return false;
		}
		for (int i = 0; i < srcLen; i++) {
			Object m = Array.get(src, i);
			Object n = Array.get(dest, i);
			if (m == n) {
				continue;
			}
			if (m == null || n == null) {
				return false;
			}
			if (!m.equals(n)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 将String数组转换成int数组
	 * 注：暂时没有想到好的方法，如有更高效方法，麻烦帮忙改进
	 * 
	 * @param scrArray
	 * @return
	 */
	public static int[] convertIntArray(String[] scrArray) {
		if (null == scrArray || scrArray.length == 0) {
			return new int[0];
		}
		int[] destArray = new int[scrArray.length];
		for (int i = 0; i < scrArray.length; i++) {
			String _temp = scrArray[i];
			if (StringUtil.isEmpty(_temp)) {
				continue;
			}
			else {
				destArray[i] = Integer.parseInt(_temp.trim());
			}
		}
		return destArray;
	}

	/**
	 * 打印二维数组至console
	 * 
	 * @param x
	 */
	public static void printArray(int[][] x) {
		System.out.println("矩阵为：");
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				System.out.print(x[i][j] + " ");
			}
			System.out.println();
		}
	}

}

/**
 * 
 */
package com.simple.base;

import com.simple.base.util.expression.Expression;
import com.simple.base.util.expression.ExpressionUtil;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年6月27日
 */
public class ExpressionTest {
	public static void main(String args[]){
		Expression express = ExpressionUtil.buildExpression("$%10");
		System.out.println(express.getValue(102));
	}
}

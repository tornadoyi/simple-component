/**
 * 
 */
package com.simple.base.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeBuilder;

/**
 * @author yongchao.zhao@happyelements.com
 * 2016年7月21日
 */
public class SimpleXmlParser {
	
	public static <T> T resolveFile(File file, Class<T> clazz) throws Exception {
		Serializer ser = new Persister();
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			InputNode inputNode = NodeBuilder.read(in);
			T t = ser.read(clazz, inputNode, false);
			return t;
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
	}
}

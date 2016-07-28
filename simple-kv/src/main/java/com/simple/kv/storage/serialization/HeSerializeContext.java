package com.simple.kv.storage.serialization;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.zip.Deflater;

/**
 * 序列化时的上下文环境
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
class HeSerializeContext {

	/** 超过这个字节时就压缩 */
	static final int COMPRESS_NEED = 100;
	/** version，用于将来更换实现实兼容旧数据，version的值在0~63之间 */
	static final int VERSION = 1;
	
	static final int NO_COMPRESS_FLAG = 0x00;
	static final int DEFLATE_COMPRESS_FLAG = 1 << 5;
	
	static final int VERSION_MASK = 0x1f;
	static final int COMPRESS_MASK = 0xe0;
	
	private final ByteArrayOutputStream out;
	private int written;
	private IdentityHashMap<Object, Integer> writtenObjectMap;
	
	private byte curKey;
	private String curField;
	private String curClass;
	
	HeSerializeContext() {
		out = new ByteArrayOutputStream();
		out.write(0); // 占位，不计入written
	}

	private void incCount(int value) {
		written += value;
	}
	
	/**
	 * 记录即将写入的对象，用于做引用检查，偏移量使用当前已写入的字节数
	 * @param obj 即将写入的对象
	 */
	void recordWrittenObject(Object obj) {
		if(!asRef(obj)) {
			return;
		}
		if(writtenObjectMap == null) {
			writtenObjectMap = new IdentityHashMap<Object, Integer>();
		}
		writtenObjectMap.put(obj, written);
	}
	
	private static boolean asRef(Object o) {
		if (o == null || o instanceof Number || o instanceof Boolean || o instanceof String || o instanceof Character || o instanceof Date) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * 是否包含已经写过的对象
	 * @param obj 比较对象
	 * @return
	 */
	boolean containsWrittenObject(Object obj) {
		return writtenObjectMap != null && writtenObjectMap.containsKey(obj);
	}
	
	/**
	 * 得到已写入过对象的offset
	 * @param obj 对象
	 * @return 已写入对象在序列化字节数组中的偏移量，返回-1表示没有
	 */
	int getWrittenObjectOffset(Object obj) {
		if(writtenObjectMap != null) {
			Integer offset = writtenObjectMap.get(obj);
			return offset != null ? offset : -1;
		} else {
			return -1;
		}
	}
	
	/**
	 * 按引用方式写入对象（即找到之前和它同一引用的已写入对象的偏移量，然后写入该偏移量）
	 * @param obj 对象
	 */
	void writeObjectRefOffset(Object obj) {
		int offset = getWrittenObjectOffset(obj);
		if(offset <= 0xff) {
			write(1);
			write(offset);
		} else if(offset <= 0xffff) {
			write(2);
			write((offset >>> 8) & 0xFF);
			write((offset >>> 0) & 0xFF);
		} else if(offset <= 0xffffff) {
			write(3);
			write((offset >>> 16) & 0xFF);
			write((offset >>> 8) & 0xFF);
			write((offset >>> 0) & 0xFF);
		} else {
			write(4);
			write((offset >>> 24) & 0xFF);
			write((offset >>> 16) & 0xFF);
			write((offset >>> 8) & 0xFF);
			write((offset >>> 0) & 0xFF);
		}
	}
	
	/**
	 * 写入一个字节
	 * 
	 * @param b
	 *            字节
	 */
	void write(int b) {
		out.write(b);
		incCount(1);
	}

	/**
	 * 写入字节数组
	 * 
	 * @param b
	 *            字节数组
	 * @param off
	 *            偏移量
	 * @param len
	 *            从偏移量开始的长度
	 */
	void write(byte b[], int off, int len) {
		out.write(b, off, len);
		incCount(off + len > b.length ? b.length - off : len);
	}

	/**
	 * 得到已写入的字节数
	 * 
	 * @return
	 */
	int getWritten() {
		return written;
	}

	private static byte[] compress(byte[] src, int offset, int len) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		if(offset > 0) { // 先写入不需要压缩的部分
			bout.write(src, 0, offset);
		}
		Deflater compresser = new Deflater();
		compresser.setInput(src, offset, len);
		compresser.finish();
		byte[] buf = new byte[512];
		int length = compresser.deflate(buf);
		while(length > 0) {
			bout.write(buf, 0, length);
			length = compresser.deflate(buf);
		}
		compresser.end();
		return bout.toByteArray();
	}
	
	/**
	 * 得到最终的字节数组
	 * @return
	 */
	byte[] toFinallyByteArray() {
		byte[] bytes = out.toByteArray();
		int flag = VERSION;
		if(bytes.length >= COMPRESS_NEED) {
			bytes = compress(bytes, 1, bytes.length - 1);
			flag |= DEFLATE_COMPRESS_FLAG;
		} else {
			flag |= NO_COMPRESS_FLAG;
		}
		bytes[0] = (byte)flag;
		return bytes;
	}

	byte getCurKey() {
		return curKey;
	}

	void setCurKey(byte curKey) {
		this.curKey = curKey;
	}

	String getCurField() {
		return curField;
	}

	void setCurField(String curField) {
		this.curField = curField;
	}

	String getCurClass() {
		return curClass;
	}

	void setCurClass(String curClass) {
		this.curClass = curClass;
	}
	
	String curInfoStr() {
		return "curClass: " + this.curClass + ", curField: " + this.curField + ", curKey: " + this.curKey;
	}

}

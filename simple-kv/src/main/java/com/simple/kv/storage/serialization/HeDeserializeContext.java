package com.simple.kv.storage.serialization;

import static com.simple.kv.storage.serialization.HeSerializeContext.COMPRESS_MASK;
import static com.simple.kv.storage.serialization.HeSerializeContext.DEFLATE_COMPRESS_FLAG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.simple.kv.storage.error.KVException;

/**
 * 反序列化的上下文环境
 * 
 * @author <a href="mailto:daniel.zeng@happyelements.com">daniel.zeng</a>
 * 
 */
class HeDeserializeContext {

	private final ByteArrayInputStream in;
	private int read;
	private Map<Integer, Object> readObjectMap;

	public HeDeserializeContext(byte[] bytes) {
		int flag = bytes[0] & 0xff;
		int compressFlag = flag & COMPRESS_MASK;
		if(compressFlag == DEFLATE_COMPRESS_FLAG) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			Inflater decompresser = new Inflater();
			decompresser.setInput(bytes, 1, bytes.length - 1);
			byte[] buf = new byte[512];
			try {
				int length = decompresser.inflate(buf);
				while(length > 0) {
					bout.write(buf, 0, length);
					length = decompresser.inflate(buf);
				}
			} catch (DataFormatException e) {
				throw new KVException("UNKNOWN_ERROR decompress error", e);
			}
			decompresser.end();
			in = new ByteArrayInputStream(bout.toByteArray());
		} else {
			in = new ByteArrayInputStream(bytes, 1, bytes.length - 1);
		}
	}

	private void incCount(int value) {
		read += value;
	}

	/**
	 * 记录读出的对象，用于做引用检查
	 * 
	 * @param obj
	 *            读出的对象
	 * @param offset
	 *            读出对象在序列化字节数组中的偏移量
	 */
	void recordReadObject(Object obj, int offset) {
		if (readObjectMap == null) {
			readObjectMap = new HashMap<Integer, Object>();
		}
		readObjectMap.put(offset, obj);
	}

	/**
	 * 记录读出的对象，用于做引用检查，在创建对象但未读时可调用此方法，偏移量使用当前已读出的字节数
	 * 
	 * @param obj
	 *            已创建，但未填充的对象
	 */
	void recordReadObject(Object obj) {
		recordReadObject(obj, read);
	}

	/**
	 * 得到已读出对象
	 * 
	 * @param offset
	 *            偏移量
	 * @return 已读出对象
	 */
	Object getReadObjectByOffset(Integer offset) {
		if (readObjectMap != null) {
			return readObjectMap.get(offset);
		} else {
			return null;
		}
	}

	/**
	 * 按引用方式读出对象（即找到之前和它同一引用的已读出对象）
	 * @return 根据引用值找到的对象
	 */
	Object readObjectAsRef() {
		int offset;
		int b = read();
		if (b == 1) {
			offset = read();
			if(offset < 0) {
				throw new KVException("error object ref offset");
			}
		} else if (b == 2) {
			int ch1 = read();
			int ch2 = read();
			if ((ch1 | ch2) < 0) {
				throw new KVException("error object ref offset");
			}
			offset = (ch1 << 8) + (ch2 << 0);
		} else if (b == 3) {
			int ch1 = read();
			int ch2 = read();
			int ch3 = read();
			if ((ch1 | ch2 | ch3) < 0) {
				throw new KVException("error object ref offset");
			}
			offset = (ch1 << 16) + (ch2 << 8) + (ch3 << 0);
		} else if (b == 4) {
			int ch1 = read();
	        int ch2 = read();
	        int ch3 = read();
	        int ch4 = read();
	        if ((ch1 | ch2 | ch3 | ch4) < 0) {
	        	throw new KVException("error object ref offset");
	        }
	        offset = ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
		} else {
			throw new KVException("error object ref offset num flag: " + b);
		}
		return getReadObjectByOffset(offset);
	}

	/**
	 * 读出一个字节
	 * 
	 * @return 读出的字节，If no byte is available because the end of the stream has been reached, the value -1 is returned
	 */
	int read() {
		int ch = in.read();
		if (ch > -1) {
			incCount(1);
		}
		return ch;
	}

	/**
	 * 读出一些字节到给定的字节数组中
	 * 
	 * @param b
	 *            给定的字节数组
	 * @param off
	 *            偏移量
	 * @param len
	 *            读取的长度
	 */
	void readFully(byte b[], int off, int len) {
		if (len < 0)
		    throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
		    int count = in.read(b, off + n, len - n);
		    if (count < 0)
		    	throw new KVException("EOF_ERROR deserializing");
		    n += count;
		}
		incCount(n);
	}

	/**
	 * 得到已读出的字节数
	 * 
	 * @return
	 */
	int getReadBytesNum() {
		return read;
	}

}

package com.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;

public class IMageUtil {
	private static   final String jpg ="data:image/jpeg;base64,";
	private static   final String png="data:image/png;base64,";
	private static   final String tif="data:image/tiff;base64,";
	private static   final String bmp="data:image/bmp;base64";

	/**
	 * TODO:将byte数组以Base64方式编码为字符串
	 * 
	 * @param bytes
	 *            待编码的byte数组
	 * @return 编码后的字符串
	 */
	public static String encode(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}

	/**
	 * TODO:将以Base64方式编码的字符串解码为byte数组
	 * 
	 * @param encodeStr
	 *            待解码的字符串
	 * @return 解码后的byte数组
	 * @throws IOException
	 */
	public static byte[] decode(String encodeStr) throws IOException {
		byte[] bt = null;
		bt = Base64.decodeBase64(encodeStr);
		return bt;
	}

	/**
	 * TODO:将两个byte数组连接起来后，返回连接后的Byte数组
	 * 
	 * @param front
	 *            拼接后在前面的数组
	 * @param after
	 *            拼接后在后面的数组
	 * @return 拼接后的数组
	 */
	public static byte[] connectBytes(byte[] front, byte[] after) {
		byte[] result = new byte[front.length + after.length];
		System.arraycopy(front, 0, result, 0, after.length);
		System.arraycopy(after, 0, result, front.length, after.length);
		return result;
	}

	/**
	 * TODO:将图片以Base64方式编码为字符串
	 * 
	 * @param imgUrl
	 *            图片的绝对路径（例如：D:\\jsontest\\abc.jpg）
	 * @return 编码后的字符串
	 * @throws IOException
	 */
	public static String encodeImage(String imgUrl) throws IOException {
		FileInputStream fis = new FileInputStream(imgUrl);
		byte[] rs = new byte[fis.available()];
		fis.read(rs);
		fis.close();
		return encode(rs);
	}
	
	public static String removeScheme(String base64Img) {
		String result=null;
		if(base64Img.contains(jpg)){
			 result=base64Img.replace(jpg, "");
		}else if(base64Img.contains(tif)){
			result=base64Img.replace(tif, "");
		}else if (base64Img.contains(bmp)){
			result=base64Img.replace(bmp, "");
		}else {
			result=base64Img.replace(png, "");
		}
		return result;
	}
}

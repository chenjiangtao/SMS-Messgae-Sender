package com.fangcs.sms.protocol.cmpp.util;

import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA. User: Administrator Date: 14-6-26 Time: 上午11:15
 * To change this template use File | Settings | File Templates.
 */
public class MD5 {
	public static byte[] md5(byte[] source) {
		byte[] tmp = null;

		java.security.MessageDigest md;
		try {
			md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			tmp = md.digest();
		} catch (NoSuchAlgorithmException e) {
			tmp = null;
		}
		return tmp;
	}

}

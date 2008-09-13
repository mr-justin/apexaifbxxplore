/**
 * <copyright>
 * Copyright (c) 2006 IBM Corporation.
 * All Rights Reserved.
 * </copyright>
 *
 * $Id: MD5Encrypt.java,v 1.2 2007/04/18 06:55:34 lql Exp $
 */
package com.ibm.semplore.util;

import java.security.MessageDigest;

/**
 * @author liu qiaoling
 *
 */
public class MD5Encrypt
{

    private final static String[] hexDigits = {
        "0", "1", "2", "3", "4", "5", "6", "7",
        "8", "9", "a", "b", "c", "d", "e", "f"};

    /**
     * 转换字节数组为16进制字串
     * @param b 字节数组
     * @return 16进制字串
     */
    private static String byteArrayToString(byte[] b) {
      StringBuffer resultSb = new StringBuffer();
      for (int i = 0; i < b.length; i++) {
        resultSb.append(byteToHexString(b[i]));//若使用本函数转换则可得到加密结果的16进制表示，即数字字母混合的形式
      }
      return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
      int n = b;
      if (n < 0) {
        n = 256 + n;
      }
      int d1 = n / 16;
      int d2 = n % 16;
      return hexDigits[d1] + hexDigits[d2];
    } 

    /**
     * Returns the message digest of origin string in form of hex string of length no more than given limit.
     * @param origin
     * @param resultLenLimit the length limit of result 
     * @return
     */
    public static String MD5Encode(String origin, int resultLenLimit) {
      String resultString = null;
      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        resultString = byteArrayToString(md.digest(origin.getBytes()));
        if (resultString.length() > resultLenLimit)
            resultString = resultString.substring(0, resultLenLimit);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      return resultString;
    }

}

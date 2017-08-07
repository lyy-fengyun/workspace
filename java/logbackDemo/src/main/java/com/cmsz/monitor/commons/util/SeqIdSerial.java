package com.cmsz.monitor.commons.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * 生成流水号类
 * 
 * @author wuhang
 * 
 */
public class SeqIdSerial {

	/**
	 * 以随机种子初始化random对象
	 */
	private static final Random RANDOM = new Random(UUID.randomUUID()
			.hashCode());

	/**
	 * 内部交易码长度为30位
	 */
	private static int INTER_TRANS_ID_LENGTH = 30;
	/**
	 * 通知流水号长度为32位
	 */
	private static int RCV_TRANS_ID_LENGTH = 32;

	private static final String DEFAULT_TANSID_TYPE = "10";
	private static final String DEFAULT_TANSID_PROCODE = "999";

	private static final char[] mm = new char[] { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9' };

	/**
	 * 获取transid 操作流水号，生成规则 消息类型（10） + 省编码（999） + 17位日期毫秒数 + 10位随机数
	 * 
	 * @return String transid
	 */
	public static String getTransID() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.getDefault());
		
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_TANSID_TYPE).append(DEFAULT_TANSID_PROCODE)
				.append(sdf.format(new Date()))
				.append(generateRandomString(10));
		return sb.toString();
	}

	/**
	 * 获取内部流水号，生成规则 17位日期毫秒数 + 当前应用标识APP_KEY + 随机数补齐30位
	 * 
	 * @return String interTransId
	 */
	public static String getInterTransID() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.getDefault());
		
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_TANSID_TYPE)
				.append(sdf.format(new Date()))
				.append(generateRandomString(INTER_TRANS_ID_LENGTH
						- sb.length()));
		return sb.toString();
	}

	/**
	 * 获取内部流水号，生成规则 17位日期毫秒数 + 当前应用标识APP_KEY + 随机数补齐32位
	 * 
	 * @return String interTransId
	 */
	public static String getRcvTransID() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.getDefault());
		
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_TANSID_TYPE)
				.append(sdf.format(new Date()))
				.append(generateRandomString(RCV_TRANS_ID_LENGTH - sb.length()));
		return sb.toString();
	}

	/**
	 * 生成len长度的随机数序列
	 * 
	 * @param len
	 * @return String randomString
	 */
	public static String generateRandomString(int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(mm[RANDOM.nextInt(mm.length)]);
		}
		return sb.toString();
	}

}
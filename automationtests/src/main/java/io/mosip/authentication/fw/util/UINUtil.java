package io.mosip.authentication.fw.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import io.mosip.authentication.fw.dto.UinDto;
import io.mosip.authentication.fw.dto.UinStaticPinDto;
import io.mosip.authentication.fw.dto.VidDto;

public class UINUtil extends RunConfigUtil{
	
	private static final Logger uinUtilLogger = Logger.getLogger(UINUtil.class);
	
	/**
	 * The method return random UIN from property file
	 * 
	 * @return Random UIN
	 */
	private static int uinCount = 0;

	public static String getRandomUINKey() {
		getUinPropertyValue(getUinPropertyPath());
		if (UinDto.getUinData().size() <= uinCount)
			uinCount = 0;
		while (UinDto.getUinData().size() > uinCount) {
			Object[] randomKeys = UinDto.getUinData().keySet().toArray();
			Object key = randomKeys[uinCount];
			if (UinDto.getUinData().get(key).toString().contains("valid")) {
				uinUtilLogger.info("UIN Key: " + UinDto.getUinData().get(key).toString());
				uinCount++;
				return key.toString();
			} else
				uinCount++;
			if (UinDto.getUinData().size() <= uinCount)
				uinCount = 0;
		}
		return "NoUINFound";
	}
	/**
	 * The method get UIN number using keyword from property file
	 * 
	 * @param keyword
	 * @return UIN number
	 */
	public static String getUinNumber(String keyword) {
		if (keyword.contains("EVEN")) {
			int count = 1;
			while (count > 0) {
				String key = getRandomUINKey();
				String lastNumberAsString = key.substring(key.length() - 1, key.length());
				int lastNum = Integer.parseInt(lastNumberAsString);
				if (lastNum % 2 == 0)
					return key;
				else
					count++;
			}
		} else if (keyword.contains("ODD")) {
			int count = 1;
			while (count > 0) {
				String key = getRandomUINKey();
				String lastNumberAsString = key.substring(key.length() - 1, key.length());
				int lastNum = Integer.parseInt(lastNumberAsString);
				if (lastNum % 2 != 0)
					return key;
				else
					count++;
			}
		} else if (keyword.equals("$UIN$")) {
			String key = getRandomUINKey();
			return key;
		} else {
			keyword = keyword.replace("$", "");
			String keys[] = keyword.split(":");
			String keywrdToFind = keys[2];
			return getUINKey(keywrdToFind);
		}
		return "NoLoadedUINFound";
	}
	/**
	 * The method get static pin for UIN
	 * 
	 * @return static pin
	 */
	public static String getRandomStaticPinUINKey() {
		getStaticPinUinPropertyValue(getStaticPinUinPropertyPath());
		Object[] randomKeys = UinStaticPinDto.getUinStaticPin().keySet().toArray();
		Object key = randomKeys[new Random().nextInt(randomKeys.length)];
		return key.toString();
	}
	/**
	 * The method get UIN using keyword from property file
	 * 
	 * @param keywordToFind
	 * @return UIN
	 */
	@SuppressWarnings("null")
	public static String getUINKey(String keywordToFind) {
		getUinPropertyValue(getUinPropertyPath());
		List<String> randomKeys = new ArrayList<String>();
		for (Entry<String, String> entry : UinDto.getUinData().entrySet()) {
			if (entry.getValue().contains(keywordToFind)) 
				randomKeys.add(entry.getKey());
		}
		if (randomKeys.size() != 0)
			return randomKeys.get(new Random().nextInt(randomKeys.size()));
		else
			return "NoLoadedUINFound";
	}
	
	/**
	 * The method get UIN property value from property file
	 * 
	 * @param path
	 */
	protected static void getUinPropertyValue(String path) {
		Properties prop = AuthTestsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		UinDto.setUinData(map);
	}
	/**
	 * The method get static pin for UIN property value
	 * 
	 * @param path
	 */
	public static void getStaticPinUinPropertyValue(String path) {
		Properties prop = AuthTestsUtil.getPropertyFromRelativeFilePath(path);
		Map<String, String> map = new HashMap<String, String>();
		for (String key : prop.stringPropertyNames()) {
			String value = prop.getProperty(key);
			map.put(key, value);
		}
		UinStaticPinDto.setUinStaticPin(map);
	}
	
	/**
	 * The method get VID for vidKey
	 * 
	 * @param uin
	 * @return VID
	 */
	public static String getUinForVid(String vid) {
		VIDUtil.getVidPropertyValue(getVidPropertyPath());
		for (Entry<String, String> entry : VidDto.getVid().entrySet()) {
			if (entry.getValue().contains(vid))
				return entry.getKey();
		}
		return "NoLoadedVIDFound";
	}

}

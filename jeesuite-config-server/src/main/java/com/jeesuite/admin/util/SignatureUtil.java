package com.jeesuite.admin.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.jeesuite.common.util.DigestUtils;

public class SignatureUtil {

	public static String buildSign(Map<String, String> sPara) {
		
//    	String prestr = createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
//    	
//    	prestr = prestr + PayConfig.getConfig("alipay.key");
//    	String mysign = DigestUtils.md5Hex(getContentBytes(prestr, PayConfig.getConfig("alipay.charset")));
//        return mysign;
		return null;
    }
	

    

    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {
    	
    	if(params == null || params.isEmpty())return "";

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
}

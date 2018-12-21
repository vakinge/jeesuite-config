package com.jeesuite.admin.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.admin.exception.JeesuiteBaseException;
import com.jeesuite.common.json.JsonUtils;

public class ConfigParseUtils {

	
	public static Map<String, Object> parseConfigToKVMap(AppconfigEntity config) {
		Map<String, Object> result = new LinkedHashMap<>();
		parseConfigToKVMap(result, config);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static void parseConfigToKVMap(Map<String, Object> result, AppconfigEntity config) {
		if(config.getType() == 1){
			if(config.getName().toLowerCase().endsWith(".xml")){
				parseDataFromXML(result,config.getContents());
			}else{				
				parseFromProps(result, config.getContents());
			}
		}else if(config.getType() == 2){
			result.put(config.getName(), config.getContents());
		}else if(config.getType() == 3){
			Map<String,Object> configs = JsonUtils.toObject(config.getContents(), Map.class);
			result.putAll(configs);
		}
		
		//替换引用
		String value;
		for (String key : result.keySet()) {
			value =  StringUtils.trimToEmpty(result.get(key).toString());
			if(value.contains("${")){
				setReplaceHolderRefValue(result,key,value);
			}
		}
	}

	private static void setReplaceHolderRefValue(Map<String, Object> result, String key, String value) {
		
		String[] segments = value.split("\\$\\{");
		String seg;
		
		StringBuilder finalValue = new StringBuilder();
		for (int i = 0; i < segments.length; i++) {
			seg = StringUtils.trimToNull(segments[i]);
			if(StringUtils.isBlank(seg))continue;
			
			if(seg.contains("}")){	
				String refKey = seg.substring(0, seg.indexOf("}")).trim();
				//其他非${}的占位符如：{{host}}
				String withBraceString = null;
				if(seg.contains("{")){
					withBraceString = seg.substring(seg.indexOf("}")+1);
				}
				
				//如果包含默认值，如：${host:127.0.0.1}
				String orginKey = refKey;
				if(refKey.contains(":")){
					refKey = refKey.split(":")[0];
				}
				
				String refValue = result.containsKey(refKey) ? result.get(refKey).toString() : "${" + orginKey + "}";
				finalValue.append(refValue);
				
				if(withBraceString != null){
					finalValue.append(withBraceString);
				}else{
					String[] segments2 = seg.split("\\}");
					if(segments2.length == 2){
						finalValue.append(segments2[1]);
					}
				}
			}else{
				finalValue.append(seg);
			}
		}
		
		result.put(key, finalValue.toString());
	}

	public static void parseFromProps(Map<String, Object> result, String content) {
		String[] lines = content.split("\n");
		for (String line : lines) {
			if(StringUtils.isBlank(line) || line.startsWith("#") || line.indexOf("=") < 0)continue;
			//考虑 value包含=的情况
			String key = line.substring(0, line.indexOf("=")).trim();
			String value = line.substring(line.indexOf("=") + 1).trim();
			if(StringUtils.isNotBlank(value)){	
				result.put(key, value);
			}
		}
	}

	private static void parseDataFromXML(Map<String, Object> result, String xmlContents) {
		 Document doc = null;
		try {
            //doc = DocumentHelper.parseText(xmlContents);
			SAXReader reader = new SAXReader();
			 //忽略dtd验证
			reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); 
            InputSource source = new InputSource(new StringReader(xmlContents));
		    source.setEncoding("UTF-8");
		    doc = reader.read(source);
            Element rootElt = doc.getRootElement(); 
            Iterator<?> iter = rootElt.elementIterator("entry"); 
            // 遍历head节点
            while (iter.hasNext()) {
                Element elm = (Element) iter.next();
                String stringValue = elm.getStringValue();
                if(StringUtils.isNotBlank(stringValue)){                	
                	result.put(elm.attribute("key").getStringValue(), stringValue.trim());
                }
            }
        } catch (Exception e) {
        	if(e instanceof  org.dom4j.DocumentException){
        		throw new JeesuiteBaseException(500, "xml文件格式错误");
        	}
        	throw new RuntimeException(e);
        }
	}
	
   public static void main(String[] args) throws Exception {
		
		Map<String, Object> result = new HashMap<>();
		result.put("spring.cloud.client.ipAddress", "10.121.10.111");
		result.put("spring.application.name", "demo");
		result.put("server.port", "8002");
		//
		String key = "instance.id";
		String value = "${spring.cloud.client.ipAddress}->${spring.application.name}:${server.port}";
		result.put(key, value);
		
		setReplaceHolderRefValue(result, key, value); 
		
		System.out.println(result.get(key));
		
		key = "instance.id";
		value = "127.0.0.1:${server.port}";
		result.put(key, value);
		
		setReplaceHolderRefValue(result, key, value); 
		
		System.out.println(result.get(key));
	}
}

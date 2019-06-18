package com.jeesuite.admin.util;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.yaml.snakeyaml.Yaml;

import com.jeesuite.admin.dao.entity.AppconfigEntity;
import com.jeesuite.common.JeesuiteBaseException;
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
			}else if(config.getName().toLowerCase().endsWith(".yml") || config.getName().toLowerCase().endsWith(".yaml")){
				parseDataFromYaml(result,config.getContents());
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

	private static void parseFromProps(Map<String, Object> result, String content) {
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
        		throw new JeesuiteBaseException(500, "xml文件内容格式错误");
        	}
        	throw new RuntimeException(e);
        }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void parseDataFromYaml(Map<String, Object> result, String yamlContents) {
		 Yaml yaml = new Yaml();
		 try {			
			 Map map = yaml.load(yamlContents);
			 parseYamlInnerMap(null, result, map);
		} catch (Exception e) {
			e.printStackTrace();
			throw new JeesuiteBaseException(500, "文件内容格式错误");
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void parseYamlInnerMap(String keyPrefix,Map<String, Object> result,Map<String, Object> yamlData){
		Object value;
		String currentKey;
		for (Object key : yamlData.keySet()) {
			currentKey = keyPrefix == null ? key.toString() : keyPrefix + "." + key.toString();
			value = yamlData.get(key);
			if(value instanceof Map){
				parseYamlInnerMap(currentKey, result, (Map)value);
			}else{
				result.put(currentKey, value);
			}
		}
		
	}
	
   public static void main(String[] args) throws Exception {
	   String content = FileUtils.readFileToString(new File("/Users/jiangwei/tikv-docker-compose.yml"));
	   Map<String, Object> result = new HashMap<>();
	   parseDataFromYaml(result, content);
	   
	   System.out.println(JsonUtils.toPrettyJson(result));
   }
}

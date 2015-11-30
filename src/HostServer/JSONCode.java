package HostServer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import simple.JSONObject;
import simple.parser.ContainerFactory;
import simple.parser.JSONParser;
import simple.parser.ParseException;

public class JSONCode {
	public static String encode(Map<String,Object> map){
		JSONObject obj = new JSONObject();
		for(Map.Entry<String,Object> entry : map.entrySet()) {
			 String key = entry.getKey();
			 Object value = entry.getValue();
			 obj.put(key, value);
		}
		return obj.toJSONString();
	}
	public static Map<String,Object> decode(String code) throws ParseException{
		ContainerFactory containerFactory = new ContainerFactory(){
			public List creatArrayContainer() {
				return new LinkedList();
			}
			public Map createObjectContainer() {
				return new LinkedHashMap();
			}
		};
		JSONParser parser = new JSONParser();
		return (Map<String,Object>)parser.parse(code, containerFactory);
	}
}

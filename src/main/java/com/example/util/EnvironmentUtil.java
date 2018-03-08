package com.example.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;

public class EnvironmentUtil {
	
	public  static <E> List<E> getList(Environment evn,String prfix,Class<E> elementClazz){
		String[] strs=prfix.split("\\.");
		RelaxedPropertyResolver propertyResolver=null;
		Map<String, Object> dspropers=null;
		if(strs.length<=1) {
			propertyResolver = new RelaxedPropertyResolver(evn);
			dspropers=propertyResolver.getSubProperties(strs[0]);
		}else {
			String path= prfix.substring(0, prfix.length()-strs[strs.length-1].length());
			propertyResolver = new RelaxedPropertyResolver(evn, path);
			String key=strs[strs.length-1];
			dspropers=propertyResolver.getSubProperties(key);
		}
		Map<String, Map<String,String>> infos=new HashMap<>();
		for (String s : dspropers.keySet()) {
			String index=s.substring(0,s.indexOf('.'));
			if(infos.get(index)==null) {
				infos.put(index, new HashMap<>());
			}
			String key=s.substring(s.indexOf('.')+1);
			infos.get(index).put(key, dspropers.get(s).toString());
		}
		List<E> list=new ArrayList<>();
		for (Map<String, String> info : infos.values()) {
			try {
				E o=elementClazz.newInstance();
				for (String  fieldName : info.keySet()) {
					Field f =o.getClass().getDeclaredField(fieldName);
					f.setAccessible(true);
					String type = f.getType().toString();//得到此属性的类型  
		          	if (type.endsWith("String")) {  
		            f.set(o,info.get(fieldName)) ;        //给属性设值  
		          	}else if(type.endsWith("int") || type.endsWith("Integer")){  
		              f.set(o,Integer.parseInt(info.get(fieldName))) ;//给属性设值  
		          	}else{  
		              System.out.println(f.getType()+"\t");  
		          	}
				}
				list.add(o);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

}

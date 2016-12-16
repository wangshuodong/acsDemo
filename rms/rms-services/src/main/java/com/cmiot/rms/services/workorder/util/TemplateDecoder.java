package com.cmiot.rms.services.workorder.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cmiot.rms.services.workorder.bean.*;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cmiot.rms.services.workorder.exception.TemplateValidateException;

/**
 * 模版解析
 * @author lili
 */
public class TemplateDecoder {
	
	private final static Logger logger = LoggerFactory.getLogger(TemplateDecoder.class);
	
	public static SAXReader reader;  
    private static EntityResolver aresolve;//校验器  

	static {
		if (aresolve == null) {
			aresolve = new EntityResolver() {
				public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
					InputStream is = TemplateDecoder.class.getClassLoader().getResourceAsStream("tpl/BusiTemplate.xsd");
					return new InputSource(is);
				}

			};
		}
		if (reader == null) {
			reader = new SAXReader(true);
		}
		try {
			// 符合的标准
			reader.setFeature("http://apache.org/xml/features/validation/schema", true);
			reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
			reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
		} catch (SAXException e1) {
			logger.error("设置校验参数错误");
		}

	} 
	
	
	public static OperationFlow xmlDecoder(File file) throws DocumentException, FileNotFoundException{
		return xmlDecoder(new FileInputStream(file));
	}
	
	public static OperationFlow xmlDecoder(String xml) throws DocumentException, FileNotFoundException, UnsupportedEncodingException{
		return xmlDecoder(new ByteArrayInputStream(xml.getBytes("UTF-8")));
	}
	
	@SuppressWarnings("unchecked")
	public static OperationFlow xmlDecoder(InputStream is) throws DocumentException{
		// 创建saxReader对象
		SAXReader reader = new SAXReader();
		// 通过read方法读取一个文件 转换成Document对象
		Document document = reader.read(is);
		// 获取根节点元素对象
		Element root = document.getRootElement();
		//根节点为OperationFlow
		OperationFlow operationFlow = null;
		
		// 遍历所有的元素节点
		List<Element> elementList = root.elements();
		
	    for (Element e : elementList) {
	    	if("OperationFlow".equals(e.getName())){
	    		
	    		List<Attribute> attrs = e.attributes();
	    		
	    		operationFlow = parseOperationFlow(e);
	    		// 遍历属性节点
	    		for (Attribute attr : attrs) {
	    			if("operationReturn".equals(attr.getName())){
	    				operationFlow.setOperationReturn(attr.getValue());
	    			}else if ("serviceName".equals(attr.getName())) {
	    				operationFlow.setServiceName(attr.getValue());
					}
	    		}
	    	}
	    }
		return operationFlow;
	}

	
	/**
	 * 解析 OperationFlow 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static OperationFlow parseOperationFlow(Element node) {
		OperationFlow operationFlow = new OperationFlow();
		// 遍历所有的元素节点
		List<Element> elementList = node.elements();
		
	    for (Element e : elementList) {
			if("FlowBean".equals(e.getName())){
	    		operationFlow.addFlow(parseFlowBean(e));
	    	}
	    }
		return operationFlow;
	}
	
	/**
	 * 解析 FlowBean 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static FlowBean parseFlowBean(Element node) {
		FlowBean flowBean = new FlowBean();
		
		List<Attribute> attrs = node.attributes();
		
		// 遍历属性节点
		for (Attribute attr : attrs) {
			if("id".equals(attr.getName())){
				flowBean.setId(attr.getValue());
			}else if("ref".equals(attr.getName())){
				flowBean.setRef(attr.getValue());
			}else if("refSearch".equals(attr.getName())){
				flowBean.setRefSearch(attr.getValue());
			}else if("isNeedExcute".equals(attr.getName())){
				flowBean.setIsNeedExcute(attr.getValue());
			}
		}
		
		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("FlowBeanAdd".equals(child.getName())){
				flowBean.addFlow(parseFlowBeanAdd(child));
	    	}else if("FlowBeanSet".equals(child.getName())){
	    		flowBean.addFlow(parseFlowBeanSet(child));
	    	}else if("FlowBeanSearch".equals(child.getName())){
				flowBean.addFlow(parseFlowBeanSearch(child));
			}else if("FlowBeanDelete".equals(child.getName())){
				flowBean.addFlow(parseFlowBeanDelete(child));
			}
		}
		
		return flowBean;
	}
	
	/**
	 * 解析 FlowBeanAdd 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static FlowBeanAdd parseFlowBeanAdd(Element node) {
		FlowBeanAdd flowBeanAdd = new FlowBeanAdd();
		List<Attribute> attrs = node.attributes();
		
		// 遍历属性节点
		for (Attribute attr : attrs) {
			if("id".equals(attr.getName())){
				flowBeanAdd.setId(attr.getValue());
			}else if("ref".equals(attr.getName())){
				flowBeanAdd.setRef(attr.getValue());
			}else if("searchFirst".equals(attr.getName())){
				flowBeanAdd.setSearchFirst(attr.getValue());
			}else if("refSearch".equals(attr.getName())){
				flowBeanAdd.setRefSearch(attr.getValue());
			}else if("isNeedExcute".equals(attr.getName())){
				flowBeanAdd.setIsNeedExcute(attr.getValue());
			}
		}
		
		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("Command".equals(child.getName())){
				flowBeanAdd.setCommand(child.getTextTrim());
			}else if("ParamKey".equals(child.getName())){
				flowBeanAdd.setParamKey(child.getTextTrim());
			}
		}
		return flowBeanAdd;
	}
	
	
	/**
	 * 解析 FlowBeanSearch 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static FlowBeanDelete parseFlowBeanDelete(Element node) {
		FlowBeanDelete flowBeanDelete = new FlowBeanDelete();
		List<Attribute> attrs = node.attributes();
		// 遍历属性节点
		for (Attribute attr : attrs) {
			if("id".equals(attr.getName())){
				flowBeanDelete.setId(attr.getValue());
			}else if("ref".equals(attr.getName())){
				flowBeanDelete.setRef(attr.getValue());
			}else if("refSearch".equals(attr.getName())){
				flowBeanDelete.setRefSearch(attr.getValue());
			}else if("isNeedExcute".equals(attr.getName())){
				flowBeanDelete.setIsNeedExcute(attr.getValue());
			}
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("Command".equals(child.getName())){
				flowBeanDelete.setCommand(child.getTextTrim());
			}
		}
		return flowBeanDelete;
	}
	
	
	/**
	 * 解析 FlowBeanSearch 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static FlowBeanSearch parseFlowBeanSearch(Element node) {
		FlowBeanSearch flowBeanSearch = new FlowBeanSearch();
		List<Attribute> attrs = node.attributes();
		// 遍历属性节点
		for (Attribute attr : attrs) {
			if("id".equals(attr.getName())){
				flowBeanSearch.setId(attr.getValue());
			}else if("ref".equals(attr.getName())){
				flowBeanSearch.setRef(attr.getValue());
			}else if("refSearch".equals(attr.getName())){
				flowBeanSearch.setRefSearch(attr.getValue());
			}else if("isNeedExcute".equals(attr.getName())){
				flowBeanSearch.setIsNeedExcute(attr.getValue());
			}
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("Command".equals(child.getName())){
				flowBeanSearch.setCommand(child.getTextTrim());
			}else if("SplitPath".equals(child.getName())){
				flowBeanSearch.setSplitPath(child.getTextTrim());
			}else if("SplitStr".equals(child.getName())){
				flowBeanSearch.setSplitStr(child.getTextTrim());
			}else if("FlowBeanMatch".equals(child.getName())){
				flowBeanSearch.addList(parseFlowBeanMatch(child));
			}
		}
		return flowBeanSearch;
	}

	/**
	 * 解析 FlowBeanMatch 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static FlowBeanMatch parseFlowBeanMatch(Element node) {
		FlowBeanMatch flowBeanMatch = new FlowBeanMatch();
		// 当前节点下面子节点迭代器
		List<Attribute> attrs = node.attributes();
		// 遍历属性节点
		for (Attribute attr : attrs) {
			if("id".equals(attr.getName())){
				flowBeanMatch.setId(attr.getValue());
			}else if("ref".equals(attr.getName())){
				flowBeanMatch.setRef(attr.getValue());
			}else if("refSearch".equals(attr.getName())){
				flowBeanMatch.setRefSearch(attr.getValue());
			}else if("isNeedExcute".equals(attr.getName())){
				flowBeanMatch.setIsNeedExcute(attr.getValue());
			}
		}

		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("Pattern".equals(child.getName())){
				flowBeanMatch.setPattern(child.getTextTrim());
			}else if("MatchValue".equals(child.getName())){
				flowBeanMatch.setMatchValue(child.getTextTrim());
			}else if("leaf".equals(child.getName())){
				flowBeanMatch.setLeaf(child.getTextTrim());
			}
		}
		return flowBeanMatch;
	}

	/**
	 * 解析 FlowBeanSet 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static FlowBeanSet parseFlowBeanSet(Element node) {
		FlowBeanSet flowBeanSet = new FlowBeanSet();
		List<Attribute> attrs = node.attributes();
		// 遍历属性节点
		for (Attribute attr : attrs) {
			if("id".equals(attr.getName())){
				flowBeanSet.setId(attr.getValue());
			}else if("ref".equals(attr.getName())){
				flowBeanSet.setRef(attr.getValue());
			}else if("refSearch".equals(attr.getName())){
				flowBeanSet.setRefSearch(attr.getValue());
			} else if("isNeedExcute".equals(attr.getName())){
				flowBeanSet.setIsNeedExcute(attr.getValue());
			}
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("SetParam".equals(child.getName())){
				Param param = parseSetParam(child);
				flowBeanSet.addParam(param);
			}
		}
		return flowBeanSet;
	}
	
	/**
	 * 解析 SetParam 节点
	 * @param node
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Param parseSetParam(Element node) {
		Param param = new Param();
		// List<Attribute> attrs = node.attributes();
		
		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element child = it.next();
			if("Command".equals(child.getName())){
				param.setCommand(child.getTextTrim());
			}else if("ParamKey".equals(child.getName())){
				param.setParamKey(child.getTextTrim());
			}else if("Default".equals(child.getName())){
				param.setDefaultV(child.getTextTrim());
			}else if("stitch".equals(child.getName())){
				param.setStitch(child.getTextTrim());
			}else if("split".equals(child.getName())){
				param.setSplit(child.getTextTrim());
			}else if("beforeOrAfter".equals(child.getName())){
				param.setBeforeOrAfter(child.getTextTrim());
			}else if("valueType".equals(child.getName())){
				param.setValueType(child.getTextTrim());
			}else if("DefaultKey".equals(child.getName())){
				param.setDefaultKey(child.getTextTrim());
			}
		}
		return param;
	}
	
	/**
	 * 验证xml格式是否符合标准
	 * @param xml 模板内容
	 * @return
	 * @throws TemplateValidateException
	 */
    public static Map<String,String> validXmlData(String xml) {  
    	Map<String,String> retMap = new HashMap<String,String>();
        InputStreamReader streamReader=null;  
        try {  
            streamReader= new InputStreamReader(new ByteArrayInputStream(xml.getBytes("UTF-8")),"UTF-8");  
        } catch (Exception e) {  
        	logger.error("读取XML出现异常:{}",e.getLocalizedMessage());
            //throw new TemplateValidateException("读取XML出现异常:"+e.getLocalizedMessage());  
        	retMap.put("MSG_CODE", "1");
        	retMap.put("MSG", "读取XML出现异常");
            return retMap; 
        }  
          
        try {  
        	reader.setEntityResolver(aresolve);
            Document dateXml = reader.read(streamReader);  
            dateXml.setXMLEncoding("UTF-8");  
            retMap.put("MSG_CODE", "0");
            logger.info("验证通过:{}",dateXml.toString());
            return retMap;  
        } catch (DocumentException e) {  
        	logger.error("读取XML出现异常:{}",e.getMessage());
        	retMap.put("MSG_CODE", "1");
        	retMap.put("MSG", "文件校验失败");
            return retMap;  
        	//throw new TemplateValidateException("文件校验失败:"+e.getLocalizedMessage());  
        }  
          
    }  
	
}

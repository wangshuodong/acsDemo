package com.cmiot.acs.model;

import com.cmiot.acs.model.struct.DownloadStruct;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zjialin on 2016/3/3.
 */
public class RequestDownload extends AbstractMethod {
    private static final long serialVersionUID = -3151536420583179098L;
    private static final String FileType = "FileType";
    private static final String FileTypeArg = "FileTypeArg";
    private static final String ArgStructPo = "DownloadStruct";
    private static final String Name = "Name";
    private static final String Value = "Value";

    private String fileType;
    private List<DownloadStruct> fileTypeArg;

    public RequestDownload() {
        methodName = "RequestDownload";
        fileTypeArg = new ArrayList<DownloadStruct>();
    }


    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public List<DownloadStruct> getFileTypeArg() {
        return fileTypeArg;
    }

    public void setFileTypeArg(List<DownloadStruct> fileTypeArg) {
        this.fileTypeArg = fileTypeArg;
    }

    @Override
    protected void addField2Body(Element body, SoapMessageModel soapMessageModel) {
        Element fileTypeElement = soapMessageModel.createElement(FileType);
        Element fileTypeArgElement = soapMessageModel.createElement(FileTypeArg);
        if (fileTypeArg.size() > 0) {
            getArrayTypeAttribute(fileTypeArgElement, FileTypeArg, fileTypeArg.size());
            for (DownloadStruct argStruct : fileTypeArg) {
                Element argStructElement = soapMessageModel.createElement(ArgStructPo);
                Element nameItem = soapMessageModel.createElement(Name);
                nameItem.setTextContent(argStruct.getName());
                Element valueItem = soapMessageModel.createElement(Value);
                valueItem.setTextContent(argStruct.getValue());
                argStructElement.appendChild(nameItem);
                argStructElement.appendChild(valueItem);
                fileTypeArgElement.appendChild(argStructElement);
            }
        }
        body.appendChild(fileTypeElement);
        body.appendChild(fileTypeArgElement);
    }

    @Override
    protected void parseBody2Filed(Element body, SoapMessageModel soapMessageModel) {
        fileType = getRequestElement(body, FileType);
        Element paramListElement = getRequestChildElement(body, FileTypeArg);
        NodeList nodeList = paramListElement.getElementsByTagName(ArgStructPo);
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node paramStruct = nodeList.item(index);
            if (paramStruct instanceof Element) {
                DownloadStruct argStruct = new DownloadStruct();
                argStruct.setName(getRequestChildElement((Element) paramStruct, Name).getTextContent());
                argStruct.setValue(getRequestChildElement((Element) paramStruct, Value).getTextContent());
                fileTypeArg.add(argStruct);
            }
        }
    }
}

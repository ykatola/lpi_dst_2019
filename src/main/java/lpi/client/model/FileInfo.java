package lpi.client.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FileInfo {

    private String receiver;
    private String fileName;
    private String content;

    public FileInfo() {
    }

    public FileInfo(String receiver, String fileName, String content) {
        this.receiver = receiver;
        this.fileName = fileName;
        this.content = content;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }
}

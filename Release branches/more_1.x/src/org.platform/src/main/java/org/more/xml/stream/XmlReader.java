/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.more.xml.stream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.more.util.StringUtils;
import org.more.xml.stream.TextEvent.Type;
/**
 * <b>Level 1</b>�����ݷ��ʲ��ԡ�����Ĺ����ǽ�xml������ת����Ϊxml�¼��������ҿ�����ɨ��xmlʱִ��xml�ĺ��Բ��ԡ�
 * @version 2010-9-8
 * @author ������ (zyc@byshell.org)
 */
public class XmlReader {
    /**Ĭ��ʹ�ñ���*/
    public final static String DefaultEncoding = "utf-8";
    private InputStream        xmlStrema       = null;   //��ȡXml���ݵ���������
    private boolean            ignoreComment   = true;   //�Ƿ����Xml�е�����ע�ͽڵ㡣
    private boolean            ignoreSpace     = true;   //�Ƿ����Xml�пɺ��ԵĿո�
    //--------------------------------------------------------------------
    /**����һ��XmlReader���������Ķ�fileName������������Xml�ļ���*/
    public XmlReader(String fileName) throws FileNotFoundException {
        this.xmlStrema = new FileInputStream(fileName);
    }
    /**����һ��XmlReader���������Ķ�file������������Xml�ļ���*/
    public XmlReader(File file) throws FileNotFoundException {
        this.xmlStrema = new FileInputStream(file);
    }
    /**����һ��XmlReader���������Ķ�xmlStrema������������Xml�ļ�����*/
    public XmlReader(InputStream xmlStrema) {
        if (xmlStrema == null)
            throw new NullPointerException("InputStream���Ͳ���Ϊ�ա�");
        this.xmlStrema = xmlStrema;
    }
    //--------------------------------------------------------------------
    /**����һ��booleanֵ����ֵ��ʾ���Ƿ�����ڶ�ȡXML�ڼ䷢�ֵ������ڵ㡣����true��ʾ���ԣ�false��ʾ�����ԡ�*/
    public boolean isIgnoreComment() {
        return this.ignoreComment;
    }
    /**����һ��booleanֵ����ֵ��ʾ���Ƿ�����ڶ�ȡXML�ڼ䷢�ֵ������ڵ㡣true��ʾ���ԣ�false��ʾ�����ԡ�*/
    public void setIgnoreComment(boolean ignoreComment) {
        this.ignoreComment = ignoreComment;
    }
    /**����һ��booleanֵ����ֵ��ʾ���Ƿ�����ڶ�ȡXML�ڼ䷢�ֵĿɺ��ԵĿո��ַ������� [XML], 2.10 "White Space Handling"��������true��ʾ���ԣ�false��ʾ�����ԡ�*/
    public boolean isIgnoreSpace() {
        return this.ignoreSpace;
    }
    /**����һ��booleanֵ����ֵ��ʾ���Ƿ��ڶ�ȡXML�ڼ���Կɺ��ԵĿո��ַ������� [XML], 2.10 "White Space Handling"����true��ʾ���ԣ�false��ʾ�����ԡ�*/
    public void setIgnoreSpace(boolean ignoreSpace) {
        this.ignoreSpace = ignoreSpace;
    }
    //--------------------------------------------------------------------
    /** ��ȡStax�Ķ����Ĺ��������������ͨ���÷�������չXmlReader�ڶ�ȡxml�ڼ���Ժ��Ե���Ŀ��*/
    protected StreamFilter getXmlStreamFilter() {
        return null;
    };
    /**
     * �÷��������ھ�������XPath�Ƿ���һ�������Ĺ�ϵ���÷����ķ���ֵ�����˽������Ƿ�������xml��Ŀ�������������д������ɸ���Ŀ��ơ�
     * @param currentXPath ��ǰ������ɨ�赽��XPath��
     * @param testXPath ��ʾ������Ե�XPath��
     * @return ����һ��booleanֵ����ֵ�������Ƿ���Ե�ǰXPath��Ŀ��
     */
    protected boolean ignoreXPath(String currentXPath, String testWild) {
        if (testWild == null)
            return false;
        //XXX:XPath�Ƚ��㷨���Ƚ�currentXPath�Ƿ�����testXPath��Χ�ڵģ�Ŀǰʹ�õ���?��*ͨ�����
        return StringUtils.matchWild(testWild, currentXPath);
    }
    /**
     * ִ�н���Xml�ļ��������γ�xml�¼�������Щ�¼��������뵽{@link XmlAccept}���Ͷ����С�
     * ���������ignoreXPath���������γ��¼���ʱXmlReader���ᷢ���������xpath��xml�¼�����
     * @param accept ָ���¼������ն���
     * @param ignoreXPath ָ��Ҫ���Ե�XPath·����
     * @throws IOException 
     */
    public synchronized void reader(XmlAccept accept, String ignoreXPath) throws XMLStreamException, IOException {
        this.reader(accept, DefaultEncoding, ignoreXPath);
    }
    /**
     * ִ�н���Xml�ļ��������γ�xml�¼�������Щ�¼��������뵽{@link XmlAccept}���Ͷ����С�
     * ���������ignoreXPath���������γ��¼���ʱXmlReader���ᷢ���������xpath��xml�¼�����
     * @param accept ָ���¼������ն���
     * @param ignoreXPath ָ��Ҫ���Ե�XPath·����
     * @throws IOException 
     */
    public synchronized void reader(XmlAccept accept, String encoding, String ignoreXPath) throws XMLStreamException, IOException {
        if (accept == null)
            return;
        accept.beginAccept();
        //1.׼��ɨ������档
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(this.xmlStrema, encoding);
        StreamFilter filter = new NullStreamFilter(this, this.getXmlStreamFilter());
        reader = factory.createFilteredReader(reader, filter);
        //2.׼������
        StringBuffer currentXPath = new StringBuffer("/");//XPath
        ElementTree currentElement = null;//���õ�ǰ�¼�������Ԫ��
        XmlStreamEvent currentEvent = null;
        //3.��ѯ�����¼���
        while (true) {
            //(1).�����¼�����
            int xmlEvent = reader.getEventType();//��ǰ�¼�����
            //(2).�����¼�����
            switch (xmlEvent) {
            case XMLStreamConstants.START_DOCUMENT:
                //��ʼ�ĵ�
                currentEvent = new StartDocumentEvent(currentXPath.toString(), reader);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            case XMLStreamConstants.END_DOCUMENT:
                //�����ĵ�
                currentEvent = new EndDocumentEvent(currentXPath.toString(), reader);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            case XMLStreamConstants.START_ELEMENT:
                //��ʼԪ��
                if (currentXPath.indexOf("/") != currentXPath.length() - 1)
                    currentXPath.append("/");
                currentXPath.append(this.getName(reader.getName()));
                currentEvent = new StartElementEvent(currentXPath.toString(), reader);
                currentElement = new ElementTree(reader.getName(), currentElement);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            case XMLStreamConstants.END_ELEMENT:
                //����Ԫ��
                currentEvent = new EndElementEvent(currentXPath.toString(), reader);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                int index = currentXPath.lastIndexOf("/");
                index = (index == 0) ? 1 : index;
                currentXPath = currentXPath.delete(index, currentXPath.length());
                currentElement = currentElement.getParent();
                break;
            case XMLStreamConstants.COMMENT:
                //ע��
                currentEvent = new TextEvent(currentXPath.toString(), reader, Type.Comment);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            case XMLStreamConstants.CDATA:
                //CDATA����
                currentEvent = new TextEvent(currentXPath.toString(), reader, Type.CDATA);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            //---------------------------------------------
            case XMLStreamConstants.SPACE:
                //���Ժ��ԵĿո�
                currentEvent = new TextEvent(currentXPath.toString(), reader, Type.Space);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            case XMLStreamConstants.CHARACTERS:
                //�ַ�����
                currentEvent = new TextEvent(currentXPath.toString(), reader, Type.Chars);
                currentEvent.setCurrentElement(currentElement);//���õ�ǰԪ��
                break;
            }
            //(3).ִ�к���
            if (xmlEvent == XMLStreamConstants.COMMENT && this.ignoreComment == true) {
                //ִ�к���
                xmlEvent = this.readEvent(reader);
                continue;
            } else if (xmlEvent == XMLStreamConstants.SPACE && this.ignoreSpace == true) {
                //ִ�к���
                xmlEvent = this.readEvent(reader);
                continue;
            }
            //(4).�����¼�
            this.pushEvent(accept, currentEvent, ignoreXPath);
            if (xmlEvent == XMLStreamConstants.START_ELEMENT) {
                XmlStreamEvent elementEvent = currentEvent;
                int attCount = reader.getAttributeCount();
                for (int i = 0; i < attCount; i++) {
                    //���������¼�
                    String namespace = reader.getAttributeNamespace(i);
                    String localName = reader.getAttributeLocalName(i);
                    String prefix = reader.getAttributePrefix(i);
                    //
                    namespace = (namespace == null) ? "" : namespace;
                    localName = (localName == null) ? "" : localName;
                    prefix = (prefix == null) ? "" : prefix;
                    //
                    QName qn = new QName(namespace, localName, prefix);
                    StringBuffer currentXPathTemp = new StringBuffer(currentXPath.toString());
                    currentXPathTemp.append("/@");
                    currentXPathTemp.append(this.getName(qn));
                    currentElement = new ElementTree(qn, currentElement);
                    currentEvent = new AttributeEvent(elementEvent, currentXPathTemp.toString(), reader, i);
                    currentEvent.setCurrentElement(currentElement.getParent());//�����Եĵ�ǰ�ڵ����ó���������Ԫ�ؽڵ㡣
                    currentElement = currentElement.getParent();
                    this.pushEvent(accept, currentEvent, ignoreXPath);
                }
            }
            //(5).��ȡ��һ��xml�ĵ����¼���
            xmlEvent = this.readEvent(reader);
            if (xmlEvent == 0)
                break;
        }
        //
        accept.endAccept();
    }
    private int readEvent(XMLStreamReader reader) throws XMLStreamException {
        if (reader.hasNext() == false)
            return 0;
        return reader.next();
    }
    private String getName(QName qname) {
        String prefix = qname.getPrefix();
        StringBuffer sb = new StringBuffer();
        if (prefix == null || prefix.equals("") == true) {} else {
            sb.append(prefix);
            sb.append(":");
        }
        return sb.append(qname.getLocalPart()).toString();
    }
    /**ִ��XPath�����жϡ� */
    private XmlStreamEvent skipEvent = null; //Ҫ�������¼�
    private void pushEvent(XmlAccept accept, XmlStreamEvent e, String ignoreXPath) throws XMLStreamException, IOException {
        //(1).XPath���ԡ��жϡ�
        boolean ignore = this.ignoreXPath(e.getXpath(), ignoreXPath);
        if (ignore == true)
            return;
        //(2).��һ����ǩ����֮�󣬽����������б�ǩ��ִ���������жϡ�
        if (this.skipEvent != null) {
            e.skip();
            if (this.skipEvent.isPartner(e) == true)
                this.skipEvent = null;//�����ǰ��ǩ����һ��������ǩ���ֵܹ�ϵ�� ��ôskipEvent�ÿա�
        }
        //(3).ִ������
        if (e.isSkip() == true)
            return;
        //(4).ִ���¼�
        this.pushEvent(accept, e);
        //(5).���������¼�
        if (this.skipEvent == null)
            if (e.isSkip() == true)
                this.skipEvent = e;
    }
    /**���������¼��ķ������������ͨ����չ�÷����������¼��ڼ䴦��һЩ���������� �����Եĺͱ��������¼���������ܵ��÷����ĵ��á�*/
    protected void pushEvent(XmlAccept accept, XmlStreamEvent e) throws XMLStreamException, IOException {
        if (accept != null)
            accept.sendEvent(e);
    }
}
/**
 * �����Ŀ���ǿ��Բ��ܿ�StreamFilter���Ե�Ӱ�졣
 * @version 2010-9-8
 * @author ������ (zyc@byshell.org)
 */
class NullStreamFilter implements StreamFilter {
    private StreamFilter parentFilter;
    public NullStreamFilter(XmlReader reader, StreamFilter parentFilter) {
        this.parentFilter = parentFilter;
    }
    public boolean accept(XMLStreamReader reader) {
        boolean accept = true;
        if (this.parentFilter != null)
            accept = this.parentFilter.accept(reader);
        return accept;
    }
}
class ElementTree {
    private QName       qname  = null;
    private ElementTree parent = null;
    public ElementTree(QName qname, ElementTree parent) {
        this.qname = qname;
        this.parent = parent;
    }
    public QName getQname() {
        return qname;
    }
    public ElementTree getParent() {
        return parent;
    }
}
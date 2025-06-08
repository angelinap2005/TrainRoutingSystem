package util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class DocumentParser {
    public static Document parseDocument(File fileKML) throws ParserConfigurationException, IOException, SAXException {
        //create a DocumentBuilderFactory and DocumentBuilder to parse the KML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //create a new DocumentBuilder
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fileKML);
        //convert the document to a DOM object
        doc.getDocumentElement().normalize();
        return doc;
    }
}
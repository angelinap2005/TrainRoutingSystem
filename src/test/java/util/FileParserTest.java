package util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.junit.gen5.api.Assertions.assertEquals;

public class FileParserTest {
    @Mock
    private FileParser fileParserMock;

    @Before
    public void before(){
        fileParserMock = new FileParser();
    }

    @Test
    public void testParseLineFile() throws IOException, ParserConfigurationException, SAXException {
        Document doc = parseDoc(new File("src/test/resources/testLinesFile.kml"));
        fileParserMock.traverse(doc);
        assertEquals(1416, fileParserMock.getRailLines().size());
    }

    @Test
    public void testParseStationFile() throws IOException, ParserConfigurationException, SAXException {
        Document doc = parseDoc(new File("src/test/resources/testStationFile.kml"));
        fileParserMock.traverse(doc);
        assertEquals(654, fileParserMock.getRailStations().size());
    }

    private Document parseDoc(File fileKML) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(fileKML);
        doc.getDocumentElement().normalize();
        return doc;
    }
}

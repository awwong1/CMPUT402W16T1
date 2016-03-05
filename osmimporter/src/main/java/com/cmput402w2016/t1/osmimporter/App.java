package com.cmput402w2016.t1.osmimporter;

import com.github.davidmoten.geo.GeoHash;
import com.github.davidmoten.geo.LatLong;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Sample application to read the OSM xml files and store it into HBase
 */
public class App {
    public static void main(String[] args) {
        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLStreamReader xmlr = null;
        try {
            xmlr = xmlif.createXMLStreamReader(new FileInputStream("data/test.osm"));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (xmlr == null) {
            System.out.println("Document failed to load. Exiting...");
            System.exit(0);
        }

        int eventType;
        try {
            while (xmlr.hasNext()) {
                eventType = xmlr.getEventType();
                System.out.println(getEventTypeString(eventType));
                System.out.println(xmlr);
                xmlr.next();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the string representation of the StAX parser event.
     *
     * @param eventType
     * @return
     */
    public static String getEventTypeString(int eventType) {
        switch (eventType) {
            case XMLEvent.START_ELEMENT:
                return "START_ELEMENT";
            case XMLEvent.END_ELEMENT:
                return "END_ELEMENT";
            case XMLEvent.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLEvent.CHARACTERS:
                return "CHARACTERS";
            case XMLEvent.COMMENT:
                return "COMMENT";
            case XMLEvent.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLEvent.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLEvent.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLEvent.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLEvent.DTD:
                return "DTD";
            case XMLEvent.CDATA:
                return "CDATA";
            case XMLEvent.SPACE:
                return "SPACE";
        }
        return "UNKNOWN_EVENT_TYPE " + "," + eventType;
    }

    public static void old_main(String[] args) {
        File fXmlFile = new File("data/test.osm");
        // todo: make this use the buffered reader for the large osm xml files
        // File fXmlFile = new File("data/edmonton_canada.osm");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document document = null;
        try {
            documentBuilder = dbFactory.newDocumentBuilder();
            document = documentBuilder.parse(fXmlFile);
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (document == null) {
            System.out.println("Document failed to load. Exiting...");
            System.exit(0);
        }

        // Get Node Stuff
        NodeList nodeList = document.getElementsByTagName("node");
        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            Node nNode = nodeList.item(temp);
            System.out.println(nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                System.out.println("\tOSM id : " + eElement.getAttribute("id"));
                String rawLat = eElement.getAttribute("lat");
                String rawLon = eElement.getAttribute("lon");
                System.out.println("\tLat : " + rawLat);
                System.out.println("\tLon : " + rawLon);
                LatLong latLon = new LatLong(new Double(rawLat), new Double(rawLon));
                String geohash = GeoHash.encodeHash(latLon);
                System.out.println("\tGeoHash: " + geohash);
            }
        }

        // Get Way Stuff
        NodeList wayList = document.getElementsByTagName("way");
        for (int temp = 0; temp < wayList.getLength(); temp++) {
            Node wNode = wayList.item(temp);
            if (wNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) wNode;
                // todo; make this reference the geohash instead of osm id
                System.out.println("way\n\tOSM id: " + eElement.getAttribute("id"));

                // Node references
                NodeList ndRefList = eElement.getElementsByTagName("nd");
                for (int ndRefIndex = 0; ndRefIndex < ndRefList.getLength(); ndRefIndex++) {
                    Node ndRefNode = ndRefList.item(ndRefIndex);
                    System.out.print("\t" + ndRefNode.getNodeName() + ": ");
                    if (ndRefNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element ndRefElement = (Element) ndRefNode;
                        System.out.println(ndRefElement.getAttribute("ref"));
                    } else {
                        System.out.println("N/A");
                    }
                }

                // Tags
                NodeList tagList = eElement.getElementsByTagName("tag");
                for (int tagIndex = 0; tagIndex < tagList.getLength(); tagIndex++) {
                    Node tagNode = tagList.item(tagIndex);
                    if (tagNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element tagElement = (Element) tagNode;
                        System.out.println("\t" + tagElement.getAttribute("k") + ": " + tagElement.getAttribute("v"));
                    }
                }
            }
        }
    }
}

package com.cmput402w2016.t1.osmimporter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Sample application to read the OSM xml files and store it into HBase
 */
public class App {
    public static void main(String[] args) {
        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLStreamReader xmlr = null;
        try {
            xmlr = xmlif.createXMLStreamReader(new FileInputStream("data/edmonton_canada.osm"));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (xmlr == null) {
            System.out.println("Document failed to load. Exiting...");
            System.exit(0);
        }

        try {
            // Read the XML

            // Current stuff
            HashMap<Long, Node> nodes = new HashMap<>();
            ArrayList<Way> ways = new ArrayList<>();

            com.cmput402w2016.t1.osmimporter.Node node = null;
            Way way = null;

            while (xmlr.hasNext()) {
                if(xmlr.isStartElement()) {
                    // Start of Element

                    // Consider only the things we care about
                    switch(xmlr.getLocalName()) {
                        case "node":
                            // Start new node
                            node = new com.cmput402w2016.t1.osmimporter.Node();

                            // Get the node attributes we care about
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                String value = xmlr.getAttributeValue(i);
                                switch(xmlr.getAttributeLocalName(i)) {
                                    case "id":
                                        //System.out.println("Setting id: " + value);
                                        node.setId(value);
                                        break;
                                    case "lat":
                                        //System.out.println("Setting latitude: " + value);
                                        node.setLat(value);
                                        break;
                                    case "lon":
                                        //System.out.println("Setting longitude: " + value);
                                        node.setLon(value);
                                        break;
                                }

                                // Stop early if we have everything
                                if(node.isComplete())
                                    break;
                            }
                            break;
                        case "way":
                            // Start new way
                            way = new Way();

                            // Get the way attributes we care about
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                String value = xmlr.getAttributeValue(i);
                                switch(xmlr.getAttributeLocalName(i)) {
                                    case "id":
                                        //System.out.println("Setting id: " + value);
                                        way.setId(value);
                                        break;
                                }
                            }
                            break;
                        case "nd":
                            // Safeguard, don't know if I actually need it...
                            if(way == null) {
                                System.out.println("Turns out we needed it, lololol");
                                break;
                            }

                            // Get the id of the node
                            double node_id = Double.MIN_VALUE;
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                switch(xmlr.getAttributeLocalName(i)) {
                                    case "ref":
                                        String value = xmlr.getAttributeValue(i);
                                        node_id = Double.parseDouble(value);
                                        break;
                                }
                            }

                            // Add node to way
                            if(node_id != Double.MIN_VALUE) {
                                way.addNode(nodes.get(node_id));
                            }
                            break;
                    }
                } else if (xmlr.isEndElement()) {
                    // End of element

                    switch(xmlr.getLocalName()) {
                        case "node":
                            // Add to map
                            nodes.put(node.id, node);

                            // Reset node
                            node = null;

                            break;
                        case "way":
                            // TODO: Determine the ways
                            ways.add(way);

                            // Reset way
                            way = null;

                            break;
                    }
                }
                xmlr.next();
            }

            // DEBUG: Stats!
            System.out.println("How many nodes did I read? This many, yo: " + nodes.size());
            System.out.println("How many ways did I read? This many, yo: " + ways.size());

            // TODO: For each nodes, add a node to the UMLs
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        Configuration hbconf =  HBaseConfiguration.create();


    }
}

package com.cmput402w2016.t1.osmimporter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.zookeeper.ZooKeeper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Sample application to read the OSM xml files and store it into HBase
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Starting...");

        // HBase configuration stuff
        Configuration hbconf =  HBaseConfiguration.create();
        System.out.println(hbconf);
        // Current stuff
        HashMap<Long, Node> nodes = new HashMap<Long, Node>();
        ArrayList<Way> ways = new ArrayList<Way>();

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
            System.exit(1);
        }

        try {
            // Read the XML
            com.cmput402w2016.t1.osmimporter.Node node = null;
            Way way = null;

            while (xmlr.hasNext()) {
                if(xmlr.isStartElement()) {
                    // Start of Element

                    // Consider only the things we care about
                    String s1 = xmlr.getLocalName();
                    if (s1.equals("node")) {// Start new node
                        node = new Node();

                        // Get the node attributes we care about
                        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                            String value = xmlr.getAttributeValue(i);
                            String s = xmlr.getAttributeLocalName(i);
                            if (s.equals("id")) {//System.out.println("Setting id: " + value);
                                node.setId(value);

                            } else if (s.equals("lat")) {//System.out.println("Setting latitude: " + value);
                                node.setLat(value);

                            } else if (s.equals("lon")) {//System.out.println("Setting longitude: " + value);
                                node.setLon(value);

                            }

                            // Stop early if we have everything
                            if (node.isComplete())
                                break;
                        }

                    } else if (s1.equals("way")) {// Start new way
                        way = new Way();

                        // Get the way attributes we care about
                        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                            String value = xmlr.getAttributeValue(i);
                            String s = xmlr.getAttributeLocalName(i);
                            if (s.equals("id")) {//System.out.println("Setting id: " + value);
                                way.setId(value);

                            }
                        }

                    } else if (s1.equals("nd")) {// Safeguard, don't know if I actually need it...
                        if (way == null) {
                            System.out.println("Turns out we needed it, lololol");
                            break;
                        }

                        // Get the id of the node
                        double node_id = Double.MIN_VALUE;
                        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                            String s = xmlr.getAttributeLocalName(i);
                            if (s.equals("ref")) {
                                String value = xmlr.getAttributeValue(i);
                                node_id = Double.parseDouble(value);

                            }
                        }

                        // Add node to way
                        if (node_id != Double.MIN_VALUE) {
                            way.addNode(nodes.get(node_id));
                        }

                    }
                } else if (xmlr.isEndElement()) {
                    // End of element

                    String s = xmlr.getLocalName();
                    if (s.equals("node")) {// Add to map
                        nodes.put(node.id, node);

                        // Reset node
                        node = null;


                    } else if (s.equals("way")) {// TODO: Determine the ways
                        ways.add(way);

                        // Reset way
                        way = null;


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

        // Perform all the actions on the nodes
        HTable nodeTable = null;
        try {
            nodeTable = new HTable(hbconf, "node");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (nodeTable == null) {
            System.out.println("Node HBase table failed to load. Exiting...");
            System.exit(1);
        }
        Iterator it = nodes.entrySet().iterator();

        final byte[] DATA = Bytes.toBytes("data");
        final byte[] LAT = Bytes.toBytes("lat");
        final byte[] LON = Bytes.toBytes("lon");
        final byte[] OSM_ID = Bytes.toBytes("osm_id");
        final byte[] ND = Bytes.toBytes("nd");

        while(it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            Node node = (Node) pair.getValue();
            Put p = new Put(Bytes.toBytes(node.computeGeohash()));
            p.addColumn(DATA, LAT, Bytes.toBytes(node.lat));
            p.addColumn(DATA, LON, Bytes.toBytes(node.lon));
            p.addColumn(DATA, OSM_ID, Bytes.toBytes(node.id));
            // System.out.println(p);
            try {
                nodeTable.put(p);
            } catch (IOException e) {
                System.out.println("Node put failed");
                e.printStackTrace();
            }
        }
        System.out.println("Added all nodes");

        // Perform all the actions on the ways
        HTable wayTable = null;
        try {
            wayTable = new HTable(hbconf, "way");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wayTable == null) {
            System.out.println("Way HBase table failed to load. Exiting...");
            System.exit(1);
        }
        for(Way way : ways) {
            Put p = new Put(Bytes.toBytes(way.id));
            int index = 0;
            for (Node node : way.geohash_nodes) {
                p.addColumn(ND, Bytes.toBytes(index), Bytes.toBytes(node.computeGeohash()));
                index += 1;
            }
            // todo: add way tags
            try {
                wayTable.put(p);
            } catch (IOException e) {
                System.out.println("Way put failed");
                e.printStackTrace();
            }
        }
        System.out.println("Added all ways");
    }
}

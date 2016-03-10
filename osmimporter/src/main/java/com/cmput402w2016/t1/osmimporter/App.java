package com.cmput402w2016.t1.osmimporter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Sample application to read the OSM xml files and store it into HBase
 */
public class App {
    public static final boolean ADD_NODES = true;
    public static final boolean ADD_SEGMENTS = true;

    static final byte[] DATA = Bytes.toBytes("data");
    static final byte[] LAT = Bytes.toBytes("lat");
    static final byte[] LON = Bytes.toBytes("lon");
    static final byte[] OSM_ID = Bytes.toBytes("osm_id");
    static final byte[] NODE = Bytes.toBytes("node");

    public static void main(String[] args) {
        System.out.println("Starting...");

        // HBase configuration stuff
        Configuration hbconf = HBaseConfiguration.create();
        System.out.println(hbconf);
        // Current stuff
        HashMap<Long, Node> nodes = new HashMap<>();
        ArrayList<Way> ways = new ArrayList<>();

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

            label:
            while (xmlr.hasNext()) {
                if (xmlr.isStartElement()) {
                    // Start of Element

                    // Consider only the things we care about
                    String s1 = xmlr.getLocalName();
                    switch (s1) {
                        case "node": // Start new node
                            node = new Node();

                            // Get the node attributes we care about
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                String value = xmlr.getAttributeValue(i);
                                String s = xmlr.getAttributeLocalName(i);
                                switch (s) {
                                    case "id": //System.out.println("Setting id: " + value);
                                        node.setId(value);
                                        break;
                                    case "lat": //System.out.println("Setting latitude: " + value);
                                        node.setLat(value);
                                        break;
                                    case "lon": //System.out.println("Setting longitude: " + value);
                                        node.setLon(value);
                                        break;
                                }
                                // Stop early if we have everything
                                if (node.isComplete())
                                    break;
                            }

                            break;
                        case "way": // Start new way
                            way = new Way();
                            // Get the way attributes we care about
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                String value = xmlr.getAttributeValue(i);
                                String s = xmlr.getAttributeLocalName(i);
                                if (s.equals("id")) {//System.out.println("Setting id: " + value);
                                    way.setId(value);

                                }
                            }
                            break;
                        case "nd": // Safeguard, don't know if I actually need it...
                            if (way == null) {
                                System.out.println("Turns out we needed it, lololol");
                                break label;
                            }
                            // Get the id of the node
                            long node_id = Long.MIN_VALUE;
                            for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                                String s = xmlr.getAttributeLocalName(i);
                                if (s.equals("ref")) {
                                    String value = xmlr.getAttributeValue(i);
                                    node_id = Long.parseLong(value);

                                }
                            }
                            // Add node to way
                            if (node_id != Double.MIN_VALUE) {
                                way.addNode(nodes.get(node_id));
                            }
                            break;
                    }
                } else if (xmlr.isEndElement()) {
                    // End of element
                    String s = xmlr.getLocalName();
                    if (s.equals("node") && node != null) {// Add to map
                        nodes.put(node.id, node);
                        // Reset node
                        node = null;
                    } else if (s.equals("way")) {
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
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        if (ADD_NODES) {
            // Perform all the actions on the nodes
            HTable nodeTable = null;
            try {
                //noinspection deprecation
                nodeTable = new HTable(hbconf, "node");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nodeTable == null) {
                System.out.println("Node HBase table failed to load. Exiting...");
                System.exit(1);
            }
            List<Put> puts = new ArrayList<>();
            int counter = 0;
            int batch = 500;
            for (HashMap.Entry<Long, Node> pair : nodes.entrySet()) {
                Node node = pair.getValue();
                Put p = new Put(Bytes.toBytes(node.computeGeohash()));
                p.addColumn(DATA, LAT, Bytes.toBytes(String.valueOf(node.lat)));
                p.addColumn(DATA, LON, Bytes.toBytes(String.valueOf(node.lon)));
                p.addColumn(DATA, OSM_ID, Bytes.toBytes(String.valueOf(node.id)));
                puts.add(p);
                counter += 1;
                if (counter % batch == 0) {
                    try {
                        System.out.println("Batch " + counter + " / " + nodes.size());
                        nodeTable.put(puts);
                        puts.clear();
                    } catch (IOException e) {
                        System.out.println("Node put failed");
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Created all node puts");
            try {
                nodeTable.put(puts);
                puts.clear();
            } catch (IOException e) {
                System.out.println("Node put failed");
                e.printStackTrace();
            }
            System.out.println("Added all nodes");
        }
        if (ADD_SEGMENTS) {
            // Perform all the actions on the ways
            System.out.println("Adding ways");
            int counter = 0;
            int batch = 100;
            HTable wayTable = null;
            try {
                //noinspection deprecation
                wayTable = new HTable(hbconf, "segment");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (wayTable == null) {
                System.out.println("Way HBase table failed to load. Exiting...");
                System.exit(1);
            }
            List<Put> puts = new ArrayList<>();
            for (Way way : ways) {
                Node previousNode = null;
                for (Node node : way.geohash_nodes) {
                    if (previousNode == null) {
                        previousNode = node;
                        continue;
                    }
                    Put p = new Put(Bytes.toBytes(previousNode.computeGeohash()));
                    p.addColumn(NODE, Bytes.toBytes(node.computeGeohash()), Bytes.toBytes(String.valueOf(node.id)));
                    puts.add(p);
                    p = new Put(Bytes.toBytes(node.computeGeohash()));
                    p.addColumn(NODE, Bytes.toBytes(previousNode.computeGeohash()), Bytes.toBytes(String.valueOf(previousNode.id)));
                    puts.add(p);
                }
                counter += 1;
                if (counter % batch == 0) {
                    try {
                        System.out.println("Batch " + counter + " / " + ways.size());
                        wayTable.put(puts);
                        puts.clear();
                    } catch (IOException e) {
                        System.out.println("Way put failed");
                        e.printStackTrace();
                    }
                }
            }
            try {
                wayTable.put(puts);
            } catch (IOException e) {
                System.out.println("Way put failed");
                e.printStackTrace();
            }
            System.out.println("Added all ways");
        }
    }
}

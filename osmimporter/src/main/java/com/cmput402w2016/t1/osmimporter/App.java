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
import java.util.*;

/**
 * Sample application to read the OSM xml files and store it into HBase
 */
public class App {
    public static final boolean ADD_NODES = false;
    public static final boolean ADD_SEGMENTS = true;

    private static final byte[] DATA = Bytes.toBytes("data");
    private static final byte[] LAT = Bytes.toBytes("lat");
    private static final byte[] LON = Bytes.toBytes("lon");
    private static final byte[] OSM_ID = Bytes.toBytes("osm_id");
    private static final byte[] NODE = Bytes.toBytes("node");

    public static void main(String[] args) {
        System.out.println("Starting...");

        // HBase configuration stuff
        Configuration hbconf = HBaseConfiguration.create();
        System.out.println(hbconf);
        // Current stuff
        HashMap<Long, Node> nodes = new HashMap<Long, Node>();
        HashMap<String, String[]> segments = new HashMap<>();

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
            Node node = null;
            Node previousNode = null;
            while (xmlr.hasNext()) {
                if (xmlr.isStartElement()) {
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
                    } else if (s1.equals("way")) {
                        // Start new way
                        // Don't care about Ways anymore, just do nothing
                        xmlr.next();
                        continue;
                    } else if (s1.equals("nd")) {// Safeguard, don't know if I actually need it...
                        long node_id = Long.MIN_VALUE;
                        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                            String s = xmlr.getAttributeLocalName(i);
                            if (s.equals("ref")) {
                                String value = xmlr.getAttributeValue(i);
                                node_id = Long.parseLong(value);
                                break;
                            }
                        }
                        if (node_id != Long.MIN_VALUE) {
                            if (previousNode == null) {
                                // Add node to way
                                previousNode = nodes.get(node_id);
                            } else {
                                Node currentNode = nodes.get(node_id);
                                if (!segments.containsKey(previousNode.computeGeohash())) {
                                    segments.put(previousNode.computeGeohash(), new String[] {});
                                }
                                ArrayList<String> t_list;
                                String[] cs;
                                t_list = new ArrayList<>(Arrays.asList(segments.get(previousNode.computeGeohash())));
                                t_list.add(currentNode.computeGeohash());
                                cs = t_list.toArray(new String[t_list.size()]);
                                segments.put(previousNode.computeGeohash(), cs);
                                if (!segments.containsKey(currentNode.computeGeohash())) {
                                    segments.put(currentNode.computeGeohash(), new String[] {});
                                }
                                t_list = new ArrayList<>(Arrays.asList(segments.get(currentNode.computeGeohash())));
                                t_list.add(previousNode.computeGeohash());
                                cs = t_list.toArray(new String[t_list.size()]);
                                segments.put(currentNode.computeGeohash(), cs);
                                previousNode = currentNode;
                            }
                        } else {
                            System.out.println("Something went wrong and the node lookup barfed :(");
                        }
                    }
                } else if (xmlr.isEndElement()) {
                    // End of element
                    String s = xmlr.getLocalName();
                    if (s.equals("node") && node != null) {
                        nodes.put(node.getId(), node);
                        // Reset node
                        node = null;
                    } else if (s.equals("way")) {
                        // Reset segment
                        previousNode = null;
                    }
                }
                xmlr.next();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        // DEBUG: Stats!
        System.out.println("How many nodes did I read? This many, yo: " + nodes.size());
        System.out.println("How many segments did I process? This many, yo: " + segments.size());

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
            int batch = 1000;
            for (Map.Entry pair : segments.entrySet()) {
                Node node = (Node) pair.getValue();
                Put p = new Put(Bytes.toBytes(node.computeGeohash()));
                p.addColumn(DATA, LAT, Bytes.toBytes(String.valueOf(node.getLat())));
                p.addColumn(DATA, LON, Bytes.toBytes(String.valueOf(node.getLon())));
                p.addColumn(DATA, OSM_ID, Bytes.toBytes(String.valueOf(node.getId())));
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
            System.out.println("Adding segments");
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
                System.out.println("Segment HBase table failed to load. Exiting...");
                System.exit(1);
            }

            List<Put> puts = new ArrayList<>();
            for (Map.Entry pair : segments.entrySet()) {
                String n_key = (String) pair.getKey();
                @SuppressWarnings("unchecked")
                ArrayList<String> n_vals = (ArrayList<String>) pair.getValue();
                Put p = new Put(Bytes.toBytes(n_key));
                for (String n_val : n_vals) {
                    p.addColumn(NODE, Bytes.toBytes(n_val), Bytes.toBytes(String.valueOf(nodes.get(Long.valueOf(n_val)).getId())));
                }
                puts.add(p);
                counter += 1;
                if (counter % batch == 0) {
                    try {
                        System.out.println("Batch " + counter + " / " + segments.size());
                        wayTable.put(puts);
                        puts.clear();
                    } catch (IOException e) {
                        System.out.println("Segment put failed");
                        e.printStackTrace();
                    }
                }
            }
            try {
                wayTable.put(puts);
            } catch (IOException e) {
                System.out.println("Segment put failed");
                e.printStackTrace();
            }
            System.out.println("Added all segments");
        }
    }
}

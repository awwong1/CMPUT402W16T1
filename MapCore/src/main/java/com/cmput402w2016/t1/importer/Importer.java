package com.cmput402w2016.t1.importer;

import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.Way;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Importer {
    // Values for the HBase Column Families and Keys
    private static final byte[] DATA = Bytes.toBytes("data");
    private static final byte[] LAT = Bytes.toBytes("lat");
    private static final byte[] LON = Bytes.toBytes("lon");
    private static final byte[] OSM_ID = Bytes.toBytes("osm_id");
    private static final byte[] NODE = Bytes.toBytes("node");

    // Holder variables for the HBase Configurations and XML parsed nodes and ways
    private static Configuration hbconf = HBaseConfiguration.create();
    private static HashMap<Long, Node> nodes = new HashMap<>();
    private static ArrayList<Way> ways = new ArrayList<>();

    public static void import_from_file(String filename) throws XMLStreamException {
        System.out.println("Starting...");
        System.out.println(hbconf.toString());
        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLStreamReader xmlr;
        try {
            xmlr = xmlif.createXMLStreamReader(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            System.err.println("OSM XML file not found!");
            e.printStackTrace();
            return;
        }

        // Begin reading the XML
        Node node = null;
        Way way = null;

        while (xmlr.hasNext()) {
            if (xmlr.isStartElement()) {
                // Start of Element, consider only the things we care about
                String s1 = xmlr.getLocalName();
                switch (s1) {
                    case "node": // Start new node
                        node = new Node();
                        // Get the node attributes we care about
                        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
                            String value = xmlr.getAttributeValue(i);
                            String s = xmlr.getAttributeLocalName(i);
                            switch (s) {
                                case "id":
                                    node.setId(value);
                                    break;
                                case "lat":
                                    node.setLat(value);
                                    break;
                                case "lon":
                                    node.setLon(value);
                                    break;
                            }
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
                            if (s.equals("id")) {
                                way.setId(value);
                            }
                        }
                        break;
                    case "nd": // Start the way's node reference
                        if (way == null) {
                            System.err.println("Parser Error, Turns out we needed our way, lololol");
                            return;
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
                String s = xmlr.getLocalName();
                if (s.equals("node") && node != null) {// Add to map
                    nodes.put(node.getId(), node);
                    node = null;
                } else if (s.equals("way")) {
                    ways.add(way);
                    way = null;
                }
            }
            xmlr.next();
        }

        System.out.println("Number of nodes read: " + nodes.size());
        System.out.println("Number of ways read: " + ways.size());
        import_nodes();
        import_ways();
        System.out.println("Finished!");
    }

    private static Table get_table(String raw_table_name) throws IOException {
        Connection conn = ConnectionFactory.createConnection(hbconf);
        Admin admin = conn.getAdmin();
        TableName[] table_names = admin.listTableNames("node");
        for (TableName table_name : table_names) {
            if (table_name.getNameAsString().equals("node")) {
                return conn.getTable(table_name);
            }
        }
        return null;
    }

    private static void import_nodes() {
        System.out.println("Importing nodes...");
        // Perform all the actions on the nodes
        Table nodeTable = null;
        try {
            nodeTable = get_table("node");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (nodeTable == null) {
            System.err.println("Node table failed to load.");
            return;
        }

        List<Put> puts = new ArrayList<>();
        int counter = 0;
        int batch = 500;
        for (HashMap.Entry<Long, Node> pair : nodes.entrySet()) {
            Node node = pair.getValue();
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
                    System.err.println("Node put failed");
                    e.printStackTrace();
                }
            }
        }
        try {
            nodeTable.put(puts);
            puts.clear();
        } catch (IOException e) {
            System.out.println("Node put failed");
            e.printStackTrace();
        }
        System.out.println("Added all nodes!");
    }

    private static void import_ways() {
        // Perform all the actions on the ways
        System.out.println("Importing ways (segments)...");
        int counter = 0;
        int batch = 100;
        Table segmentTable = null;
        try {
            segmentTable = get_table("segment");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (segmentTable == null) {
            System.err.println("Segment table failed to load.");
            return;
        }
        List<Put> puts = new ArrayList<>();
        for (Way way : ways) {
            Node previousNode = null;
            for (Node node : way.getNodes()) {
                if (previousNode == null) {
                    previousNode = node;
                    continue;
                }
                Put p = new Put(Bytes.toBytes(previousNode.computeGeohash()));
                p.addColumn(NODE, Bytes.toBytes(node.computeGeohash()), Bytes.toBytes(String.valueOf(node.getId())));
                puts.add(p);
                p = new Put(Bytes.toBytes(node.computeGeohash()));
                p.addColumn(NODE, Bytes.toBytes(previousNode.computeGeohash()),
                        Bytes.toBytes(String.valueOf(previousNode.getId())));
                puts.add(p);
            }
            counter += 1;
            if (counter % batch == 0) {
                try {
                    System.out.println("Batch " + counter + " / " + ways.size());
                    segmentTable.put(puts);
                    puts.clear();
                } catch (IOException e) {
                    System.out.println("Segment put failed");
                    e.printStackTrace();
                }
            }
        }
        try {
            segmentTable.put(puts);
        } catch (IOException e) {
            System.out.println("Segment put failed");
            e.printStackTrace();
        }
        System.out.println("Added all segments!");
    }
}

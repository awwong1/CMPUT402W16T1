package com.cmput402w2016.t1.importer;

import com.cmput402w2016.t1.Main;
import com.cmput402w2016.t1.data.Node;
import com.cmput402w2016.t1.data.Way;
import com.cmput402w2016.t1.util.Util;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
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
import java.util.Map;

/**
 * Class containing all the logic necessary to import an OSM xml file to HBase
 */
public class Importer {
    // Values for the HBase Column Families and Keys
    private static final byte[] DATA = Bytes.toBytes("data");
    private static final byte[] TAGS = Bytes.toBytes("tags");
    private static final byte[] NODE = Bytes.toBytes("node");

    // Holder variables for the HBase Configurations and XML parsed nodes and ways
    private static final Configuration configuration = HBaseConfiguration.create();
    private static final HashMap<Long, Node> nodes = new HashMap<>();
    private static final ArrayList<Way> ways = new ArrayList<>();

    /**
     * Given the OSM XML file, parse out all of the nodes and ways and then run the import methods to
     * put the values into HBase.
     *
     * @param filename path to the OSM XML file
     * @throws XMLStreamException XML file is invalid
     */
    public static void import_from_file(String filename) throws XMLStreamException {
        System.out.println("Starting...");
        System.out.println(configuration.toString());
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new XMLStreamException("OSM XML file not found!");
        }

        // Begin reading the XML
        Node node = null;
        Way way = null;
        while (xmlStreamReader.hasNext()) {
            if (xmlStreamReader.isStartElement()) {
                // Start of Element, consider only the things we care about
                String s1 = xmlStreamReader.getLocalName();
                switch (s1) {
                    case "node": // Start new node
                        node = new Node();
                        // Get the node attributes we care about
                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            String value = xmlStreamReader.getAttributeValue(i);
                            String s = xmlStreamReader.getAttributeLocalName(i);
                            switch (s) {
                                case "id":
                                    node.setOsmId(value);
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
                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            String value = xmlStreamReader.getAttributeValue(i);
                            String s = xmlStreamReader.getAttributeLocalName(i);
                            if (s.equals("id")) {
                                way.setId(value);
                            }
                        }
                        break;
                    case "nd": // Start the way's node reference
                        if (way == null) {
                            System.err.println("Parser Error, Turns out we needed our way object.");
                            return;
                        }
                        // Get the id of the node
                        long node_id = Long.MIN_VALUE;
                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            String s = xmlStreamReader.getAttributeLocalName(i);
                            if (s.equals("ref")) {
                                String value = xmlStreamReader.getAttributeValue(i);
                                node_id = Long.parseLong(value);
                            }
                        }
                        // Add node to way
                        if (node_id != Long.MIN_VALUE) {
                            way.addNode(nodes.get(node_id));
                        }
                        break;
                    case "tag": // Start the tag parsing
                        String key = "";
                        String value = "";
                        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                            String s = xmlStreamReader.getAttributeLocalName(i);
                            if (s.equals("k")) {
                                key = xmlStreamReader.getAttributeValue(i);
                            }
                            if (s.equals("v")) {
                                value = xmlStreamReader.getAttributeValue(i);
                            }
                        }
                        if (!key.equals("") && !value.equals("")) {
                            if (node != null) {
                                // This is a node tag
                                node.addTag(key, value);
                                System.out.println("Added Node Tag " + key + ": " + value + "for " + node.getOsmId());
                            } else if (way != null) {
                                // This is a way tag
                                way.addTag(key, value);
                                System.out.println("Added Way Tag " + key + ": " + value + "for " + way.getId());
                            }
                            /*
                            else {
                                // This will barf because we don't care about 'relation' data types
                                // System.err.println(key + ": " + value + " not associated to node or way!");
                            }
                            */
                        }
                        break;
                }

            } else if (xmlStreamReader.isEndElement()) {
                String s = xmlStreamReader.getLocalName();
                if (s.equals("node") && node != null) {
                    nodes.put(node.getOsmId(), node);
                    System.out.print("\rAdded Node " + String.valueOf(node.getOsmId()));
                    node = null;
                } else if (s.equals("way") && way != null) {
                    ways.add(way);
                    System.out.print("\rAdded Way " + String.valueOf(way.getId()) + "     ");
                    way = null;
                }
            }
            xmlStreamReader.next();
        }

        System.out.println("Number of nodes read: " + nodes.size());
        System.out.println("Number of ways read: " + ways.size());

        Main.print_heap_usage();
        import_nodes();
        Main.print_heap_usage();
        import_ways();

        System.out.println("Finished!");
    }

    /**
     * Import all of the nodes from the parsed XML into the HBase table as a node.
     */
    private static void import_nodes() {
        System.out.println("Importing nodes...");
        Table nodeTable = Util.get_table("node");
        if (nodeTable == null) {
            System.err.println("Node table failed to load.");
            return;
        }

        int counter = 0;
        int batch = 500;
        List<Put> puts = new ArrayList<>();
        for (Map.Entry<Long, Node> pair : nodes.entrySet()) {
            Node node = pair.getValue();
            Put p = new Put(Bytes.toBytes(node.getGeohash()));
            p.addColumn(DATA, TAGS, Bytes.toBytes(node.getTagsWithIDAsSerializedJSON()));
            puts.add(p);
            counter += 1;
            if (counter % batch == 0) {
                try {
                    System.out.print("\rBatch " + counter + " / " + nodes.size());
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

    /**
     * Import all of the ways from the parsed XML into the HBase table as a segment.
     * Ways are OSM values which consist of a list of nodes.
     * Segments are custom values we use which represent a single node and its neighbors.
     */
    private static void import_ways() {
        System.out.println("Importing ways (segments)...");
        Table segmentTable = Util.get_table("segment");
        if (segmentTable == null) {
            System.err.println("Segment table failed to load.");
            return;
        }

        int counter = 0;
        int batch = 100;
        List<Put> puts = new ArrayList<>();
        for (Way way : ways) {
            Node previousNode = null;
            for (Node node : way.getNodes()) {
                if (previousNode == null) {
                    previousNode = node;
                    continue;
                }
                Put p = new Put(Bytes.toBytes(previousNode.getGeohash()));
                p.addColumn(NODE, Bytes.toBytes(node.getGeohash()),
                        Bytes.toBytes(String.valueOf(way.getTagsAsSerializedJSON())));
                puts.add(p);
                p = new Put(Bytes.toBytes(node.getGeohash()));
                p.addColumn(NODE, Bytes.toBytes(previousNode.getGeohash()),
                        Bytes.toBytes(String.valueOf(way.getTagsAsSerializedJSON())));
                puts.add(p);
            }
            counter += 1;
            if (counter % batch == 0) {
                try {
                    System.out.print("\rBatch " + counter + " / " + ways.size());
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

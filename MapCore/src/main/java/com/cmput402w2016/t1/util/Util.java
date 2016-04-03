package com.cmput402w2016.t1.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

public class Util {
    /**
     * Get the table from the HBase connection's administrative interface.
     *
     * @param raw_table_name Name of the table
     * @return Table matching the raw_table_name, or null
     */
    public static Table get_table(String raw_table_name) {
        try {
            Configuration configuration = HBaseConfiguration.create();
            Connection conn = ConnectionFactory.createConnection(configuration);
            Admin admin = conn.getAdmin();
            TableName[] table_names = admin.listTableNames(raw_table_name);
            for (TableName table_name : table_names) {
                if (table_name.getNameAsString().equals(raw_table_name)) {
                    return conn.getTable(table_name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Take the string, shorten it by one character, return the string.
     * If the string is the empty string "", return null.
     *
     * @param str String to shorten
     * @return Shortened string or null
     */
    public static String shorten(String str) {
        if ("".equals(str)) {
            return null;
        }
        if (str != null && str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        if (str == null) {
            return "";
        }
        return str;
    }

}

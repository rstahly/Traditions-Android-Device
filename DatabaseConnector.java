package com.example.rachelfeddersen.testapp5;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Created by Rachel Feddersen on 11/10/2016.
 * This class creates and maintains the connection to the database for the app
 */

public class DatabaseConnector {
    static CallableStatement cs ;
    static Statement stmt;
    static Connection conn;
    DatabaseConnector dc2;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        DatabaseConnector dc2 = new DatabaseConnector();
    }

    /**
     * Create the frame.
     */
    public DatabaseConnector() {
        session();
        start();
    }

    public void home() {
    }

    //establish the session
    public void session(){
        int assigned_port;
        final int local_port=3306;

        // Remote host and port
        final int remote_port=3306;
        final String remote_host="cs.unk.edu";

        try {
            JSch jsch = new JSch();

            // Create SSH session.  Port 22 is your SSH port which
            // is open in your fire-wall setup.
            Session session = jsch.getSession("UNKTraditions", remote_host, 22);
            String passwordString = new String("UNKTraditions");
            session.setPassword(passwordString);

            // Additional SSH options.  See your ssh_config manual for
            // more options.  Set options according to your requirements.
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("Compression", "yes");
            config.put("ConnectionAttempts","2");

            session.setConfig(config);

            // Connect
            session.connect();
            //        System.out.println("Host: "+session.getHost());

            // Create the tunnel through port forwarding.
            // This is basically instructing jsch session to send
            // data received from local_port in the local machine to
            // remote_port of the remote_host
            // assigned_port is the port assigned by jsch for use,
            // it may not always be the same as
            // local_port.

            assigned_port = session.setPortForwardingL(local_port,
                    "127.0.0.1", remote_port);

        } catch (JSchException e1) {
            System.out.println(Level.SEVERE + " " + e1.getMessage());
            System.exit(0);
            return;
        }

        if (assigned_port == 0) {
            System.out.println(Level.SEVERE + " " + "Port forwarding failed !");
            System.exit(0);
            return;
        }

        System.out.println("-Connected to the server!!");
    }


    //Starting login screen
    public void start() {
        String user = "UNKTraditions";
        String passwordString = "UNKTraditions";
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            try {
                //String url = "jdbc:mysql://cs.unk.edu:22/UNKTraditionsApp";
                String url = "jdbc:mysql://localhost/UNKTraditionsApp?noAccessToProcedureBodies=true";
                conn = DriverManager.getConnection(url,user,passwordString);
                System.out.println("-Connected to the UNKTraditionsApp database");
                //home();
            } catch (SQLException ex) {
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }
}

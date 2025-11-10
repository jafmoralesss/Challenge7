package org.example.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String DEV_DB_URL = "jdbc:h2:mem:collectorzone;DB_CLOSE_DELAY=-1";
    private static final String DEV_USER = "sa";
    private static final String DEV_PASS = "";

    public static Connection getConnection() throws SQLException{

        String env = System.getenv("APP_ENV");

        if ("prod".equals(env)) {

            String dbUrl = System.getenv("PROD_DB_URL");
            String dbUser = System.getenv("PROD_DB_USER");
            String dbPass = System.getenv("PROD_DB_PASS");

            if (dbUrl == null || dbUser == null || dbPass == null) {
                throw new SQLException("Production's environment variables not found.");
            }

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e){
                throw new SQLException("PostgreSQL's driver not found", e);
            }
            return DriverManager.getConnection(dbUrl, dbUser, dbPass);

        } else {

            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("H2 driver not found", e);
            }
            return DriverManager.getConnection(DEV_DB_URL, DEV_USER, DEV_PASS);
        }
    }
}

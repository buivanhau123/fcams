package dal;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp kết nối CSDL SQL Server thông qua JDBC (Tiêu chí 1.6).
 * Đọc cấu hình từ WEB-INF/ConnectDB.properties theo chuẩn PRJ301 FPT University.
 */
public class DBContext {
    protected Connection connection;

    public DBContext() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("../ConnectDB.properties");
            if (inputStream != null) {
                try {
                    properties.load(inputStream);
                } catch (IOException ex) {
                    Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String user = properties.getProperty("userID", "sa");
            String pass = properties.getProperty("password", "123");
            String url = properties.getProperty("url",
                    "jdbc:sqlserver://localhost:1433;databaseName=FCAMS_DB;encrypt=false;trustServerCertificate=true;");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}


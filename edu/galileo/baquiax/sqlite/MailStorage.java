package edu.galileo.baquiax.sqlite;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MailStorage {
    private static MailStorage shared;
    private static Connection connection;
    private static String DB_NAME = "mails.db";
    private static String MODEL_NAME = "model.sql";

    public synchronized static MailStorage getSharedInstance() {
        if (MailStorage.shared == null) {
            MailStorage.shared = new MailStorage();
        }
        return MailStorage.shared;
    }

    private MailStorage() {
        try {
            String sql = new String(Files.readAllBytes(Paths.get(MODEL_NAME)));
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
            print("Database opened: " + DB_NAME);
            
            Statement stmt = this.connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();

            this.verifyEmailAddress("admin@baquiax.com");
            if (!this.verifyEmailAddress("baquiax@baquiax.com")) {                
                this.createAccount("baquiax@baquiax.com", "Alexander Baquiax");
            }

            if (!this.verifyEmailAddress("guest@baquiax.com")) {                
                this.createAccount("guest@baquiax.com", "Guest");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }   

    public void print(String s) {
        System.out.println("MailStorage > " + s);
    }

    public boolean createAccount(String email, String name) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO user(email, name) VALUES(?, ?)");
            stmt.setString(1,email);
            stmt.setString(2,name);
            stmt.executeUpdate();
            this.print(email + " created!");     
            stmt.close();                   
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveEmail(String from, String to, String email) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement("INSERT INTO email(fromUser, toUser, message) VALUES(?, ?, ?)");
            stmt.setString(1,from);
            stmt.setString(2,to);
            stmt.setString(3,email);
            stmt.executeUpdate();
            stmt.close();
            this.print("Email saved!");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifyEmailAddress(String email) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement("SELECT 1 FROM user WHERE email = ?");
            stmt.setString(1,email);            
            ResultSet rs = stmt.executeQuery();
            stmt.close();            
            return (rs != null && !rs.next());                                               
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
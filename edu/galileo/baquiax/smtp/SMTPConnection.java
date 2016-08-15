package edu.galileo.baquiax.smtp;
import edu.galileo.baquiax.sqlite.MailStorage;
import edu.galileo.baquiax.smtp.SMTPClient;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; 

public class SMTPConnection implements Runnable {
    public static String DOMAIN = "@baquiax.com";
    private Socket client;
    private ArrayList<String> commandHistory;
    public SMTPConnection(Socket c) {
        this.client = c;
        this.commandHistory = new ArrayList<String>();
    }

    public String readFromClent() {
        String result = "";
        try {
            int length = 0;
            while (true) {
                length = this.client.getInputStream().available();
                if (length > 0) break;
            }
            byte[] data = new byte[length];
            this.client.getInputStream().read(data, 0, length);
            result = new String(data);            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void  print(String m) {
        System.out.println("SRV > " + m + "\r\n");
    }

    @Override
    public void run() {
        try {
            this.sendToClient(this.getDefaultBanner());
            BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            String command;            
            while ((command = in.readLine()) != null) {
                this.print(command);
                if (this.processCommand(command) == -1) {
                    break;
                }
            }
            this.client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte processCommand(String command) {        
        String[] commandChunks = command.split("[ ]+");        
        print("Command: " + command + " (" + command.length() + ")-("+ commandChunks.length + ")" );        
        
        if (commandChunks[0].toUpperCase().equals("QUIT") && commandHistory.size() != 5) {
            this.sendToClient("200 Bye\r\n");
            try {
                this.client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        } else if (commandChunks[0].toUpperCase().equals("HELLO")  && commandHistory.size() == 0) {
            if (commandChunks.length == 2) {
                this.sendToClient("200 Hello, please to meet you\r\n");
                this.commandHistory.add(command);
            } else {
                this.sendToClient("400 Invalid HELLO command.\r\n");                
            }
        } else if (commandChunks[0].toUpperCase().equals("MAIL") && commandHistory.size() == 1) {
            if (commandChunks.length == 3 && commandChunks[1].toUpperCase().equals("FROM") && this.validateEmail(commandChunks[2])) {
                this.sendToClient("200 OK\r\n");                
                this.commandHistory.add(commandChunks[2]);
            } else {
                this.sendToClient("400 Invalid MAIL command.\r\n");                
            }            
        } else if (commandChunks[0].toUpperCase().equals("RCPT") && (commandHistory.size() == 2 || commandHistory.size() == 3)) {
            if (commandChunks.length == 3 && commandChunks[1].toUpperCase().equals("TO")) {
                this.sendToClient("200 OK\r\n");
                if (this.commandHistory.size() == 3) {
                    this.commandHistory.set(2,this.commandHistory.get(2) + "," + commandChunks[2]);                    
                } else if (this.validateEmail(commandChunks[2])) {
                    this.commandHistory.add(commandChunks[2]);
                } else {
                    this.sendToClient("400 Invalid email.\r\n");
                }
            } else {
                this.sendToClient("400 Invalid RCPT command.\r\n");                
            }
        } else if (commandChunks[0].toUpperCase().equals("DATA") && commandHistory.size() == 3) {
            if (commandChunks.length == 1) {
                this.sendToClient("200 OK End data with <CR><LF>.<CR><LF>\r\n");
                this.commandHistory.add(command);
                this.commandHistory.add(""); //Prepare email content
            } else {
                this.sendToClient("400 Invalid DATA command.\r\n");
            }
        } else {
            if (this.commandHistory.size() == 5) {
                //Message data
                String currentMessage = this.commandHistory.get(4);
                currentMessage += command + "\r\n";
                this.commandHistory.set(4,currentMessage);

                if (currentMessage.endsWith("\r\n.\r\n\r\n")) {
                    //End of Message
                    //String uniqueID = UUID.randomUUID().toString();
                    this.commandHistory.add("\r\n\r\n");
                    //this.saveMail(uniqueID);
                    this.sendEmail();
                    this.sendToClient("250 Ok: Task queued \r\n");
                    //this.sendToClient("250 Ok: queued as " + uniqueID + ".\r\n");
                }                    
            } else {
                this.sendToClient("400 Invalid command.\r\n");
            }
        }
        return 0;
    }

    public boolean validateEmail(String email) {
        boolean result = true;
        return result;
    }

    public void sendEmail() {
        String to[] = this.commandHistory.get(2).split(",");
        for(String userTo : to) {
            if (this.isMyUser(userTo)) {
                MailStorage.getSharedInstance().saveEmail(this.commandHistory.get(1), userTo ,this.commandHistory.get(4));
            } else {
                //Open SMTP Connection
                Thread t = new Thread(new SMTPClient(this.commandHistory.get(1), userTo ,this.commandHistory.get(4)));
                t.start();
            }            
        }        
    }

    public boolean isMyUser(String email) {
        return email.endsWith(DOMAIN);
    }

    public void saveMail(String id) {
        try {
            PrintWriter writer = new PrintWriter(id + ".txt", "UTF-8");
            writer.println(commandHistory.get(4));
            writer.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    public void sendToClient(String msg) {
        try {
            this.client.getOutputStream().write(msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDefaultBanner() {
        String banner = "_       _         _                                    _  \r\n";
        banner += "( )  _  ( )       (_ )                                 ( )\r\n";
        banner += "| | ( ) | |   __   | |    ___    _     ___ ___     __  | |\r\n";
        banner += "| | | | | | /'__`\\ | |  /'___) /'_`\\ /' _ ` _ `\\ /'__`\\| |\r\n";
        banner += "| (_/ \\_) |(  ___/ | | ( (___ ( (_) )| ( ) ( ) |(  ___/| |\r\n";
        banner += "`\\___x___/'`\\____)(___)`\\____)`\\___/'(_) (_) (_)`\\____)(_)\r\n";
        banner += "                                                       (_)\r\n\r\n";
        banner += "-------------------------------\r\n";
        banner += "A simple SMTP Server!\r\n";
        banner += "Use it, it works!\r\n";
        banner += "-------------------------------\r\n\r\n";        
        return banner;
    }
}
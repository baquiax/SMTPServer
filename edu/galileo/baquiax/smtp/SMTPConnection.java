package edu.galileo.baquiax.smtp;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.io.PrintWriter;

public class SMTPConnection implements Runnable {
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
            while(true) {                 
                String command = this.readFromClent();
                this.print(command);
                this.processCommand(command);                
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processCommand(String command) {        
        String[] commandChunks = command.split("[ ]+");        
        print("Command: " + command + " (" + command.length() + ")-("+ commandChunks.length + ")" );

        if (commandChunks.length == 0) return;        
        switch (commandChunks[0].toUpperCase()) {
            case "QUIT\r\n":
                this.sendToClient("200 Bye\r\n");
                try {
                    this.client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            case "HELLO":
                if (commandChunks.length == 2 && commandHistory.size() == 0) {
                    this.sendToClient("200 Hello, please to meet you\r\n");
                } else {
                    this.sendToClient("400 Invalid HELLO command.\r\n");
                    return;
                }
                break;
            case "MAIL":
                if (commandChunks.length == 3 && commandChunks[1].toUpperCase().equals("FROM") && commandHistory.size() == 1) {
                    this.sendToClient("200 OK\r\n");
                } else {
                    this.sendToClient("400 Invalid MAIL command.\r\n");
                    return;
                }
                break;
            case "RCPT":
                if (commandChunks.length == 3 && commandChunks[1].toUpperCase().equals("TO") && commandHistory.size() == 2) {
                    this.sendToClient("200 OK\r\n");
                } else {
                    this.sendToClient("400 Invalid RCPT command.\r\n");
                    return;
                }
                break;
            default:
                if (this.commandHistory.size() == 3) {
                    //Message data
                    String currentMessage = this.commandHistory.get(2);
                    if (currentMessage == null) {
                        currentMessage = "";
                    }
                    currentMessage += command;
                    this.commandHistory.set(2,currentMessage);
                    if (currentMessage.endsWith("\r\n\r\n")) {
                        //End of Message
                        String uniqueID = UUID.randomUUID().toString();
                        this.commandHistory.add("\r\n\r\n");
                        this.saveMail(uniqueID);
                        this.sendToClient("250 Ok: queued as " + uniqueID + ".\r\n");
                    }                    
                } else {
                    this.sendToClient("400 Invalid command.\r\n");
                }
                return;

        }        
        this.commandHistory.add(command);        
    }

    public void saveMail(String id) {
        try {
            PrintWriter writer = new PrintWriter(id + ".txt", "UTF-8");
            writer.println(commandHistory.get(2));            
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
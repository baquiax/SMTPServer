package edu.galileo.baquiax.smtp;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader; 

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
                
        switch (commandChunks[0].toUpperCase()) {
            case "QUIT":
                this.sendToClient("200 Bye\r\n");
                try {
                    this.client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return -1;
            case "HELLO":
                if (commandChunks.length == 2 && commandHistory.size() == 0) {
                    this.sendToClient("200 Hello, please to meet you\r\n");
                } else {
                    this.sendToClient("400 Invalid HELLO command.\r\n");
                    return 0;
                }
                break;
            case "MAIL":
                if (commandChunks.length == 3 && commandChunks[1].toUpperCase().equals("FROM") && commandHistory.size() == 1) {
                    this.sendToClient("200 OK\r\n");
                } else {
                    this.sendToClient("400 Invalid MAIL command.\r\n");
                    return 0;
                }
                break;
            case "RCPT":
                if (commandChunks.length == 3 && commandChunks[1].toUpperCase().equals("TO") && commandHistory.size() == 2) {
                    this.sendToClient("200 OK\r\n");
                } else {
                    this.sendToClient("400 Invalid RCPT command.\r\n");
                    return 0;
                }
                break;
            default:
                if (this.commandHistory.size() == 3) {
                    this.commandHistory.add(command + "\r\n");
                } else if (this.commandHistory.size() == 4) {
                    //Message data
                    String currentMessage = this.commandHistory.get(3);
                    currentMessage += command + "\r\n";
                    this.commandHistory.set(3,currentMessage);
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
                return 0;
                     
        }        
        this.commandHistory.add(command);
        return 0;        
    }

    public void saveMail(String id) {
        try {
            PrintWriter writer = new PrintWriter(id + ".txt", "UTF-8");
            writer.println(commandHistory.get(3));
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
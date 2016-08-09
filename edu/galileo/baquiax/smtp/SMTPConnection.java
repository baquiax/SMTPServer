package edu.galileo.baquiax.smtp;
import java.net.Socket;
import java.util.ArrayList;

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
            String tmpCommand = "";
            while(true) {                 
                String command = this.readFromClent();
                this.print(command);
                if (command.equals("\r\n")) {
                    this.processCommand(tmpCommand);                    
                }
                tmpCommand += command; 
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processCommand(String command) {
        String[] commandChunks = command.split("[ ]+");
        if (commandChunks.length == 0) return;

        switch (commandChunks[0].toLowerCase()) {
            case "HELLO":
                if (commandChunks.length == 2 && commandHistory.size() == 0) {
                    this.sendToClient("200 Hello, please to meet you");                    
                } else {
                    this.sendToClient("400 Invalid HELLO command.");                
                }
                break;
            case "MAIL":
                if (commandChunks.length == 3 && commandChunks[1].equals("FROM")) {

                } else {

                }
                break;
            default:

        }

        for (int i = 0; i < commandChunks.length; i++) {
            this.commandHistory.add(commandChunks[i]);
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
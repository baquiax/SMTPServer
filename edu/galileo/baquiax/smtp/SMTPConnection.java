package edu.galileo.baquiax.smtp;
import java.net.Socket;

public class SMTPConnection implements Runnable {
    private Socket client;

    public SMTPConnection(Socket c) {
        this.client = c;
    }

    public String readFromClent() {
        String result = "";
        try {
            int lenght = 0;
            while (true) {
                lenght = this.client.getInputStream().available();
                if (lenght > 0) break;
            }
            byte[] data = new byte[lenght];
            this.client.getInputStream().read(data, 0, lenght);
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
            this.client.getOutputStream().write(this.getDefaultBanner().getBytes());
            while(true) {                
                this.print(this.readFromClent());
            }            
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
package edu.galileo.baquiax.smtp;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SMTPClient implements Runnable {
    private String fromUser;
    private String toUser;
    private String message;
    private Socket socket;

    public SMTPClient(String fromUser, String toUser, String message) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(getEmailDomain(this.toUser), 25);
            String response = this.sendStringToServer("HELLO baquiax.com");
            this.print("RECEIVED: " + response);
            if (response.startsWith("2")) { //2xx
                    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStringFromServer() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String command;            
            while ((command = in.readLine()) != null) {
                return command;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendStringToServer(String message) {
        this.print("SEND: " + message);
        try {
            this.socket.getOutputStream().write(message.getBytes());
            return getStringFromServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getEmailDomain(String email) {
        String[] split = email.split("@");
        return split[split.length - 1];
    }

    private void print(String s) {
        System.out.println("SMTP Client > " + s);
    }
}
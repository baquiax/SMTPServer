import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.galileo.baquiax.smtp.SMTPConnection;
import edu.galileo.baquiax.sqlite.MailStorage;

public class SMTPServer {
    static ExecutorService pool;
    static ServerSocket server;
    private static int MAX_THREADS = 100;
    private static int PORT = 25;

    public static void main (String args[]) {        
        try {
            server = new ServerSocket(PORT);            
            pool = Executors.newFixedThreadPool(MAX_THREADS);
            MailStorage.getSharedInstance(); //Prepare tables
            SMTPConnection.DOMAIN = "@baquiax.com";

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {                
                        server.close();
                        System.out.println("Close server ...");        
                    } catch (Exception ex) {	            
                        ex.printStackTrace();
                    }
                }
            });

            while(true) {
                SMTPConnection c = new SMTPConnection(server.accept());
                pool.execute(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
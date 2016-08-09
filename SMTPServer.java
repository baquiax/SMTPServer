import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.galileo.baquiax.smtp.SMTPConnection;

public class SMTPServer {
    static ExecutorService pool;
    static ServerSocket server;
    private static int MAX_THREADS = 100;
    private static int PORT = 23;

    public static void main (String args[]) {        
        try {
            server = new ServerSocket(PORT);            
            pool = Executors.newFixedThreadPool(MAX_THREADS);

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
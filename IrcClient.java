import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class IrcClient {
    
    // Program begins with a call to main()
    public static void main(String[] args) throws Exception
    {
        Socket socket = new Socket("irc.libera.chat", 6667);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println("NICK MrAllMight");
        out.println("USER AllMight 0 * :AllMight");

        Thread.sleep(5000);
        out.println("JOIN #test");

        String line;
        while((line = in.readLine()) != null) {
            System.out.println(line);

            if(line.startsWith("PING")) {
                String response = line.replace("PING", "PONG");
                out.println(response);
                System.out.println("Sent: " + response);
            }
        }

        socket.close();
    }
}
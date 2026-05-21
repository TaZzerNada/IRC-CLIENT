import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class MrAllMight {

    private static String CHANNEL = "#My_Hero_Academia";
    private static String NICK = "MrAllMight";

    // Program begins with a call to main()
    public static void main(String[] args) throws Exception
    {
    String SERVER = "irc.libera.chat";
    int PORT = 6667;

    if (args.length >= 1) {
        SERVER = args[0];
    }

    if (args.length >= 2) {
        PORT = Integer.parseInt(args[1]);
    }

    if (args.length >= 3) {
        NICK = args[2];
    }

    if (args.length >= 4) {
        CHANNEL = args[3];

        if (!CHANNEL.startsWith("#")) {
            CHANNEL = "#" + CHANNEL;
        }
    }

        Socket socket = new Socket(SERVER, PORT);


        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );


        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println("NICK " + NICK);
        out.println("USER " + NICK + " 0 * :" + NICK);

        Thread.sleep(5000);
        out.println("JOIN " + CHANNEL);

        saveToLog("[SYSTEM] Joined channel: " + CHANNEL);
        System.out.println("Joined to " + SERVER);
        System.out.println("Channel: " + CHANNEL);
        System.out.println("Commands: /help, /quit, /join <channel>, /nick <nickname>");

        Scanner scanner = new Scanner(System.in);

        Thread inputThread = new Thread(() -> {
        while (true) {
        String message = scanner.nextLine();

        if (message.equalsIgnoreCase("/help")){
                    System.out.println("Available commands:");
                    System.out.println("/help - show help");
                    System.out.println("/quit - disconnect from the server");
                    System.out.println("/join <channel> - join a channel");
                    System.out.println("/nick <nickname> - change your nickname");
                    System.out.println("Any other text will be sent to the channel");
                    continue;
            }

        if (message.equalsIgnoreCase("/quit")) {
            out.println("QUIT :Goodbye");
            break;
        }

         if (message.startsWith("/join ")) {
                    String newChannel = message.substring(6).trim();

                    if (!newChannel.startsWith("#")) {
                        newChannel = "#" + newChannel;
                    }

                    CHANNEL = newChannel;
                    out.println("JOIN " + CHANNEL);

                    System.out.println("Joining channel: " + CHANNEL);
                    continue;
        }

                if (message.startsWith("/nick ")) {
                    String newNick = message.substring(6).trim();

                    if (newNick.isEmpty()) {
                        System.out.println("Nickname cannot be empty.");
                        continue;
                    }

                    out.println("NICK " + newNick);

                    System.out.println("Trying to change nickname to: " + newNick);
                    continue;
                }

                if (message.trim().isEmpty()) {
                    continue;
                }

        out.println("PRIVMSG " + CHANNEL + " :" + message);
        saveToLog("[ME] " + NICK + ": " + message);
    }
});

inputThread.start();
        String line;
        while((line = in.readLine()) != null) {

            if(line.startsWith("PING")) {
                String response = line.replace("PING", "PONG");
                out.println(response);
                System.out.println("[SYSTEM] PING received, PONG sent");
                continue;
            }

            printFormattedLine(line);
        }

        socket.close();
        scanner.close();
    }

    private static void printFormattedLine(String line) {
    if (line.contains(" PRIVMSG ")) {
        int nickEnd = line.indexOf("!");
        int messageStart = line.indexOf(" :");

        if (nickEnd != -1 && messageStart != -1) {
            String sender = line.substring(1, nickEnd);
            String message = line.substring(messageStart + 2);

            System.out.println("[CHAT] " + sender + ": " + message);
            saveToLog("[CHAT] " + sender + ": " + message);
            return;
        }
    }

    if (line.contains(" 001 ")) {
        System.out.println("[SERVER] Connected successfully.");
        return;
    }

    if (line.contains(" JOIN ")) {
        System.out.println("[SERVER] Joining channel: " + CHANNEL);
        saveToLog("[SERVER] Joined channel: " + CHANNEL);
        return;
    }

    if (line.contains(" NICK :")) {
    int nickEnd = line.indexOf("!");
    int newNickStart = line.indexOf(" NICK :");

    if (nickEnd != -1 && newNickStart != -1) {
        String oldNick = line.substring(1, nickEnd);
        String newNick = line.substring(newNickStart + 7);

        if (oldNick.equals(NICK)) {
            NICK = newNick;
        }

        System.out.println("[SERVER] " + oldNick +
                " changed nickname to " + newNick);

        return;
    }
}

if (line.contains(" 433 ")) {
    System.out.println("[ERROR] Nickname already in use.");
    return;
}

    if (line.startsWith("ERROR")) {
        System.out.println("[SERVER] Connection closed.");
        return;
    }

    //System.out.println("[RAW] " + line);
}

    private static void saveToLog(String text) {
        try (FileWriter writer = new FileWriter("chat_log.txt", true)) {
            writer.write("[" + LocalDateTime.now() + "] " + text + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Could not write to log file.");
        }
    }
}
package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private static Socket socket = null;
    private static DataInputStream input = null;
    private static DataOutputStream output = null;
    private static MessageWriter write_messages;
    private static MessageReader messageReader;
    private static Thread write_message_thread, read_message_thread;
    public static boolean alive = true;

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        InetAddress ip = null;
        try {
            ip = InetAddress.getByName("localhost");
            System.out.println("Insira seu nome:  ");
            String name = scanner.nextLine();
            socket = new Socket(ip, 8080);
            System.out.println("Conecatdo ao servodr.\nJá pode começar a teclar.: ");
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.writeUTF(name);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error ao conectar com o servidor.");
            }
            write_messages = new MessageWriter(output);
            messageReader = new MessageReader(input);
            write_message_thread = new Thread(write_messages);
            read_message_thread = new Thread(messageReader);
            write_message_thread.start();
            read_message_thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAlive() {
        return alive;
    }

    public static void setAlive(boolean alive) {
        alive = alive;
    }
}

class MessageWriter implements Runnable{

    private static DataOutputStream output = null;
    public MessageWriter(DataOutputStream output) {
        MessageWriter.output = output;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line_to_send = scanner.nextLine();
            if (line_to_send.equals("exit()")) {
                ChatClient.setAlive(false);
            }
            try {
                output.writeUTF(line_to_send);
            } catch (IOException e) {
                System.out.println("Erro ao mandar mensagem para o servidor.");
                e.printStackTrace();
            }
        }
    }
}

class MessageReader implements Runnable {
    private static DataInputStream input = null;

    public MessageReader(DataInputStream input) {
        this.input = input;
    }

    @Override
    public void run() {

        while (true) {
            if(!ChatClient.isAlive()) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            else{
                try {
                    String line_to_read = input.readUTF();
                    System.out.println(line_to_read);
                } catch (IOException e) {
                    System.out.println("An error occurred while trying to read from the SERVER");
                    e.printStackTrace();
                }
            }
        }
    }
}

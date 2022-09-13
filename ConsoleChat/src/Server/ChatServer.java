package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ChatServer {
    private static Socket socket = null;
    private static ServerSocket server = null;
    private static DataInputStream input =  null;
    private static DataOutputStream output = null;
    private static Thread clientThread;
    private static ClientThread client;
    public static ArrayList<ClientThread> client_list = new ArrayList<ClientThread>();
    public static int id = 0;

    public static void main(String args[]) throws IOException {

        System.out.println("Servidor iniciado.\nNenhum usuário conectado no momento.");
        try {
            server = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true){
            socket = null;
            try {
                socket = server.accept();
                System.out.println("Novo usuário conectado: " + socket);
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                client = new ClientThread(socket, input, output, id);
                clientThread = new Thread(client);
                client_list.add(client);
                clientThread.start();
            } catch (IOException e) {
                socket.close();
                for(int i = 0; i < client_list.size(); i++){
                    ClientThread client_to_close = client_list.get(i);
                    client_to_close.socket.close();
                    client_to_close.output.close();
                    client_to_close.input.close();
                }
                e.printStackTrace();
            }
            id++;
        }
    }

    public static synchronized void disconnectFromServer(int user_id){
        ClientThread client_to_close = client_list.get(user_id);
        try {
            client_to_close.output.writeUTF("Conexão com o servidor foi encerrada.");
            client_to_close.input.close();
            client_to_close.output.close();
            client_to_close.socket.close();
            client_list.remove(user_id);
            for (int i = 0; i < client_list.size(); i++) {
                ClientThread new_id_for_client = client_list.get(i);
                new_id_for_client.setId(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static synchronized void messageAll(String s) {
        for (int i = 0; i < client_list.size(); i++) {
            ClientThread client_to_recieve = client_list.get(i);
            try {
                client_to_recieve.output.writeUTF(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

class ClientThread implements Runnable{

    public Socket socket;
    public DataInputStream input;
    public DataOutputStream output;
    private String name;
    private int id;

    public ClientThread(Socket socket, DataInputStream input, DataOutputStream output, int user_id){
        this.socket = socket;
        this.input = input;
        this.output = output;
        try {
            this.name = this.input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.id = user_id;
        try {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String line = "";
        while (true){
            try
            {
                System.out.println("Client : ///(USERNAME)->" + getName() + " sends: ");
                line = input.readUTF();
                System.out.println("\t" + line);
                if(line.equals("exit()")){
                    System.out.println("Usuário : " + this.socket + " " + getName() + " está de saída.");
                    ChatServer.disconnectFromServer(id);
                    break;
                }
                else if(!line.equals("exit()")){
                    String newline = getName() + " : " + line;
                    ChatServer.messageAll(newline);
                }
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }
}

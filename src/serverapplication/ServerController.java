package serverapplication;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerController extends Thread implements Runnable{

    private final int serverPort;
    private ArrayList<ServerWorker> serverWorkersList = new ArrayList<ServerWorker>();
    private ServerSocket serverSocket=null;
    private volatile boolean exit = false;
    // public int counter=0;

    public ServerController(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = serverSocket = new ServerSocket(serverPort);
            while (!exit) {
                System.out.println("> Server connection information...");
                Socket clientSocket = serverSocket.accept();
                // counter++;
                System.out.println("> Accepted connection: " + clientSocket);
                ServerWorker serverWorker = new ServerWorker(this, clientSocket);
                serverWorkersList.add(serverWorker);
                serverWorker.start();
                if(Thread.currentThread().isInterrupted()){
                    System.out.println("> Stopping the task...");
                    throw new InterruptedException();
                }
            }
        } catch (IOException | InterruptedException exception) {
            System.err.println("> exception: ServerController.main(): " + exception.getMessage());
        }
    }



    /* public int getCounter(){
        return this.counter;
    }*/

    public ArrayList<ServerWorker> getServerWorkers() {
        return serverWorkersList;
    }

    public void removeServerWorker(ServerWorker serverWorker) {
        serverWorkersList.remove(serverWorker);
    }

    public void stopConnection(){
        this.exit=true;
    }
    public void restartConnection(){
        this.exit=false;
    }
}

//
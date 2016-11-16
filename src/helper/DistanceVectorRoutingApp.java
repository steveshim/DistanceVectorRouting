package helper;


import models.Peer;
import models.Route;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class DistanceVectorRoutingApp {

    private ArrayList<Peer> peers;
    private ArrayList<Peer> neighbors;
    private ArrayList<Route> routes;
    private Integer myPort;
    private Integer myId;
    private String myIp;
    private DatagramSocket dSocket;
    private BufferedReader input;
    private HashMap<Peer, DataOutputStream> peerOutput;
    private Integer packetCounter;
    private Integer interval;
    private Integer nodes;
    private Integer numOfNeighbors;
    private int serverId = 0;

    public DistanceVectorRoutingApp() throws IOException {
        myIp = Inet4Address.getLocalHost().getHostAddress();
        peers = new ArrayList();

        input = new BufferedReader(new InputStreamReader(System.in));
        peerOutput = new HashMap();
        packetCounter = 0;
        peers = new ArrayList();
        routes = new ArrayList();
        neighbors = new ArrayList();
    }

    public void begin() throws IOException{
        System.out.println("Beginning Distance Vector Routing Program for CS4470.");
        System.out.println("Type 'server -t [filename] -i [time interval]' to begin.");

        while(true){
            String userInput = input.readLine();
            String choice = userInput.split(" ")[0].toLowerCase();

            switch(choice){
                case "server":
                    initiateApp(userInput);
                    break;
                case "help":
                    helpMessage();
                    break;
                case "myip":
                    System.out.println("Your IP address is: " + myIp);
                    break;
                case "update":
                    //need to implement
                    break;
                case "step":
                    //need to implement
                    break;
                case "packets":
                    System.out.println("You have received " + packetCounter + " packets since you last called 'packets'.");
                    break;
                case "display":
                    displayTable();
                    break;
                case "disable":
                    //need to implement
                    break;
                case "crash":
                    System.exit(0);
                    break;
                default:
                    System.out.println("That is not a valid command. Type 'help' for a list of commands.");
            }
        }
    }

    public void initiateApp(String userInput) throws IOException{
        String[] check = userInput.split(" ");
        if (check.length != 5){
            System.out.println("Invalid number of arguments given. Expected: 5, Given: " + check.length);
        } else if (!check[1].equals("-t") || !check[3].equals("-i")){
            System.out.println("Invalid arguments given. Must be 'server -t [filename] -i [interval]'.");
        } else {
            try {
                interval = Integer.parseInt(check[4]);
                try{
                    BufferedReader br = new BufferedReader(new FileReader(check[2]));
                    nodes = Integer.parseInt(br.readLine());
                    numOfNeighbors = Integer.parseInt(br.readLine());
                    for (int i=0; i<nodes; i++){
                        String[] temp = br.readLine().split(" ");
                        if (!temp[1].equals(myIp.toString())) {
                            Peer tempPeer = new Peer(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[2]));
                            peers.add(tempPeer);
                            serverId++;
                        } else{
                            myId = Integer.parseInt(temp[0]);
                            myPort = Integer.parseInt(temp[2]);
                        }
                    }
                    for (int j=0; j<numOfNeighbors; j++){
                        String[] temp = br.readLine().split(" ");
                        int tempServerId = Integer.parseInt(temp[1]);
                        Peer tempPeer = peers.get(peers.indexOf(new Peer(tempServerId)));
                        neighbors.add(tempPeer);
                        routes.add(new Route(tempPeer, new Peer(myId), Integer.parseInt(temp[2])));
                    }
                    System.out.println("Your ip is " + myIp + ", listening on port " + myPort + " with server id " + myId);
                    System.out.println("Toplogy file " + check[2] + " has been read.");
                    displayTable();
                    System.out.println("Type 'help' for more commands.");
                    startServerSocket();
                } catch (IOException e){
                    System.out.println("File not found.");
                }
            } catch (NumberFormatException e){
                System.out.println("Last argument must be an integer.");
            }
        }
    }

    public void startServerSocket(){
        new Thread(() -> {
            try {
                DatagramSocket serverSocket = new DatagramSocket(myPort);
                byte[] buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while(true){
                    try {
                        serverSocket.receive(receivePacket);
                        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        Peer tempPeer = new Peer(receivePacket.getAddress().toString().substring(1));
                        System.out.println("Server " + peers.get(peers.indexOf(tempPeer)).getServerId() + " sends: \n"
                                + receivedMessage + "\n");
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e){
                e.printStackTrace();
            }
        }).start();
    }

    public void helpMessage(){
        System.out.println("\n");
        System.out.println("Command \t \t \t \t Function");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("server -t [filename] -i [time interval] \t Loads topology file and transmits ever [time interval] seconds.");
        System.out.println("help \t \t \t \t \t \t \t \t \t \t Displays valid commands for chat application.");
        System.out.println("myip \t \t \t \t \t \t \t \t \t \t Displays your IP address.");
        System.out.println("update [server id 1] [server id 2] [cost] \t Specifies new link [cost] between [server id 1] and [sever id 2].");
        System.out.println("step \t \t \t \t \t \t \t \t \t \t Sends routing table update to all neighbors.");
        System.out.println("packets \t \t \t \t \t \t \t \t \t Display total number of distance vector packets this server has received since last invocation.");
        System.out.println("display \t \t \t \t \t \t \t \t \t Displays current routing table.");
        System.out.println("disable [server id] \t \t \t \t \t \t Disable link to [server id].");
        System.out.println("crash \t \t \t \t \t \t \t \t \t \t Close all connections and terminate application.");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("\n");
    }

    public void displayTable(){
        if (myPort == null)
            System.out.println("Need to start server with 'server' command first.");
        else{
            for(Route route: routes){
                System.out.println(route);
            }
        }
    }


}

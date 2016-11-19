package helper;


import models.Peer;
import models.Route;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DistanceVectorRoutingApp {

    private ArrayList<Peer> peers;
    private ArrayList<Peer> neighbors;
    private ArrayList<Route> routes;
    private Peer me;
    private Integer myPort;
    private Integer myId;
    private String myIp;
    private DatagramSocket serverSocket;
    private BufferedReader input;
    private Integer packetCounter;
    private Integer interval;
    private Integer nodes;
    private Integer numOfNeighbors;
    private ScheduledExecutorService scheduledUpdate;

    public DistanceVectorRoutingApp() throws IOException {
        myIp = Inet4Address.getLocalHost().getHostAddress();
        input = new BufferedReader(new InputStreamReader(System.in));
        packetCounter = 0;
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
                    if (myPort == null)
                        System.out.println("Need to start server with 'server' command first.");
                    else
                        update(userInput);
                    break;
                case "step":
                    if (myPort == null)
                        System.out.println("Need to start server with 'server' command first.");
                    else{
                        step();
                    }
                    break;
                case "packets":
                    System.out.println("You have received " + packetCounter + " packets since you last called 'packets'.");
                    packetCounter = 0;
                    break;
                case "display":
                    if (myPort == null)
                        System.out.println("Need to start server with 'server' command first.");
                    else
                        displayTable();
                    break;
                case "disable":
                    if (myPort == null)
                        System.out.println("Need to start server with 'server' command first.");
                    else{
                        //need to implement
                    }
                    break;
                case "crash":
                    System.out.println("You are exiting the program.");
                    System.exit(0);
                    break;
                case "neighbors":
                    //for testing purposes
                    displayNeighbors();
                    break;
                default:
                    System.out.println("That is not a valid command. Type 'help' for a list of commands.");
            }
        }
    }

    /*
    When user types "server"
     */
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
                    peers = new ArrayList();
                    routes = new ArrayList();
                    neighbors = new ArrayList();
                    nodes = Integer.parseInt(br.readLine());
                    numOfNeighbors = Integer.parseInt(br.readLine());
                    for (int i=0; i<nodes; i++){
                        String[] temp = br.readLine().split(" ");
                        if (!temp[1].equals(myIp.toString())) {
                            Peer tempPeer = new Peer(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[2]));
                            peers.add(tempPeer);
                        } else{
                            myId = Integer.parseInt(temp[0]);
                            myPort = Integer.parseInt(temp[2]);
                            me = new Peer(myId, myIp, myPort);
                            peers.add(me);
                            routes.add(new Route(me, me, 0));
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
                    startRoutingUpdateInterval(interval);
                } catch (IOException e){
                    System.out.println("File not found.");
                }
            } catch (NumberFormatException e){
                System.out.println("Last argument must be an integer.");
            }
        }
    }

    //Listening socket for incoming messages
    public void startServerSocket(){
        new Thread(() -> {
            try {
                serverSocket = new DatagramSocket(myPort);
                byte[] buffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while(true){
                    try {
                        serverSocket.receive(receivePacket);
                        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        Peer tempPeer = new Peer(receivePacket.getAddress().toString().substring(1));
                        System.out.println("Server " + peers.get(peers.indexOf(tempPeer)).getServerId() + " sends: \n"
                                + receivedMessage + "\n");
                        packetCounter++;
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e){
                e.printStackTrace();
            }
        }).start();
    }

    //Start periodically sending routing update
    public void startRoutingUpdateInterval(int userInterval){
        scheduledUpdate = Executors.newScheduledThreadPool(1);
        scheduledUpdate.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run(){
                String message = peers.size() + "\n";
                for(int i=0; i<peers.size(); i++){
                    Peer tempPeer = peers.get(i);
                    message = message + tempPeer.getHost() + "\n"
                        + tempPeer.getPort() + "\n"
                        + tempPeer.getServerId() + " ";
                    Route tempRoute = new Route(tempPeer, me, 0);
                    if (routes.contains(tempRoute)){
                        message = message + routes.get(routes.indexOf(tempRoute)).getCost() + "\n";
                    } else{
                        message = message + "inf" + "\n";
                    }
                }
                //System.out.println(message);
                byte[] sendData = new byte[1024];
                sendData = message.getBytes();
                InetAddress destinationIp;
                int destinationPort;
                DatagramPacket out;
                for(Peer peer:neighbors){
                    try {
                        destinationIp = InetAddress.getByName(peer.getHost());
                        destinationPort = peer.getPort();
                        out = new DatagramPacket(sendData, sendData.length, destinationIp, destinationPort);
                        serverSocket.send(out);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }, userInterval, userInterval, TimeUnit.SECONDS);
    }

    /*
    When user types "help"
     */
    public void helpMessage(){
        System.out.println("\n");
        System.out.println("Command \t \t \t \t \t \t \t \t \t Function");
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

    /*
    When user types "update"
     */
    public void update(String userInput){
        String[] updates = userInput.split(" ");
        if (updates.length != 4){
            System.out.println("Invalid number of arguments, expected 4, received " + updates.length);
        } else{
            try{
                int id1 = Integer.parseInt(updates[1]);
                int id2 = Integer.parseInt(updates[2]);
                int cost;
                if (updates[3].toLowerCase().equals("inf"))
                    cost = Integer.MAX_VALUE;
                else
                    cost = Integer.parseInt(updates[3]);
                Peer p1 = new Peer(id1);
                Peer p2 = new Peer(id2);
                Route tempRoute = new Route(p1, p2, cost);
                if (!peers.contains(p1) || !peers.contains(p2)){
                    System.out.println("These peers do not exist in the network.");
                } else if (routes.contains(tempRoute)){
                    routes.get(routes.indexOf(tempRoute)).setCost(tempRoute.getCost());
                    System.out.println("Updated cost of route \n" + tempRoute);
                } else{
                    p1 = peers.get(peers.indexOf(p1));
                    p2 = peers.get(peers.indexOf(p2));
                    tempRoute = new Route(p1, p2, cost);
                    routes.add(tempRoute);
                    if(p1.getServerId()==myId)
                        neighbors.add(p2);
                    if(p2.getServerId()==myId)
                        neighbors.add(p1);
                    System.out.println("New route added \n" + tempRoute);
                }
                sortRoutes();
            } catch (NumberFormatException e){
                System.out.println("Must input integers in form of 'update [server id 1] [server id 2] [cost]'");
            }
        }
    }

    /*
    When user types "step"
     */
    public void step(){

    }

    /*
    When user types "display"
     */
    public void displayTable(){
        System.out.println("Routing table:");
        for(Route route: routes) {
            System.out.println(route);
        }
        System.out.print("\n");
    }


    /*
    HELPER METHOD TO SORT ROUTES
     */
    public void sortRoutes(){
        for(Route route: routes){
            Peer tempPeer1 = route.getPeerFrom();
            Peer tempPeer2 = route.getPeerTo();
            if(tempPeer2.getServerId()==myId){
                route.setPeerFrom(tempPeer2);
                route.setPeerTo(tempPeer1);
            } else if(tempPeer1.getServerId() != myId && neighbors.contains(tempPeer2) && !neighbors.contains(tempPeer1)){
                route.setPeerFrom(tempPeer2);
                route.setPeerTo(tempPeer1);
            }
        }
        Collections.sort(routes, (a,b)->a.getPeerTo().getServerId().compareTo(b.getPeerTo().getServerId()));
    }

    /*
    HELPER TO DISPLAY NEIGHBORS
     */
    private void displayNeighbors(){
        System.out.println("Current neighbors are: ");
        for(Peer peer:neighbors){
            System.out.println(peer);
        }
        System.out.print("\n");
    }


}

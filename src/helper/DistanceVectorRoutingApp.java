package helper;


import models.Peer;
import models.Route;

import java.io.*;
import java.net.*;
import java.util.*;
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
    private Map<Peer, ArrayList<Route>> destinationRoutes;

    public DistanceVectorRoutingApp() throws IOException {
        myIp = Inet4Address.getLocalHost().getHostAddress();
        input = new BufferedReader(new InputStreamReader(System.in));
        destinationRoutes = new HashMap();
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
                        disable(userInput);
                    }
                    break;
                case "crash":
                    System.out.println("crash SUCCESS");
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
            System.out.println("server: Invalid number of arguments given. Expected: 5, Given: " + check.length);
        } else if (!check[1].equals("-t") || !check[3].equals("-i")){
            System.out.println("server: Invalid arguments given. Must be 'server -t [filename] -i [interval]'.");
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
                            Route routeToMyself = new Route(me, me, 0);
                            routes.add(routeToMyself);
                            ArrayList<Route> routeListToMyself = new ArrayList();
                            routeListToMyself.add(routeToMyself);
                            destinationRoutes.put(me, routeListToMyself);
                        }
                    }
                    for (int j=0; j<numOfNeighbors; j++){
                        String[] temp = br.readLine().split(" ");
                        int tempServerId = Integer.parseInt(temp[1]);
                        Peer tempPeer = peers.get(peers.indexOf(new Peer(tempServerId)));
                        neighbors.add(tempPeer);
                        Route tempRoute = new Route(tempPeer, me, Integer.parseInt(temp[2]));
                        routes.add(tempRoute);
                        ArrayList<Route> tempDestinationRoutes = new ArrayList();
                        tempDestinationRoutes.add(tempRoute);
                        destinationRoutes.put(tempPeer, tempDestinationRoutes);
                    }
                    System.out.println("server SUCCESS");
                    System.out.println("Your ip is " + myIp + ", listening on port " + myPort + " with server id " + myId);
                    System.out.println("Toplogy file " + check[2] + " has been read.");
                    displayTable();
                    System.out.println("Type 'help' for more commands.");
                    startServerSocket();
                    startRoutingUpdateInterval(interval);
                } catch (IOException e){
                    System.out.println("server: File not found.");
                }
            } catch (NumberFormatException e){
                System.out.println("server: Last argument must be an integer.");
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
                        Peer receivedFromPeer = new Peer(receivePacket.getAddress().toString().substring(1));
                        receivedFromPeer = peers.get(peers.indexOf(receivedFromPeer));
                        System.out.println("RECEIVED A MESSAGE FROM SERVER " + receivedFromPeer.getServerId());
                        //        + ":" + " \n" + receivedMessage + "\n");
                        packetCounter++;
                        //receiving an update message
                        if (receivedMessage.toLowerCase().startsWith("update")){
                            String[] updateMessage = receivedMessage.split("\\s");
                            Peer p1 = new Peer(Integer.parseInt(updateMessage[1]));
                            p1 = peers.get(peers.indexOf(p1));
                            Peer p2 = new Peer(Integer.parseInt(updateMessage[2]));
                            p2 = peers.get(peers.indexOf(p2));
                            int cost = Integer.parseInt(updateMessage[3]);
                            Route tempRoute = new Route(p1, p2, cost);
                            if (routes.contains(tempRoute)){
                                if (cost != Integer.MAX_VALUE) {
                                    routes.get(routes.indexOf(tempRoute)).setCost((cost));
                                } else{
                                    if (p1.equals(me)) {
                                        neighbors.remove(p2);
                                        routes.remove(tempRoute);
                                        destinationRoutes.remove(p2);
                                    }
                                    else {
                                        neighbors.remove(p1);
                                        routes.remove(tempRoute);
                                        destinationRoutes.remove(p1);
                                    }
                                }
                            } else if (cost != Integer.MAX_VALUE){
                                routes.add(tempRoute);
                                ArrayList<Route> tempRoutes = new ArrayList();
                                tempRoutes.add(tempRoute);
                                if (p1.equals(me)) {
                                    neighbors.add(p2);
                                    destinationRoutes.put(p2, tempRoutes);
                                }
                                else {
                                    neighbors.add(p1);
                                    destinationRoutes.put(p1, tempRoutes);
                                }
                            }
                            sendTable();
                        }
                        //receiving a disable message
                        else if (receivedMessage.toLowerCase().startsWith("disable")){
                            System.out.println("You have been disabled.");
                            System.exit(0);
                        }
                        //receiving a routing table from step or periodic schedule
                        else{
                            String[] routingMessage = receivedMessage.split("\\s");
                            int numberOfUpdates = Integer.parseInt(routingMessage[0]);
                            HashMap<Peer, Integer> routingTable = new HashMap();
                            for (int i = 1; i < (numberOfUpdates * 5); i = i + 5) {
                                if (!routingMessage[i + 4].equals("inf")) {
                                    Peer tempPeer = new Peer(Integer.parseInt(routingMessage[i + 3]));
                                    tempPeer = peers.get(peers.indexOf(tempPeer));
                                    int routingCost = Integer.parseInt(routingMessage[i + 4]);
                                    routingTable.put(tempPeer, routingCost);
                                }
                            }
                            dvrAlgorithm(receivedFromPeer, routingTable);

                        }
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
                sendTable();
            }
        }, userInterval, userInterval, TimeUnit.SECONDS);
    }

    public void sendTable(){
        String message = peers.size() + "\n";
        for(int i=0; i<peers.size(); i++){
            Peer tempPeer = peers.get(i);
            message = message + tempPeer.getHost() + "\n"
                    + tempPeer.getPort() + " 0x0\n"
                    + tempPeer.getServerId() + " ";
            if (destinationRoutes.containsKey(tempPeer)){
                message = message + calculateCost(destinationRoutes.get(tempPeer)) + "\n";
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
                System.out.println("Sending message to " + destinationIp + ":" + destinationPort);
                out = new DatagramPacket(sendData, sendData.length, destinationIp, destinationPort);
                serverSocket.send(out);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
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
            System.out.println("update: Invalid number of arguments, expected 4, received " + updates.length);
        } else{
            try{
                int id1 = Integer.parseInt(updates[1]);
                int id2 = Integer.parseInt(updates[2]);
                int cost;
                if (updates[3].toLowerCase().equals("inf")) {
                    cost = Integer.MAX_VALUE;
                }
                else
                    cost = Integer.parseInt(updates[3]);
                Peer p1 = new Peer(id1);
                Peer p2 = new Peer(id2);
                Route tempRoute = new Route(p1, p2, cost);
                if (!peers.contains(p1) || !peers.contains(p2)){
                    System.out.println("update: These peers do not exist in the network.");
                } else {
                    p1 = peers.get(peers.indexOf(p1));
                    p2 = peers.get(peers.indexOf(p2));
                    if (routes.contains(tempRoute)){
                        if (cost==Integer.MAX_VALUE){
                            if (p1.equals(me)) {
                                neighbors.remove(p2);
                                routes.remove(tempRoute);
                                destinationRoutes.remove(p2);
                            }
                            else {
                                neighbors.remove(p1);
                                routes.remove(tempRoute);
                                destinationRoutes.remove(p1);
                            }
                        }else {
                            tempRoute = routes.get(routes.indexOf(tempRoute));
                            tempRoute.setCost(cost);
                            System.out.println("Updated cost of route \n" + tempRoute);
                            sendUpdate(p1, p2, cost);
                            System.out.println("update SUCCESS");
                        }
                    } else{
                        if (cost != Integer.MAX_VALUE && (p1.equals(me) || p2.equals(me))){
                            routes.add(tempRoute);
                            ArrayList<Route> tempRouteList = new ArrayList();
                            tempRouteList.add(tempRoute);
                            if(p1.equals(me)){
                                neighbors.add(p2);
                                destinationRoutes.put(p2, tempRouteList);
                            } else{
                                neighbors.add(p1);
                                destinationRoutes.put(p1, tempRouteList);
                            }

                        }
                        System.out.println("New route added \n" + tempRoute);
                        sendUpdate(p1, p2, cost);
                        System.out.println("update SUCCESS");
                    }
                }
                sortRoutes();
            } catch (NumberFormatException e){
                System.out.println("update: Must input integers in form of 'update [server id 1] [server id 2] [cost]'");
            }
        }
    }

    public void sendUpdate(Peer p1, Peer p2, int cost){
        String message = "update " + p1.getServerId() + " " + p2.getServerId() + " " + cost;
        byte[] sendData = new byte[1024];
        sendData = message.getBytes();
        InetAddress destinationIp;
        int destinationPort;
        DatagramPacket out;
        if (!p1.equals(me)){
            try {
                destinationIp = InetAddress.getByName(p1.getHost());
                destinationPort = p1.getPort();
                System.out.println("Sending update to " + destinationIp + ":" + destinationPort);
                out = new DatagramPacket(sendData, sendData.length, destinationIp, destinationPort);
                serverSocket.send(out);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        if (!p2.equals(me)){
            try {
                destinationIp = InetAddress.getByName(p2.getHost());
                destinationPort = p2.getPort();
                System.out.println("Sending update to " + destinationIp + ":" + destinationPort);
                out = new DatagramPacket(sendData, sendData.length, destinationIp, destinationPort);
                serverSocket.send(out);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /*
    When user types "step"
     */
    public void step(){
        sendTable();
        System.out.println("step SUCCESS");
    }

    /*
    When user types "display"
     */
    public void displayTable(){
        System.out.println("Routing table:");
        sortDestinationRoutes();
        for(Peer peer: peers) {
            if (destinationRoutes.containsKey(peer)){
                ArrayList<Route> routeToPeer = destinationRoutes.get(peer);
                Route lastRouteInList = routeToPeer.get(routeToPeer.size()-1);
                System.out.println(peer.getServerId() + " " + lastRouteInList.getPeerFrom().getServerId()
                        + " " + calculateCost(routeToPeer));
            } else{
                System.out.println(peer.getServerId() + " " + myId + " inf");
            }
        }
        System.out.println("display SUCCESS");
        System.out.print("\n");
    }

    /*
    When user types "disable"
     */
    public void disable(String userInput){
        String[] input = userInput.split(" ");
        if (input.length != 2)
            System.out.println("disable: Invalid number of arguments, expected 2, received :" + input.length);
        else {
            try{
                int id = Integer.parseInt(input[1]);
                Peer tempPeer = new Peer(id);
                if (peers.contains(tempPeer)){
                    tempPeer = peers.get(peers.indexOf(tempPeer));
                    if (neighbors.contains(tempPeer))
                        neighbors.remove(tempPeer);
                    if (destinationRoutes.containsKey(tempPeer))
                        destinationRoutes.remove(tempPeer);
                    System.out.println("Removed server id " + tempPeer.getServerId() + " from network.");
                    System.out.println("disable SUCCESS");
                    sendDisableMessage(tempPeer);
                } else
                    System.out.println("disable: Server id " + tempPeer.getServerId() + " does not exist in network.");
            } catch (NumberFormatException e){
                System.out.println("disable: Second argument must be an integer.");
            }
        }
    }

    public void sendDisableMessage(Peer peer){
        String message = "disable";
        byte[] sendData = new byte[1024];
        sendData = message.getBytes();
        InetAddress destinationIp;
        int destinationPort;
        DatagramPacket out;
        try{
            destinationIp = InetAddress.getByName(peer.getHost());
            destinationPort = peer.getPort();
            System.out.println("Sending disable instruction to " + destinationIp + ":" + destinationPort);
            out = new DatagramPacket(sendData, sendData.length, destinationIp, destinationPort);
            serverSocket.send(out);
        }catch(Exception e){
            e.printStackTrace();
        }
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
    HELPER METHOD TO SORT DESTINATIONROUTES
     */
    public void sortDestinationRoutes(){
        Collections.sort(peers, (a,b)->a.getServerId().compareTo(b.getServerId()));
        for (Peer peer: peers){
            if(destinationRoutes.containsKey(peer)){
                ArrayList<Route> tempRoutes = destinationRoutes.get(peer);
                Route firstRoute = tempRoutes.get(0);
                if(!firstRoute.getPeerFrom().equals(me)){
                    firstRoute.setPeerTo(firstRoute.getPeerFrom());
                    firstRoute.setPeerFrom(me);
                }
                if (tempRoutes.size()>1){
                    Route secondRoute = tempRoutes.get(1);
                    if(!secondRoute.getPeerFrom().equals(firstRoute.getPeerTo())){
                        Peer destPeer = secondRoute.getPeerFrom();
                        secondRoute.setPeerFrom(firstRoute.getPeerTo());
                        secondRoute.setPeerTo(destPeer);
                    }
                }
            }
        }
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

    /*
    HELPER FOR DISTANCE VECTOR ALGORITHM
     */
    private void dvrAlgorithm(Peer receivedFrom, HashMap<Peer, Integer> routingTable){
        Route tempRoute = new Route(receivedFrom, me, 0);
        Route routeToReceivedFrom = tempRoute;
        int costToReceivedFrom = tempRoute.getCost();
        if(routes.contains(tempRoute)) {
            routeToReceivedFrom = routes.get(routes.indexOf(tempRoute));
            costToReceivedFrom = routeToReceivedFrom.getCost();
        }

        for(Map.Entry<Peer, Integer> entry:routingTable.entrySet()){
            Peer toPeer = entry.getKey();
            Route tempRouteToDestination = new Route(toPeer, receivedFrom, entry.getValue());
            Integer newRouteCost = entry.getValue() + costToReceivedFrom;
            int currentCost;
            ArrayList<Route> tempListRoutes = new ArrayList();
            tempListRoutes.add(routeToReceivedFrom);
            tempListRoutes.add(tempRouteToDestination);
            if (destinationRoutes.containsKey(toPeer)){
                currentCost = calculateCost(destinationRoutes.get(toPeer));
                if (currentCost > newRouteCost ||
                        (destinationRoutes.get(toPeer).equals(tempListRoutes) && currentCost != newRouteCost)){
                    destinationRoutes.replace(toPeer, tempListRoutes);
                }
            } else{
                destinationRoutes.put(toPeer, tempListRoutes);
            }
        }

    }

    /*
    HELPER FOR CALCULATING COST IN ARRAYLIST OF ROUTES
     */
    private int calculateCost(ArrayList<Route> dRoutes){
        int cost = 0;
        for (Route route: dRoutes){
            cost += route.getCost();
        }
        return cost;
    }


}

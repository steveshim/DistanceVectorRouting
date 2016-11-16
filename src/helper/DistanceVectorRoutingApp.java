package helper;


import models.Peer;

import java.io.*;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;

public class DistanceVectorRoutingApp {

    private ArrayList<Peer> peers;
    private Integer myPort;
    private Integer myId;
    private String myIp;
    private DatagramSocket dSocket;
    private BufferedReader input;
    private HashMap<Peer, DataOutputStream> peerOutput;
    private HashMap<Peer, Integer> neighborCosts;
    private Integer packetCounter;
    private Integer interval;
    private Integer nodes;
    private Integer neighbors;
    private int serverId = 0;

    public DistanceVectorRoutingApp() throws IOException {
        myIp = Inet4Address.getLocalHost().getHostAddress();
        peers = new ArrayList();

        input = new BufferedReader(new InputStreamReader(System.in));
        peerOutput = new HashMap();
        packetCounter = 0;
    }

    public void begin() throws IOException{
        System.out.println("Beginning Distance Vector Routing Program for CS4470.");
        System.out.println("Type 'server -t [filename] -i [time interval]' to begin.");
        System.out.println("Type 'help' for more commands.");

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
            } catch (NumberFormatException e){
                System.out.println("Last argument must be an integer.");
            }
            BufferedReader br = new BufferedReader(new FileReader(check[2]));
            nodes = Integer.parseInt(br.readLine());
            neighbors = Integer.parseInt(br.readLine());
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
            for (int j=0; j<neighbors; j++){
                String[] temp = br.readLine().split(" ");
                int tempServerId = Integer.parseInt(temp[0]);
                Peer tempPeer = peers.get(peers.indexOf(new Peer(tempServerId)));
                neighborCosts.put(tempPeer, Integer.parseInt(temp[1]));
            }
        }
    }

    public void helpMessage(){
        System.out.println("\n");
        System.out.println("Command \t \t \t \t Function");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("server -t [filename] -i [time interval] \t Loads topology file and transmits ever [time interval] seconds.");
        System.out.println("help \t \t \t Displays valid commands for chat application.");
        System.out.println("myip \t \t \t Displays your IP address.");
        System.out.println("update [server id 1] [server id 2] [cost] \t Specifies new link [cost] between [server id 1] and [sever id 2].");
        System.out.println("step \t \t Sends routing table update to all neighbors.");
        System.out.println("packets \t \t Display total number of distance vector packets this server has received since last invocation.");
        System.out.println("display \t \t Displays current routing table.");
        System.out.println("disable [server id] \t \t Disable link to [server id].");
        System.out.println("crash \t \t \t Close all connections and terminate application.");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("\n");
    }
}

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

        while(true){
            String userInput = input.readLine();
            String choice = userInput.split(" ")[0].toLowerCase();

            switch(choice){
                case "server":
                    initiateApp(userInput);
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
                    Peer tempPeer = new Peer(temp[1], Integer.parseInt(temp[2]));
                    peers.add(tempPeer);
                } else{
                    myPort = Integer.parseInt(temp[2]);
                }
            }
            for (int j=0; j<neighbors; j++){
                String[] temp = br.readLine().split(" ");

            }
        }
    }
}

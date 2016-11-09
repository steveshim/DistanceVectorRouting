package helper;


import models.Peer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private Integer packetCounter;

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
            System.out.println("Invalid arguments given.");
        } else {
            try {
                Integer.parseInt(check[4]);
            } catch (NumberFormatException e){
                System.out.println("Last argument must be an integer.");
            }
        }
    }
}

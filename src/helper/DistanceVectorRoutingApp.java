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

    public DistanceVectorRoutingApp() throws IOException {
        myIp = Inet4Address.getLocalHost().getHostAddress();
        peers = new ArrayList();

        input = new BufferedReader(new InputStreamReader(System.in));
        peerOutput = new HashMap();
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
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("That is not a valid command. Type 'help' for a list of commands.");
            }
        }
    }

    public void initiateApp(String userInput){

    }
}

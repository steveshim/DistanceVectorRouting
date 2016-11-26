package run;


import models.Peer;
import models.Route;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) throws Exception{
        Map<Peer, ArrayList<Route>> tester = new HashMap<>();
        Peer p0 = new Peer(0);
        Peer p1 = new Peer(1);
        Route r0 = new Route(p0, p1, 10);
        Peer p2 = new Peer(2);
        Route r1 = new Route(p1, p2, 2);
        ArrayList<Route> routes = new ArrayList<>();
        routes.add(r0);
        routes.add(r1);
        tester.put(p2, routes);
        Peer p3 = new Peer(3);
        Route r2 = new Route(p2, p3, 500);
        System.out.println(tester.get(new Peer(2)));
        tester.get(new Peer(2)).add(r2);
        System.out.println(tester.get(p2));
        ArrayList<Route> routes2 = new ArrayList<>();
        routes2.add(new Route(p0, p2, 10));
        tester.replace(p2,routes2);
        System.out.println("new list" + tester.get(p2));

        ArrayList<Route> testR1 = new ArrayList();
        testR1.add(new Route(new Peer(1), new Peer(2), 10));
        testR1.add(new Route(new Peer(2), new Peer(3), 500));
        ArrayList<Route> testR2 = new ArrayList<>();
        testR2.add(new Route(new Peer(1), new Peer(2), 2));
        testR2.add(new Route(new Peer(2), new Peer(3), 4));
        testR2.add(new Route(new Peer(3), new Peer(4), 10));
        if(testR1.equals(testR2)){
            System.out.println("theyre equal");
        }

    }
}

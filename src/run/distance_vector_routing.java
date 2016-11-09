package run;


import helper.DistanceVectorRoutingApp;

public class distance_vector_routing {

    public static void main(String[] args){
        try{
            DistanceVectorRoutingApp dvr = new DistanceVectorRoutingApp();
            dvr.begin();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

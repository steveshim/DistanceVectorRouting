package models;

public class Route {
    private Peer peerFrom, peerTo;
    private Integer cost;

    public Route(Peer to, Peer from, int cost){
        this.peerFrom = from;
        this.peerTo = to;
        this.cost = cost;
    }

    public Peer getPeerFrom() {
        return peerFrom;
    }

    public void setPeerFrom(Peer peerFrom) {
        this.peerFrom = peerFrom;
    }

    public Peer getPeerTo() {
        return peerTo;
    }

    public void setPeerTo(Peer peerTo) {
        this.peerTo = peerTo;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (!(o instanceof Route))
            return false;
        Route route = (Route) o;
        return ((getPeerTo().equals(route.getPeerTo()) && getPeerFrom().equals(route.getPeerFrom()))
            || (getPeerFrom().equals(route.getPeerTo()) && getPeerTo().equals(route.getPeerFrom())));
    }

    @Override
    public String toString(){
        return "To server " + getPeerTo().getServerId() + " from server " + getPeerFrom().getServerId() +
                " has cost of " + getCost() + ".";
    }


}

package models;


public class Peer {
    private String host;
    private int port;
    private Integer serverId;

    public Peer(int serverId, String host, int port){
        this.serverId = serverId;
        this.host = host;
        this.port = port;

    }

    public Peer(int serverId){
        this.serverId = serverId;
    }

    public Peer(String ip){
        this.host = ip;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (!(o instanceof Peer))
            return false;
        Peer peer = (Peer) o;
        return (((getServerId() != null && peer.getServerId() != null) && getServerId() == peer.getServerId()) ||
                ((getHost() != null && peer.getHost() != null) && getHost().equals(peer.getHost())));
    }

    @Override
    public int hashCode(){
        return getServerId();
    }

    @Override
    public String toString(){
        return "Server id is " + getServerId();
    }
}

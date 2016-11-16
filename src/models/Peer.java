package models;


public class Peer {
    private String host;
    private int port;
    private Integer serverId;

    public Peer(String host, int port, int serverId){
        this.host = host;
        this.port = port;

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
}

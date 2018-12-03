package de.patruck.stepaluja;

import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class IpPortAddress
{
    public String ipAddress = null;
    public int port = 0;
    public String localIpAddress = null;
    public int localPortHere = 0;

    public Selector selector = null;
    public DatagramChannel channel = null;
}

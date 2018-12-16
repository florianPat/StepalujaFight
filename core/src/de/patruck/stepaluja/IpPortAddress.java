package de.patruck.stepaluja;

import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class IpPortAddress
{
    public InetAddress ipAddress = null;
    public int port = 0;
    public InetAddress localIpAddress = null;

    public Selector selector = null;
    public DatagramChannel channel = null;
}

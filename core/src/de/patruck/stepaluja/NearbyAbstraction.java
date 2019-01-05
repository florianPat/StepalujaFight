package de.patruck.stepaluja;

import java.nio.ByteBuffer;
import java.util.Vector;

public abstract class NearbyAbstraction
{
    public Vector<NearbyServerList.ListItem> listItems = null;
    public int connectedFlag = 0;

    public abstract void startAdvertising(String username, char level);

    public abstract void startDiscovery();

    public abstract void stopAdvertising();

    public abstract void stopDiscovery();

    public abstract void establishConnection(String endpointId);

    public abstract void disconnectFromAllEndpoints();

    public abstract void send(byte[] bytes);

    public abstract boolean receive(ByteBuffer readByteBuffer);
}

package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class GameClientLobbyLevel extends LoadingLevel
{
    private String realConnAddress;
    private String serverUid;
    private boolean showingUpInDB = false;

    @Override
    public void render(float dt)
    {
        super.render(dt);

        if(!showingUpInDB)
        {
            switch(NativeBridge.registerClientResult(serverUid))
            {
                case -1:
                {
                    Utils.logBreak(NativeBridge.errorMsg, screenManager, worldSize);
                    break;
                }
                case 0:
                {
                    return;
                }
                case 1:
                {
                    Utils.log("Client showing up in DB!");
                    showingUpInDB = true;
                    break;
                }
                default:
                {
                    Utils.invalidCodePath();
                    break;
                }
            }
        }
        else
        {
            Utils.log("Lets connect!");
        }
    }

    public GameClientLobbyLevel(String connAddress, GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        final int nPipes = 3;

        int indexOfFirstPipe = connAddress.indexOf('|');
        realConnAddress = connAddress.substring(0, indexOfFirstPipe);
        serverUid = connAddress.substring(indexOfFirstPipe + nPipes);

        msg = "Server found! Lets connect to it!";
    }

    @Override
    public void create()
    {
        super.create();

        String ipAddress;
        String localIpAddress;

        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
        }
        catch(SocketException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        try
        {
            Utils.aassert(socket != null);
            socket.connect(new InetSocketAddress("google.com", 80));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        ipAddress = socket.getInetAddress().getHostAddress();
        localIpAddress = socket.getLocalAddress().getHostAddress();
        socket.disconnect();
        socket.close();
        socket = null;

        NativeBridge.registerClient(ipAddress + "__" + localIpAddress, serverUid);
    }
}

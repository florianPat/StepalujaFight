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
    private String clientExternalIp = null;
    private String clientExternalPort = null;
    private String clientLocalIp = null;
    private float packetInterval = 0.0f;

    public GameClientLobbyLevel(String connAddress, GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        final int nPipes = 3;

        int indexOfFirstPipe = connAddress.indexOf('|');
        realConnAddress = connAddress.substring(0, indexOfFirstPipe);
        serverUid = connAddress.substring(indexOfFirstPipe + nPipes);

        int posUnderscore = realConnAddress.indexOf('_');
        int posPipe = realConnAddress.indexOf('-');

        clientExternalIp = realConnAddress.substring(0, posUnderscore);
        clientExternalPort = realConnAddress.substring(posUnderscore + 1, posPipe);
        clientLocalIp = realConnAddress.substring(posPipe + 1);

        msg = "Server found! Lets connect to it!";
    }

    @Override
    public void create()
    {
        super.create();

        String ipAddress;
        int port;
        String localIpAddress;
        int localPortHere;

//        try
//        {
//            ipAddress = Ipify.getPublicIp(true);
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//            Utils.invalidCodePath();
//        }

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
            socket.connect(new InetSocketAddress("google.com", GameServerLobbyLevel.localPort));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        ipAddress = socket.getInetAddress().getHostAddress();
        port = socket.getPort();
        localIpAddress = socket.getLocalAddress().getHostAddress();
        localPortHere = socket.getLocalPort();
        socket.disconnect();
        socket.close();

        NativeBridge.registerClient(ipAddress + "_" + port + "-" + localIpAddress, serverUid);
    }

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
            Utils.log("Now its communicating time!");
        }
    }
}

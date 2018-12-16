package de.patruck.stepaluja;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class GameClientLobbyLevel extends LoadingLevel
{
    private String realConnAddress;
    private String serverUid;
    private boolean showingUpInDB = false;
    private InetAddress clientExternalIp;
    private int clientExternalPort;
    private InetAddress clientLocalIp;
    private DatagramChannel channel = null;
    private Selector selector = null;
    private NetworkManager networkManager;
    private float writeTimer = 0.0f;

    public GameClientLobbyLevel(String connAddress, GameStart screenManager)
    {
        super(screenManager);

        final int nPipes = 3;

        int indexOfFirstPipe = connAddress.indexOf('|');
        realConnAddress = connAddress.substring(0, indexOfFirstPipe);
        serverUid = connAddress.substring(indexOfFirstPipe + nPipes);

        int posUnderscore = realConnAddress.indexOf('_');
        int posPipe = realConnAddress.indexOf('-');

        String sClientExternalIp = realConnAddress.substring(0, posUnderscore);
        clientExternalPort = Integer.valueOf(realConnAddress.substring(posUnderscore + 1, posPipe));
        String sClientLocalIp = realConnAddress.substring(posPipe + 1);

        try
        {
            clientExternalIp = InetAddress.getByName(sClientExternalIp);
        }
        catch(UnknownHostException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        try
        {
            clientLocalIp = InetAddress.getByName(sClientLocalIp);
        }
        catch(UnknownHostException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }

        msg = "Server found! Lets connect to it!";
    }

    @Override
    public void create()
    {
        super.create();

        IpPortAddress ipPortAddress = GameServerLobbyLevel.getAddressesAndCreateChannelAsWellAsSelector();
        channel = ipPortAddress.channel;
        selector = ipPortAddress.selector;

        SocketAddress connectSocketAddress = new InetSocketAddress(clientLocalIp, clientExternalPort);
        try
        {
            channel.connect(connectSocketAddress);
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }

        networkManager = new NetworkManager(channel, selector);

        NativeBridge.registerClient(ipPortAddress.ipAddress.getHostAddress() + "_" + ipPortAddress.port + "-" +
                ipPortAddress.localIpAddress.getHostAddress(), serverUid);
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
                    Utils.logBreak(NativeBridge.errorMsg, screenManager);
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
            networkManager.select(clientLocalIp, clientExternalPort, dt);

            while(networkManager.hasNext())
            {
                Object o = networkManager.read();
                if(o != null)
                {
                    if(o instanceof String)
                    {
                        String s = (String) o;
                        msg = ("We got back a string:" + s);
                    }
                    else
                    {
                        msg = ("Unexpected reading of class: " + o.getClass().toString() + " ;;; "
                                + o.toString());
                        //Utils.invalidCodePath();
                    }
                }
                else
                    break;
            }

            writeTimer += dt;
            if(writeTimer >= GameServerLobbyLevel.maxWriteTimer)
            {
                writeTimer = 0.0f;

                networkManager.send("HELO");
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        networkManager.dispose();
    }

    @Override
    public void resume()
    {
        super.resume();

        IpPortAddress ipPortAddress = GameServerLobbyLevel.getAddressesAndCreateChannelAsWellAsSelector();
        channel = ipPortAddress.channel;
        selector = ipPortAddress.selector;
    }

    @Override
    public void pause()
    {
        super.pause();

        //TODO: Think about if we should unregister the client here!!

        try
        {
            channel.disconnect();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            channel.close();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        try
        {
            selector.close();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
    }
}

package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class GameServerLobbyLevel extends LoadingLevel
{
    private boolean showingUpInDB;
    //NOTE: Was intended for threading, but we do not need it anymore!
//    private static Object lock;
//    private long threadPointer = 0;
    private static boolean isClientConnecting;
    public static final int localPort = 54777;
    private InetAddress clientExternalIp = null;
    private int clientExternalPort = 0;
    private InetAddress clientLocalIp = null;
    private DatagramChannel channel = null;
    private Selector selector = null;
    private NetworkManager networkManager;
    public static float maxWriteTimer = 0.8f;
    private static float writeTimer = 0.0f;

    public GameServerLobbyLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        msg = "Created a server, because there where no open ones!\nWaiting for players to join...";
    }

    public static IpPortAddress getAddressesAndCreateChannelAsWellAsSelector()
    {
        IpPortAddress result = new IpPortAddress();
        //Construct channel
        try
        {
            result.channel = DatagramChannel.open();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        Utils.aassert(result.channel != null);
        DatagramSocket socket = result.channel.socket();
        Utils.aassert(socket != null);
        try
        {
            socket.bind(null);
        }
        catch(SocketException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        //Get local address
        InetSocketAddress inetAddressLocal = new InetSocketAddress("8.8.8.8", localPort);
        try
        {
            result.channel.connect(inetAddressLocal);
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        result.localIpAddress = socket.getLocalAddress();
        result.port = socket.getLocalPort();

        socket.disconnect();
        try
        {
            result.channel.disconnect();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        //Get public address
        InetSocketAddress inetAddressGoogle = new InetSocketAddress("google.com", localPort);
        try
        {
            result.channel.connect(inetAddressGoogle);
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        result.ipAddress = socket.getLocalAddress();
        try
        {
            result.channel.disconnect();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        //Configure blocking
        try
        {
            result.channel.configureBlocking(false);
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        Utils.aassert(!result.channel.isBlocking());
        //Construct selector
        try
        {
            result.selector = Selector.open();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        Utils.aassert(result.selector != null);
        try
        {
            result.channel.register(result.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }

        return result;
    }

    @Override
    public void create()
    {
        super.create();

        showingUpInDB = false;
        isClientConnecting = false;

        IpPortAddress ipPortAddress = getAddressesAndCreateChannelAsWellAsSelector();
        channel = ipPortAddress.channel;
        selector = ipPortAddress.selector;

        networkManager = new NetworkManager(channel, selector);

        NativeBridge.registerNewServer(ipPortAddress.ipAddress.getHostAddress() + "_" + ipPortAddress.port + "-"
                + ipPortAddress.localIpAddress.getHostAddress());
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        if(clientLocalIp == null)
        {
            if(!showingUpInDB && (!isClientConnecting))
            {
                switch(NativeBridge.resultRegisterNewServer())
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
                        showingUpInDB = true;
                        Utils.log("Server Showing up in DB!");
                        NativeBridge.startClientListener();
                        break;
                    }
                    default:
                    {
                        Utils.invalidCodePath();
                        break;
                    }
                }
            }
            else if(showingUpInDB && (!isClientConnecting))
            {
                NativeBridge.updateClientListener();
            }
            else if(showingUpInDB)
            {
                Utils.log("Client is connecting!");
                String clientIpAddress = NativeBridge.getClientListenerResult();
                Utils.log(clientIpAddress);
                unregisterDBAndListener();
                Utils.aassert(isClientConnecting == false);

                int posUnderscore = clientIpAddress.indexOf('_');
                int posPipe = clientIpAddress.indexOf('-');

                String sClientExternalIp = clientIpAddress.substring(0, posUnderscore);
                clientExternalPort = Integer.valueOf(clientIpAddress.substring(posUnderscore + 1, posPipe));
                String sClientLocalIp = clientIpAddress.substring(posPipe + 1);

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

                msg = "Client found! Lets connect to it!";
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
            if(writeTimer >= maxWriteTimer)
            {
                writeTimer = 0.0f;

                networkManager.send("HELO");
            }
        }
    }

    private void unregisterDBAndListener()
    {
        if(showingUpInDB)
        {
            NativeBridge.stopClientListener();

            NativeBridge.unregisterServer();

            while(showingUpInDB)
            {
                switch(NativeBridge.resultUnregisterServer())
                {
                    case -1:
                    {
                        Utils.logBreak(NativeBridge.errorMsg, screenManager, worldSize);
                        break;
                    }
                    case 0:
                    {
                        break;
                    }
                    case 1:
                    {
                        showingUpInDB = false;
                        Utils.log("Server Not showing up in DB!");
                        break;
                    }
                    default:
                    {
                        Utils.invalidCodePath();
                        break;
                    }
                }
            }

            if(isClientConnecting)
            {
                NativeBridge.unregisterClient();

                while(isClientConnecting)
                {
                    switch(NativeBridge.unregisterClientResult())
                    {
                        case -1:
                        {
                            Utils.logBreak(NativeBridge.errorMsg, screenManager, worldSize);
                            break;
                        }
                        case 0:
                        {
                            break;
                        }
                        case 1:
                        {
                            isClientConnecting = false;
                            Utils.log("Client Not showing up in DB!");
                            break;
                        }
                        default:
                        {
                            Utils.invalidCodePath();
                            break;
                        }
                    }
                }
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
    public void pause()
    {
        super.pause();

        unregisterDBAndListener();

        try
        {
            channel.disconnect();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
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

    @Override
    public void resume()
    {
        super.resume();

        create();
    }
}

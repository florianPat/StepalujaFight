package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class GameServerLobbyLevel extends LoadingLevel
{
    private boolean showingUpInDB;
    //NOTE: Was intended for threading, but we do not need it anymore!
//    private static Object lock;
//    private long threadPointer = 0;
    private static boolean isClientConnecting;
    public static final int localPort = 54777;
    private String clientExternalIp = null;
    private String clientExternalPort = null;
    private String clientLocalIp = null;
    private float packetInterval = 0.0f;
    public static final float maxPacketInterval = 2.0f;

    public GameServerLobbyLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        msg = "Created a server, because there where no open ones!\nWaiting for players to join...";
    }

    @Override
    public void create()
    {
        super.create();

        showingUpInDB = false;
        isClientConnecting = false;

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
            socket.connect(new InetSocketAddress("google.com", localPort));
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

        NativeBridge.registerNewServer(ipAddress + "_" + port + "-" + localIpAddress);
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

                clientExternalIp = clientIpAddress.substring(0, posUnderscore);
                clientExternalPort = clientIpAddress.substring(posUnderscore + 1, posPipe);
                clientLocalIp = clientIpAddress.substring(posPipe + 1);

                msg = "Client found! Lets connect to it!";
            }
        }
        else
        {
            Utils.log("Now its communicating time!");
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
    public void pause()
    {
        super.pause();

        unregisterDBAndListener();

        //TODO: Close server!
    }

    @Override
    public void resume()
    {
        super.resume();

        create();
    }
}

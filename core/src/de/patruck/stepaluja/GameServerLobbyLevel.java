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
    private static Object lock;
    private long threadPointer = 0;
    private static boolean isClientConnecting;
    private String clientIpAddress = null;

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
        String localIpAddress;

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

        //TODO: Start server!

        NativeBridge.registerNewServer(ipAddress + "__" + localIpAddress);
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        if(clientIpAddress == null)
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
                clientIpAddress = NativeBridge.getClientListenerResult();
                unregisterDBAndListener();
                Utils.aassert(isClientConnecting == false);
            }
        }
        else
        {
            Utils.log(clientIpAddress);
            msg = "Client found! Lets connect to it!";
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

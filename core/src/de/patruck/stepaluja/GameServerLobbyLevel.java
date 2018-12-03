package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

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
    private DatagramChannel channel = null;
    private Selector selector = null;
    byte[] bufferBytes;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private float packetInterval = 0.0f;
    public static final float maxPacketInterval = 2.0f;

    public GameServerLobbyLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        msg = "Created a server, because there where no open ones!\nWaiting for players to join...";

        readBuffer = ByteBuffer.allocate(256);
        writeBuffer = ByteBuffer.allocate(256);
        bufferBytes = new byte[256];
    }

    @Override
    public void create()
    {
        super.create();

        showingUpInDB = false;
        isClientConnecting = false;

        //TODO: Think about not getting the address every time, just once on startup!
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

        try
        {
            channel = DatagramChannel.open();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        Utils.aassert(channel != null);
        DatagramSocket socket = channel.socket();
        try
        {
            Utils.aassert(socket != null);
            socket.connect(new InetSocketAddress("google.com", localPort));
        }
        catch(Exception e)
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
        try
        {
            channel.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        try
        {
            channel = DatagramChannel.open();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        Utils.aassert(channel != null);
        socket = channel.socket();
        try
        {
            socket.bind(new InetSocketAddress(localIpAddress, localPort));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        Utils.aassert(socket.isBound());
        try
        {
            channel.configureBlocking(false);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        Utils.aassert(!channel.isBlocking());

        try
        {
            selector = Selector.open();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        Utils.aassert(selector != null);
        try
        {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }

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
            int nToSelect = 0;
            Utils.log("Now its communicating time!");
            try
            {
                nToSelect = selector.selectNow();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Utils.invalidCodePath();
            }

            if(nToSelect > 0)
            {
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();

                for(Iterator<SelectionKey> iterator = selectionKeySet.iterator(); iterator.hasNext(); )
                {
                    SelectionKey it = iterator.next();

                    if(it.isReadable())
                    {
                        //NOTE: The channel really has to be the channel of the class, because we just
                        //registered one!
                        DatagramChannel channel = (DatagramChannel) it.channel();

                        InetSocketAddress address = null;
                        try
                        {
                            address = (InetSocketAddress) channel.receive(readBuffer);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            Utils.invalidCodePath();
                        }
                        Utils.aassert(address != null);

                        Utils.log("Received a packet from:" + address.getHostName());

                        readBuffer.get(bufferBytes);
                    }
                    if(it.isWritable())
                    {
                        //NOTE: The channel really has to be the channel of the class, because we just
                        //registered one!
                        DatagramChannel channel = (DatagramChannel) it.channel();

                        InetAddress inetAddress = null;
                        try
                        {
                            inetAddress = InetAddress.getByName(clientLocalIp);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            Utils.invalidCodePath();
                        }
                        Utils.aassert(inetAddress != null);
                        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, localPort);
                        try
                        {
                            channel.send(writeBuffer, socketAddress);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            Utils.invalidCodePath();
                        }

                        Utils.log("We send a msg!");
                    }

                    iterator.remove();
                }
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
    public void pause()
    {
        super.pause();

        unregisterDBAndListener();

        try
        {
            channel.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Utils.invalidCodePath();
        }
        try
        {
            selector.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
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

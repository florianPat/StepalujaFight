package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import org.ipify.Ipify;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
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
    public static final float maxPacketInterval = 0.75f;

    public GameServerLobbyLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        msg = "Created a server, because there where no open ones!\nWaiting for players to join...";

        readBuffer = ByteBuffer.allocate(256);
        writeBuffer = ByteBuffer.allocate(256);
        bufferBytes = new byte[256];
    }

    public static IpPortAddress getAddressesAndCreateChannelAsWellAsSelector()
    {
        IpPortAddress result = new IpPortAddress();

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
        try
        {
            Utils.aassert(socket != null);
            socket.connect(new InetSocketAddress("google.com", localPort));
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        //result.ipAddress = socket.getInetAddress().getHostAddress();
        result.port = socket.getPort();
        result.localIpAddress = socket.getLocalAddress().getHostAddress();
        result.localPortHere = socket.getLocalPort();
        socket.disconnect();
        socket.close();
        try
        {
            result.channel.close();
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        try
        {
            result.ipAddress = Ipify.getPublicIp(true);
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        Utils.aassert(result.ipAddress != null);

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
        socket = result.channel.socket();
        try
        {
            socket.bind(new InetSocketAddress(result.localIpAddress, localPort));
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }
        Utils.aassert(socket.isBound());
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

    public static float select(Selector selector, ByteBuffer readBuffer, ByteBuffer writeBuffer,
                               byte[] bufferBytes, float packetInterval, String toIp, int toPort, float dt)
    {
        int nToSelect = 0;
        try
        {
            nToSelect = selector.selectNow();
        }
        catch(Exception e)
        {
            Utils.log(e.getMessage());
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
                    readBuffer.clear();

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
                        Utils.log(e.getMessage());
                        Utils.invalidCodePath();
                    }
                    Utils.aassert(address != null);

                    int remaing = readBuffer.remaining();
                    if(remaing > 0)
                    {
                        Utils.log("Received a packet from:" + address.getHostName());

                        readBuffer.get(bufferBytes, 0, remaing);
                        Utils.log(Arrays.toString(bufferBytes));
                    }
                }
                if(it.isWritable())
                {
                    if(packetInterval == 0.0f)
                    {
                        writeBuffer.clear();

                        //NOTE: The channel really has to be the channel of the class, because we just
                        //registered one!
                        DatagramChannel channel = (DatagramChannel) it.channel();

                        InetAddress inetAddress = null;
                        try
                        {
                            inetAddress = InetAddress.getByName(toIp);
                        }
                        catch(Exception e)
                        {
                            Utils.log(e.getMessage());
                            Utils.invalidCodePath();
                        }
                        Utils.aassert(inetAddress != null);
                        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, toPort);

                        writeBuffer.put("HELO".getBytes());
                        writeBuffer.flip();

                        try
                        {
                            channel.send(writeBuffer, socketAddress);
                        }
                        catch(Exception e)
                        {
                            Utils.log(e.getMessage());
                            Utils.invalidCodePath();
                        }

                        Utils.log("We send a msg!");
                    }

                    packetInterval += dt;

                    if(packetInterval >= maxPacketInterval)
                        packetInterval = 0.0f;
                }

                iterator.remove();
            }
        }

        return packetInterval;
    }

    @Override
    public void create()
    {
        super.create();

        showingUpInDB = false;
        isClientConnecting = false;

        //TODO: Think about not getting the address every time, just once on startup!

        IpPortAddress ipPortAddress = getAddressesAndCreateChannelAsWellAsSelector();
        channel = ipPortAddress.channel;
        selector = ipPortAddress.selector;

        NativeBridge.registerNewServer(ipPortAddress.ipAddress + "_" + ipPortAddress.port + "-"
                + ipPortAddress.localIpAddress);
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
            String toIp = clientExternalIp;
            int toPort = localPort;

            packetInterval = select(selector, readBuffer, writeBuffer, bufferBytes, packetInterval, toIp,
                    toPort, dt);
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

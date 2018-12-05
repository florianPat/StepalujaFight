package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class GameClientLobbyLevel extends LoadingLevel
{
    private String realConnAddress;
    private String serverUid;
    private boolean showingUpInDB = false;
    private String clientExternalIp;
    private String clientExternalPort;
    private String clientLocalIp;
    private DatagramChannel channel = null;
    private Selector selector = null;
    byte[] bufferBytes;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
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

        readBuffer = ByteBuffer.allocate(256);
        writeBuffer = ByteBuffer.allocate(256);
        bufferBytes = new byte[256];

        msg = "Server found! Lets connect to it!";
    }

    @Override
    public void create()
    {
        super.create();

        IpPortAddress ipPortAddress = GameServerLobbyLevel.getAddressesAndCreateChannelAsWellAsSelector();
        channel = ipPortAddress.channel;
        selector = ipPortAddress.selector;

        NativeBridge.registerClient(ipPortAddress.ipAddress + "_" + ipPortAddress.port + "-" +
                ipPortAddress.localIpAddress, serverUid);
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
            String toIp = clientExternalIp;
            int toPort = GameServerLobbyLevel.localPort;

            packetInterval = GameServerLobbyLevel.select(selector, readBuffer, writeBuffer, bufferBytes,
                    packetInterval, toIp, toPort, dt);
        }
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

        //TODO: Unregister client here, should we?

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

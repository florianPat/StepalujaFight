package de.patruck.stepaluja;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class NetworkManager
{
    private DatagramChannel channel = null;
    private Selector selector = null;
    private ByteBuffer buffer;
    private byte[] bufferBytesRead;
    private byte[] bufferBytesWrite;
    private Kryo kryo;
    private Output output;
    private Input input;
    private float sendTimer = 0.0f;
    private final float maxSendTimer = 0.5f;

    public NetworkManager(DatagramChannel channelIn, Selector selectorIn)
    {
        channel = channelIn;
        selector = selectorIn;

        buffer = ByteBuffer.allocate(256);
        bufferBytesRead = new byte[256];
        bufferBytesWrite = new byte[256];

        kryo = new Kryo();
        output = new Output(bufferBytesWrite);
        input = new Input(bufferBytesRead);

        //NOTE: Register classes here!
        kryo.register(String.class);
    }

    public void send(Object o)
    {
        kryo.writeClassAndObject(output, o);
    }

    //NOTE: Call after each select!!
    public Object read()
    {
        Utils.aassert(hasNext());
        return kryo.readClassAndObject(input);
    }

    public boolean hasNext()
    {
        boolean result = false;
        try
        {
            result = (input.available() > 0);
        }
        catch(IOException e)
        {
            Utils.log(e.getMessage());
            Utils.invalidCodePath();
        }

        return result;
    }

    public void dispose()
    {
        output.close();
        input.close();
    }

    public void select(InetAddress inetAddress, int toPort, float dt)
    {
        sendTimer += dt;

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

            //NOTE: Change this if we have more then one channel!!
            for(Iterator<SelectionKey> iterator = selectionKeySet.iterator(); iterator.hasNext(); )
            {
                SelectionKey it = iterator.next();

                if(it.isReadable())
                {
                    input.reset();
                    buffer.clear();
                    //NOTE: The channel really has to be the channel of the class, because we just
                    //registered one!
                    DatagramChannel channel = (DatagramChannel) it.channel();

                    Utils.aassert(channel.socket().isBound());
                    Utils.aassert(channel.isConnected());

                    InetSocketAddress address = null;
                    try
                    {
                        address = (InetSocketAddress) channel.receive(buffer);
                    }
                    catch(Exception e)
                    {
                        Utils.log(e.getMessage());
                        Utils.invalidCodePath();
                    }
                    Utils.aassert(address != null);

                    buffer.flip();

                    int remaing = buffer.remaining();
                    if(remaing > 0)
                    {
                        Utils.log("Received a packet from:" + address.getHostName());

                        buffer.get(bufferBytesRead, 0, remaing);
                    }
                }
                if(it.isWritable())
                {
                    if(sendTimer >= maxSendTimer)
                    {
                        sendTimer = 0.0f;

                        if(bufferBytesWrite.length > 0)
                        {
                            buffer.clear();

                            //NOTE: The channel really has to be the channel of the class, because we just
                            //registered one!
                            DatagramChannel channel = (DatagramChannel) it.channel();
                            Utils.aassert(inetAddress != null);
                            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, toPort);

                            buffer.put(bufferBytesWrite);
                            buffer.flip();

                            Utils.aassert(channel.socket().isBound());
                            Utils.aassert(channel.isConnected());

                            try
                            {
                                channel.send(buffer, socketAddress);
                            }
                            catch(Exception e)
                            {
                                Utils.log(e.getMessage());
                                Utils.invalidCodePath();
                            }

                            output.flush();
                            output.reset();

                            Utils.log("We send a msg!");
                        }
                    }
                }

                iterator.remove();
            }
        }
    }
}

package de.patruck.stepaluja;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;

public class NearbyNetworkManager
{
    private NearbyAbstraction nearbyAbstraction;
    private byte[] bufferBytesRead;
    private byte[] bufferBytesWrite;
    private Kryo kryo;
    private Output output;
    private Input input;

    public NearbyNetworkManager(NearbyAbstraction nearbyAbstractionIn)
    {
        nearbyAbstraction = nearbyAbstractionIn;

        bufferBytesRead = new byte[256];
        bufferBytesWrite = new byte[256];

        kryo = new Kryo();
        output = new Output(bufferBytesWrite);
        input = new Input(bufferBytesRead);

        //NOTE: Register classes here!
        kryo.register(String.class);
    }

    public void write(Object o)
    {
        kryo.writeClassAndObject(output, o);
    }

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

    public void send()
    {
        Utils.aassert(nearbyAbstraction.connectedFlag == 1);
        nearbyAbstraction.send(bufferBytesWrite);
        output.flush();
    }

    public byte[] getBufferBytesRead()
    {
        return bufferBytesRead;
    }
}

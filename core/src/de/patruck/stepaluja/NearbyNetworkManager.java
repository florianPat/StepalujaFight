package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NearbyNetworkManager
{
    private NearbyAbstraction nearbyAbstraction;
    private ByteBuffer buffer;
    private byte[] bufferBytesRead;
    private byte[] bufferBytesWrite;
    private Kryo kryo;
    private Output output;
    private Input input;

    public NearbyNetworkManager(NearbyAbstraction nearbyAbstractionIn)
    {
        nearbyAbstraction = nearbyAbstractionIn;

        buffer = ByteBuffer.allocate(256);
        bufferBytesRead = new byte[256];
        bufferBytesWrite = new byte[256];

        kryo = new Kryo();
        output = new Output(bufferBytesWrite);
        input = new Input(bufferBytesRead);

        //NOTE: Register classes here!
        kryo.register(Vector2.class);
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

        if(!result)
        {
            input.reset();
        }

        return result;
    }

    public void send()
    {
        Utils.aassert(nearbyAbstraction.connectedFlag == 1);
        nearbyAbstraction.send(bufferBytesWrite);
        output.flush();
        output.reset();
    }

    public void update()
    {
        nearbyAbstraction.receive(buffer);

        buffer.flip();
        int remaing = buffer.remaining();
        if(remaing > 0)
        {
            Utils.log("Received a packet from");

            buffer.get(bufferBytesRead, 0, remaing);
        }
        buffer.flip();

        input.reset();
        buffer.clear();
    }
}

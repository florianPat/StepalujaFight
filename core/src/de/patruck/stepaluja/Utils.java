package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils
{
    private static final AtomicInteger counter = new AtomicInteger();

    private static final boolean debug = true;

    public static void aassert(boolean exp)
    {
        if(debug && (!exp))
        {
            throw new AssertionError();
        }
    }

    public static void log(String msg) { Gdx.app.log("UtilsLog", msg); }

    public static void logBreak(String msg, GameStart screenManager, Vector2 worldSize)
    {
        screenManager.setScreen(new ErrorLevel(screenManager, worldSize, msg));
    }

    public static BitmapFont getFont(int fontSize)
    {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/framd.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.color = Color.WHITE;
        parameter.borderColor = Color.DARK_GRAY;
        BitmapFont result = generator.generateFont(parameter);
        generator.dispose();
        return result;
    }

    public static void invalidCodePath() { aassert(false); }

    public static long Kilobyte(long x) {
        return x * 1024l;
    }

    public static long Megabyte(long x) {
        return x * Kilobyte(x) * 1024l;
    }

    public static long Gigabyte(long x) {
        return x * Megabyte(x) * 1024l;
    }

    public static int getGUID() {
        return counter.getAndIncrement();
    }

    public static DelegateFunction getDelegateFromFunction(Function function)
    {
        DelegateFunction result = new DelegateFunction(getGUID(), function);
        return result;
    }

    public static Preferences getGlobalPreferences()
    {
        return Gdx.app.getPreferences("preferences");
    }

    private static boolean networkConnection()
    {
        boolean result = true;

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
        catch(Exception e)
        {
            result = false;
        }
        socket.disconnect();
        socket.close();

        return result;
    }

    public static void checkNetworkConnection(GameStart screenManager, Vector2 worldSize)
    {
        if(!networkConnection())
        {
            Utils.logBreak("No Network connection!", screenManager, worldSize);
        }
    }
}


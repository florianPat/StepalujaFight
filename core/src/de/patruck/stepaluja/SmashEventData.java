package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class SmashEventData extends EventData implements Serializable
{
    public static int eventId = Utils.getGUID();
    private int playerId = 0;
    private Vector2 smashHitDir;

    //NOTE: Only for Kryo!
    public SmashEventData()
    {
        super(eventId);
    }

    public SmashEventData(int playerIdIn, Vector2 smashHitDirIn)
    {
        super(eventId);
        playerId = playerIdIn;
        smashHitDir = smashHitDirIn;
    }
    
    public int getPlayerId()
    {
        return playerId;
    }
    
    public Vector2 getSmashHitDir()
    {
        return smashHitDir;
    }
}

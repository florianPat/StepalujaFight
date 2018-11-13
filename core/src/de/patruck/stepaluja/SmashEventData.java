package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

public class SmashEventData extends EventData
{
    public static int eventId = Utils.getGUID();
    private int playerId;
    private Vector2 smashHitDir;
    
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

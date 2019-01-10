package de.patruck.stepaluja;

import java.io.Serializable;

public class DeadEventData extends EventData implements Serializable
{
    public static int eventId = Utils.getGUID();
    private boolean localDead = false;

    //NOTE: Only for kryo!
    public DeadEventData()
    {
        super(eventId);
    }

    public DeadEventData(boolean localDeadIn)
    {
        super(eventId);
        localDead = localDeadIn;
    }

    public boolean isPlayerLocal()
    {
        return localDead;
    }
}

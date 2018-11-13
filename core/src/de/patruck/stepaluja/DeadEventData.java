package de.patruck.stepaluja;

public class DeadEventData extends EventData
{
    public static int eventId = Utils.getGUID();
    private int playerId;

    public DeadEventData(int playerIdIn)
    {
        super(eventId);
        playerId = playerIdIn;
    }

    public int getPlayerId()
    {
        return playerId;
    }
}

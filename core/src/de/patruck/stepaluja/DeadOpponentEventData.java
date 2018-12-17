package de.patruck.stepaluja;

public class DeadOpponentEventData extends EventData
{
    public static int eventId = Utils.getGUID();

    public DeadOpponentEventData()
    {
        super(eventId);
    }
}

package de.patruck.stepaluja;

public abstract class EventData
{
    protected final int eventId;

    public EventData(int eventId)
    {
        this.eventId = eventId;
    }

    public final int getEventId()
    {
        return eventId;
    }
}
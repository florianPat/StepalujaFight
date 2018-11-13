package de.patruck.stepaluja;

public class EventTest extends EventData {

    public static int eventId = Utils.getGUID();
    private String test = "Test";

    public EventTest() {
        super(eventId);
    }

    public final String getTest()
    {
        return test;
    }
}

package de.patruck.stepaluja;

public class GameClientNearbyLevel extends LoadingLevel
{
    private char playerId;
    private String level;
    private NearbyNetworkManager networkManager;
    private float currentTime = 0.0f;
    private float connectionTimeout = 6.0f;

    public GameClientNearbyLevel(GameStart screenManager, char playerId, String level,
                                 NearbyNetworkManager networkManager)
    {
        super(screenManager);

        this.playerId = playerId;
        this.level = level;
        this.networkManager = networkManager;
        msg = "Trying to connect to server...";
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        currentTime += dt;
        if(currentTime > connectionTimeout)
        {
            screenManager.setScreen(new MsgInfoLevel(screenManager, "Connection timeout!"));
            screenManager.nearbyAbstraction.disconnectFromAllEndpoints();
        }

        if(screenManager.nearbyAbstraction.connectedFlag != 0)
        {
            if(screenManager.nearbyAbstraction.connectedFlag == 1)
            {
                screenManager.setScreen(new TestLevel(screenManager, playerId, playerId,
                        level, networkManager, false));
            }
            else if(screenManager.nearbyAbstraction.connectedFlag == -1)
            {
                screenManager.setScreen(new MsgInfoLevel(screenManager, "The server is already connected to another player!"));
            }
            else if(screenManager.nearbyAbstraction.connectedFlag == -2)
            {
                Utils.logBreak("Connection error!", screenManager);
            }
        }
    }
}

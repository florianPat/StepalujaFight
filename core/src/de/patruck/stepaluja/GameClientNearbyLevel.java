package de.patruck.stepaluja;

public class GameClientNearbyLevel extends LoadingLevel
{
    char playerId;
    String level;
    NearbyNetworkManager networkManager;

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

        if(screenManager.nearbyAbstraction.connectedFlag != 0)
        {
            if(screenManager.nearbyAbstraction.connectedFlag == 1)
            {
                screenManager.setScreen(new TestLevel(screenManager, playerId, playerId,
                        level, networkManager, false));
            }
            else
            {
                Utils.aassert(screenManager.nearbyAbstraction.connectedFlag == -1);
                Utils.logBreak("Connect error!", screenManager);
            }
        }
    }
}

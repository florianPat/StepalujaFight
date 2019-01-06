package de.patruck.stepaluja;

public class GameServerNearbyLevel extends LoadingLevel
{
    char playerId;
    char level;
    NearbyNetworkManager networkManager;

    public GameServerNearbyLevel(GameStart screenManager, char playerId, char level)
    {
        super(screenManager);

        this.playerId = playerId;
        this.level = level;
        this.networkManager = new NearbyNetworkManager(screenManager.nearbyAbstraction);

        msg = "Created server!\nWaiting for players to join...";
    }

    @Override
    public void resume()
    {
        super.resume();

        screenManager.nearbyAbstraction.startAdvertising("Username", level);
    }

    @Override
    public void hide()
    {
        super.hide();

        screenManager.nearbyAbstraction.stopAdvertising();
    }

    @Override
    public void pause()
    {
        super.pause();

        screenManager.nearbyAbstraction.stopAdvertising();
    }

    @Override
    public void create()
    {
        super.create();

        screenManager.nearbyAbstraction.startAdvertising("Username", level);
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
                        LevelSelectMenuComponent.getLevelName(level), networkManager, true));
            }
            else
            {
                Utils.aassert(screenManager.nearbyAbstraction.connectedFlag == -2);
                Utils.logBreak("Connect error!", screenManager);
            }
        }
    }
}

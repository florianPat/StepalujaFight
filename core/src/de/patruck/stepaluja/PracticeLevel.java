package de.patruck.stepaluja;

public class PracticeLevel extends TileMapLevel
{
    private final int opponents = 4;
    private Function deadFlaggedFunction;
    private HeartComponent hc;
    private char playerNumber;

    public PracticeLevel(GameStart screenManager, char player)
    {
        super("maps/map2.txt", screenManager);

        playerNumber = player;
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        hc.viewport.update(width, height, true);
        hc.recalculateHeartPos();
    }

    private void createPlayer(String playerName, int n)
    {
        String[] textureAtlas = new String[11];
        for(int i = 0; i < textureAtlas.length; ++i)
        {
            textureAtlas[i] = playerName + "/" + (i + 1) + ".png";
        }

        Actor actor = gom.addActor();
        hc = new HeartComponent(assetManager, spriteBatch, n);
        actor.addComponent(new PlayerComponent(eventManager, assetManager, spriteBatch, physics,
                actor, textureAtlas, n, onScreenControls.input, camera,
                map.getWidth(), map.getHeight(), hc, 'P'));
    }

    private void createOpponent(int n, String[] textureAtlasOpponent)
    {
        Actor actor = gom.addActor();
        actor.addComponent(new OpponentComponent(eventManager, assetManager, spriteBatch, physics,
                actor, textureAtlasOpponent, n, map.getWidth(), map.getHeight()));
    }

    @Override
    public void create()
    {
        super.create();

        char opponentNumber = playerNumber == '0' ? '1' : '0';
        String[] textureAtlasOpponent = new String[11];
        String opponentName = "player" + opponentNumber;
        for(int i = 0; i < textureAtlasOpponent.length; ++i)
        {
            textureAtlasOpponent[i] = opponentName + "/" + (i + 1) + ".png";
        }

        for(int i = 1; i <= opponents; ++i)
        {
            createOpponent(i, textureAtlasOpponent);
        }

        createPlayer("player" + playerNumber, 0);

        deadFlaggedFunction = new Function()
        {
            @Override
            public void Event(EventData eventData)
            {
                Utils.aassert(eventData instanceof DeadEventData);
                DeadEventData event = (DeadEventData) eventData;

                int playerId = event.getPlayerId();

                Utils.log("Player " + playerId + " is dead!");
                // screenManager.setScreen(new GameOverScreen(playerId));
                screenManager.setScreen(new MenuLevel(screenManager,
                        MenuLevel.LevelComponentName.MainMenu));
            }
        };
        eventManager.addListener(DeadEventData.eventId, Utils.getDelegateFromFunction(deadFlaggedFunction));
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        hc.draw();
    }
}

package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

public class TestLevel extends TileMapLevel
{
    private final int playerCount = 2;
    private Function deadFlaggedFunction;
    private HeartComponent[] heartComponents;

    public TestLevel(GameStart screenManager, Vector2 worldSize)
    {
        super("maps/map2.txt", screenManager, worldSize);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        for(HeartComponent hc : heartComponents)
        {
            hc.viewport.update(width, height, true);
            hc.recalculateHeartPos();
        }
    }

    private void createPlayer(String playerName, int n)
    {
        String[] textureAtlas = new String[9];
        for(int i = 0; i < textureAtlas.length; ++i)
        {
            textureAtlas[i] = playerName + "/" + (i+1) + ".png";
        }
        Actor actor = gom.addActor();
        heartComponents[n] = new HeartComponent(assetManager, spriteBatch, n);
        actor.addComponent(new PlayerComponent(eventManager, assetManager, spriteBatch, physics,
                actor, textureAtlas, n, worldSize.x, worldSize.y, onScreenControls.input, camera,
                map.getWidth(), map.getHeight(), heartComponents[n]));
    }

    @Override
    public void create()
    {
        super.create();

        heartComponents = new HeartComponent[playerCount];

        for(int i = 0; i < playerCount; ++i)
        {
            createPlayer("player1", i);
        }

        deadFlaggedFunction = new Function() {
            @Override
            public void Event(EventData eventData) {
                Utils.aassert(eventData instanceof DeadEventData);
                DeadEventData event = (DeadEventData) eventData;

                int playerId = event.getPlayerId();

                Utils.log("Player " + playerId + " is dead!");
                // screenManager.setScreen(new GameOverScreen(playerId));
            }
        };

        eventManager.addListener(DeadEventData.eventId, Utils.getDelegateFromFunction(deadFlaggedFunction));
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        for(HeartComponent hc : heartComponents)
        {
            hc.draw();
        }
    }
}

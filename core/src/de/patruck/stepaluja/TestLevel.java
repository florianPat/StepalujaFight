package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

public class TestLevel extends TileMapLevel
{
    private final int playerCount = 2;
    private Function deadFlaggedFunction;

    public TestLevel(GameStart screenManager, Vector2 worldSize)
    {
        super("maps/map2.txt", screenManager, worldSize);
    }

    private void createPlayer(String playerName, int n)
    {
        String[] textureAtlas = new String[9];
        for(int i = 0; i < textureAtlas.length; ++i)
        {
            textureAtlas[i] = playerName + "/" + (i+1) + ".png";
        }
        Actor actor = gom.addActor();
        actor.addComponent(new PlayerComponent(eventManager, assetManager, spriteBatch, physics,
                actor, textureAtlas, n, worldSize.x, worldSize.y, onScreenControls.input, camera,
                map.getWidth(), map.getHeight()));
    }

    @Override
    public void create()
    {
        super.create();
        
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
}

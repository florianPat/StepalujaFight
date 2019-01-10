package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TestLevel extends TileMapLevel
{
    private final int playerCount = 2;
    private Function deadFlaggedFunction;
    private HeartComponent[] heartComponents;
    private char[] playerNumbers;
    private NearbyNetworkManager networkManager;
    private float sendTimer = 0.0f;
    private final float maxSendTimer = 0.2f;
    private PlayerComponent localPlayer;
    private RemotePlayerComponent remotePlayer;
    private boolean isServer;
    private boolean shouldEndGame = false;
    private boolean haveSend = false;
    private boolean localDead;

    public TestLevel(GameStart screenManager, char player0, char player1, String map,
                     NearbyNetworkManager networkManagerIn, boolean isServerIn)
    {
        super(map, screenManager);

        playerNumbers = new char[playerCount];
        playerNumbers[0] = player0;
        playerNumbers[1] = player1;
        networkManager = networkManagerIn;
        isServer = isServerIn;
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
        String[] textureAtlas = new String[11];
        for(int i = 0; i < textureAtlas.length; ++i)
        {
            textureAtlas[i] = playerName + "/" + (i+1) + ".png";
        }
        Actor actor = gom.addActor();
        heartComponents[n] = new HeartComponent(assetManager, spriteBatch, n);

        //TODO: Why is the zooming bad?
        if((isServer && n == 0) || ((!isServer) && n == 1))
        {
            char gameMode = n == 0 ? 'S' : 'C';

            localPlayer = new PlayerComponent(eventManager, assetManager, spriteBatch, physics,
                    actor, textureAtlas, n, onScreenControls.input, camera,
                    map.getWidth(), map.getHeight(), heartComponents[n], gameMode);
            actor.addComponent(localPlayer);
        }
        else
        {
            char gameMode = n == 0 ? 'C' : 'S';

            remotePlayer = new RemotePlayerComponent(eventManager, assetManager, spriteBatch, physics,
                    actor, textureAtlas, n, camera,
                    map.getWidth(), map.getHeight(), heartComponents[n], gameMode, maxSendTimer);
            actor.addComponent(remotePlayer);
        }
    }

    @Override
    public void create()
    {
        super.create();

        heartComponents = new HeartComponent[playerCount];

        for(int i = 0; i < playerCount; ++i)
        {
            createPlayer("player" + playerNumbers[i], i);
        }

        deadFlaggedFunction = new Function() {
            @Override
            public void Event(EventData eventData) {
                Utils.aassert(eventData instanceof DeadEventData);
                DeadEventData event = (DeadEventData) eventData;

                localDead = event.isPlayerLocal();

                Utils.log(localDead ? "We are dead" : "Remote is dead");
                shouldEndGame = true;
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

        networkManager.update();

        while(networkManager.hasNext())
        {
            Object o = networkManager.read();
            if(o != null)
            {
                if(o instanceof Vector2)
                {
                    Vector2 s = (Vector2) o;
                    Utils.log("We got back a Vec2:" + s.toString());
                    remotePlayer.setNewPos(s);
                }
                else if(o instanceof SmashEventData)
                {
                    SmashEventData smashEventData = (SmashEventData) o;
                    Utils.log("We got back a SmashEventData");
                    eventManager.TriggerEvent(smashEventData);
                }
                else if(o instanceof Vector3)
                {
                    Utils.log("We got back a live less");
                    remotePlayer.setLoseLive();
                }
                else if(o instanceof DeadEventData)
                {
                    Utils.log("We got back a DeadEventData");
                    DeadEventData eventData = (DeadEventData) o;
                    screenManager.nearbyAbstraction.disconnectFromAllEndpoints();
                    screenManager.setScreen(new GameOverScreen(screenManager, eventData.isPlayerLocal()));
                    return;
                }
                else
                {
                    Utils.log("Unexpected reading of class: " + o.getClass().toString() + " ;;; "
                            + o.toString());
                    //Utils.invalidCodePath();
                }
            }
        }

        if(shouldEndGame && !haveSend)
        {
            networkManager.write(new DeadEventData(!localDead));

            if(screenManager.nearbyAbstraction.connectedFlag != 1)
            {
                screenManager.setScreen(new MsgInfoLevel(screenManager, "Connection lost!"));
                return;
            }
            networkManager.send();
            haveSend = true;
            return;
        }

        SmashEventData eventDataToSend = localPlayer.getToSendEvent();
        sendTimer += dt;
        if(localPlayer.shouldLiveLess())
        {
            Utils.log("Should live less");
            networkManager.write(new Vector3());
            sendTimer = maxSendTimer + 1.0f;
        }
        if(eventDataToSend != null)
        {
            networkManager.write(eventDataToSend);
            sendTimer = maxSendTimer + 1.0f;
        }
        if(sendTimer > maxSendTimer)
        {
            if(screenManager.nearbyAbstraction.connectedFlag != 1)
            {
                if(haveSend)
                {
                    screenManager.setScreen(new GameOverScreen(screenManager, localDead));
                    return;
                }
                else
                {
                    screenManager.setScreen(new MsgInfoLevel(screenManager, "Connection lost!"));
                    return;
                }
            }
            sendTimer = 0.0f;
            networkManager.write(localPlayer.getPos());
            networkManager.send();
        }
    }
}

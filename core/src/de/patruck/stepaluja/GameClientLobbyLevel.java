package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

public class GameClientLobbyLevel extends LoadingLevel
{
    private String realConnAddress;
    private String serverUid;
    private boolean showingUpInDB = false;

    @Override
    public void render(float dt)
    {
        super.render(dt);

        if(!showingUpInDB)
        {
            switch(NativeBridge.registerClientResult(serverUid))
            {
                case -1:
                {
                    Utils.logBreak(NativeBridge.errorMsg, screenManager, worldSize);
                    break;
                }
                case 0:
                {
                    return;
                }
                case 1:
                {
                    Utils.log("Client showing up in DB!");
                    showingUpInDB = true;
                    break;
                }
                default:
                {
                    Utils.invalidCodePath();
                    break;
                }
            }
        }
    }

    public GameClientLobbyLevel(String connAddress, GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        int indexOfFirstPipe = connAddress.indexOf('|');
        realConnAddress = connAddress.substring(0, indexOfFirstPipe);
        serverUid = connAddress.substring(indexOfFirstPipe + 3);

        msg = "Server found! Lets connect to it!";
    }

    @Override
    public void create()
    {
        super.create();

        NativeBridge.registerClient(realConnAddress, serverUid);
    }
}

package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;
import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.exceptions.NNCantStartServer;
import com.jmr.wrapper.common.listener.SocketListener;
import com.jmr.wrapper.server.Server;
import org.ipify.Ipify;

import java.io.IOException;

public class GameServerLobbyLevel extends LoadingLevel
{
    private class ServerListener implements SocketListener
    {
        @Override
        public void received(Connection con, Object object)
        {
            Utils.log("Received: " + object);
        }

        @Override
        public void connected(Connection con)
        {
            Utils.log("New client connected.");
        }

        @Override
        public void disconnected(Connection con)
        {
            Utils.log("Client has disconnected.");
        }
    }

    private Server server;
    private boolean showingUpInDB = false;

    public GameServerLobbyLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        msg = "Created a server, because there where no open ones!\nWaiting for players to join...";
    }

    @Override
    public void create()
    {
        super.create();

        try {
            server = new Server(4395, 4395);
            server.setListener(new ServerListener());

            if (server.isConnected())
            {
                Utils.log("Server started sucessfully!");
            }
        }
        catch (NNCantStartServer e)
        {
            Utils.log(e.getMessage());
        }

        try
        {
            NativeBridge.registerNewServer(Ipify.getPublicIp(true));
        }
        catch (IOException e)
        {
            Utils.log(e.getMessage());
        }
    }

    @Override
    public void render(float dt) {
        super.render(dt);

        if(!showingUpInDB)
        {
            switch (NativeBridge.resultRegisterNewServer())
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
                    showingUpInDB = true;
                    Utils.log("Showing up in DB!");
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

    @Override
    public void pause() {
        super.pause();

        if(showingUpInDB)
        {
            NativeBridge.unregisterServer();

            while(showingUpInDB)
            {
                switch (NativeBridge.resultUnregisterServer())
                {
                    case -1:
                    {
                        Utils.logBreak(NativeBridge.errorMsg, screenManager, worldSize);
                        break;
                    }
                    case 0:
                    {
                        break;
                    }
                    case 1:
                    {
                        showingUpInDB = false;
                        Utils.log("Not showing up in DB!");
                        break;
                    }
                    default:
                    {
                        Utils.invalidCodePath();
                        break;
                    }
                }
            }

            server.close();
        }
    }

    @Override
    public void resume() {
        super.resume();

        showingUpInDB = false;
        create();
    }
}

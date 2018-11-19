package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;
import com.jmr.wrapper.client.Client;
import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;

public class GameClientLobbyLevel extends LoadingLevel
{
    private class ClientListener implements SocketListener {

        @Override
        public void received(Connection con, Object object) {
            Utils.log("Received: " + object);
        }

        @Override
        public void connected(Connection con) {
            Utils.log("Connected to the server.");
        }

        @Override
        public void disconnected(Connection con) {
            Utils.log("Disconnected the server.");
        }

    }

    private String connAddress;
    private Client client;

    @Override
    public void render(float dt) {
        super.render(dt);
    }

    public GameClientLobbyLevel(String connAddressIn, GameStart screenManager, Vector2 worldSize) {
        super(screenManager, worldSize);

        connAddress = connAddressIn;

        msg = "Server found! Lets connect to it!";
    }

    @Override
    public void create() {
        super.create();

        client = new Client(connAddress, 4395, 4395);
        client.setListener(new ClientListener());
        client.connect();
        if (client.isConnected()) {
            System.out.println("Connected to the server.");
        }
    }
}

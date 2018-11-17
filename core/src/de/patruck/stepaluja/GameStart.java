package de.patruck.stepaluja;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

public class GameStart extends Game
{
    private Vector2 worldSize;
    private boolean googlePlayServicesAvailable;

    public GameStart(Vector2 worldSize, boolean servicesAvailable)
    {
        this.worldSize = worldSize;
        googlePlayServicesAvailable = servicesAvailable;
    }

    public void startGame()
    {
        NativeBridge.firebaseInit();

        Preferences prefs = Utils.getGlobalPreferences();
        if(!prefs.contains("username"))
        {
            Utils.log("create new user!");
            setScreen(new SignUpLevel(this, worldSize));
        }
        else
        {
            Utils.log("sign in");
            setScreen(new LogInLevel(this, worldSize));
        }
    }

    @Override
    public void create()
    {
        //NOTE: Set first level!
        if(!googlePlayServicesAvailable)
        {
            setScreen(new ServicesAvaliableChecker(this, worldSize));
        }
        else
        {
            startGame();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if(screen != null)
        {
            screen.dispose();
        }
    }
}

package de.patruck.stepaluja;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Preferences;

public class GameStart extends Game
{
    private boolean googlePlayServicesAvailable;

    public GameStart(boolean servicesAvailable)
    {
        googlePlayServicesAvailable = servicesAvailable;
    }

    public void startGame()
    {
        NativeBridge.firebaseInit();

        Preferences prefs = Utils.getGlobalPreferences();
        if(!prefs.contains("username"))
        {
            Utils.log("create new user!");
            setScreen(new SignUpAnonymouslyLevel(this));
        }
        else
        {
            Utils.log("sign in");
            setScreen(new LogInLevel(this));
        }
    }

    @Override
    public void create()
    {
        //NOTE: Set first level!
        if(!Utils.checkNetworkConnection(this)) return;

        if(!googlePlayServicesAvailable)
        {
            setScreen(new ServicesAvaliableChecker(this));
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

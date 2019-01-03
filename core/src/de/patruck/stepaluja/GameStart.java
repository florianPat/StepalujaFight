package de.patruck.stepaluja;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Preferences;

public class GameStart extends Game
{
    private boolean googlePlayServicesAvailable;
    private boolean hasInternetAccess;
    private boolean hasNeabryPermission = false;
    private PermissionQuery permissionQuery = null;
    public NearbyAbstraction nearbyAbstraction = null;

    public GameStart(boolean servicesAvailable)
    {
        googlePlayServicesAvailable = servicesAvailable;
    }

    public GameStart(boolean servicesAvailable, boolean nearbyAvailable, PermissionQuery query,
                     NearbyAbstraction nearbyAbs)
    {
        googlePlayServicesAvailable = servicesAvailable;
        hasNeabryPermission = nearbyAvailable;
        permissionQuery = query;
        nearbyAbstraction = nearbyAbs;
    }

    public void startGame()
    {
        Preferences prefs = Utils.getGlobalPreferences();
        if(!prefs.contains("highscore"))
        {
            prefs.putInteger("highscore", -1);
            prefs.flush();
        }

        if(hasInternetAccess)
        {
            NativeBridge.firebaseInit();

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
        else
        {
            Utils.log("singeplayer!");
            setScreen(new MenuLevel(this, MenuLevel.LevelComponentName.MainMenu));
        }
    }

    @Override
    public void create()
    {
        hasInternetAccess = Utils.networkConnection();

        //NOTE: Set first level!
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

    public boolean hasInternetAccess()
    {
        return hasInternetAccess;
    }

    public boolean hasNeabryPermission()
    {
        if(hasNeabryPermission)
            return true;
        else if(permissionQuery != null)
            return permissionQuery.isPermissonGranted();
        else
        {
            Utils.logBreak("I-O-S. What should I say?", this);
            return false;
        }
    }

    public void requestNearbyPermission()
    {
        if(permissionQuery == null)
        {
            Utils.logBreak("I-O-S. What should I say?", this);
            return;
        }

        permissionQuery.requestPermission();
    }

    public void nearbyPermissionResult(boolean result)
    {
        hasNeabryPermission = result;
    }
}

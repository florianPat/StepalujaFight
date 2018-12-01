package de.patruck.stepaluja;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

public class SignUpAnonymouslyLevel extends LoadingLevel
{
    private String username = "Anonymous";

    public SignUpAnonymouslyLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);
    }

    @Override
    public void create()
    {
        super.create();
        NativeBridge.userSignUpAnonymously();
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        int result = NativeBridge.resultSignUpAnonymously();
        switch(result)
        {
            case 0:
            {
                return;
            }
            case 1:
            {
                Preferences preferences = Utils.getGlobalPreferences();
                preferences.putString("username", username);
                preferences.flush();

                screenManager.setScreen(new MenuLevel("menu/Titelbild.jpg", screenManager,
                        worldSize, MenuLevel.LevelComponentName.MainMenu));
                break;
            }
            case -1:
            {
                Utils.logBreak("User creation got wrong!", screenManager, worldSize);
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

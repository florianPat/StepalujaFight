package de.patruck.stepaluja;

import com.badlogic.gdx.Preferences;

public class SignUpAnonymouslyLevel extends LoadingLevel
{
    private String username = "Anonymous";

    public SignUpAnonymouslyLevel(GameStart screenManager)
    {
        super(screenManager);
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

                screenManager.setScreen(new MenuLevel(screenManager, MenuLevel.LevelComponentName.MainMenu,
                        true));
                break;
            }
            case -1:
            {
                Utils.logBreak("User creation got wrong!", screenManager);
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

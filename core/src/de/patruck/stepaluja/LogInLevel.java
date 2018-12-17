package de.patruck.stepaluja;

public class LogInLevel extends Level
{
    public LogInLevel(GameStart screenManager)
    {
        super(screenManager);
    }

    @Override
    public void create()
    {}

    @Override
    public void render(float dt)
    {

        if(NativeBridge.existsCurrentUser())
        {
            screenManager.setScreen(new MenuLevel(screenManager, MenuLevel.LevelComponentName.MainMenu,
                    NativeBridge.isCurrentUserAnonymous()));
        }
        else
        {
            Utils.logBreak("Log in failed!", screenManager);
        }
    }
}

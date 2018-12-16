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
        /*switch (NativeBridge.initializationPhase)
        {
            case 0:
            {
                return;
            }
            case 1:
            {
                screenManager.setScreen(new MenuLevel(screenManager, MenuLevel.LevelComponentName.MainMenu));
                break;
            }
            case -1:
            {
                Utils.logBreak("Log in failed!");
                break;
            }
            default:
            {
                Utils.invalidCodePath();
                break;
            }
        }*/
            screenManager.setScreen(new MenuLevel(screenManager, MenuLevel.LevelComponentName.MainMenu));
        }
        else
        {
            Utils.logBreak("Log in failed!", screenManager);
        }
    }
}

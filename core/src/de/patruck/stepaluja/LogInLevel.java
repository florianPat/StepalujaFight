package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

public class LogInLevel extends Level
{
    public LogInLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);
    }

    @Override
    public void create()
    {}

    @Override
    public void render(float dt)
    {
        /*switch (NativeBridge.initializationPhase)
        {
            case 0:
            {
                return;
            }
            case 1:
            {
                screenManager.setScreen(new MenuLevel("menu/Titelbild.jpg", screenManager,
                        worldSize, MenuLevel.LevelComponentName.MainMenu));
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

        if(NativeBridge.existsCurrentUser())
        {
            screenManager.setScreen(new MenuLevel("menu/Titelbild.jpg", screenManager,
                    worldSize, MenuLevel.LevelComponentName.MainMenu));
        }
        else
        {
            Utils.logBreak("Log in failed!", screenManager, worldSize);
        }
    }
}

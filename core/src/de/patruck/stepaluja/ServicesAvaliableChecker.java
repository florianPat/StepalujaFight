package de.patruck.stepaluja;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

public class ServicesAvaliableChecker extends Level
{
    public native static int avaliableCheckerResult();

    private GameStart screenManager;

    public ServicesAvaliableChecker(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);

        this.screenManager = screenManager;
    }

    @Override
    public void create()
    {}

    @Override
    public void render(float dt)
    {
        int result = avaliableCheckerResult();

        switch (result)
        {
            case 0:
            {
                break;
            }
            case 1:
            {
                screenManager.startGame();
                break;
            }
            case 2:
            {
                Utils.logBreak("Google play services not avaliable!", screenManager, worldSize);
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

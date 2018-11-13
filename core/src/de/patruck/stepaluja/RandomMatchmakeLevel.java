package de.patruck.stepaluja;

import com.badlogic.gdx.math.Vector2;

public class RandomMatchmakeLevel extends LoadingLevel
{
    public RandomMatchmakeLevel(GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);
    }

    @Override
    public void create()
    {
        super.create();

        NativeBridge.matchmakeWithRandom();
    }

    @Override
    public void render(float dt)
    {
        super.render(dt);

        String matchmakeWithRandomResult = NativeBridge.resultMatchmakeWithRandom();

        if(matchmakeWithRandomResult.equals("-2"))
        {
            Utils.log("MakeServerLobbyLevel");
            screenManager.setScreen(new GameServerLobbyLevel(screenManager, worldSize));
        }
        else if(matchmakeWithRandomResult.equals("-1"))
        {
            Utils.logBreak(NativeBridge.errorCode != -2 ? NativeBridge.errorMsg : "MatchmakeWithRandom Error!",
                    screenManager, worldSize);
        }
        else if(!matchmakeWithRandomResult.equals("0"))
        {
            Utils.log("MakeClientLevel");
            screenManager.setScreen(new TestLevel(screenManager, worldSize));
            //screenManager.setScreen(new GameClientLevel(matchmakeWithRandomResult, screenManager, worldSize));
        }
    }
}

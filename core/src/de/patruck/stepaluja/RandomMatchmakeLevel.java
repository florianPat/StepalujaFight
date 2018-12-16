package de.patruck.stepaluja;

public class RandomMatchmakeLevel extends LoadingLevel
{
    public RandomMatchmakeLevel(GameStart screenManager)
    {
        super(screenManager);

        msg = "Looking for open servers to join!";
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
            screenManager.setScreen(new GameServerLobbyLevel(screenManager));
        }
        else if(matchmakeWithRandomResult.equals("-1"))
        {
            Utils.logBreak(NativeBridge.errorCode != -2 ? NativeBridge.errorMsg : "MatchmakeWithRandom Error!",
                    screenManager);
        }
        else if(!matchmakeWithRandomResult.equals("0"))
        {
            Utils.log("MakeClientLevel");
            screenManager.setScreen(new GameClientLobbyLevel(matchmakeWithRandomResult, screenManager));
        }
    }
}

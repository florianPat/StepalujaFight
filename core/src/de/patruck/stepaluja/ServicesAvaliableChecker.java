package de.patruck.stepaluja;

public class ServicesAvaliableChecker extends Level
{
    public native static int avaliableCheckerResult();

    private GameStart screenManager;

    public ServicesAvaliableChecker(GameStart screenManager)
    {
        super(screenManager);

        this.screenManager = screenManager;
    }

    @Override
    public void create()
    {}

    @Override
    public void render(float dt)
    {
        int result = avaliableCheckerResult();
        Utils.log("Here we are!");

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
                Utils.logBreak("Google play services not avaliable!", screenManager);
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

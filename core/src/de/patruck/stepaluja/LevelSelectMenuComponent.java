package de.patruck.stepaluja;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class LevelSelectMenuComponent extends MenuBtnsBackComponent
{
    public LevelSelectMenuComponent(ExtendViewport viewport, GameStart screenManager,
                                    Object arg)
    {
        super(viewport, screenManager, arg);

        btns = new Rectangle[3];
    }

    @Override
    public void resetBtns()
    {
        super.resetBtns();

        btns[0] = new Rectangle(30.0f, 60.0f, 40.0f, 50.0f);
        btns[1] = new Rectangle(95.0f, 60.0f, 60.0f, 50.0f);
        btns[2] = new Rectangle(170.0f, 60.0f, 60.0f, 50.0f);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        super.touchUp(screenX, screenY, pointer, button);

        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(btns[0].contains(viewportPosition))
        {
            setNextScreen('E');
        }
        else if(btns[1].contains(viewportPosition))
        {
            setNextScreen('M');
        }
        else if(btns[2].contains(viewportPosition))
        {
            setNextScreen('H');
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }

    private void setNextScreen(char level)
    {
        Utils.aassert(componentArg instanceof String);
        String s = (String) componentArg;
        Utils.aassert(s.length() == 1);

        if(s.charAt(0) != 'S')
            screenManager.setScreen(new MenuLevel(screenManager,
                MenuLevel.LevelComponentName.ChooseCharacterMenu, s + level));
        else
        {
            //TODO: Server created!
            Utils.log("server created!");
        }
    }
}
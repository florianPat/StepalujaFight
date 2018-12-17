package de.patruck.stepaluja;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class PlayMenuComponent extends MenuBtnsBackComponent
{
    public PlayMenuComponent(ExtendViewport viewport, GameStart screenManager)
    {
        super(viewport, screenManager);

        btns = new Rectangle[2];
    }

    @Override
    public void resetBtns()
    {
        super.resetBtns();

        btns[0] = new Rectangle(45.0f, 69.0f, 48.0f, 23.0f);
        btns[1] = new Rectangle(174.0f, 69.0f, 48.0f, 23.0f);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        super.touchUp(screenX, screenY, pointer, button);

        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(btns[0].contains(viewportPosition))
        {
            screenManager.setScreen(new MenuLevel(screenManager,
                    MenuLevel.LevelComponentName.ChooseCharacterMenu, "U"));
        }
        else if(btns[1].contains(viewportPosition))
        {
            if(!screenManager.hasInternetAccess())
                Utils.logBreak("No Network connection!", screenManager);
            else
                screenManager.setScreen(new MenuLevel(screenManager,
                        MenuLevel.LevelComponentName.ChooseCharacterMenu, "O"));
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }
}
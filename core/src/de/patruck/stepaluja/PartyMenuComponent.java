package de.patruck.stepaluja;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class PartyMenuComponent extends MenuBtnsBackComponent
{
    public PartyMenuComponent(ExtendViewport viewport, GameStart screenManager)
    {
        super(viewport, screenManager);

        btns = new Rectangle[1];
    }

    @Override
    public void resetBtns()
    {
        super.resetBtns();

        btns[0] = new Rectangle(43.0f, 45.0f, 40.0f, 40.0f);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        super.touchUp(screenX, screenY, pointer, button);

        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(btns[0].contains(viewportPosition))
        {
            screenManager.setScreen(new MenuLevel(screenManager,
                    MenuLevel.LevelComponentName.ChooseCharacterMenu, "P"));
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }
}
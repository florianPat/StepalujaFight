package de.patruck.stepaluja;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class SettingsMenuComponent extends MenuBtnsBackComponent
{
    public SettingsMenuComponent(ExtendViewport viewport, GameStart screenManager)
    {
        super(viewport, screenManager);

        btns = new Rectangle[2];
    }

    @Override
    public void resetBtns()
    {
        super.resetBtns();

        btns[0] = new Rectangle(140.0f, 101.0f, 25.0f, 20.0f);
        btns[1] = new Rectangle(195.0f, 101.0f, 30.0f, 20.0f);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        super.touchUp(screenX, screenY, pointer, button);

        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(btns[0].contains(viewportPosition))
        {
            //NOTE: Sound on!
        }
        else if(btns[1].contains(viewportPosition))
        {
            //NOTE: Sound off!
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }
}
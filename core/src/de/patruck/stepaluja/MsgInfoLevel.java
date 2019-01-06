package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class MsgInfoLevel extends ErrorLevel
{
    public MsgInfoLevel(GameStart screenManager, String msg)
    {
        super(screenManager, msg);
    }

    @Override
    public void render(float dt)
    {
        showingErrorTime += dt;
        if(showingErrorTime >= 3.0f)
        {
            screenManager.setScreen(new MenuLevel(screenManager, MenuLevel.LevelComponentName.MainMenu));
        }
        else
        {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();
            spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

            spriteBatch.begin();
            font.draw(spriteBatch, errorMsg, 0.0f, viewport.getWorldHeight() / 2);
            spriteBatch.end();
        }
    }
}

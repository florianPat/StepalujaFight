package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class TryRequirePermissionLevel extends ErrorLevel
{
    public TryRequirePermissionLevel(GameStart screenManager)
    {
        super(screenManager, "The following permission is\nrequired for the nearby multiplayer\nto work!");
    }

    @Override
    public void render(float dt)
    {
        showingErrorTime += dt;
        if(showingErrorTime >= 3.0f)
        {
            screenManager.requestNearbyPermission();
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

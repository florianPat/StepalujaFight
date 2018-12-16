package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class ErrorLevel extends Level {

    private BitmapFont font;
    private float showingErrorTime = 0.0f;
    private String errorMsg;

    public ErrorLevel(GameStart screenManager, String errorMsg)
    {
        super(screenManager);

        this.errorMsg = errorMsg;
    }

    @Override
    public void create()
    {
        font = Utils.getFont(36);
    }

    @Override
    public void render(float dt)
    {
        showingErrorTime += dt;
        if(showingErrorTime >= 5.0f)
        {
            Gdx.app.error("UtilsLogBreak", errorMsg);
        }
        else
        {
            Gdx.gl.glClearColor( 0, 0, 0, 1 );
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();
            spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

            spriteBatch.begin();
            font.draw(spriteBatch, errorMsg,0.0f, viewport.getWorldHeight() / 2);
            spriteBatch.end();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        font.dispose();
    }
}

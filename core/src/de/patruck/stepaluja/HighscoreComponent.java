package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class HighscoreComponent
{
    private BitmapFont font;
    public int highscore = 0;
    public ExtendViewport viewport;
    private final int VIEWPORT_SIZE = 400;
    private SpriteBatch spriteBatch;
    private Vector2 pos;

    public HighscoreComponent(SpriteBatch spriteBatchIn)
    {
        viewport = new ExtendViewport(VIEWPORT_SIZE, VIEWPORT_SIZE);
        spriteBatch = spriteBatchIn;
        font = Utils.getFont(16);
        pos = new Vector2();
    }

    public void recalculateHighscorePosition()
    {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float highscoreY = worldHeight - font.getLineHeight() - worldHeight / 64.0f;

        float highscoreWidth = font.getSpaceWidth() * (("Highscore".length() + 30));

        pos.x = (worldWidth - highscoreWidth);
        pos.y = highscoreY;
    }

    public void draw()
    {
        //spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        font.draw(spriteBatch, "Highscore: " + highscore, pos.x, pos.y);

        spriteBatch.end();
    }
}

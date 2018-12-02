package de.patruck.stepaluja;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class HeartComponent
{
    public enum HeartState
    {
        FULL,
        EMPTY
    }

    private Texture[] heartTextures;
    private Sprite[] heartSprites;
    public ExtendViewport viewport;
    private ExtendViewport gameViewport;
    private final int VIEWPORT_SIZE = 400;
    private SpriteBatch spriteBatch;
    private int n;

    public HeartComponent(AssetManager assetManager, SpriteBatch spriteBatchIn, int nIn)
    {
        spriteBatch = spriteBatchIn;
        n = nIn;

        viewport = new ExtendViewport(VIEWPORT_SIZE, VIEWPORT_SIZE);

        assetManager.load("menu/Herz.png", Texture.class);
        assetManager.load("menu/Herz_leer.png", Texture.class);
        assetManager.finishLoading();

        heartTextures = new Texture[2];
        heartTextures[0] = assetManager.get("menu/Herz.png", Texture.class);
        heartTextures[1] = assetManager.get("menu/Herz_leer.png", Texture.class);

        heartSprites = new Sprite[3];
        heartSprites[0] = new Sprite(heartTextures[0]);
        heartSprites[1] = new Sprite(heartTextures[0]);
        heartSprites[2] = new Sprite(heartTextures[0]);
    }

    public void recalculateHeartPos()
    {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float heartY = worldHeight - heartSprites[0].getHeight() - worldHeight / 64.0f;
        float heartXPadding = worldWidth / 64.0f;

        float heartWidth = heartSprites[0].getWidth();

        heartSprites[0].setPosition(n == 0 ? (heartXPadding) : (worldWidth - heartXPadding - heartWidth), heartY);
        heartWidth *= (n == 0 ? 1 : -1);
        heartSprites[1].setPosition(heartSprites[0].getX() + heartWidth, heartY);
        heartSprites[2].setPosition(heartSprites[1].getX() + heartWidth, heartY);
    }

    public void setHeartState(int i, HeartState heartState)
    {
        Utils.aassert(i <= heartSprites.length && i >= 0);
        heartSprites[i].setTexture(heartState == HeartState.FULL ? heartTextures[0] : heartTextures[1]);
    }

    public void draw()
    {
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        for(Sprite sprite : heartSprites)
        {
            sprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }
}

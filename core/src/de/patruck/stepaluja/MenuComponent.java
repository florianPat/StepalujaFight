package de.patruck.stepaluja;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

abstract class MenuComponent extends InputAdapter
{
    protected ExtendViewport viewport;
    protected ShapeRenderer renderer;
    protected Vector2 worldSize;
    protected Vector2 imgSize;
    protected GameStart screenManager;
    protected SpriteBatch spriteBatch;

    public MenuComponent(ExtendViewport viewport, Vector2 worldSize, Vector2 imgSize,
                         GameStart screenManager, SpriteBatch spriteBatch)
    {
        this.viewport = viewport;
        this.worldSize = worldSize;
        this.imgSize = imgSize;
        renderer = new ShapeRenderer();
        this.screenManager = screenManager;
        this.spriteBatch = spriteBatch;
    }

    abstract public void recalculateBtnPositions();

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        return super.touchUp(screenX, screenY, pointer, button);
    }

    abstract public void debugRenderBtns();

    public void render()
    {
    }

    public void dispose()
    {
        renderer.dispose();
    }
}
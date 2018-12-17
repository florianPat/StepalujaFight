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
    protected Vector2 imgSize = null;
    protected GameStart screenManager;
    protected SpriteBatch spriteBatch = null;
    protected Object componentArg = null;

    public MenuComponent(ExtendViewport viewport, GameStart screenManager)
    {
        this.viewport = viewport;
        renderer = new ShapeRenderer();
        this.screenManager = screenManager;
    }

    public MenuComponent(ExtendViewport viewport, GameStart screenManager,
                         Object componentArg)
    {
        this(viewport, screenManager);
        this.componentArg = componentArg;
    }

    public MenuComponent(ExtendViewport viewport, GameStart screenManager, SpriteBatch spriteBatch)
    {
        this(viewport, screenManager);
        this.spriteBatch = spriteBatch;
    }

    public MenuComponent(ExtendViewport viewport, GameStart screenManager, SpriteBatch spriteBatch,
                         Object componentArg)
    {
        this(viewport, screenManager);
        this.spriteBatch = spriteBatch;
        this.componentArg = componentArg;
    }

    public void setImgSize(Vector2 imgSizeIn)
    {
        imgSize = imgSizeIn;
    }

    public void recalculateBtnPositions()
    {
        Utils.aassert(imgSize != null);
    }

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
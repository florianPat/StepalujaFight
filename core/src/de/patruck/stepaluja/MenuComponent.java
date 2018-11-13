package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.Rectangle;

abstract class MenuComponent extends InputAdapter
{
    protected ExtendViewport viewport;
    protected ShapeRenderer renderer;
    protected Vector2 worldSize;
    protected Vector2 imgSize;
    protected GameStart screenManager;

    public MenuComponent(ExtendViewport viewport, Vector2 worldSize, Vector2 imgSize,
                         GameStart screenManager)
    {
        this.viewport = viewport;
        this.worldSize = worldSize;
        this.imgSize = imgSize;
        renderer = new ShapeRenderer();
        this.screenManager = screenManager;
    }

    abstract public void recalculateBtnPositions();

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        return super.touchUp(screenX, screenY, pointer, button);
    }

    abstract public void debugRenderBtns();

    public void dispose()
    {
        renderer.dispose();
    }
}
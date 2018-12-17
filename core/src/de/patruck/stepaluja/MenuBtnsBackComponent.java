package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

class MenuBtnsBackComponent extends MenuComponent
{
    protected Rectangle btns[];
    protected Rectangle backBtn;

    public MenuBtnsBackComponent(ExtendViewport viewport, GameStart screenManager)
    {
        super(viewport, screenManager);
    }

    public MenuBtnsBackComponent(ExtendViewport viewport, GameStart screenManager,
                                 Object menuArg)
    {
        super(viewport, screenManager, menuArg);
    }

    public MenuBtnsBackComponent(ExtendViewport viewport, GameStart screenManager,
                                 SpriteBatch spriteBatch)
    {
        super(viewport, screenManager, spriteBatch);
    }

    public void resetBtns()
    {
        backBtn = new Rectangle(0.0f, 0.0f, 40.0f, 15.0f);
    }

    @Override
    public void recalculateBtnPositions()
    {
        super.recalculateBtnPositions();

        resetBtns();

        float scaleX = viewport.getWorldWidth() / imgSize.x;
        float scaleY = viewport.getWorldHeight() / imgSize.y;

        for(Rectangle btn : btns)
        {
            Vector2 origin = new Vector2(btn.getX() + (btn.getWidth() / 2.0f),
                    btn.getY() + (btn.getHeight() / 2.0f));

            Vector2 localSpacePos = new Vector2(-(origin.x - btn.getX()), -(origin.y - btn.getY()));

            btn.setWidth(btn.getWidth() * scaleX);
            btn.setHeight(btn.getHeight() * scaleY);

            localSpacePos.scl(scaleX, scaleY);
            origin.scl(scaleX, scaleY);
            localSpacePos.add(origin);

            btn.setPosition(localSpacePos);
        }

        Vector2 origin = new Vector2(backBtn.getX() + (backBtn.getWidth() / 2.0f),
                backBtn.getY() + (backBtn.getHeight() / 2.0f));

        Vector2 localSpacePos = new Vector2(-(origin.x - backBtn.getX()), -(origin.y - backBtn.getY()));

        backBtn.setWidth(backBtn.getWidth() * scaleX);
        backBtn.setHeight(backBtn.getHeight() * scaleY);

        localSpacePos.scl(scaleX, scaleY);
        origin.scl(scaleX, scaleY);
        localSpacePos.add(origin);

        backBtn.setPosition(localSpacePos);
    }

    @Override
    public void debugRenderBtns()
    {
        renderer.setProjectionMatrix(viewport.getCamera().combined);

        renderer.begin(ShapeRenderer.ShapeType.Line);

        for(Rectangle btn : btns)
        {
            renderer.rect(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight());
        }

        renderer.rect(backBtn.getX(), backBtn.getY(), backBtn.getWidth(), backBtn.getHeight());

        renderer.end();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(backBtn.contains(viewportPosition))
        {
            screenManager.setScreen(new MenuLevel(screenManager,
                    MenuLevel.LevelComponentName.MainMenu));
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }
}
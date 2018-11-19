package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

class CreditsMenuComponent extends MenuComponent
{
    private Rectangle backBtn;

    public CreditsMenuComponent(ExtendViewport viewport, Vector2 worldSize, Vector2 imgSize,
                                GameStart screenManager)
    {
        super(viewport, worldSize, imgSize, screenManager);
    }

    public void resetBtns()
    {
        //TODO: !!
        backBtn = new Rectangle(5.0f, 85.0f, 50.0f, 60.0f);
    }

    @Override
    public void recalculateBtnPositions()
    {
        resetBtns();

        float scaleX = worldSize.x / imgSize.x;
        float scaleY = worldSize.y / imgSize.y;

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

        renderer.rect(backBtn.getX(), backBtn.getY(), backBtn.getWidth(), backBtn.getHeight());

        renderer.end();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(backBtn.contains(viewportPosition))
        {
            screenManager.setScreen(new MenuLevel("menu/Titelbild.jpg", screenManager,
                    worldSize, MenuLevel.LevelComponentName.MainMenu));
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }
}
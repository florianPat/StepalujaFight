package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

class MainMenuComponent extends MenuComponent
{
    private Rectangle btns[] = new Rectangle[4];
    private Rectangle convertAnToPerBtn;
    private boolean isAnonymous;
    private BitmapFont font;

    // private Rectangle playBtn;
    // private Rectangle partyBtn;
    // private Rectangle settingsBtn;
    // private Rectangle creditsBtn;

    public MainMenuComponent(ExtendViewport viewport, Vector2 worldSize, Vector2 imgSize,
                             GameStart screenManager, SpriteBatch spriteBatchIn)
    {
        super(viewport, worldSize, imgSize, screenManager, spriteBatchIn);

        isAnonymous = NativeBridge.isCurrentUserAnonymous();
        font = Utils.getFont();
    }

    public void resetBtns()
    {
        final float btnWidth = 125.0f;
        final float btnHeight = 12.0f;
        final float btnX = 80.0f;

        btns[0] = new Rectangle(btnX, 85.0f, btnWidth, btnHeight);
        btns[1] = new Rectangle(btnX, btns[0].y - btnHeight, btnWidth, btnHeight);
        btns[2] = new Rectangle(btnX, btns[1].y - btnHeight, btnWidth, btnHeight);
        btns[3] = new Rectangle(btnX, btns[2].y - btnHeight, btnWidth, btnHeight);

        if(isAnonymous)
        {
            convertAnToPerBtn = new Rectangle(0.0f, 0.0f, btnWidth, btnHeight);
        }
    }

    @Override
    public void recalculateBtnPositions()
    {
        resetBtns();

        float scaleX = worldSize.x / imgSize.x;
        float scaleY = worldSize.y / imgSize.y;

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

        if(isAnonymous)
        {
            convertAnToPerBtn.setWidth(convertAnToPerBtn.getWidth() * scaleX);
            convertAnToPerBtn.setHeight(convertAnToPerBtn.getHeight() * scaleY);
        }
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

        if(isAnonymous)
        {
            renderer.rect(convertAnToPerBtn.getX(), convertAnToPerBtn.getY(), convertAnToPerBtn.getWidth(),
                    convertAnToPerBtn.getHeight());
        }

        renderer.end();
    }

    @Override
    public void dispose()
    {
        super.dispose();

        font.dispose();
    }

    @Override
    public void render()
    {
        if(isAnonymous)
        {
            font.draw(spriteBatch, "Make a real account here!!", 0.0f, font.getLineHeight());
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(btns[0].contains(viewportPosition))
        {
            //screenManager.setScreen(new TestLevel(screenManager, worldSize));
            screenManager.setScreen(new RandomMatchmakeLevel(screenManager, worldSize));
        }
        else if(btns[1].contains(viewportPosition))
        {
            Utils.log("Currently not implemented! (party)");
        }
        else if(btns[2].contains(viewportPosition))
        {
            Utils.log("Currently not implemented! (settings)");
        }
        else if(btns[3].contains(viewportPosition))
        {
            screenManager.setScreen(new MenuLevel("menu/Mitwirkende.jpg", screenManager,
                    worldSize, MenuLevel.LevelComponentName.CreditsMenu));
        }

        if(isAnonymous)
        {
            if(convertAnToPerBtn.contains(viewportPosition))
            {
                screenManager.setScreen(new SignUpLevel(screenManager, worldSize));
            }
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }
}
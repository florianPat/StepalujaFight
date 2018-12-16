package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class LoadingLevel extends Level
{
    protected Texture menuTex;
    protected Sprite menuSprite;
    protected BitmapFont font;
    protected String msg = "Loading some cute cat photos from a server. Who knows from wich? XD";

    public LoadingLevel(GameStart screenManager)
    {
        super(screenManager);
    }

    @Override
    public void create()
    {
        assetManager.load("menu/Ladebildschirm.jpg", Texture.class);
        assetManager.finishLoading();
        menuTex = assetManager.get("menu/Ladebildschirm.jpg");

        menuSprite = new Sprite(menuTex);

        font = Utils.getFont();
    }

    @Override
    public void render(float dt)
    {
        Gdx.gl.glClearColor( 0, 0, 0, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        menuSprite.draw(spriteBatch);
        font.draw(spriteBatch, msg, 0.0f, viewport.getWorldHeight() * 0.15f);
        spriteBatch.end();
    }

    @Override
    public void dispose()
    {
        super.dispose();

        menuTex.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        menuSprite.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
    }
}

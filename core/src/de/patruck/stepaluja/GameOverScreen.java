package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class GameOverScreen extends Level
{
    private boolean gameOver;
    private Texture texture;
    private Sprite sprite;
    private float timer = 0.0f;

    public GameOverScreen(GameStart screenManager, boolean gameOverIn)
    {
        super(screenManager);

        gameOver = gameOverIn;
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        sprite.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    @Override
    public void render(float dt)
    {
        timer += dt;
        if(timer > 2.0f)
            screenManager.setScreen(new MenuLevel(screenManager, MenuLevel.LevelComponentName.MainMenu));

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        sprite.draw(spriteBatch);

        spriteBatch.end();
    }

    @Override
    public void create()
    {
        String texName;

        if(gameOver)
            texName = "menu/Lose.png";
        else
            texName = "menu/Win.png";

        assetManager.load(texName, Texture.class);
        assetManager.finishLoading();
        texture = assetManager.get(texName);

        sprite = new Sprite(texture);
    }
}

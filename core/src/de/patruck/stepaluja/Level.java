package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public abstract class Level implements Screen
{
    protected OrthographicCamera camera;
    protected ExtendViewport viewport;
    protected SpriteBatch spriteBatch;
    protected GameObjectManager gom;
    protected EventManager eventManager;
    protected AssetManager assetManager;
    protected Physics physics;
    protected GameStart screenManager;
    protected Vector2 worldSize;

    public Level(GameStart screenManager, Vector2 worldSize)
    {
        this.screenManager = screenManager;
        this.worldSize = worldSize;
    }

    public abstract void create();

    @Override
    public void show()
    {
        viewport = new ExtendViewport(worldSize.x, worldSize.y);
        camera = new OrthographicCamera();
        viewport.setCamera(camera);
        spriteBatch = new SpriteBatch();
        gom = new GameObjectManager();
        eventManager = new EventManager();
        physics = new Physics();
        assetManager = new AssetManager();

        create();

        assetManager.finishLoading();
    }

    @Override
    public void render(float dt)
    {
        gom.updateActors(dt);
        eventManager.removeListeners();

        physics.update(dt);

        Gdx.gl.glClearColor( 0, 0, 0, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        gom.drawActors();
        spriteBatch.end();
    }

    @Override
    public void dispose()
    {
        spriteBatch.dispose();
        assetManager.dispose();
        physics.dispose();
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height, true);

        worldSize.x = viewport.getWorldWidth();
        worldSize.y = viewport.getWorldHeight();
    }

    @Override
    public void pause()
    {
    }

    @Override
    public void resume()
    {
    }

    @Override
    public void hide()
    {
        NativeBridge.dispose();
    }
}
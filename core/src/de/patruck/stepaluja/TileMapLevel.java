package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.math.Vector2;

public class TileMapLevel extends Level
{
    protected Tilemap map;
    protected String levelName;
    protected OnScreenControls onScreenControls;

    public TileMapLevel(String levelName, GameStart screenManager, Vector2 worldSize)
    {
        super(screenManager, worldSize);
        this.levelName = levelName;
    }

    @Override
    public void create()
    {
        map = new Tilemap(levelName, assetManager, physics, screenManager, worldSize);

        onScreenControls = new OnScreenControls(assetManager);
        Gdx.input.setInputProcessor(onScreenControls);
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

        map.draw(spriteBatch);
        gom.drawActors();
        spriteBatch.end();

        onScreenControls.render();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        onScreenControls.viewport.update(width, height, true);
        onScreenControls.recalculateButtonPositions();
    }

    @Override
    public void dispose()
    {
        super.dispose();

        onScreenControls.dispose();
    }
}

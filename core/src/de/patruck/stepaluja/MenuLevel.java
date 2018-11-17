package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class MenuLevel extends Level
{
    public enum LevelComponentName
    {
        MainMenu,
        CreditsMenu
    }

    private Texture menuTex;
    private Sprite menuSprite;
    private String menuTexName;
    private Vector2 worldSize;
    private MenuComponent menuComponent;
    private LevelComponentName levelComponentName;

    public MenuLevel(String menuTexName, GameStart screenManager, Vector2 worldSize,
                     LevelComponentName levelComponentName)
    {
        super(screenManager, worldSize);
        this.menuTexName = menuTexName;
        this.worldSize = worldSize;
        this.levelComponentName = levelComponentName;
    }

    @Override
    public void create()
    {
        assetManager.load(menuTexName, Texture.class);
        assetManager.finishLoading();
        menuTex = assetManager.get(menuTexName);

        menuSprite = new Sprite(menuTex);

        switch(levelComponentName)
        {
            case MainMenu:
            {
                menuComponent = new MainMenuComponent(viewport, worldSize,
                        new Vector2(menuSprite.getWidth(), menuSprite.getHeight()), screenManager);
                break;
            }
            case CreditsMenu:
            {
                menuComponent = new CreditsMenuComponent(viewport, worldSize,
                        new Vector2(menuSprite.getWidth(), menuSprite.getHeight()), screenManager);
                break;
            }
            default:
            {
                Utils.invalidCodePath();
            }
        }

        menuSprite.setSize(worldSize.x, worldSize.y);

        Gdx.input.setInputProcessor(menuComponent);
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

        spriteBatch.end();

        menuComponent.debugRenderBtns();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        menuSprite.setSize(width, height);
        menuComponent.recalculateBtnPositions();
    }

    @Override
    public void dispose()
    {
        super.dispose();

        menuTex.dispose();
        menuComponent.dispose();
    }
}

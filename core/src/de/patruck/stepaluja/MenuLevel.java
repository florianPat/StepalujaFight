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
        CreditsMenu,
        SettingsMenu,
        PartyMenu,
        PlayMenu,
        ChooseCharacterMenu,
        LevelSelectMenu
    }

    private Texture menuTex;
    private Sprite menuSprite;
    private String menuTexName;
    private MenuComponent menuComponent;
    private LevelComponentName levelComponentName;
    private Object menuComponentArg = null;

    public MenuLevel(GameStart screenManager, LevelComponentName levelComponentName)
    {
        super(screenManager);
        this.levelComponentName = levelComponentName;
    }

    public MenuLevel(GameStart screenManager, LevelComponentName levelComponentName,
                     Object menuComponentArg)
    {
        this(screenManager, levelComponentName);

        if(menuComponentArg != null)
            this.menuComponentArg = menuComponentArg;
    }

    @Override
    public void create()
    {
        switch(levelComponentName)
        {
            case MainMenu:
            {
                menuTexName = "menu/Titelbild.jpg";
                if(menuComponentArg != null)
                    menuComponent = new MainMenuComponent(viewport, screenManager, spriteBatch, menuComponentArg);
                else
                    menuComponent = new MainMenuComponent(viewport, screenManager, spriteBatch);
                break;
            }
            case CreditsMenu:
            {
                menuTexName = "menu/Mitwirkende.jpg";
                menuComponent = new CreditsMenuComponent(viewport, screenManager);
                break;
            }
            case SettingsMenu:
            {
                menuTexName = "menu/Einstellungen.png";
                menuComponent = new SettingsMenuComponent(viewport, screenManager);
                break;
            }
            case PartyMenu:
            {
                menuTexName = "menu/PartyModus.jpg";
                menuComponent = new PartyMenuComponent(viewport, screenManager);
                break;
            }
            case PlayMenu:
            {
                menuTexName = "menu/Spielen.jpg";
                menuComponent = new PlayMenuComponent(viewport, screenManager, spriteBatch);
                break;
            }
            case ChooseCharacterMenu:
            {
                menuTexName = "menu/SpielfigurenAuswahl.jpg";
                Utils.aassert(menuComponentArg != null);
                menuComponent = new ChooseCharacterMenuComponent(viewport, screenManager,
                        menuComponentArg);
                break;
            }
            case LevelSelectMenu:
            {
                menuTexName = "menu/MapAuswahl.jpg";
                Utils.aassert(menuComponentArg != null);
                menuComponent = new LevelSelectMenuComponent(viewport, screenManager,
                        menuComponentArg);
                break;
            }
            default:
            {
                Utils.invalidCodePath();
                break;
            }
        }

        assetManager.load(menuTexName, Texture.class);
        assetManager.finishLoading();
        menuTex = assetManager.get(menuTexName);

        menuSprite = new Sprite(menuTex);

        menuComponent.setImgSize(new Vector2(menuSprite.getWidth(), menuSprite.getHeight()));

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
        menuComponent.render();

        spriteBatch.end();

        menuComponent.debugRenderBtns();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        menuSprite.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
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

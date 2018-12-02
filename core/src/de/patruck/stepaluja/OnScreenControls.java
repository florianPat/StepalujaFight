package de.patruck.stepaluja;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class OnScreenControls extends InputAdapter
{
    private final int VIEWPORT_SIZE = 400;
    private final int BUTTON_RADIUS = 32;

    public ExtendViewport viewport;
    private Vector2 moveLeftCenter = new Vector2();
    private Vector2 moveRightCenter = new Vector2();
    private Vector2 hitCenter = new Vector2();
    private Vector2 jumpCenter = new Vector2();
    private int moveLeftPointer;
    private int moveRightPointer;
    private int jumpPointer;
    private int hitPointer;
    private SpriteBatch renderer;

    private Texture textures[];
    private Sprite sprites[];

    public class InputSystem
    {
        private boolean hit = false;
        private boolean jump = false;
        private boolean moveLeft = false;
        private boolean moveRight = false;

        public boolean isHitPressed()
        {
            return hit;
        }

        public boolean isJumpPressed()
        {
            return jump;
        }

        public boolean isMoveLeftPressed()
        {
            return moveLeft;
        }

        public boolean isMoveRightPressed()
        {
            return moveRight;
        }
    }

    public InputSystem input;

    public OnScreenControls(AssetManager assetManager)
    {
        input = new InputSystem();
        viewport = new ExtendViewport(VIEWPORT_SIZE, VIEWPORT_SIZE);

        renderer = new SpriteBatch();

        textures = new Texture[4];

        assetManager.load("menu/Pfeil_links.png", Texture.class);
        assetManager.load("menu/Pfeil_oben.png", Texture.class);
        assetManager.load("menu/Pfeil_rechts.png", Texture.class);
        assetManager.load("menu/Faust.png", Texture.class);
        assetManager.finishLoading();

        textures[0] = assetManager.get("menu/Pfeil_links.png", Texture.class);
        textures[1] = assetManager.get("menu/Pfeil_rechts.png", Texture.class);
        textures[2] = assetManager.get("menu/Pfeil_oben.png", Texture.class);
        textures[3] = assetManager.get("menu/Faust.png", Texture.class);

        sprites = new Sprite[4];
        sprites[0] = new Sprite(textures[0]);
        sprites[1] = new Sprite(textures[1]);
        sprites[2] = new Sprite(textures[2]);
        sprites[3] = new Sprite(textures[3]);

        for(Sprite sprite : sprites)
        {
            sprite.setSize(BUTTON_RADIUS * 2.0f, BUTTON_RADIUS * 2.0f);
        }
    }

    public void updateJustTouched()
    {
        input.hit = false;
        input.jump = false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if (viewportPosition.dst(hitCenter) < BUTTON_RADIUS)
        {
            hitPointer = pointer;
            input.hit = true;
        }
        else if (viewportPosition.dst(jumpCenter) < BUTTON_RADIUS)
        {
            jumpPointer = pointer;
            input.jump = true;
        }
        else if (viewportPosition.dst(moveLeftCenter) < BUTTON_RADIUS)
        {
            moveLeftPointer = pointer;
            input.moveLeft = true;
        }
        else if (viewportPosition.dst(moveRightCenter) < BUTTON_RADIUS)
        {
            moveRightPointer = pointer;
            input.moveRight = true;
        }

        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer == hitPointer)
        {
            input.hit = false;
            hitPointer = 0;
        }
        if(pointer == jumpPointer)
        {
            input.jump = false;
            jumpPointer = 0;
        }
        if(pointer == moveLeftPointer)
        {
            input.moveLeft = false;
            moveLeftPointer = 0;
        }
        if(pointer == moveRightPointer)
        {
            input.moveRight = false;
            moveRightPointer = 0;
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if (pointer == moveLeftPointer && viewportPosition.dst(moveRightCenter) < BUTTON_RADIUS)
        {
            input.moveLeft = false;

            input.moveRight = true;

            moveLeftPointer = 0;

            moveRightPointer = pointer;
        }

        if (pointer == moveRightPointer && viewportPosition.dst(moveLeftCenter) < BUTTON_RADIUS)
        {
            input.moveRight = false;
            input.moveLeft = true;
            moveRightPointer = 0;
            moveLeftPointer = pointer;
        }

        return super.touchDragged(screenX, screenY, pointer);
    }

    public void render()
    {
        viewport.apply();
        renderer.setProjectionMatrix(viewport.getCamera().combined);

        renderer.begin();
        sprites[0].draw(renderer);
        sprites[1].draw(renderer);
        sprites[2].draw(renderer);
        sprites[3].draw(renderer);
        renderer.end();
    }

    public void recalculateButtonPositions()
    {
        float yPos = viewport.getWorldHeight() * 1 / 6;
        float xPosPart = viewport.getWorldWidth() * 1 / 4 / 2;

        moveLeftCenter.set(xPosPart, yPos);
        moveRightCenter.set(xPosPart * 2, yPos);
        hitCenter.set(viewport.getWorldWidth() - xPosPart * 2, yPos);
        jumpCenter.set(viewport.getWorldWidth() - xPosPart, yPos);

        sprites[0].setCenter(moveLeftCenter.x, moveLeftCenter.y);
        sprites[1].setCenter(moveRightCenter.x, moveRightCenter.y);
        sprites[2].setCenter(jumpCenter.x, jumpCenter.y);
        sprites[3].setCenter(hitCenter.x, hitCenter.y);
    }

    public void dispose()
    {
        renderer.dispose();
    }
}

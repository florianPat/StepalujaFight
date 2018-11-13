package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private ShapeRenderer renderer;

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

    public OnScreenControls()
    {
        input = new InputSystem();
        viewport = new ExtendViewport(VIEWPORT_SIZE, VIEWPORT_SIZE);
        renderer = new ShapeRenderer();
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode)
        {
            case Input.Keys.DOWN:
            {
                input.hit = true;
                break;
            }
            case Input.Keys.UP:
            {
                input.jump = true;
                break;
            }
            case Input.Keys.LEFT:
            {
                input.moveLeft = true;
                break;
            }
            case Input.Keys.RIGHT:
            {
                input.moveRight = true;
                break;
            }
        }

        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode)
        {
            case Input.Keys.DOWN:
            {
                input.hit = false;
                break;
            }
            case Input.Keys.UP:
            {
                input.jump = false;
                break;
            }
            case Input.Keys.LEFT:
            {
                input.moveLeft = false;
                break;
            }
            case Input.Keys.RIGHT:
            {
                input.moveRight = false;
                break;
            }
        }

        return super.keyUp(keycode);
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

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.circle(moveLeftCenter.x, moveLeftCenter.y, BUTTON_RADIUS);
        renderer.circle(moveRightCenter.x, moveRightCenter.y, BUTTON_RADIUS);
        renderer.circle(hitCenter.x, hitCenter.y, BUTTON_RADIUS);
        renderer.circle(jumpCenter.x, jumpCenter.y, BUTTON_RADIUS);
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
    }

    public void dispose()
    {
        renderer.dispose();
    }
}

package de.patruck.stepaluja;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class RemotePlayerComponent extends AnimationComponent
{
    public static final int id = Utils.getGUID();

    protected float speed = 50.0f;
    private float stateTime = 0.0f;

    private enum WalkState
    {
        LEFT,
        RIGHT
    }

    WalkState walkState = WalkState.RIGHT;

    private enum JumpState
    {
        JUMPING,
        FALLING,
        NONE
    }

    private JumpState jumpState = JumpState.NONE;
    private float jumpTimer = 0.0f;
    private float maxJumpTime = 0.35f;

    private float smashTimer = 0.0f;
    private final float maxSmashTimer = 0.125f;
    private JumpState smashState = JumpState.NONE;
    private short nJumps = 3;

    private Texture textureSmashLeft;
    private Texture textureSmashRight;
    private Texture textureSmash;
    private Rectangle rectSmash;
    private Collider colliderSmash;
    private Body bodySmash;
    private Vector2 size;
    private float yOffset;
    private Vector2 offset;

    private int playerId;
    private Function smashFunction;
    private boolean lockMotion = false;
    private final float maxHitTimer = 1.0f;
    private final float maxLockMotionTimer = maxHitTimer / 2.0f;
    private float hitTimer = 0.0f;
    private Vector2 hitVec = new Vector2();
    private Vector2 hittingVec;
    private boolean getHit = false;
    private final Color hitColor = Color.GOLDENROD;

    private Sprite sprite;

    private final float norHitPoints = 50.0f;
    private float hitPoints = 1.0f;
    private final float norPlusMultHitPoints = 0.5f;

    private final float MIN_CAMERA_ZOOM = 0.35f;
    private final float MAX_CAMERA_ZOOM = 0.65f;

    private OrthographicCamera camera;
    private float camZoom = MAX_CAMERA_ZOOM;
    private float newCamZoom = MIN_CAMERA_ZOOM;
    private float currentProgress = 0.0f;

    private int tilemapWidth, tilemapHeight;

    private int nLives = 3;
    private boolean respawn = false;
    private float maxRespawnTime = 2.0f;

    private HeartComponent heartComponent = null;

    private char gameMode;

    private Vector2 ooldPos;
    private Vector2 nnewPos;
    private float currentPosProgress = 0.0f;

    public RemotePlayerComponent(EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics, Actor owner, String[] textureAtlas,
                                 int n, OrthographicCamera cameraIn, int tilemapWidthIn, int tilemapHeightIn, HeartComponent heartComponentIn,
                                 char gameModeIn)
    {
        super(eventManager, assetManager, spriteBatch, physics, owner, textureAtlas);

        playerId = n;
        gameMode = gameModeIn;

        tilemapWidth = tilemapWidthIn;
        tilemapHeight = tilemapHeightIn;

        camera = cameraIn;

        textureSmashLeft = assetManager.get(textureAtlas[9]);
        textureSmashRight = assetManager.get(textureAtlas[10]);
        textureSmash = textureSmashLeft;

        //create body
        rect = new Rectangle();
        collider = new Collider(rect);
        ArrayList<String> s = new ArrayList<String>();
        s.add("Ground");
        body = new Body(new Vector2(450 + (n == 0 ? 0 : 100), tilemapHeight - 64), "Player" + n, collider, s, false, false);
        ooldPos = new Vector2(body.pos);
        nnewPos = new Vector2(body.pos);

        size = new Vector2(16.0f, 16.0f);
        rectSmash = new Rectangle(0.0f, 0.0f, size.x, size.y);

        yOffset = 8.0f;
        offset = new Vector2(0.0f, yOffset);
        hittingVec = new Vector2(1.0f, 0.0f);

        Utils.aassert(animation.containsKey("right-walk"));
        current = animation.get("right-walk").getKeyFrame(0.0f);
        sprite = new Sprite(current);
    }

    public int getPlayerId()
    {
        return playerId;
    }

    public void getAHit(Vector2 smashHitDir)
    {
        if((!getHit) && (!respawn))
        {
            Vector2 smashHitDirNor = new Vector2(smashHitDir).nor();
            Vector2 hittingVecNor = new Vector2(hittingVec).nor();

            if(smashHitDirNor.x != hittingVecNor.x)
            {
                hitVec.x = smashHitDirNor.x;
                hitVec.y = -hittingVecNor.y;
            }
            else
            {
                hitVec.x = hittingVecNor.x;
                hitVec.y = -hittingVecNor.y;
            }

            hitVec.scl(norHitPoints * hitPoints);

            if(hitVec.y == 0.0f)
            {
                hitVec.y = physics.gravity;
            }

            hitPoints += norPlusMultHitPoints;
            lockMotion = true;
            getHit = true;
            sprite.setColor(hitColor);
            jumpState = JumpState.FALLING;
        }
    }

    @Override
    public void update(float dt)
    {
        Utils.aassert(nJumps >= 0);

        currentPosProgress += dt;
        if(currentPosProgress > 1.0f)
            currentPosProgress = 1.0f;


        body.pos = new Vector2(MathUtils.lerp(ooldPos.x, nnewPos.x, currentPosProgress),
                MathUtils.lerp(ooldPos.y, nnewPos.y, currentPosProgress));
        Vector2 newPos = body.pos;
        offset.x = walkState == WalkState.LEFT ? 0.0f : 16.0f;
        offset.y = yOffset;

        if(current == null)
        {
            switch(walkState)
            {
                case LEFT:
                {
                    Utils.aassert(animation.containsKey("left-walk"));
                    current = animation.get("left-walk").getKeyFrame(0.0f);
                    break;
                }
                case RIGHT:
                {
                    Utils.aassert(animation.containsKey("right-walk"));
                    current = animation.get("right-walk").getKeyFrame(0.0f);
                    break;
                }
                default:
                {
                    Utils.invalidCodePath();
                }
            }
        }

        sprite.setTexture(current);

        offset.add(newPos);
        rectSmash.x = offset.x;
        rectSmash.y = offset.y;
    }

    @Override
    public void draw()
    {
        sprite.setPosition(body.pos.x, body.pos.y);
        sprite.draw(spriteBatch);
    }

    private void clipCamera()
    {
        float minX = (camera.viewportWidth / 2.0f) * camZoom;
        float minY = (camera.viewportHeight / 2.0f) * camZoom;
        if(camera.position.x < minX)
        {
            camera.position.x = minX;
        }
        else if(camera.position.x > (tilemapWidth - minX))
        {
            camera.position.x = tilemapWidth - minX;
        }

        if(camera.position.y < minY)
        {
            camera.position.y = minY;
        }
        else if(camera.position.y > (tilemapHeight - minY))
        {
            camera.position.y = tilemapHeight - minY;
        }
    }

    public void setNewPos(Vector2 newPos)
    {
        ooldPos = nnewPos;
        nnewPos = newPos;
        currentPosProgress = 0.0f;
    }
}
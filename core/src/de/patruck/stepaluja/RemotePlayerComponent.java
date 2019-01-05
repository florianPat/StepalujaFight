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
    private final float rtt;

    public RemotePlayerComponent(EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics, Actor owner, String[] textureAtlas,
                                 int n, OrthographicCamera cameraIn, int tilemapWidthIn, int tilemapHeightIn, HeartComponent heartComponentIn,
                                 char gameModeIn, float rttIn)
    {
        super(eventManager, assetManager, spriteBatch, physics, owner, textureAtlas);

        playerId = n;
        gameMode = gameModeIn;
        rtt = rttIn;

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
        //s.add("Ground");
        body = new Body(new Vector2(450 + (n == 0 ? 0 : 100), tilemapHeight - 64), "Player" + n, collider, s, false, false);
        physics.addElement(body);
        ooldPos = new Vector2(body.pos);
        nnewPos = new Vector2(body.pos);

        size = new Vector2(16.0f, 16.0f);
        rectSmash = new Rectangle(0.0f, 0.0f, size.x, size.y);
        size = new Vector2(16.0f, 16.0f);
        rectSmash = new Rectangle(0.0f, 0.0f, size.x, size.y);
        colliderSmash = new Collider(rectSmash);
        ArrayList<String> sSmash = new ArrayList<String>();
        switch(gameMode)
        {
            case 'P':
            {
                sSmash = physics.getAllCollisionIdsWhichContain("Opponent");
                break;
            }
            case 'O':
            {
                sSmash.add("Player" + (n == 0 ? 1 : 0));
                break;
            }
            default:
            {
                Utils.invalidCodePath();
                break;
            }
        }
        bodySmash = new Body(new Vector2(rectSmash.getX(), rectSmash.getY()), "PlayerSmashTrigger" + n, colliderSmash, sSmash, true, false);
        bodySmash.setIsActive(false);
        physics.addElement(bodySmash);

        yOffset = 8.0f;
        offset = new Vector2(0.0f, yOffset);
        hittingVec = new Vector2(1.0f, 0.0f);

        Utils.aassert(animation.containsKey("right-walk"));
        current = animation.get("right-walk").getKeyFrame(0.0f);
        sprite = new Sprite(current);

        heartComponent = heartComponentIn;

        smashFunction = new Function()
        {
            @Override
            public void Event(EventData eventData)
            {
                Utils.aassert(eventData instanceof SmashEventData);
                SmashEventData event = (SmashEventData) eventData;

                int playerId = event.getPlayerId();

                if(playerId == getPlayerId())
                {
                    getAHit(event.getSmashHitDir());
                }
            }
        };

        eventManager.addListener(SmashEventData.eventId, Utils.getDelegateFromFunction(smashFunction));
    }

    public int getPlayerId()
    {
        return playerId;
    }

    public void getAHit(Vector2 smashHitDir)
    {
        if((!getHit) && (!respawn))
        {
            lockMotion = true;
            getHit = true;
            Utils.log("setColor");
            sprite.setColor(hitColor);
            jumpState = JumpState.FALLING;
        }
    }

    @Override
    public void update(float dt)
    {
        current = null;

        if(respawn)
        {
            jumpTimer += dt;

            if(jumpTimer < 0.5f)
            {
                sprite.setAlpha(MathUtils.lerp(1.0f, 0.0f, jumpTimer * 2.0f));
            }
            else if(jumpTimer < 1.0f && jumpTimer >= 0.5f)
            {
                sprite.setAlpha(MathUtils.lerp(0.0f, 1.0f, (jumpTimer - 0.5f) * 2.0f));
            }
            else if(jumpTimer < 1.5f && jumpTimer >= 1.0f)
            {
                sprite.setAlpha(MathUtils.lerp(1.0f, 0.0f, (jumpTimer - 1.0f) * 2.0f));
            }
            else if(jumpTimer < 2.0f && jumpTimer >= 1.0f)
            {
                sprite.setAlpha(MathUtils.lerp(1.0f, 0.0f, (jumpTimer - 1.5f) * 2.0f));
            }

            if(jumpTimer >= maxRespawnTime)
            {
                jumpTimer = 0.0f;
                respawn = false;
                lockMotion = false;
                sprite.setAlpha(1.0f);
            }
        }

        if(getHit)
        {
            hitTimer += dt;

            if(hitTimer >= maxHitTimer)
            {
                hitTimer = 0.0f;
                sprite.setColor(Color.WHITE);
                getHit = false;
            }
        }

        if(body.triggerInformation.triggerBodyPart == Physics.TriggerBodyPart.SHOES)
        {
            jumpState = JumpState.NONE;
            hittingVec.y = 0.0f;
        }

        currentPosProgress += dt;
        if(currentPosProgress >= 0.5f)
            currentPosProgress = 0.5f;

        float currentPosProgrssPercent = currentPosProgress / rtt;

        body.pos = new Vector2(MathUtils.lerp(ooldPos.x, nnewPos.x, currentPosProgrssPercent),
                MathUtils.lerp(ooldPos.y, nnewPos.y, currentPosProgrssPercent));
        Vector2 newPos = body.pos;
        offset.x = walkState == WalkState.LEFT ? 0.0f : 16.0f;
        offset.y = yOffset;

        if(ooldPos.x < nnewPos.x)
        {
            walkState = WalkState.RIGHT;
        }
        else if(ooldPos.x > nnewPos.x)
        {
            walkState = WalkState.LEFT;
        }

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

        sprite.setTexture(current);
        Physics.applySpriteToBoundingBox(current, collider, newPos);

        offset.add(newPos);
        rectSmash.x = offset.x;
        rectSmash.y = offset.y;
        colliderSmash.updateRectCollider();
    }

    @Override
    public void draw()
    {
        sprite.setPosition(body.pos.x, body.pos.y);
        sprite.draw(spriteBatch);

        if(playerId == 0)
        {
            camera.position.x = body.pos.x;
            camera.position.y = body.pos.y;

            camera.zoom = camZoom;
        }
        else if(playerId == 1)
        {
            Vector2 newCamPos = new Vector2((body.pos.x + camera.position.x) / 2.0f,
                    (body.pos.y + camera.position.y) / 2.0f);

            boolean farAway = false;

            float length = body.pos.x - camera.position.x;
            if(Math.abs(length) >= 200.0f)
            {
                farAway = true;
            }
            else
            {
                length = body.pos.y - camera.position.y;
                if(Math.abs(length) >= 100.0f)
                {
                    farAway = true;
                }
            }

            if(farAway && newCamZoom != MAX_CAMERA_ZOOM)
            {
                newCamZoom = MAX_CAMERA_ZOOM;
                currentProgress = 0.0f;
            }
            else if(!farAway && newCamZoom != MIN_CAMERA_ZOOM)
            {
                newCamZoom = MIN_CAMERA_ZOOM;
                currentProgress = 0.0f;
            }
            if(newCamZoom != camZoom)
            {
                camZoom = MathUtils.lerp(camZoom, newCamZoom, currentProgress);
            }
            camera.zoom = camZoom;
            camera.position.x = newCamPos.x;
            camera.position.y = newCamPos.y;

            clipCamera();
        }
        else
        {
            Utils.invalidCodePath();
        }

        camera.update();
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
        if(currentPosProgress >= rtt)
        {
            Utils.log("Current pos: " + body.pos.x + " , oldPos: " + nnewPos.x);
            ooldPos = nnewPos;
            nnewPos = newPos;
            currentPosProgress = 0.0f;
        }
    }

    public void setLoseLive()
    {
        --nLives;
        heartComponent.setHeartState(nLives, HeartComponent.HeartState.EMPTY);
        if(nLives > 0)
        {
            //body.pos = new Vector2(450 + (playerId == 0 ? 0 : 100), tilemapHeight - 64);
            lockMotion = true;
            walkState = WalkState.RIGHT;
            jumpState = JumpState.NONE;
            jumpTimer = 0.0f;
            smashTimer = 0.0f;
            smashState = JumpState.NONE;
            nJumps = 3;
            sprite.setColor(Color.WHITE);
            getHit = false;
            respawn = true;
        }
        else
        {
            Utils.log("DeadEventData");
            eventManager.TriggerEvent(new DeadEventData(playerId));
        }
    }
}
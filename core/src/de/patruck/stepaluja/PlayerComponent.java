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

public class PlayerComponent extends AnimationComponent {
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
    private final float maxSmashTimer = 0.5f;
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

    private OnScreenControls.InputSystem inputSystem;

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

    private SmashEventData eventData = null;
    private boolean liveLess = false;
    private float velX = 0.0f;

    public PlayerComponent(EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics, Actor owner, String[] textureAtlas,
                           int n, OnScreenControls.InputSystem inputSystemIn,
                           OrthographicCamera cameraIn, int tilemapWidthIn, int tilemapHeightIn, HeartComponent heartComponentIn,
                           char gameModeIn)
    {
        super(eventManager, assetManager, spriteBatch, physics, owner, textureAtlas);

        playerId = n;
        gameMode = gameModeIn;

        tilemapWidth = tilemapWidthIn;
        tilemapHeight = tilemapHeightIn;

        inputSystem = inputSystemIn;

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
        physics.addElement(body);

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

        smashFunction = new Function() {
            @Override
            public void Event(EventData eventData) {
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
    public void update(float dt) {
        Utils.aassert(nJumps >= 0);

        eventData = null;
        liveLess = false;

        currentProgress += 0.05f * dt;

        stateTime += dt;
        current = null;

        if(smashState == JumpState.JUMPING)
        {
            current = textureSmash;
            smashTimer += dt;
            if(smashTimer > maxSmashTimer)
            {
                smashTimer = 0.0f;
                smashState = JumpState.NONE;
                bodySmash.setIsActive(false);
            }
            if(body.vel.x != 0.0f)
            {
                body.vel.x = MathUtils.lerp(velX, 0.0f, smashTimer / maxSmashTimer);

                Utils.log("Vel.x:" + body.vel.x);
            }
        }
        else
        {
            body.vel.x = 0.0f;
        }
        //move (also see above :))
        body.vel.y += physics.gravity;

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
            body.vel.x = hitVec.x;
            body.vel.y = hitVec.y;

            hitTimer += dt;

            if(hitTimer >= maxLockMotionTimer)
            {
                lockMotion = false;
                sprite.setColor(Color.WHITE);
            }

            if(hitTimer >= maxHitTimer)
            {
                hitTimer = 0.0f;
                hitVec.x = 0.0f;
                hitVec.y = 0.0f;
                getHit = false;
                body.vel.x = 0.0f;
                body.vel.y = physics.gravity * 2.0f;
            }
        }

        if((!lockMotion) && smashState == JumpState.NONE)
        {
            if(inputSystem.isMoveLeftPressed())
            {
                body.vel.x = -speed;
                hittingVec.x = -1.0f;
                if(jumpState == JumpState.NONE)
                {
                    Utils.aassert(animation.containsKey("left-walk"));
                    current = animation.get("left-walk").getKeyFrame(stateTime, true);
                }
                walkState = WalkState.LEFT;
            }
            if(inputSystem.isMoveRightPressed())
            {
                body.vel.x = speed;
                hittingVec.x = 1.0f;
                if(jumpState == JumpState.NONE)
                {
                    Utils.aassert (animation.containsKey("right-walk"));
                    current = animation.get("right-walk").getKeyFrame(stateTime, true);
                }
                walkState = WalkState.RIGHT;
            }

            if(inputSystem.isJumpPressed())
            {
                if(nJumps > 0)
                {
                    if(jumpState == JumpState.JUMPING)
                    {
                        jumpTimer = 0.0f;
                        --nJumps;
                    }
                    else if(jumpState == JumpState.NONE || jumpState == jumpState.FALLING)
                    {
                        jumpState = JumpState.JUMPING;
                        --nJumps;
                    }
                    else
                    {
                        Utils.invalidCodePath();
                    }
                }
            }

            //fighting
            if(inputSystem.isHitPressed() && smashState == JumpState.NONE)
            {
                textureSmash = walkState == WalkState.RIGHT ? textureSmashRight : textureSmashLeft;
                current = textureSmash;
                bodySmash.setIsActive(true);
                smashState = JumpState.JUMPING;
                velX = body.vel.x;
            }
        }

        if(body.triggerInformation.triggerBodyPart == Physics.TriggerBodyPart.SHOES)
        {
            nJumps = 3;
        }

        if(jumpState == JumpState.JUMPING)
        {
            jumpTimer += dt;
            body.vel.y = speed * 3.5f * Math.abs(jumpTimer - 1.0f);
            hittingVec.y = 1.0f;
            if(jumpTimer >= maxJumpTime || body.triggerInformation.triggerBodyPart == Physics.TriggerBodyPart.HEAD)
            {
                jumpTimer = 0.0f;
                jumpState = JumpState.FALLING;
            }
        }
        else if(jumpState == JumpState.FALLING)
        {
            hittingVec.y = -1.0f;
            if(body.triggerInformation.triggerBodyPart == Physics.TriggerBodyPart.SHOES)
            {
                jumpState = JumpState.NONE;
                hittingVec.y = 0.0f;
            }
        }

        if(bodySmash.getIsTriggered())
        {
            bodySmash.setIsActive(false);
            if(gameMode == 'P')
            {
                String hitOpponent = bodySmash.getTriggerInformation().triggerElementCollision;
                char numberChar = hitOpponent.charAt(hitOpponent.length() - 1);
                eventManager.TriggerEvent(new SmashEventData(numberChar - '0', hittingVec));
            }
            else
            {
                eventData = new SmashEventData(playerId == 0 ? 1 : 0, hittingVec);
                eventManager.TriggerEvent(eventData);
            }
        }

        //set frame
        Vector2 newPos = body.pos.add(body.vel.scl(dt));
        offset.x = walkState == WalkState.LEFT ? 0.0f : 16.0f;
        offset.y = yOffset;

        if(current == null)
        {
            switch (walkState)
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

        if(newPos.y < -current.getHeight() || newPos.x < -current.getHeight() ||
                newPos.y > tilemapHeight + current.getHeight() ||
                newPos.x > tilemapWidth + current.getWidth())
        {
            --nLives;
            heartComponent.setHeartState(nLives, HeartComponent.HeartState.EMPTY);
            if(nLives > 0)
            {
                newPos = new Vector2(450 + (playerId == 0 ? 0 : 100), tilemapHeight - 64);
                body.setPos(newPos);
                body.vel.x = 0.0f;
                body.vel.y = 0.0f;
                lockMotion = true;
                walkState = WalkState.RIGHT;
                jumpState = JumpState.NONE;
                jumpTimer = 0.0f;
                smashTimer = 0.0f;
                smashState = JumpState.NONE;
                nJumps = 3;
                sprite.setColor(Color.WHITE);
                hitTimer = 0.0f;
                getHit = false;
                respawn = true;
                hitPoints = 1.0f;
                liveLess = true;
            }
            else
            {
                eventManager.TriggerEvent(new DeadEventData(playerId));
            }
        }

        sprite.setTexture(current);

        //apply updated body to physics
        Physics.applySpriteToBoundingBox(current, collider, newPos);

        offset.add(newPos);
        rectSmash.x = offset.x;
        rectSmash.y = offset.y;
        colliderSmash.updateRectCollider();
    }

    @Override
    public void draw() {
        sprite.setPosition(body.pos.x, body.pos.y);
        sprite.draw(spriteBatch);

        if(playerId == 0)
        {
            camera.position.x = body.pos.x;
            camera.position.y = body.pos.y;

            camera.zoom = camZoom;

            if(gameMode == 'P')
            {
                clipCamera();
            }
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

    public Vector2 getPos()
    {
        return new Vector2(sprite.getX(), sprite.getY());
    }

    public SmashEventData getToSendEvent()
    {
        return eventData;
    }

    public boolean shouldLiveLess()
    {
        return liveLess;
    }
}
package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    private float maxJumpTime = 1.0f;

    private float smashTimer = 0.0f;
    private final float maxSmashTimer = 1.0f;
    private JumpState smashState = JumpState.NONE;

    private Texture textureSmash;
    private Rectangle rectSmash;
    private Collider colliderSmash;
    private Body bodySmash;
    private Vector2 size;
    private float yOffset;
    private Vector2 offset;

    // order left, top, right, bottom
    private int input[] = new int[4];

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

    private float worldWidth, worldHeight;

    private OnScreenControls.InputSystem inputSystem;

    private final float MIN_CAMERA_ZOOM = 0.35f;
    private final float MAX_CAMERA_ZOOM = 0.65f;

    private OrthographicCamera camera;
    private float camZoom = MIN_CAMERA_ZOOM;
    private float newCamZoom = MIN_CAMERA_ZOOM;
    private float currentProgress = 0.0f;

    private int tilemapWidth;
    private int tilemapHeight;

    public PlayerComponent(EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics, Actor owner, String[] textureAtlas,
                           int n, float worldWidthIn, float worldHeightIn, OnScreenControls.InputSystem inputSystemIn,
                           OrthographicCamera cameraIn, int tilemapWidthIn, int tilemapHeightIn) {
        super(eventManager, assetManager, spriteBatch, physics, owner, textureAtlas);

        playerId = n;

        worldWidth = worldWidthIn;
        worldHeight = worldHeightIn;

        tilemapWidth = tilemapWidthIn;
        tilemapHeight = tilemapHeightIn;

        inputSystem = inputSystemIn;

        camera = cameraIn;

        // 8 or 3
        textureSmash = assetManager.get(textureAtlas[8]);

        //create body
        rect = new Rectangle();
        collider = new Collider(rect);
        ArrayList<String> s = new ArrayList<String>();
        s.add("Ground");
        body = new Body(new Vector2(450 + (n == 0 ? 0 : 100), 600-32), "Player" + n, collider, s, false, false);
        physics.addElement(body);

        size = new Vector2(16.0f, 16.0f);
        rectSmash = new Rectangle(0.0f, 0.0f, size.x, size.y);
        colliderSmash = new Collider(rectSmash);
        ArrayList<String> sSmash = new ArrayList<String>();
        sSmash.add("Player" + (n == 0 ? 1 : 0));
        bodySmash = new Body(new Vector2(rectSmash.getX(), rectSmash.getY()), "PlayerSmashTrigger" + n, colliderSmash, sSmash, true, false);
        bodySmash.setIsActive(false);
        physics.addElement(bodySmash);

        yOffset = 8.0f;
        offset = new Vector2(0.0f, yOffset);
        hittingVec = new Vector2(1.0f, 0.0f);

        Utils.aassert(animation.containsKey("right-walk"));
        current = animation.get("right-walk").getKeyFrame(0.0f);
        sprite = new Sprite(current);

        if(n == 0)
        {
            input[0] = Input.Keys.LEFT;
            input[1] = Input.Keys.UP;
            input[2] = Input.Keys.RIGHT;
            input[3] = Input.Keys.DOWN;
        }
        else
        {
            input[0] = Input.Keys.A;
            input[1] = Input.Keys.W;
            input[2] = Input.Keys.D;
            input[3] = Input.Keys.S;
        }

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
        if(!getHit)
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
            hitPoints += norPlusMultHitPoints;
            lockMotion = true;
            getHit = true;
            sprite.setColor(hitColor);
            jumpState = JumpState.FALLING;
        }
    }

    @Override
    public void update(float dt) {
        currentProgress += 0.1f * dt;

        //move
        body.vel.x = 0.0f;
        body.vel.y += physics.gravity;

        stateTime += dt;
        current = null;

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

        //NOTE: If we do not want that you can walk if you hit, delete the && smashState == JumpS...
        if((!lockMotion) && smashState == JumpState.NONE)
        {
            if(playerId == 0 ? inputSystem.isMoveLeftPressed() : Gdx.input.isKeyPressed(input[0]))
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
            if(playerId == 0 ? inputSystem.isMoveRightPressed() : Gdx.input.isKeyPressed(input[2]))
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

            if(playerId == 0 ? inputSystem.isJumpPressed() : Gdx.input.isKeyPressed(input[1]))
            {
                if(jumpState == JumpState.NONE && body.triggerInformation.triggerBodyPart == Physics.TriggerBodyPart.SHOES)
                {
                    jumpState = JumpState.JUMPING;
                }
            }

            if(jumpState == JumpState.JUMPING)
            {
                jumpTimer += dt;
                body.vel.y = speed;
                hittingVec.y = 1.0f;
                if(jumpTimer >= maxJumpTime)
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

            //fighting
            if(playerId == 0 ? inputSystem.isHitPressed() : Gdx.input.isKeyJustPressed(input[3])
                    && smashState == JumpState.NONE)
            {
                textureSmash = walkState == WalkState.RIGHT ? animation.get("right-walk").getKeyFrame(0.0f, true) : animation.get("left-walk").getKeyFrame(0.0f, true);
                current = textureSmash;
                bodySmash.setIsActive(true);
                smashState = JumpState.JUMPING;
            }
        }

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
        }

        if(bodySmash.getIsTriggered())
        {
            eventManager.TriggerEvent(new SmashEventData(playerId == 0 ? 1 : 0, hittingVec));
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

        if(newPos.x < -current.getWidth() || newPos.x >= worldWidth || newPos.y < -current.getHeight() || newPos.y >= worldHeight)
        {
            eventManager.TriggerEvent(new DeadEventData(playerId));
        }

        sprite.setTexture(current);

        //apply updated body to physics
        Physics.applySpriteToBoundingBox(current, collider, newPos);
        collider.updateRectCollider();

        offset.add(newPos);
        bodySmash.pos = offset;
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
                Utils.log("newCamZoom = 0.5f");
            }
            else if(!farAway && newCamZoom != MIN_CAMERA_ZOOM)
            {
                newCamZoom = MIN_CAMERA_ZOOM;
                currentProgress = 0.0f;
                Utils.log("newCamZoom = 0.25f");
            }
            if(newCamZoom != camZoom)
            {
                camZoom = MathUtils.lerp(camZoom, newCamZoom, currentProgress);
            }
            camera.zoom = camZoom;
            camera.position.x = newCamPos.x;
            camera.position.y = newCamPos.y;

            float minX = worldWidth * camZoom / 2.0f;
            float minY = worldHeight * camZoom / 2.0f;
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

            camera.update();
        }
        else
        {
            Utils.invalidCodePath();
        }
    }
}

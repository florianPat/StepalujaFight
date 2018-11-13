package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class AnimationComponent extends Component {

    public static final int ID = Utils.getGUID();

    protected Array<Texture> atlas;
    protected HashMap<String, Animation<Texture>> animation;

    protected Rectangle rect;
    protected Collider collider;
    protected Body body;

    protected Texture current;

    public AnimationComponent(EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics, Actor owner, String[] textureAtlas) {
        super(ID, eventManager, assetManager, spriteBatch, physics, owner);

        //Load textures
        for(int i = 0; i < textureAtlas.length; ++i)
        {
            assetManager.load(textureAtlas[i], Texture.class);
        }
        assetManager.finishLoading();

        animation = new HashMap<String, Animation<Texture>>();

        //create left-walk
        atlas = new Array<Texture>();
        for(int i = 3; i < 6; ++i)
        {
            Texture t = assetManager.get(textureAtlas[i]);
            atlas.add(t);
        }
        animation.put("left-walk", new Animation<Texture>(0.3f, atlas, Animation.PlayMode.LOOP));

        //create right-walk
        atlas.clear();
        for(int i = 6; i < 9; ++i)
        {
            Texture t = assetManager.get(textureAtlas[i]);
            atlas.add(t);
        }
        animation.put("right-walk", new Animation<Texture>(0.3f, atlas, Animation.PlayMode.LOOP));
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public void draw() {
    }
}

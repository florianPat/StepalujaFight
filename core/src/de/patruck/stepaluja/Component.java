package de.patruck.stepaluja;

import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Component
{
    protected final int id;
    protected EventManager eventManager;
    protected Actor owner;
    protected AssetManager assetManager;
    protected SpriteBatch spriteBatch;
    protected Physics physics;

    public Component(int id, EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics,
                     Actor owner) {
        this.id = id;
        this.eventManager = eventManager;
        this.assetManager = assetManager;
        this.spriteBatch = spriteBatch;
        this.physics = physics;
        this.owner = owner;
    }

    public abstract void update(float dt);
    public abstract void draw();
    //public gomSort::SortKey sort() { return gomSort::SortKey{ 0, 0.0f }; }
    public int getId() { return id; }
}

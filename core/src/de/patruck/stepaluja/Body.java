package de.patruck.stepaluja;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Body
{
    public class TriggerInformation
    {
        public String triggerElementCollision = "";
        public Physics.TriggerBodyPart triggerBodyPart = Physics.TriggerBodyPart.NONE;
    }

    public boolean isActive = true;
    public boolean isStatic;
    public boolean isTrigger;
    public boolean triggered = false;
    public TriggerInformation triggerInformation = new TriggerInformation();
    public Vector2 pos;
    public String id;
    public ArrayList<PhysicsElement> physicsElments;

    public Vector2 vel = new Vector2();

    public Body(Vector2 pos, String name, Collider collider, ArrayList<String> collisionId, boolean isTrigger, boolean isStatic)
    {
        this.isStatic = isStatic;
        this.isTrigger = isTrigger;
        this.pos = pos;
        this.id = name;
        physicsElments = new ArrayList<PhysicsElement>();

        PhysicsElement physicsElement = new PhysicsElement();
        physicsElement.collisionIds = collisionId;
        physicsElement.collider = collider;

        physicsElments.add(physicsElement);
    }

    public Body(String name, ArrayList<Collider> colliders, boolean isTrigger)
    {
        this.isStatic = true;
        this.isTrigger = isTrigger;
        this.pos = new Vector2();
        physicsElments = new ArrayList<PhysicsElement>();

        for(Collider it : colliders)
        {
            PhysicsElement physicsElement = new PhysicsElement();
            physicsElement.collisionIds = new ArrayList<String>();
            physicsElement.collider = it;

            physicsElments.add(physicsElement);
        }
    }

    public boolean getIsTriggered()
    {
        return triggered;
    }

    public Vector2 getPos()
    {
        Utils.aassert(!isStatic);

        return pos;
    }

    public void setPos(Vector2 newPos)
    {
        Utils.aassert(!isStatic);

        pos = newPos;
    }

    TriggerInformation getTriggerInformation()
    {
        return triggerInformation;
    }

    String getId()
    {
        return id;
    }
    
    void setIsActive(boolean active)
    {
        isActive = active;
    }
    
    boolean getIsActive()
    {
        return isActive;
    }
}
package de.patruck.stepaluja;

import java.util.HashMap;
import java.util.Iterator;

public class Actor
{
    private final int id;
    private HashMap<Integer, Component> components;

    public void update(float dt) {
        for(Iterator<Component> it = components.values().iterator(); it.hasNext();)
        {
            it.next().update(dt);
        }
    }

    public void draw() {
        for(Iterator<Component> it = components.values().iterator(); it.hasNext();)
        {
            it.next().draw();
        }
    }

    //TODO: Implement sorting if you need it :)
    //public void sort(std::multimap<gomSort::SortKey, unsigned long long, gomSort::SortCompare>& sortedActors);

    // public long GetActorComponentId(int componentId) {
    //     return ((long)id << 32l) | (long)componentId;
    // }

    public Actor(int id) {
        this.id = id;
        components = new HashMap<Integer, Component>();
    }

    public void addComponent(Component component) {
        if(!components.containsKey(component.getId())) {
            components.put(component.getId(), component);
        }
    }

    public void removeComponent(int componentId) {
        components.remove(componentId);
    }

    public Component getComponent(int componentId)
    {
        Utils.aassert(components.containsKey(componentId));
        return components.get(componentId);
    }

    public int getId()
    {
        return id;
    }
}

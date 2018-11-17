package de.patruck.stepaluja;

import java.util.ArrayList;
import java.util.HashMap;

public class EventManager
{
    private class Pair
    {
        public int id;
        public DelegateFunction delegateFunction;

        public Pair(int id, DelegateFunction delegateFunction)
        {
            this.id = id;
            this.delegateFunction = delegateFunction;
        }
    }

    private HashMap<Integer, ArrayList<DelegateFunction>> eventListenerMap;
    private ArrayList<Pair> eventDeleteMap;

    public EventManager()
    {
        eventListenerMap = new HashMap<Integer, ArrayList<DelegateFunction>>();
        eventDeleteMap = new ArrayList<Pair>();
    }

    public boolean addListener(int eventType, DelegateFunction delegateFunction)
    {
        ArrayList<DelegateFunction> eventListenerList = eventListenerMap.get(eventType);

        if(eventListenerList == null)
        {
            eventListenerMap.put(eventType, new ArrayList<DelegateFunction>());
            eventListenerList = eventListenerMap.get(eventType);
        }

        for(DelegateFunction it : eventListenerList)
        {
            if(it.id == delegateFunction.id)
            {
                System.err.println("Attempting to double - register a delegate");
                return false;
            }
        }

        eventListenerList.add(delegateFunction);
        return true;
    }

    public void removeListener(int eventType, DelegateFunction delegateFunction)
    {
        eventDeleteMap.add(new Pair(eventType, delegateFunction));
    }

    public boolean TriggerEvent(EventData eventData)
    {
        boolean processed = false;
        ArrayList<DelegateFunction> findIt = eventListenerMap.get(eventData.getEventId());
        if(findIt != null)
        {
            for(DelegateFunction it : findIt)
            {
                it.function.Event(eventData);
                processed = true;
            }
        }
        return processed;
    }

    public void removeListeners()
    {
        if(!eventDeleteMap.isEmpty())
        {
            for(Pair it : eventDeleteMap)
            {
                int eventType = it.id;
                DelegateFunction delegateFunction = it.delegateFunction;

                ArrayList<DelegateFunction> findIt = eventListenerMap.get(eventType);
                if(findIt != null)
                {
                    for(DelegateFunction foundIt : findIt)
                    {
                        if(foundIt.id == delegateFunction.id)
                        {
                            findIt.remove(foundIt);
                            break;
                        }
                    }
                    if(findIt.isEmpty())
                    {
                        eventListenerMap.remove(eventType);
                    }
                }
            }
            eventDeleteMap.clear();
        }
    }
}
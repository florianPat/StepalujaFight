package de.patruck.stepaluja;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AnotherTestComponent extends Component {

    public static int ID = Utils.getGUID();
    //private Function function;

    public AnotherTestComponent(EventManager eventManager, AssetManager assetManager, SpriteBatch spriteBatch, Physics physics, Actor owner) {
        super(ID, eventManager, assetManager, spriteBatch, physics, owner);

        /*function = new Function() {
            @Override
            public void Event(EventData eventData) {
                Utils.aassert (eventData instanceof EventTest);
                EventTest event = (EventTest) eventData;
                Utils.log(event.getTest());
            }
        };

        eventManager.addListener(EventTest.eventId, Utils.getDelegateFromFunction(function));*/
    }

    @Override
    public void update(float dt) {
        //eventManager.TriggerEvent(new EventTest());
    }

    @Override
    public void draw() {

    }
}

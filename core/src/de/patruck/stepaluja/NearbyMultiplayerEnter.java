package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class NearbyMultiplayerEnter extends MenuBtnsBackComponent
{
    private char playerId;
    private BitmapFont font;
    private NearbyServerList nearbyServerList;
    private String msg = "Create nearby server";

    public NearbyMultiplayerEnter(ExtendViewport viewport, GameStart screenManager, SpriteBatch spriteBatch, Object menuArg)
    {
        super(viewport, screenManager, spriteBatch, menuArg);

        btns = new Rectangle[2];

        Utils.aassert(menuArg instanceof Character);
        playerId = (Character) menuArg;

        font = Utils.getFont();

        nearbyServerList = new NearbyServerList(spriteBatch, font);
        for(int i = 0; i < 5; ++i)
        {
            nearbyServerList.listItems.add(nearbyServerList.new ListItem(String.valueOf(i), String.valueOf(i)));
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();

        font.dispose();
    }

    @Override
    public void recalculateBtnPositions()
    {
        super.recalculateBtnPositions();

        nearbyServerList.resetBtnPositions(btns[0].getX(), btns[0].getY(), btns[0].getWidth(), btns[0].getHeight());
    }

    @Override
    public void resetBtns()
    {
        super.resetBtns();

        btns[0] = new Rectangle(40.0f, 50.0f, 200.0f, 100.0f);
        btns[1] = new Rectangle(100.0f, 0.0f, font.getSpaceWidth() * msg.length() * 0.55f, 15.0f);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        nearbyServerList.touchDragged(viewportPosition, pointer);

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        super.touchUp(screenX, screenY, pointer, button);

        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        nearbyServerList.touchUp(viewportPosition);
        if(btns[1].contains(viewportPosition))
        {
            //TODO: create server!
            Utils.log("Create server!");
        }

        return true;
    }

    @Override
    public void render()
    {
        super.render();

        nearbyServerList.render();
        font.draw(spriteBatch, msg, btns[1].x, btns[1].y + font.getLineHeight());
    }
}

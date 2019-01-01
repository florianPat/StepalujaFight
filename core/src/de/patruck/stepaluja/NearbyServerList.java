package de.patruck.stepaluja;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Vector;

public class NearbyServerList
{
    public class ListItem
    {
        private String serverName = "ServerNameTest";
        private String mapName = "MapNameTest";

        public ListItem()
        {
        }

        public ListItem(String serverName, String mapName)
        {
            this.serverName = serverName;
            this.mapName = mapName;
        }

        public void render(float x, float y, String itemEnd)
        {
            font.draw(spriteBatch, serverName + '\n' + mapName + '\n' + itemEnd, x, y);
        }
    }

    private SpriteBatch spriteBatch;
    private Rectangle rect;
    private int dragPointer = -1;
    private BitmapFont font;
    private float lastDragPosY = 0.0f;
    private float localYStart = 0.0f;
    private static final int nStrignsInOneListItem = 3;
    private float distanceDragged = 0.0f;
    public Vector<ListItem> listItems;

    public NearbyServerList(SpriteBatch spriteBatch, BitmapFont font)
    {
        this.spriteBatch = spriteBatch;

        rect = new Rectangle();
        this.font = font;
        listItems = new Vector<ListItem>();
    }

    public void resetBtnPositions(float btnX, float btnY, float btnWidth, float btnHeight)
    {
        rect.x = btnX;
        rect.y = btnY;
        rect.width = btnWidth;
        rect.height = btnHeight;
    }

    public void touchUp(Vector2 viewportPosition)
    {
        if(rect.contains(viewportPosition) && distanceDragged < 5.0f)
        {
            int firstIndex = (int) localYStart;
            viewportPosition.y -= rect.y;
            int index = ((int) (viewportPosition.y / getItemHeight())) + firstIndex;
            //TODO: Select server here!
        }
        distanceDragged = 0.0f;
        dragPointer = -1;
    }

    private float getItemHeight()
    {
        return font.getLineHeight() * nStrignsInOneListItem;
    }

    public void touchDragged(Vector2 viewportPosition, int pointer)
    {
        if(pointer == dragPointer && lastDragPosY != 0.0f)
        {
            float difference = 0.0f;

            if(lastDragPosY > viewportPosition.y)
            {
                difference = 0.1f;
            }
            else if(lastDragPosY < viewportPosition.y)
            {
                difference = -0.1f;
            }
            else if(lastDragPosY == viewportPosition.y)
            {
                Utils.log("WTF!");
            }
            else
            {
                Utils.invalidCodePath();
            }

            localYStart += difference;

            if(distanceDragged < 100.0f)
                distanceDragged += Math.abs(lastDragPosY - viewportPosition.y);

            float maxY = listItems.size() - ((int) (rect.height / getItemHeight()));

            if(localYStart < 0.0f)
                localYStart = 0.0f;
            else if(localYStart > maxY)
                localYStart = maxY;
        }
        else
        {
            if(rect.contains(viewportPosition))
            {
                dragPointer = pointer;
                lastDragPosY = viewportPosition.y;
            }
            else
            {
                Utils.log("Drag started outside of the menu!");
            }
        }
    }

    public void render()
    {
        float itemHeight = getItemHeight();
        float worldStartX = rect.x;

        int nItemEnd = (int) (rect.width / font.getSpaceWidth());
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < nItemEnd; ++i)
        {
            builder.append('-');
        }
        String itemEnd = builder.toString();

        int index = (int) localYStart;
        float end = ((int) (rect.height / itemHeight)) + index;
        for(float i = itemHeight; (index < end) && (index < listItems.size()); i += itemHeight)
        {
            float worldStartY = i + rect.y;
            ListItem it = listItems.get(index++);
            it.render(worldStartX, worldStartY, itemEnd);
        }
    }
}

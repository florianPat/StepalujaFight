package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Tilemap {

    private class Tile
    {
        private TextureAtlas.AtlasRegion texture;

        public Tile(TextureAtlas.AtlasRegion texture, boolean collider, Vector2 pos, Body body)
        {
            this.texture = texture;
            if(collider)
            {
                Collider last = body.physicsElments.get(body.physicsElments.size() - 1).collider;

                if(last.getType() == Collider.Type.circle)
                {
                    body.physicsElments.clear();

                    PhysicsElement physicsElement = new PhysicsElement();
                    physicsElement.collisionIds = new ArrayList<String>();
                    physicsElement.collider = new Collider(new Rectangle(pos.x, pos.y, texture.getRegionWidth(),
                            texture.getRegionHeight()));

                    body.physicsElments.add(physicsElement);
                    return;
                }

                if(((last.unionCollider.rect.x + last.unionCollider.rect.width) == pos.x)
                        && ((last.unionCollider.rect.y == pos.y)))
                {
                    last.unionCollider.rect.merge(new Rectangle(pos.x, pos.y, texture.getRegionWidth(),
                            texture.getRegionHeight()));
                    last.updateRectCollider();
                }
                else
                {
                    PhysicsElement physicsElement = new PhysicsElement();
                    physicsElement.collisionIds = new ArrayList<String>();
                    physicsElement.collider = new Collider(new Rectangle(pos.x, pos.y, texture.getRegionWidth(),
                            texture.getRegionHeight()));

                    body.physicsElments.add(physicsElement);
                }
            }
        }

        public final TextureAtlas.AtlasRegion getTexture()
        {
            return texture;
        }
    }

    //NOTE: Change if tile size is not quadratic!
    private final int TILE_SIZE = 32;

    private Tile[][] karte;
    private int width, height;
    private Body body = new Body(new Vector2(0.0f, 0.0f), "Ground", new Collider(new Circle(0.0f, 0.0f, 1.0f)), new ArrayList<String>(), false, true);

    private TextureAtlas.AtlasRegion gras;
    private TextureAtlas.AtlasRegion dirt;
    private TextureAtlas.AtlasRegion stone;
    private TextureAtlas.AtlasRegion lava;
    private TextureAtlas.AtlasRegion water;
    private TextureAtlas.AtlasRegion sky;
    private TextureAtlas.AtlasRegion caveTransition;
    private TextureAtlas.AtlasRegion cave;

    private Physics physics;

    /**
     * Erstellt eine Tilemap von einer Textdatei.
     *
     * @param filename Der Textdateiname der Tilemap, welche eingelesen wird und danach gerendert werden kann
     */
    public Tilemap(String filename, AssetManager assetManager, Physics physics, GameStart screenManager)
    {
        this.physics = physics;

        assetManager.load("tileset/tileset.atlas", TextureAtlas.class);

        assetManager.load("tileset/gras.jpg", Texture.class);
        assetManager.load("tileset/dirt.jpg", Texture.class);
        assetManager.load("tileset/stone.jpg", Texture.class);
        assetManager.load("tileset/lava.jpg", Texture.class);
        assetManager.load("tileset/water.jpg", Texture.class);
        assetManager.load("tileset/sky.jpg", Texture.class);
        assetManager.load("tileset/caveTransition.jpg", Texture.class);
        assetManager.load("tileset/cave.jpg", Texture.class);

        assetManager.finishLoading();

        TextureAtlas textureAtlas = assetManager.get("tileset/tileset.atlas");
        Array<TextureAtlas.AtlasRegion> atlasRegions = textureAtlas.getRegions();
        cave = atlasRegions.get(0);
        caveTransition = atlasRegions.get(1);
        dirt = atlasRegions.get(2);
        gras = atlasRegions.get(3);
        lava = atlasRegions.get(4);
        sky = atlasRegions.get(5);
        stone = atlasRegions.get(6);
        water = atlasRegions.get(7);

        try
        {
            ladeKarte(filename);
        }
        catch(IOException e)
        {
            Utils.log("Failed to init map!");
            Utils.invalidCodePath();
            //erstelleKarte();
        }
    }

    public int getWidth()
    {
        return width * TILE_SIZE;
    }

    public int getHeight()
    {
        return height * TILE_SIZE;
    }

    private void ladeKarte(String dateiname) throws IOException
    {
        FileHandle file = Gdx.files.internal(dateiname);
        Utils.aassert (file.exists());
        BufferedReader br = file.reader((int) file.length());

        String zeile = br.readLine();

        width = zeile.length();
        height = ((int)file.length()) / (width + 1);

        karte = new Tile[height][width];

        for (int i = height - 1; i >= 0; i--)
        {
            for (int j = 0; j < width; j++)
            {
                if(zeile.charAt(j) == '0')
                {
                    karte[i][j] = new Tile(gras, true, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '1')
                {
                    karte[i][j] = new Tile(dirt, true, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '2')
                {
                    karte[i][j] = new Tile(stone, true, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '3')
                {
                    karte[i][j] = new Tile(lava, false, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '4')
                {
                    karte[i][j] = new Tile(water, false, new Vector2(j * TILE_SIZE, i * TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '5')
                {
                    karte[i][j] = new Tile(cave, false, new Vector2(j * TILE_SIZE, i * TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '6')
                {
                    karte[i][j] = new Tile(caveTransition, false, new Vector2(j * TILE_SIZE, i * TILE_SIZE), body);
                }
                else if(zeile.charAt(j) == '7')
                {
                    karte[i][j] = new Tile(water, false, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else
                {
                    Utils.log("Tilemap loadMap error!");
                }
            }
            zeile = br.readLine();
        }

        physics.addElement(body);
    }

    private void erstelleKarte()
    {
        width = 10;
        height = 10;
        karte = new Tile[height][width];

        int zufall;

        for (int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                zufall = (int)(Math.random()*5);

                if(zufall < 3)
                {
                    karte[i][j] = new Tile(water, false, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else if(zufall == 3)
                {
                    karte[i][j] = new Tile(dirt, true, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else if(zufall == 4)
                {
                    karte[i][j] = new Tile(stone, true, new Vector2(j*TILE_SIZE, i*TILE_SIZE), body);
                }
                else
                {
                    Utils.log("Tilemap createMap error!");
                }
            }
        }
        physics.addElement(body);
    }

    /**
     * Zeichnet die Tilemap
     */
    public void draw(SpriteBatch spriteBatch)
    {
        for (int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                Tile tile = karte[i][j];
                Utils.aassert(tile != null);
                Utils.aassert(tile.getTexture() != null);
                spriteBatch.draw(tile.getTexture(), j*TILE_SIZE, i*TILE_SIZE);
            }
        }
    }
}
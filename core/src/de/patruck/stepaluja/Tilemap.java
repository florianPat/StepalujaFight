package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Tilemap {

    private class Tile
    {
        private Texture texture;

        public Tile(Texture texture, boolean collider, Vector2 pos, Body body)
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
                    physicsElement.collider = new Collider(new Rectangle(pos.x, pos.y, texture.getWidth(), texture.getHeight()));

                    body.physicsElments.add(physicsElement);
                    return;
                }

                if(((last.unionCollider.rect.x + last.unionCollider.rect.width) == pos.x)
                        && ((last.unionCollider.rect.y == pos.y)))
                {
                    last.unionCollider.rect.merge(new Rectangle(pos.x, pos.y, texture.getWidth(), texture.getHeight()));
                    last.updateRectCollider();
                }
                else
                {
                    PhysicsElement physicsElement = new PhysicsElement();
                    physicsElement.collisionIds = new ArrayList<String>();
                    physicsElement.collider = new Collider(new Rectangle(pos.x, pos.y, texture.getWidth(), texture.getHeight()));

                    body.physicsElments.add(physicsElement);
                }
            }
        }

        public final Texture getTexture()
        {
            return texture;
        }
    }

    private Tile[][] karte;
    private int width, height;
    private Body body = new Body(new Vector2(0.0f, 0.0f), "Ground", new Collider(new Circle(0.0f, 0.0f, 1.0f)), new ArrayList<String>(), false, true);

    private Texture gras;
    private Texture dirt;
    private Texture stone;
    private Texture lava;
    private Texture water;
    private Physics physics;

    public Tilemap(String filename, AssetManager assetManager, Physics physics, GameStart screenManager,
                   Vector2 worldSize)
    {
        this.physics = physics;

        assetManager.load("tileset/gras.jpg", Texture.class);
        assetManager.load("tileset/dirt.jpg", Texture.class);
        assetManager.load("tileset/stone.jpg", Texture.class);
        assetManager.load("tileset/lava.jpg", Texture.class);
        assetManager.load("tileset/water.jpg", Texture.class);

        assetManager.finishLoading();

        gras = assetManager.get("tileset/gras.jpg");
        dirt = assetManager.get("tileset/dirt.jpg");
        stone = assetManager.get("tileset/stone.jpg");
        lava = assetManager.get("tileset/lava.jpg");
        water = assetManager.get("tileset/water.jpg");

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
                    karte[i][j] = new Tile(gras, true, new Vector2(j*32, i*32), body);
                }
                else if(zeile.charAt(j) == '1')
                {
                    karte[i][j] = new Tile(dirt, true, new Vector2(j*32, i*32), body);
                }
                else if(zeile.charAt(j) == '2')
                {
                    karte[i][j] = new Tile(stone, true, new Vector2(j*32, i*32), body);
                }
                else if(zeile.charAt(j) == '3')
                {
                    karte[i][j] = new Tile(lava, false, new Vector2(j*32, i*32), body);
                }
                else if(zeile.charAt(j) == '4')
                {
                    karte[i][j] = new Tile(water, false, new Vector2(j*32, i*32), body);
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
            for (int j = 0;j < width; j++)
            {
                zufall = (int)(Math.random()*5);

                if(zufall < 3)
                {
                    karte[i][j] = new Tile(water, false, new Vector2(j*32, i*32), body);
                }
                else if(zufall == 3)
                {
                    karte[i][j] = new Tile(dirt, true, new Vector2(j*32, i*32), body);
                }
                else if(zufall == 4)
                {
                    karte[i][j] = new Tile(stone, true, new Vector2(j*32, i*32), body);
                }
                else
                {
                    Utils.log("Tilemap createMap error!");
                }
            }
        }
        physics.addElement(body);
    }

    public void draw(SpriteBatch spriteBatch)
    {
        for (int i = 0; i < height; i++)
        {
            for (int j = 0;j < width; j++)
            {
                Tile tile = karte[i][j];
                Utils.aassert(tile != null);
                Utils.aassert(tile.getTexture() != null);
                spriteBatch.draw(tile.getTexture(), j*32, i*32);
            }
        }
    }
}
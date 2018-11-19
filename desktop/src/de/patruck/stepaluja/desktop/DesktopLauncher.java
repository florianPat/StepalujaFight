package de.patruck.stepaluja.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.math.Vector2;

import de.patruck.stepaluja.GameStart;

public class DesktopLauncher {
	public static void main (String[] arg) {
		final Vector2 worldSize = new Vector2(900.0f, 600.0f);

		new LwjglApplication(new GameStart(worldSize, true), "Krasses Spiel", (int)worldSize.x, (int)worldSize.y);
	}
}

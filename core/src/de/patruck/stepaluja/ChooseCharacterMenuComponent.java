package de.patruck.stepaluja;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class ChooseCharacterMenuComponent extends MenuBtnsBackComponent
{
    public ChooseCharacterMenuComponent(ExtendViewport viewport, GameStart screenManager,
                                        Object componentArg)
    {
        super(viewport, screenManager, componentArg);

        btns = new Rectangle[2];
    }

    @Override
    public void resetBtns()
    {
        super.resetBtns();

        btns[0] = new Rectangle(40.0f, 60.0f, 40.0f, 50.0f);
        btns[1] = new Rectangle(105.0f, 60.0f, 60.0f, 50.0f);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        super.touchUp(screenX, screenY, pointer, button);

        Vector2 viewportPosition = viewport.unproject(new Vector2(screenX, screenY));

        if(btns[0].contains(viewportPosition))
        {
            setNextScreen('0');
        }
        else if(btns[1].contains(viewportPosition))
        {
            setNextScreen('1');
        }

        return super.touchUp(screenX, screenY, pointer, button);
    }

    private void setNextScreen(char playerId)
    {
        Utils.aassert(componentArg instanceof String);
        String s = (String) componentArg;

        String map = "";
        switch(s.charAt(1))
        {
            case 'E':
            {
                map = "maps/easy.txt";
                break;
            }
            case 'M':
            {
                map = "maps/middle.txt";
                break;
            }
            case 'H':
            {
                map = "maps/hard.txt";
                break;
            }
            default:
            {
                Utils.invalidCodePath();
                break;
            }
        }

        switch(s.length())
        {
            case 2:
            {
                char gameMode = s.charAt(0);

                switch(gameMode)
                {
                    case 'U':
                    {
                        screenManager.setScreen(new PracticeLevel(screenManager,
                                playerId, map));
                        break;
                    }
                    case 'O':
                    {
                        screenManager.setScreen(new MenuLevel(screenManager,
                                MenuLevel.LevelComponentName.ChooseCharacterMenu,
                                s + playerId));
                        break;
                    }
                    default:
                    {
                        Utils.invalidCodePath();
                        break;
                    }
                }
                break;
            }
            case 3:
            {
                char player0 = s.charAt(2);
                screenManager.setScreen(new TestLevel(screenManager, player0, playerId, map));
                break;
            }
            default:
            {
                Utils.invalidCodePath();
                break;
            }
        }
    }
}
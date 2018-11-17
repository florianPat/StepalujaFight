package de.patruck.stepaluja;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;

public class SignUpLevel extends Level
{
    private SignUpComponent signUpComponent;
    private boolean signingUp = false;
    private String username = "";
    private String password = "";
    private BitmapFont font;
    private boolean showingError = false;
    private float showingErrorTime = 0.0f;

    public SignUpLevel(GameStart screenManager, Vector2 worldSize) {
        super(screenManager, worldSize);
    }

    @Override
    public void create() {
        signUpComponent = new SignUpComponent();
        Gdx.input.getTextInput(signUpComponent, "Choose a username", "Username", "verycoolshit55");

        font = Utils.getFont(36);
    }

    @Override
    public void render(float dt) {
        if(showingError)
        {
            showingErrorTime += dt;
            if(showingErrorTime >= 5.0f)
            {
                Gdx.gl.glClearColor( 0, 0, 0, 1 );
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                showingErrorTime = 0.0f;
                showingError = false;
                signingUp = false;
                username = "";
                password = "";
                signUpComponent = new SignUpComponent();
                Gdx.input.getTextInput(signUpComponent, "Choose a username", "Username", "verycoolshit55");
            }
        }
        else if(signingUp)
        {
            int result = NativeBridge.signUpCompleted();
            switch (result)
            {
                case 0:
                {
                    return;
                }
                case 1:
                {
                    Preferences preferences = Utils.getGlobalPreferences();
                    preferences.putString("username", signUpComponent.username);
                    preferences.flush();

                    screenManager.setScreen(new MenuLevel("menu/Titelbild.jpg", screenManager,
                            worldSize, MenuLevel.LevelComponentName.MainMenu));
                    break;
                }
                case -1:
                {
                    if(NativeBridge.errorCode == NativeBridge.kAuthErrorEmailAlreadyInUse)
                    {
                        showingError = true;
                        Gdx.gl.glClearColor( 0, 0, 0, 1 );
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                        viewport.apply();
                        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

                        spriteBatch.begin();
                        font.draw(spriteBatch, NativeBridge.errorMsg,0.0f, viewport.getWorldHeight() / 2);
                        spriteBatch.end();
                    }
                    else
                    {
                        Utils.logBreak("User creation got wrong!", screenManager, worldSize);
                    }
                    break;
                }
                default:
                {
                    Utils.invalidCodePath();
                    break;
                }
            }
        }
        else if (signUpComponent.badName || signUpComponent.canceled)
        {
            if(!username.equals(""))
            {
                signUpComponent = new SignUpComponent();
                Gdx.input.getTextInput(signUpComponent, "Choose a password", "Password", "badpassword123");
            }
            else
            {
                signUpComponent = new SignUpComponent();
                Gdx.input.getTextInput(signUpComponent, "Choose a username", "Username", "verycoolshit55");
            }
        }
        else if(!username.equals("") && !signUpComponent.username.equals(""))
        {
            password = signUpComponent.username;
            NativeBridge.userSignUp(username + "@irgendeineKomischeE-MailwelcheEhniceMandKennt.de", password);
            signingUp = true;
        }
        else if(!signUpComponent.username.equals(""))
        {
            username = signUpComponent.username;
            signUpComponent = new SignUpComponent();
            Gdx.input.getTextInput(signUpComponent, "Choose a password", "Password", "badpassword123");
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        font.dispose();
    }
}

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
    private boolean merge = false;
    private boolean success = false;
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
        if(success)
        {
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();
            spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

            spriteBatch.begin();
            font.draw(spriteBatch, "Success!!", 0.0f, viewport.getWorldHeight() / 2);
            spriteBatch.end();

            showingErrorTime += dt;
            if(showingErrorTime >= 2.0f)
            {
                screenManager.setScreen(new MenuLevel("menu/Titelbild.jpg", screenManager,
                        worldSize, MenuLevel.LevelComponentName.MainMenu));
            }
        }
        else if(showingError)
        {
            showingErrorTime += dt;
            if(showingErrorTime >= 3.0f)
            {
                Gdx.gl.glClearColor( 0, 0, 0, 1 );
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                showingErrorTime = 0.0f;
                showingError = false;
                signingUp = false;
                merge = false;
                success = false;
                username = "";
                password = "";
                signUpComponent = new SignUpComponent();
                Gdx.input.getTextInput(signUpComponent, "Choose a username", "Username", "verycoolshit55");
            }
        }
        else if(merge)
        {
            int result = NativeBridge.resultConvertAnonymousToPermanentEmailAccount();
            switch(result)
            {
                case 0:
                {
                    return;
                }
                case 1:
                {
                    Preferences preferences = Utils.getGlobalPreferences();
                    preferences.putString("username", username);
                    preferences.flush();

                    success = true;
                    break;
                }
                case -1:
                {
                    if(NativeBridge.errorCode == NativeBridge.kAuthErrorEmailAlreadyInUse)
                    {
                        showingError = true;
                        Gdx.gl.glClearColor(0, 0, 0, 1);
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                        viewport.apply();
                        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

                        spriteBatch.begin();
                        font.draw(spriteBatch, NativeBridge.errorMsg, 0.0f, viewport.getWorldHeight() / 2);
                        font.draw(spriteBatch, "Note that Upper and lowercase do not matter here!", 0.0f, viewport.getWorldHeight() / 2 - font.getLineHeight());
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
                    preferences.putString("username", username);
                    preferences.flush();

                    success = true;
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

            Preferences preferences = Utils.getGlobalPreferences();

            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();
            spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

            spriteBatch.begin();
            font.draw(spriteBatch, "Loading...", 0.0f, viewport.getWorldHeight() / 2);
            spriteBatch.end();

            if(preferences.contains("username"))
            {
                merge = true;
                NativeBridge.convertAnonymousToPermanentEmailAccount(username + "@irgendeineKomischeE-MailwelcheEhniceMandKennt.de", password);
                return;
            }

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

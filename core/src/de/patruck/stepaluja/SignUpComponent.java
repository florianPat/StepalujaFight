package de.patruck.stepaluja;

import com.badlogic.gdx.Input;

public class SignUpComponent implements Input.TextInputListener
{
    public String username = "";
    public boolean canceled = false;
    public boolean badName = false;

    @Override
    public void input(String text)
    {
        username = text;

        badName = !username.matches("^[A-Za-z0-9 ]*$");
    }

    @Override
    public void canceled() {
        canceled = true;
    }

    public void reset()
    {
        canceled = false;
        badName = false;
        username = "";
    }
}

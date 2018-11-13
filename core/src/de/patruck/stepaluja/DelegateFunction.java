package de.patruck.stepaluja;

public class DelegateFunction
{
    public int id;
    public Function function;

    public DelegateFunction(int id, Function function)
    {
        this.id = id;
        this.function = function;
    }
};
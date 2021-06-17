package com.thirtynineeighty.plantscare.commands;

import com.thirtynineeighty.plantscare.IMain;

public class TurnServoCommand
  extends JsonHttpCommand<TurnServoCommand, TurnServoCommand.Input, TurnServoCommand.Output>
{
  private final float angle;

  public TurnServoCommand(IMain main, float angle)
  {
    super(main, Input.class, Output.class);

    this.angle = angle;

    setServerMethod("turn-servo");
    setInput(input());
    setUrl(main.server().url());
  }

  private Input input()
  {
    Input input = new Input();
    input.key = main.protectedKey();
    input.angle = angle;
    return input;
  }

  public static class Input
  {
    public String key;
    public float angle;
  }

  public static class Output
  {
    public String result;
  }
}
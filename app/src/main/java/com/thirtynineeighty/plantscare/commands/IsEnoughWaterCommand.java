package com.thirtynineeighty.plantscare.commands;

import com.thirtynineeighty.plantscare.IMain;

public class IsEnoughWaterCommand
  extends JsonHttpCommand<IsEnoughWaterCommand, IsEnoughWaterCommand.Input, IsEnoughWaterCommand.Output>
{
  public IsEnoughWaterCommand(IMain main)
  {
    super(main, Input.class, Output.class);
    setServerMethod("is-enough-water");
    setInput(input());
    setUrl(main.server().url());
  }

  private Input input()
  {
    Input input = new Input();
    input.key = main.protectedKey();
    return input;
  }

  public static class Input
  {
    public String key;
  }

  public static class Output
  {
    public boolean result;
  }
}

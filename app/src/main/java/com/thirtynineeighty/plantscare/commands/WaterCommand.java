package com.thirtynineeighty.plantscare.commands;

import com.google.gson.annotations.SerializedName;
import com.thirtynineeighty.plantscare.IMain;

public class WaterCommand
  extends JsonHttpCommand<WaterCommand, WaterCommand.Input, WaterCommand.Output>
{
  private final int duration;
  private final boolean force;

  public WaterCommand(IMain main, int duration, boolean force)
  {
    super(main, Input.class, Output.class);

    this.duration = duration;
    this.force = force;

    setServerMethod("water");
    setInput(input());
    setUrl(main.server().url());
  }

  private Input input()
  {
    Input input = new Input();
    input.key = main.protectedKey();
    input.durationSeconds = duration;
    input.force = force;
    return input;
  }

  public static class Input
  {
    public String key;
    @SerializedName("duration_seconds")
    public int durationSeconds;
    public boolean force;
  }

  public static class Output
  {
    public boolean result;
    public String message;
  }
}

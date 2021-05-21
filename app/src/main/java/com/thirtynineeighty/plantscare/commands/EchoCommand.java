package com.thirtynineeighty.plantscare.commands;

import com.google.gson.annotations.SerializedName;
import com.thirtynineeighty.plantscare.IMain;

public class EchoCommand
  extends JsonHttpCommand<EchoCommand, EchoCommand.Input, EchoCommand.Output>
{
  public EchoCommand(IMain main)
  {
    super(main, Input.class, Output.class);
    setServerMethod("echo");
    setInput(input());
  }

  private Input input()
  {
    Input input = new Input();
    input.value = "echo test";
    return input;
  }

  public static class Input
  {
    @SerializedName("str")
    public String value;
  }

  public static class Output
  {
    @SerializedName("str")
    public String value;
  }
}

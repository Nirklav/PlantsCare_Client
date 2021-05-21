package com.thirtynineeighty.plantscare.common;

import com.thirtynineeighty.plantscare.IMain;

public abstract class RunnableNoExceptT<T>
  implements RunnableT<T>
{
  private final IMain main;
  private final String name;

  public RunnableNoExceptT(IMain main, String name)
  {
    if (main == null)
      throw new IllegalArgumentException("main");

    if (name == null)
      throw new IllegalArgumentException("name");

    this.main = main;
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  protected abstract void onRun(T value) throws Exception;

  public final void run(T value)
  {
    try
    {
      onRun(value);
    }
    catch (Exception e)
    {
      main.errors().process(e, "Error on event: %s", name);
    }
  }
}

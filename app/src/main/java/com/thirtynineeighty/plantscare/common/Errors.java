package com.thirtynineeighty.plantscare.common;

import android.util.Log;

public class Errors
{
  private static final boolean canThrow = false;

  public void process(String msg)
  {
    if (canThrow)
      throw new RuntimeException(msg);
    Log.d("process", msg);
  }

  public void process()
  {
    if (canThrow)
      throw new RuntimeException();
    Log.d("process", "Unknown process");
  }

  public void process(Throwable e)
  {
    if (canThrow)
      throw new RuntimeException(e);
    Log.d("process", "Unknown process", e);
  }

  public void process(Throwable e, String msg)
  {
    if (canThrow)
      throw new RuntimeException(msg, e);
    Log.d("process", msg, e);
  }

  public void process(Throwable e, String msg, Object... args)
  {
    process(e, String.format(msg, args));
  }
}
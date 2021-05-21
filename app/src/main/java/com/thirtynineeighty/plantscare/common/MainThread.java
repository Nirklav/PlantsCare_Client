package com.thirtynineeighty.plantscare.common;

import android.os.Handler;

public class MainThread
{
  private final Handler handler;

  public MainThread(Handler handler)
  {
    this.handler = handler;
  }

  public void post(RunnableNoExcept r)
  {
    handler.post(r);
  }
}

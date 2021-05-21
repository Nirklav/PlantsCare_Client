package com.thirtynineeighty.plantscare.common;

import com.thirtynineeighty.plantscare.BuildConfig;
import com.thirtynineeighty.plantscare.IMain;
import com.thirtynineeighty.plantscare.commands.EchoCommand;

import java.util.concurrent.atomic.AtomicInteger;

public class Server
{
  private final IMain main;
  private volatile String activeUrl;

  public Server(IMain main)
  {
    this.main = main;
  }

  public void request(Found callback)
  {
    final AtomicInteger left = new AtomicInteger(2);
    activeUrl = null;

    new EchoCommand(main)
      .setUrl(BuildConfig.serverAddressOutside)
      .setCallbacksToMainThread()
      .setOnSuccess(c -> activeUrl = BuildConfig.serverAddressOutside)
      .setOnFinished(c -> completed(callback, left))
      .sendAsync();

    new EchoCommand(main)
      .setUrl(BuildConfig.serverAddressInside)
      .setCallbacksToMainThread()
      .setOnSuccess(c -> activeUrl = BuildConfig.serverAddressInside)
      .setOnFinished(c -> completed(callback, left))
      .sendAsync();
  }

  private void completed(Found callback, AtomicInteger left)
  {
    int l = left.decrementAndGet();
    if (l == 0)
      callback.callback(activeUrl != null);
  }

  public String url()
  {
    return activeUrl;
  }

  public interface Found
  {
    void callback(boolean found);
  }
}

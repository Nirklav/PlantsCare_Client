package com.thirtynineeighty.plantscare;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.thirtynineeighty.plantscare.common.Errors;
import com.thirtynineeighty.plantscare.common.MainThread;
import com.thirtynineeighty.plantscare.common.Server;
import com.thirtynineeighty.plantscare.databinding.ActivityMainBinding;

import android.os.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity
  extends AppCompatActivity
  implements IMain
{
  private ExecutorService executorService;
  private MainThread mainThread;
  private Errors errors;
  private Server server;

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    executorService = Executors.newCachedThreadPool();
    mainThread = new MainThread(new Handler());
    errors = new Errors();
    server = new Server(this);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
  }

  @Override
  public String protectedKey()
  {
    return BuildConfig.protectedKey;
  }

  @Override
  public ExecutorService executor()
  {
    return executorService;
  }

  @Override
  public MainThread mainThread()
  {
    return mainThread;
  }

  @Override
  public Errors errors()
  {
    return errors;
  }

  @Override
  public Server server()
  {
    return server;
  }
}
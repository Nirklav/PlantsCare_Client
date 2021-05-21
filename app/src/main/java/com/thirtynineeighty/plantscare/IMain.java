package com.thirtynineeighty.plantscare;

import com.thirtynineeighty.plantscare.common.Errors;
import com.thirtynineeighty.plantscare.common.MainThread;
import com.thirtynineeighty.plantscare.common.Server;

import java.util.concurrent.ExecutorService;

public interface IMain
{
  String protectedKey();

  ExecutorService executor();
  MainThread mainThread();
  Errors errors();
  Server server();
}

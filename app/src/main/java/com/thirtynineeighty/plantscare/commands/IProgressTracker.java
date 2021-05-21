package com.thirtynineeighty.plantscare.commands;

public interface IProgressTracker<TCommand>
{
  void callback(TCommand command, int progress);
}

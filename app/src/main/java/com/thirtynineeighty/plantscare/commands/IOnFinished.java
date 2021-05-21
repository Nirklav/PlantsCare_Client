package com.thirtynineeighty.plantscare.commands;

public interface IOnFinished<TCommand>
{
  void callback(TCommand command);
}

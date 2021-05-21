package com.thirtynineeighty.plantscare.commands;

public interface IOnFailed<TCommand>
{
  void callback(TCommand command);
}
package com.thirtynineeighty.plantscare.commands;

public interface IOnSuccess<TCommand>
{
  void callback(TCommand command);
}

package com.thirtynineeighty.plantscare.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thirtynineeighty.plantscare.IMain;
import com.thirtynineeighty.plantscare.R;
import com.thirtynineeighty.plantscare.commands.TurnServoCommand;
import com.thirtynineeighty.plantscare.databinding.FragmentCameraAngleBinding;

import org.jetbrains.annotations.NotNull;

public class CameraAngleFragment
  extends DialogFragment
{
  private IMain main;
  private FragmentCameraAngleBinding binding;

  @Override
  public void onAttach(@NonNull Context context)
  {
    super.onAttach(context);
    if (context instanceof IMain)
      main = (IMain) context;
  }

  @Override
  public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    binding = FragmentCameraAngleBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    binding.editTextAngle.setOnKeyListener((v, keyCode, event) ->
    {
      if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
      {
        sendCommand();
        return true;
      }
      return false;
    });

    binding.buttonOk.setOnClickListener(btn -> sendCommand());

    binding.buttonCancel.setOnClickListener(btn -> NavHostFragment
      .findNavController(this)
      .navigate(R.id.action_cameraAngleFragment_to_MainFragment));
  }

  private void sendCommand()
  {
    Editable text = binding.editTextAngle.getText();
    float angle = Float.parseFloat(text.toString());

    new TurnServoCommand(main, angle)
      .setCallbacksToMainThread()
      .setOnFinished(cmd ->
      {
        TurnServoCommand.Output output = cmd.getOutput();

        Bundle args = new Bundle();
        args.putString("message", output.result);

        NavHostFragment
          .findNavController(this)
          .navigate(R.id.action_cameraAngleFragment_to_MainFragment, args);
      })
      .sendAsync();
  }
}
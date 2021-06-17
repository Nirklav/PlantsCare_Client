package com.thirtynineeighty.plantscare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.thirtynineeighty.plantscare.IMain;
import com.thirtynineeighty.plantscare.R;
import com.thirtynineeighty.plantscare.commands.GetCameraImageCommand;
import com.thirtynineeighty.plantscare.commands.IsEnoughWaterCommand;
import com.thirtynineeighty.plantscare.databinding.FragmentMainBinding;

import org.jetbrains.annotations.NotNull;

public class MainFragment
  extends Fragment
{
  private IMain main;
  private FragmentMainBinding binding;
  private Boolean serverStatus;
  private Boolean waterStatus;
  private String message;

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
    binding = FragmentMainBinding.inflate(inflater, container, false);

    Bundle arguments = getArguments();
    if (arguments != null)
      message = arguments.getString("message", null);

    main.server().request(s ->
    {
      serverStatus = s;
      setText();
    });

    setText();
    return binding.getRoot();
  }

  public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonMakePhoto.setOnClickListener(btn ->
    {
      setInProgress(true);

      new GetCameraImageCommand(main)
        .setCallbacksToMainThread()
        .setOnSuccess(cmd -> binding.imagePhoto.setImageBitmap(cmd.getImage()))
        .setOnFinished(cmd ->
        {
          setInProgress(false);
          binding.progressBar.setProgress(0);
        })
        .trackProgress((cmd, p) -> binding.progressBar.setProgress(p))
        .sendAsync();
    });

    binding.buttonAngle.setOnClickListener(btn -> Navigation
      .findNavController(view)
      .navigate(R.id.action_MainFragment_to_cameraAngleFragment));

    binding.buttonCheckWater.setOnClickListener(btn ->
    {
      setInProgress(true);

      new IsEnoughWaterCommand(main)
        .setCallbacksToMainThread()
        .setOnSuccess(cmd ->
        {
          waterStatus = cmd.getOutput().result;
          setText();
        })
        .setOnFinished(cmd -> setInProgress(false))
        .sendAsync();
    });

    binding.buttonWater.setOnClickListener(btn -> Navigation
      .findNavController(view)
      .navigate(R.id.action_MainFragment_to_WaterFragment));
  }

  private void setInProgress(boolean value)
  {
    binding.buttonMakePhoto.setEnabled(!value);
    binding.buttonAngle.setEnabled(!value);
    binding.buttonCheckWater.setEnabled(!value);
    binding.buttonWater.setEnabled(!value);
  }

  private void setText()
  {
    String status = "Server: %s\r\nWater: %s";
    String server = serverStatus == null
      ? "Unknown"
      : serverStatus
        ? "Connected"
        : "Disconnected";

    String water = waterStatus == null
      ? "Unknown"
      : waterStatus
        ? "Enough"
        : "Not enough";

    if (message != null)
      status += "\r\n" + message;

    binding.textViewStatus.setText(String.format(status, server, water));
  }

  @Override
  public void onDestroyView()
  {
    super.onDestroyView();
    binding = null;
  }
}
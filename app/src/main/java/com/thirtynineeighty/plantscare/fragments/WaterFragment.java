package com.thirtynineeighty.plantscare.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.thirtynineeighty.plantscare.IMain;
import com.thirtynineeighty.plantscare.R;
import com.thirtynineeighty.plantscare.commands.WaterCommand;
import com.thirtynineeighty.plantscare.databinding.FragmentWaterBinding;

import org.jetbrains.annotations.NotNull;

public class WaterFragment
  extends Fragment
{
  private IMain main;
  private FragmentWaterBinding binding;
  private boolean inProcess;

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
    binding = FragmentWaterBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    binding.buttonStart.setOnClickListener(btn ->
    {
      boolean force = binding.checkBoxForce.isChecked();
      int duration = binding.seekBarWaterDuration.getProgress();

      if (duration <= 0)
      {
        Snackbar
          .make(view, "Duration is zero", 3000)
          .show();

        return;
      }

      if (!inProcess)
      {
        inProcess = true;
        binding.progressBarLoading.setVisibility(View.VISIBLE);

        new WaterCommand(main, duration, force)
          .setCallbacksToMainThread()
          .setOnSuccess(cmd ->
          {
            inProcess = false;
            binding.progressBarLoading.setVisibility(View.INVISIBLE);

            WaterCommand.Output output = cmd.getOutput();
            String result = output.result ? "Success" : "Fail";
            String message = String.format("%s: %s", result, output.message);

            Bundle args = new Bundle();
            args.putString("message", message);

            Navigation
              .findNavController(view)
              .navigate(R.id.action_WaterFragment_to_MainFragment, args);
          })
          .sendAsync();
      }
    });
  }

  @Override
  public void onDestroyView()
  {
    super.onDestroyView();
    binding = null;
  }
}
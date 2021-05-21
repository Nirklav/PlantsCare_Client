package com.thirtynineeighty.plantscare.commands;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.gson.annotations.SerializedName;
import com.thirtynineeighty.plantscare.IMain;

public class GetCameraImageCommand
  extends JsonHttpCommand<GetCameraImageCommand, GetCameraImageCommand.Input, GetCameraImageCommand.Output>
{
  private Bitmap image;

  public GetCameraImageCommand(IMain main)
  {
    super(main, Input.class, Output.class);
    setServerMethod("get-camera-image");
    setInput(input());
    setUrl(main.server().url());
  }

  private Input input()
  {
    Input input = new Input();
    input.key = main.protectedKey();
    return input;
  }

  public Bitmap getImage()
  {
    return image;
  }

  @Override
  protected void onSuccess()
  {
    super.onSuccess();

    Output output = getOutput();
    byte[] blob = Base64.decode(output.image, Base64.DEFAULT);
    image = BitmapFactory.decodeByteArray(blob, 0, blob.length);
  }

  public static class Input
  {
    public String key;
  }

  public static class Output
  {
    @SerializedName("image_base64")
    public String image;
  }
}
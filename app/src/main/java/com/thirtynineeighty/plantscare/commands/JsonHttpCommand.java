package com.thirtynineeighty.plantscare.commands;

import com.google.gson.Gson;
import com.thirtynineeighty.plantscare.IMain;
import com.thirtynineeighty.plantscare.common.RunnableNoExcept;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class JsonHttpCommand<TCommand extends JsonHttpCommand<TCommand, TInput, TOutput>, TInput, TOutput>
  implements ICommand
{
  private final static int timeout = 10000;

  protected final IMain main;
  private final Class<TInput> inputClass;
  private final Class<TOutput> outputClass;

  private final Gson gson;

  private final TCommand inheritor;

  private String requestUrl;
  private String serverMethod;
  private boolean postToMainThread;

  private TInput input;
  private volatile TOutput output;
  private volatile ErrorOutput errorOutput;
  private volatile Exception exception;

  private volatile boolean started;
  private volatile boolean ended;

  private volatile int contentLength;

  private IOnSuccess<TCommand> onSuccessCallback;
  private IOnFailed<TCommand> onFailedCallback;
  private IOnFinished<TCommand> onFinishedCallback;
  private IProgressTracker<TCommand> progressTracker;

  @SuppressWarnings("unchecked")
  public JsonHttpCommand(IMain main, Class<TInput> inputClass, Class<TOutput> outputClass)
  {
    this.main = main;
    this.inputClass = inputClass;
    this.outputClass = outputClass;

    this.gson = new Gson();
    this.inheritor = (TCommand)this;
  }

  public TCommand setUrl(String value)
  {
    requestUrl = value;
    return inheritor;
  }

  public TCommand setServerMethod(String value)
  {
    serverMethod = value;
    return inheritor;
  }

  public TCommand setInput(TInput value)
  {
    input = value;
    return inheritor;
  }

  public TCommand setCallbacksToMainThread()
  {
    postToMainThread = true;
    return inheritor;
  }

  public TCommand setOnSuccess(IOnSuccess<TCommand> value)
  {
    if (onSuccessCallback != null)
      throw new IllegalStateException("OnSuccess already set");
    onSuccessCallback = value;
    return inheritor;
  }

  public TCommand setOnFailed(IOnFailed<TCommand> value)
  {
    if (onFailedCallback != null)
      throw new IllegalStateException("OnFailed already set");
    onFailedCallback = value;
    return inheritor;
  }

  public TCommand setOnFinished(IOnFinished<TCommand> value)
  {
    if (onFinishedCallback != null)
      throw new IllegalStateException("OnFinished already set");
    onFinishedCallback = value;
    return inheritor;
  }

  public TCommand trackProgress(IProgressTracker<TCommand> value)
  {
    if (progressTracker != null)
      throw new IllegalStateException("Progress tracker already set");
    progressTracker = value;
    return inheritor;
  }

  public TInput getInput()
  {
    return input;
  }

  public TOutput getOutput()
  {
    if (!ended)
      throw new IllegalStateException("command not ended");
    return output;
  }

  public ErrorOutput getErrorOutput()
  {
    if (!ended)
      throw new IllegalStateException("command not ended");
    return errorOutput;
  }

  public Exception getException()
  {
    if (!started)
      throw new IllegalStateException("command not started");
    return exception;
  }

  public boolean isLogicError(int code)
  {
    if (!ended)
      throw new IllegalStateException("command not ended");
    return errorOutput != null && errorOutput.code == code;
  }

  public void send()
  {
    if (started)
      throw new IllegalStateException("command already started");
    started = true;

    sendImpl();
  }

  public void sendAsync()
  {
    if (started)
      throw new IllegalStateException("command already started");
    started = true;

    main.executor().submit(new RunnableNoExcept(main, "JsonHttpCommand.sendAsync")
    {
      @Override
      protected void onRun() throws Exception
      {
        sendImpl();
      }
    });
  }

  private void sendImpl()
  {
    try
    {
      URL url = new URL(requestUrl);
      HttpURLConnection connection = (HttpURLConnection)url.openConnection();

      connection.setConnectTimeout(timeout);
      connection.setReadTimeout(timeout);

      connection.setRequestMethod("POST");

      connection.setDoInput(true);
      connection.setDoOutput(true);

      connection.addRequestProperty("Server-Method", serverMethod);
      connection.addRequestProperty("Accept", "application/json; charset=utf-8");
      connection.addRequestProperty("Accept-Encoding", "gzip, deflate");

      OutputStream outputStream = connection.getOutputStream();
      writeInput(outputStream, input);

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK)
      {
        contentLength = connection.getContentLength();

        InputStream inputStream = connection.getInputStream();

        output = readOutput(inputStream);
        inputStream.close();

        ended = true;
        onSuccessImpl(inheritor);
      }
      else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
      {
        InputStream errorStream = connection.getErrorStream();
        errorOutput = readErrorOutput(errorStream);
        errorStream.close();

        ended = true;
        onFailedImpl(inheritor);
      }
      else
      {
        ended = true;
        onFailedImpl(inheritor);
      }
    }
    catch (Exception e)
    {
      exception = e;
      onFailedImpl(inheritor);
      main.errors().process(e);
    }
    finally
    {
      onFinishedImpl(inheritor);
    }
  }

  private void writeInput(OutputStream stream, TInput input)
    throws IOException
  {
    OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
    gson.toJson(input, inputClass, writer);
    writer.flush();
  }

  private TOutput readOutput(InputStream stream)
    throws IOException
  {
    try
    {
      InputStreamWrapper outputStream = new InputStreamWrapper(stream);
      InputStreamReader reader = new InputStreamReader(outputStream, StandardCharsets.UTF_8);
      return gson.fromJson(reader, outputClass);
    }
    finally
    {
      onTrackImpl(inheritor, 100);
    }
  }

  private ErrorOutput readErrorOutput(InputStream stream)
    throws IOException
  {
    InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
    return gson.fromJson(reader, ErrorOutput.class);
  }

  private void onSuccessImpl(final TCommand command)
  {
    if (!postToMainThread)
      onSuccessSyncImpl(command);
    else
    {
      main.mainThread().post(new RunnableNoExcept(main, "JsonHttpCommand.onSuccessImpl")
      {
        @Override
        protected void onRun() throws Exception
        {
          onSuccessSyncImpl(command);
        }
      });
    }
  }

  private void onFailedImpl(final TCommand command)
  {
    if (!postToMainThread)
      onFailedSyncImpl(command);
    else
    {
      main.mainThread().post(new RunnableNoExcept(main, "JsonHttpCommand.onFailedImpl")
      {
        @Override
        protected void onRun() throws Exception
        {
          onFailedSyncImpl(command);
        }
      });
    }
  }

  private void onFinishedImpl(final TCommand command)
  {
    if (!postToMainThread)
      onFinishedSyncImpl(command);
    else
    {
      main.mainThread().post(new RunnableNoExcept(main, "JsonHttpCommand.onFinishedImpl")
      {
        @Override
        protected void onRun() throws Exception
        {
          onFinishedSyncImpl(command);
        }
      });
    }
  }

  private void onTrackImpl(final TCommand command, int progress)
  {
    if (!postToMainThread)
      onTrackSyncImpl(command, progress);
    else
    {
      main.mainThread().post(new RunnableNoExcept(main, "JsonHttpCommand.onTrackImpl")
      {
        @Override
        protected void onRun() throws Exception
        {
          onTrackSyncImpl(command, progress);
        }
      });
    }
  }

  private void onSuccessSyncImpl(TCommand command)
  {
    try
    {
      onSuccess();
      if (onSuccessCallback != null)
        onSuccessCallback.callback(command);
    }
    catch (Exception e)
    {
      main.errors().process(e);
    }
  }

  private void onFailedSyncImpl(TCommand command)
  {
    try
    {
      onFailed();
      if (onFailedCallback != null)
        onFailedCallback.callback(command);
    }
    catch (Exception e)
    {
      main.errors().process(e);
    }
  }

  private void onFinishedSyncImpl(TCommand command)
  {
    try
    {
      onFinished();
      if (onFinishedCallback != null)
        onFinishedCallback.callback(command);
    }
    catch (Exception e)
    {
      main.errors().process(e);
    }
  }

  private void onTrackSyncImpl(TCommand command, int progress)
  {
    try
    {
      if (progressTracker != null)
        progressTracker.callback(command, progress);
    }
    catch (Exception e)
    {
      main.errors().process(e);
    }
  }

  protected void onSuccess()
  {

  }

  protected void onFailed()
  {

  }

  protected void onFinished()
  {

  }

  private class InputStreamWrapper
    extends InputStream
  {
    private final InputStream inner;
    private int read;

    public InputStreamWrapper(InputStream inner)
    {
      this.inner = inner;
    }

    @Override
    public int read() throws IOException
    {
      // Call progress every 1 kb
      if (read % 1024 == 0)
        onTrackImpl(inheritor, (int)((read * 100f) / contentLength));

      read++;
      return inner.read();
    }
  }
}

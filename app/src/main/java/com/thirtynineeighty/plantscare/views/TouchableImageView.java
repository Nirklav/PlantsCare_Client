package com.thirtynineeighty.plantscare.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TouchableImageView
  extends androidx.appcompat.widget.AppCompatImageView
{
  private static final int INVALID_POINTER_ID = -1;

  private final ScaleGestureDetector scaleDetector;

  private float scaleFactor = 1f;
  private float posX;
  private float posY;
  private float lastTouchX;
  private float lastTouchY;
  private int activePointerId = INVALID_POINTER_ID;

  public TouchableImageView(@NonNull @NotNull Context context)
  {
    this(context, null);
  }

  public TouchableImageView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs)
  {
    this(context, attrs, 0);
  }

  public TouchableImageView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);

    setScaleType(ScaleType.MATRIX);
    scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
  }

  @Override
  public void setImageBitmap(Bitmap bm)
  {
    super.setImageBitmap(bm);
    scaleFactor = 0f; // set min scale
    setMatrix();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    scaleDetector.onTouchEvent(event);

    final int action = event.getAction();
    switch (action & MotionEvent.ACTION_MASK)
    {
      case MotionEvent.ACTION_DOWN:
      {
        final float x = event.getX();
        final float y = event.getY();

        lastTouchX = x;
        lastTouchY = y;
        activePointerId = event.getPointerId(0);
        break;
      }

      case MotionEvent.ACTION_MOVE:
      {
        final int pointerIndex = event.findPointerIndex(activePointerId);
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        // Only move if the ScaleGestureDetector isn't processing a gesture.
        if (!scaleDetector.isInProgress())
        {
          final float dx = x - lastTouchX;
          final float dy = y - lastTouchY;

          posX += dx;
          posY += dy;

          setMatrix();
        }

        lastTouchX = x;
        lastTouchY = y;

        break;
      }

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
      {
        activePointerId = INVALID_POINTER_ID;
        break;
      }

      case MotionEvent.ACTION_POINTER_UP:
      {
        final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == activePointerId)
        {
          // This was our active pointer going up. Choose a new
          // active pointer and adjust accordingly.
          final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
          lastTouchX = event.getX(newPointerIndex);
          lastTouchY = event.getY(newPointerIndex);
          activePointerId = event.getPointerId(newPointerIndex);
        }
        break;
      }
    }

    return true;
  }

  private void setMatrix()
  {
    // Restrict scale and positions
    Drawable drawable = getDrawable();
    if (drawable != null)
    {
      final int dWidth = drawable.getIntrinsicWidth();
      final int dHeight = drawable.getIntrinsicHeight();
      final int vWidth = getWidth() - getPaddingLeft() - getPaddingRight();
      final int vHeight = getHeight() - getPaddingTop() - getPaddingBottom();

      final float wScale = vWidth / (float) dWidth;
      final float hScale = vHeight / (float) dHeight;
      final float minScale = Math.min(wScale, hScale);
      scaleFactor = Math.max(minScale, Math.min(scaleFactor, 5.0f));

      final float rdWidth = Math.max(0, dWidth * scaleFactor - vWidth);
      final float rdHeight = Math.max(0, dHeight * scaleFactor - vHeight);
      posX = clamp(posX, -rdWidth / 2, rdWidth / 2);
      posY = clamp(posY, -rdHeight / 2, rdHeight / 2);

      final int shiftX = (int)(vWidth / 2f - dWidth * scaleFactor / 2f);
      final int shiftY = (int)(vHeight / 2f - dHeight * scaleFactor / 2f);

      Matrix matrix = new Matrix();
      matrix.setScale(scaleFactor, scaleFactor);
      matrix.postTranslate(posX + shiftX, posY + shiftY);
      setImageMatrix(matrix);
    }
  }

  private int clamp(float value, float min, float max)
  {
    value = Math.min(value, max);
    value = Math.max(value, min);
    return (int)value;
  }

  private class ScaleListener
    extends ScaleGestureDetector.SimpleOnScaleGestureListener
  {
    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
      scaleFactor *= detector.getScaleFactor();
      setMatrix();
      return true;
    }
  }
}

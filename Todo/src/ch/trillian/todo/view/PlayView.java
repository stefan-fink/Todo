package ch.trillian.todo.view;

import ch.trillian.todo.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class PlayView extends View {

  private static final int INVALID_POINTER_ID = -1;

  // attributes
  private boolean isShowText;
  private int labelPosition;
  private int labelTextSize;

  // Painters
  private Paint textPaint;
  private Paint linePaint;
  private Paint pointPaint;
  private Path linePath;

  // Positions
  float lastTouchX;
  float lastTouchY;
  int activePointerId;
  float pointPosX = 500;
  float pointPosY = 500;

  // zooming
  private ScaleGestureDetector mScaleGestureDetector;
  private GestureDetector mGestureDetector;
  private float mScaleFactor = 1.0f;

  public PlayView(Context context, AttributeSet attrs) {

    super(context, attrs);

    Log.w("TODO", "PlayView()");

    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayView, 0, 0);

    try {
      isShowText = a.getBoolean(R.styleable.PlayView_showText, false);
      labelPosition = a.getInteger(R.styleable.PlayView_labelPosition, 0);
      Log.w("TODO", "labelPosition: " + labelPosition);
      labelTextSize = a.getDimensionPixelSize(R.styleable.PlayView_labelTextSize, 10);
      Log.w("TODO", "labelTextSize: " + labelTextSize);
    } finally {
      a.recycle();
    }

    init();

    // Create our ScaleGestureDetector
    mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    mGestureDetector = new GestureDetector(context, new GestureListener());

    Log.w("TODO", "Size: " + getWidth() + ", " + getHeight());
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {

      mScaleFactor *= detector.getScaleFactor();

      // Don't let the object get too small or too large.
      mScaleFactor = Math.max(0.2f, Math.min(mScaleFactor, 5.0f));

      invalidate();
      return true;
    }
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDoubleTap(MotionEvent e) {

      mScaleFactor = 1.1f;
      invalidate();
      return true;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {

    Log.w("TODO", "onTouchEvent()");

    // Let the ScaleGestureDetector inspect all events.
    mScaleGestureDetector.onTouchEvent(ev);

    // Let the GestureDetector inspect all events.
    mGestureDetector.onTouchEvent(ev);

    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {

    case MotionEvent.ACTION_DOWN: {
      final float x = ev.getX();
      final float y = ev.getY();

      // Remember where we started
      lastTouchX = x;
      lastTouchY = y;
      activePointerId = ev.getPointerId(0);

      break;
    }

    case MotionEvent.ACTION_MOVE: {

      final int pointerIndex = ev.findPointerIndex(activePointerId);
      final float x = ev.getX(pointerIndex);
      final float y = ev.getY(pointerIndex);

      // Only move if the ScaleGestureDetector isn't processing a gesture.
      if (!mScaleGestureDetector.isInProgress()) {
        // Calculate the distance moved
        final float dx = x - lastTouchX;
        final float dy = y - lastTouchY;

        // Move the object
        pointPosX += dx;
        pointPosY += dy;

        // Invalidate to request a redraw
        invalidate();
      }

      // Remember this touch position for the next move event
      lastTouchX = x;
      lastTouchY = y;

      break;
    }

    case MotionEvent.ACTION_UP: {

      activePointerId = INVALID_POINTER_ID;
      break;
    }

    case MotionEvent.ACTION_CANCEL: {

      activePointerId = INVALID_POINTER_ID;
      break;
    }

    case MotionEvent.ACTION_POINTER_UP: {

      // Extract the index of the pointer that left the touch sensor
      final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
      final int pointerId = ev.getPointerId(pointerIndex);
      if (pointerId == activePointerId) {
        // This was our active pointer going up. Choose a new
        // active pointer and adjust accordingly.
        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
        lastTouchX = ev.getX(newPointerIndex);
        lastTouchY = ev.getY(newPointerIndex);
        activePointerId = ev.getPointerId(newPointerIndex);
      }
      break;
    }
    }

    return true;
  }

  protected void onDraw(Canvas canvas) {

    super.onDraw(canvas);

    canvas.save();
    canvas.scale(mScaleFactor, mScaleFactor);

    Log.w("TODO", "Size: " + getWidth() + ", " + getHeight());

    // Draw the label text
    if (isShowText()) {
      canvas.save();
      canvas.translate(0, getHeight() / 2);
      canvas.drawText("Ein grosses Label", getWidth() / 2, textPaint.getTextSize(), textPaint);
      canvas.drawLine(0, textPaint.getTextSize(), getWidth(), textPaint.getTextSize(), textPaint);
      canvas.drawLine(0, textPaint.getTextSize() + textPaint.ascent(), getWidth(), textPaint.getTextSize() + textPaint.ascent(), textPaint);
      canvas.drawLine(0, textPaint.getTextSize() + textPaint.descent(), getWidth(), textPaint.getTextSize() + textPaint.descent(), textPaint);
      canvas.restore();
    }

    linePath.reset();
    linePath.moveTo(getWidth() / 4, getWidth() / 4);
    linePath.lineTo(pointPosX, pointPosY);
    linePath.rLineTo(getWidth() / 4, -getWidth() / 4);
    canvas.drawPath(linePath, linePaint);

    pointPaint.setStyle(Paint.Style.FILL);
    pointPaint.setColor(0xFFFF0000);
    canvas.drawCircle(pointPosX, pointPosY, 25, pointPaint);
    pointPaint.setStyle(Paint.Style.STROKE);
    pointPaint.setColor(0xFF000000);
    canvas.drawCircle(pointPosX, pointPosY, 25, pointPaint);

    canvas.restore();
  }

  public boolean isShowText() {

    return isShowText;
  }

  public void setShowText(boolean showText) {

    this.isShowText = showText;
    invalidate();
    requestLayout();
  }

  private void init() {

    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setColor(0xFF000000);
    textPaint.setTextSize(labelTextSize);
    textPaint.setStrokeWidth(1);
    if (labelPosition == 0) {
      textPaint.setTextAlign(Paint.Align.LEFT);
    } else {
      textPaint.setTextAlign(Paint.Align.RIGHT);
    }

    linePaint = new Paint(0);
    linePaint.setStyle(Paint.Style.STROKE);
    linePaint.setStrokeWidth(15);
    linePaint.setStrokeJoin(Paint.Join.ROUND);

    linePath = new Path();

    pointPaint = new Paint(0);
    pointPaint.setStrokeWidth(5);
  }
}

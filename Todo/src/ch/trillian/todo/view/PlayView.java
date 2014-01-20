package ch.trillian.todo.view;

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
import ch.trillian.todo.R;

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

  // view size
  int sizeX;
  int sizeY;
  
  // position and zoom
  float positionX = 500;
  float positionY = 500;
  float scale = 1.0f;

  float lastTouchX;
  float lastTouchY;
  int activePointerId;

  // zooming
  ScaleGestureDetector mScaleGestureDetector;
  GestureDetector mGestureDetector;

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
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {

      float newScale = scale * detector.getScaleFactor();

      // Don't let the object get too small or too large.
      newScale = Math.max(0.2f, Math.min(newScale, 10.0f));

      float focusX = detector.getFocusX() / scale - positionX;
      float focusY = detector.getFocusY() / scale - positionY;
 
      positionX = (positionX + focusX) * scale / newScale - focusX;
      positionY = (positionY + focusY) * scale / newScale - focusY;
     
      // px = (posX + x) * scale        => x = px / scale - posX
      // px1 = (posX1 + x) * scale1 
      // px2 = (posX2 + x) * scale2
      // px1 = px2
      // (posX1 + x) * scale1 = (posX2 + x) * scale2      
      // posX2 = (posX1 + x) * scale1 / scale2 - x
      
      scale = newScale;
      
      invalidate();
      return true;
    }
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onDoubleTap(MotionEvent e) {

      scale = 1.0f;
      positionX = sizeX / 2;
      positionY = sizeY / 2;
      invalidate();
      return true;
    }
  }

  @Override
  protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    
    sizeX = w;
    sizeY = h;
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent ev) {

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

        // Move the viewport
        positionX += dx / scale;
        positionY += dy / scale;

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

    textPaint.setTextSize(labelTextSize / 2);
    canvas.drawText(String.format("x: %4.0f, y: %4.0f %% %4.1f", positionX, positionY, 100 * scale), 0, 0 - textPaint.ascent(), textPaint);

    canvas.save();
    canvas.scale(scale, scale);
    canvas.translate(positionX, positionY);

    // Draw the label text
    if (isShowText()) {
      textPaint.setTextSize(labelTextSize);
      canvas.drawText("Ein grosses Label", 0, 0 - textPaint.descent(), textPaint);
      canvas.drawLine(-1000, 0, 1000, 0, textPaint);
      canvas.drawLine(0, -1000, 0, 1000, textPaint);
    }

    linePath.reset();
    linePath.moveTo(0, 500);
    linePath.lineTo(-500, 0);
    linePath.lineTo(0, -500);
    canvas.drawPath(linePath, linePaint);

    pointPaint.setStyle(Paint.Style.FILL);
    pointPaint.setColor(0xFFFF0000);
    canvas.drawCircle(-500, 0, 25, pointPaint);
    pointPaint.setStyle(Paint.Style.STROKE);
    pointPaint.setColor(0xFF000000);
    canvas.drawCircle(-500, 0, 25, pointPaint);

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

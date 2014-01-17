package ch.trillian.todo.view;

import ch.trillian.todo.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PlayView extends View {

  private boolean isShowText;
  private int labelPosition;
  private int labelTextSize;
  private Paint textPaint;
  private Paint linePaint;
  private Paint pointPaint;
  private Path linePath;


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
    
    Log.w("TODO", "Size: " + getWidth() + ", " + getHeight());
  }
  
  protected void onDraw(Canvas canvas) {
    
    super.onDraw(canvas);
    
    Log.w("TODO", "Size: " + getWidth() + ", " + getHeight());
    
    // Draw the label text
    canvas.drawText("Ein grosses Label", getWidth() / 2,  100 + textPaint.getTextSize(), textPaint);
    canvas.drawLine(0, 100 + textPaint.getTextSize(), getWidth(), 100 + textPaint.getTextSize(), textPaint);
    canvas.drawLine(0, 100 + textPaint.getTextSize() + textPaint.ascent(), getWidth(), 100 + textPaint.getTextSize() + textPaint.ascent(), textPaint);
    canvas.drawLine(0, 100 + textPaint.getTextSize() + textPaint.descent(), getWidth(), 100 + textPaint.getTextSize() + textPaint.descent(), textPaint);
    
    linePath.reset();
    linePath.moveTo(getWidth() / 4, getWidth() / 4);
    linePath.rLineTo(getWidth() / 4, getWidth() / 4);
    linePath.rLineTo(getWidth() / 4, -getWidth() / 4);
    canvas.drawPath(linePath, linePaint);
    
    pointPaint.setStyle(Paint.Style.FILL);
    pointPaint.setColor(0xFFFF0000);
    canvas.drawCircle(getWidth() / 2, getWidth() / 2, 25, pointPaint);
    pointPaint.setStyle(Paint.Style.STROKE);
    pointPaint.setColor(0xFF000000);
    canvas.drawCircle(getWidth() / 2, getWidth() / 2, 25, pointPaint);
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

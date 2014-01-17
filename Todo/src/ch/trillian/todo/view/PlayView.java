package ch.trillian.todo.view;

import ch.trillian.todo.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

public class PlayView extends View {

  private boolean isShowText;
  private int textPosition;

  public PlayView(Context context, AttributeSet attrs) {
    
    super(context, attrs);

    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PlayView, 0, 0);

    try {
      isShowText = a.getBoolean(R.styleable.PlayView_showText, false);
      textPosition = a.getInteger(R.styleable.PlayView_labelPosition, 0);
    } finally {
      a.recycle();
    }
  }

  public boolean isShowText() {
    
    return isShowText;
  }

  public void setShowText(boolean showText) {
    
    this.isShowText = showText;
    invalidate();
    requestLayout();
  }
}

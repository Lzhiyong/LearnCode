package com.text.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.text.InputType;
import android.view.inputmethod.BaseInputConnection;
import android.text.Editable;
import android.view.KeyEvent;
import android.graphics.Typeface;
import java.util.Timer;
import java.util.TimerTask;


public class TextView extends View implements OnScrollListener {


	private TextPaint mTextPaint;
	private Paint mPaint;
    private Drawable mDrawableCursorRes;
    private Drawable mSelectHandleLeft;
    private Drawable mSelectHandleRight;
	private Drawable mSelectHandleMiddle;

	private int mScrollX, mScrollY;

	private TextBuffer mTextBuffer;

	private int screenWidth, screenHeight;
	private int statusBarHeight;
	private int blinkActionBarHeight;
	private int mCursorWidth;

	private int mCursorPosX, mCursorPosY;

	private int mCursorLine, mCursorIndex;

	private InputMethodManager imm;

    private GestureDetector mGestureDetector;
	
	private boolean isShowCursor = true;
	
	private final int MARGIN_LEFT = 100;

	private final String TAG = this.getClass().getSimpleName();

    public TextView(Context context) {
        super(context);
        initView(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {

		screenWidth = ScreenUtils.getScreenHeight(context);
		screenHeight = ScreenUtils.getScreenHeight(context);
		statusBarHeight = ScreenUtils.getStatusBarHeight(context);
		blinkActionBarHeight = ScreenUtils.getActionBarHeight(context);

		mDrawableCursorRes = context.getDrawable(R.drawable.abc_text_cursor_material);
		mDrawableCursorRes.setTint(Color.MAGENTA);
		mCursorWidth = mDrawableCursorRes.getIntrinsicWidth();
		if(mCursorWidth > 5) mCursorWidth = 5;

		mSelectHandleLeft = context.getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark);
        mSelectHandleLeft.setTint(Color.MAGENTA);

        mSelectHandleRight = context.getDrawable(R.drawable.abc_text_select_handle_right_mtrl_dark);
        mSelectHandleRight.setTint(Color.MAGENTA);

        mSelectHandleMiddle = context.getDrawable(R.drawable.abc_text_select_handle_middle_mtrl_dark);
        mSelectHandleMiddle.setTint(Color.MAGENTA);

		imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mGestureDetector = new GestureDetector(context, new GestureListener());

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.GREEN);
		mPaint.setStrokeWidth(10);
		// 设置字体大小18dp
		setTextSize(18);

		mCursorIndex = 0;
		mCursorLine = 1;
	
		requestFocus();
        setFocusable(true);
		
		startBlink();
    }

	// cursor blink
	private Runnable blinkAction = new Runnable(){

		@Override
		public void run() {
			// TODO: Implement this method
			isShowCursor = !isShowCursor;
			postDelayed(blinkAction, 500);
			
			postInvalidate();
		}
	};

	public void setTextBuffer(TextBuffer textBuffer) {
		mTextBuffer = textBuffer;
	}

	public void setTextSize(int dip) {
		// dip to pixel
		int psize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
													dip, getResources().getDisplayMetrics());
		mTextPaint.setTextSize(psize);
	}

	public void setTypeface(Typeface typeface) {
		mTextPaint.setTypeface(typeface);
	}

	public TextPaint getPaint() {
		return mTextPaint;
	}

	private int getLineCount() {
		return mTextBuffer.getLineCount();
	}

	private int getLineHeight() {
		return mTextBuffer.getLineHeight();
	}

	private int getTextMaxWidth() {
		return mTextBuffer.getTextMaxWidth();
	}

	private int getLineNumberWidth() {
		return mTextBuffer.getLineNumberWidth();
	}

	private int getLineStart(int line) {
		return mTextBuffer.getLineStart(line);
	}


	private int getLineWidth(int line) {
		return mTextBuffer.getLineWidth(line);
	}

	public OnScrollListener getScrollListener() {
		return this;
	}


	@Override
	public int getPaddingLeft() {
		// TODO: Implement this method
		return 10;
	}

	@Override
	public void onScrollX(int scrollX, int oldX) {
		// TODO: Implement this method
		mScrollX = scrollX;
        postInvalidate();
	}

	@Override
	public void onScrollY(int scrollY, int oldY) {
		// TODO: Implement this method
		mScrollY = scrollY;
        postInvalidate();
	}


	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//		int specMode = MeasureSpec.getMode(widthMeasureSpec);
//      int width = MeasureSpec.getSize(widthMeasureSpec);
//		
//		specMode = MeasureSpec.getMode(heightMeasureSpec);
//      int height = MeasureSpec.getSize(heightMeasureSpec);

		int width = getTextMaxWidth() + screenWidth / 4;
		int height = getLineCount() * getLineHeight() + screenHeight / 4;
        setMeasuredDimension(width, height);
    }

	public void drawLineBackground(Canvas canvas) {

		canvas.drawRect(getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT, 
						getPaddingTop() + mCursorPosY, 
						getTextMaxWidth() + screenWidth / 4, 
						mCursorPosY + getLineHeight(), 
						mPaint
						);
	}

	public void drawCursor(Canvas canvas) {
		if(!isShowCursor) return;
		
		int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;
		int half = 0;
		if(mCursorPosX > left) {
			half = mCursorWidth / 2;
		}

		mDrawableCursorRes.setBounds(mCursorPosX - half, 
									 getPaddingTop() + mCursorPosY, 
									 mCursorPosX - half + mCursorWidth, 
									 mCursorPosY + getLineHeight()
									 );
		mDrawableCursorRes.draw(canvas);

		//mSelectHandleMiddle.setBounds();
	}

	// 绘制文本
	public void drawEditableText(Canvas canvas) {

		int startLine = mScrollY / getLineHeight();

		int endLine = (mScrollY + screenHeight) / getLineHeight();

		if(startLine < 1) 
			startLine = 1;

		if(endLine > getLineCount())
			endLine = getLineCount();

		float lineWidth = getLineNumberWidth();

		for(int i=startLine; i <= endLine; ++i) {
			float textX = getPaddingLeft();
			// baseline
			float textY = statusBarHeight + (i - 1) * getLineHeight() - mTextPaint.descent();

			// draw line number
			mTextPaint.setColor(Color.GRAY);
			canvas.drawText(String.valueOf(i), textX, textY, mTextPaint);

			// draw vertical line
			canvas.drawLine(lineWidth + MARGIN_LEFT / 2,  (i - 1) * getLineHeight(), lineWidth + MARGIN_LEFT / 2, i * getLineHeight(), mPaint);

			// draw content text
			textX += (lineWidth + MARGIN_LEFT);
			mTextPaint.setColor(Color.BLACK);
			canvas.drawText(mTextBuffer.getLine(i), textX, textY, mTextPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO: Implement this method
		super.onDraw(canvas);

		//绘制背景
        Drawable background = getBackground();
        if(background != null) {
            background.draw(canvas);
        }

		drawLineBackground(canvas);
		// 绘制文本
	    drawEditableText(canvas);

		drawCursor(canvas);
	}

	public void startBlink() {
		// TODO: Implement this method
		postDelayed(blinkAction, 1000);
	}
	
	
	public void stopBlink(){
		removeCallbacks(blinkAction);
		isShowCursor = true;
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO: Implement this method
		super.onWindowFocusChanged(hasWindowFocus);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO: Implement this method
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO: Implement this method
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			switch(keyCode) {
			case KeyEvent.KEYCODE_ENTER:
				insert('\n');
				break;
			case KeyEvent.KEYCODE_DEL:		
				// delete text
				delete();
				break;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// Insert char
	public void insert(char c) {
		stopBlink();
		
		mTextBuffer.insert(mCursorIndex, mCursorLine, c);
		
		++mCursorIndex;

		if(c == '\n') {
			mCursorPosX = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;
			mCursorPosY += getLineHeight();
			++mCursorLine;
		} else {
			adjustCursorPositionX();
		}

		postInvalidate();
		startBlink();
	}

	// Insert text
	public void insert(String text) {
		int length = text.length();

		for(int i=0; i < length; ++i) {
			insert(text.charAt(i));
		}
	}

	// delete text
	public void delete() {
		stopBlink();
		--mCursorIndex;

		// cursor x at first position
		if(mCursorIndex < 0) {
			mCursorIndex = 0;
			return;	// no need to delete
		}

		// get delete char
		char c = mTextBuffer.getCharAt(mCursorIndex);

		if(c == '\n') {
			mCursorPosX += getLineWidth(mCursorLine - 1);
			mCursorPosY -= getLineHeight();
			--mCursorLine;
		} else {
			adjustCursorPositionX();
		}

		mTextBuffer.delete(mCursorIndex, mCursorLine);
		postInvalidate();
		startBlink();
	}

	private void adjustCursorPositionX() {
		int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;
		int startIndex = getLineStart(mCursorLine);

		String text = mTextBuffer.getLine(mCursorLine).substring(0, mCursorIndex - startIndex);
		mCursorPosX = left + (int)mTextPaint.measureText(text);
	}

	private void adjustCursorPositionY() {
		if(mCursorPosY < getPaddingTop())
			mCursorPosY = getPaddingTop();
	}

	public void setCursorPosition(float x, float y) {
		mCursorPosX = (int) x;
		mCursorPosY = (int) y;

		adjustCursorPositionX();
		adjustCursorPositionY();
	}


	public void showSoftInput(boolean show) {
		if(show)
			imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
		else 
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}


	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		// TODO: Implement this method
		outAttrs.inputType = InputType.TYPE_NULL;
		outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
		outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI; 

		return new TextInputConnection(this, true);
	}


	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO: Implement this method
			showSoftInput(true);
			stopBlink();
			// calculation cursor y
			mCursorPosY = (int)e.getY() / getLineHeight() * getLineHeight();
			int bottom = getLineCount() * getLineHeight();	

			if(mCursorPosY < getPaddingTop())
				mCursorPosY = getPaddingTop();

			if(mCursorPosY > bottom - getLineHeight())
				mCursorPosY = bottom - getLineHeight();

			// calculation cursor x
			int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;

			int prev = left;
			int next = left;

			mCursorLine = mCursorPosY / getLineHeight() + 1;
			mCursorIndex = getLineStart(mCursorLine);

			String text = mTextBuffer.getLine(mCursorLine);
			int length = text.length();

			float[] widths = new float[length];
			mTextPaint.getTextWidths(text, widths);

			for(int i=0; next < e.getX() && i < length; ++i) {
				if(i > 0) {
					prev += widths[i - 1];
				}
				next += widths[i];
			}

			if(Math.abs(e.getX() - prev) <= Math.abs(next - e.getX())) {
				mCursorPosX = prev;
			} else {
				mCursorPosX = next;
			}

			// calculation cursor index
			if(mCursorPosX > left) {
				for(int j=0; left < mCursorPosX && j < length; ++j) {
					left += widths[j];
					++mCursorIndex;
				}
			}

			Log.i(TAG, "mCursorIndex: " + mCursorIndex);
			
			postInvalidate();
			startBlink();
			return super.onSingleTapUp(e);
		}


		@Override
		public void onLongPress(MotionEvent e) {
			// TODO: Implement this method
			super.onLongPress(e);
		}
	}


	class TextInputConnection extends BaseInputConnection {

		public TextInputConnection(View view, boolean fullEditor) {
			super(view, fullEditor);
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition) {
			// TODO: Implement this method
			insert(text.toString());
			return true;
		}

		@Override
		public boolean deleteSurroundingText(int beforeLength, int afterLength) {
			// TODO: Implement this method
			return super.deleteSurroundingText(beforeLength, afterLength);
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event) {
			// TODO: Implement this method
			return onKeyDown(event.getKeyCode(), event);
		}


		@Override
		public boolean finishComposingText() {
			// TODO: Implement this method
			return true;
		}
	}
}

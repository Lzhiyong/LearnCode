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
import android.graphics.Path;
import android.graphics.PorterDuff;
import androidx.core.graphics.drawable.DrawableCompat;


public class HighlightTextView extends View implements OnScrollListener {

    private Paint mPaint;
    private TextPaint mTextPaint;

    private Drawable mDrawableCursorRes;
    private Drawable mTextSelectHandleLeftRes;
    private Drawable mTextSelectHandleRightRes;
    private Drawable mTextSelectHandleMiddleRes;

    private int mScrollX, mScrollY;
    private int mCursorPosX, mCursorPosY;
    private int mCursorLine, mCursorIndex;
    private int mCursorWidth, mCursorHeight;

    private int waterDropWidth, waterDropHeight;
    private int statusBarHeight;
    private int actionBarHeight;
    private int screenWidth, screenHeight;

    private int selectHandleWidth, selectHandleHeight;
    private int selectHandleLeftX, selectHandleLeftY;
    private int selectHandleRightX, selectHandleRightY;

    private int selectStart, selectEnd;

    private TextBuffer mTextBuffer;

    private InputMethodManager imm;

    private GestureDetector mGestureDetector;
    private GestureListener mGestureListener;

    private boolean showCursor = true;
    private boolean showWaterDrop = false;
    private boolean hasSelectText = false;

    private final int MARGIN_LEFT = 100;

    private final String TAG = this.getClass().getSimpleName();


    public HighlightTextView(Context context) {
        super(context);
        initView(context);
    }

    public HighlightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public HighlightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {

        screenWidth = ScreenUtils.getScreenHeight(context);
        screenHeight = ScreenUtils.getScreenHeight(context);
        statusBarHeight = ScreenUtils.getStatusBarHeight(context);
        actionBarHeight = ScreenUtils.getActionBarHeight(context);

        mDrawableCursorRes = context.getDrawable(R.drawable.abc_text_cursor_material);
        mDrawableCursorRes.setTint(Color.MAGENTA);

        mCursorWidth = mDrawableCursorRes.getIntrinsicWidth();
        mCursorHeight = mDrawableCursorRes.getIntrinsicHeight();
        Log.i(TAG, "mCursorWidth: " + mCursorWidth);
        Log.i(TAG, "mCursorHeight: " + mCursorHeight);
        // set cursor width
        if(mCursorWidth > 5) mCursorWidth = 5;

        // left water
        mTextSelectHandleLeftRes = context.getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark);
        mTextSelectHandleLeftRes.setTint(Color.MAGENTA);
        mTextSelectHandleLeftRes.setColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_IN);

        selectHandleWidth = mTextSelectHandleLeftRes.getIntrinsicWidth();
        selectHandleHeight = mTextSelectHandleLeftRes.getIntrinsicHeight();
        Log.i(TAG, "selectHandleWidth: " + selectHandleWidth);
        Log.i(TAG, "selectHandleHeight: " + selectHandleHeight);

        // right water
        mTextSelectHandleRightRes = context.getDrawable(R.drawable.abc_text_select_handle_right_mtrl_dark);
        mTextSelectHandleRightRes.setTint(Color.MAGENTA);

        // middle water
        mTextSelectHandleMiddleRes = context.getDrawable(R.drawable.abc_text_select_handle_middle_mtrl_dark);
        mTextSelectHandleMiddleRes.setTint(Color.MAGENTA);
        waterDropWidth = mTextSelectHandleMiddleRes.getIntrinsicWidth();
        waterDropHeight = mTextSelectHandleMiddleRes.getIntrinsicHeight();

        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mGestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(context, mGestureListener);
        //mGestureDetector.setIsLongpressEnabled(false);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(10);
        // set text size 18dp
        setTextSize(18);

        mCursorIndex = 0;
        mCursorLine = 1;

        requestFocus();
        setFocusable(true);

        startBlink();
    }


    // cursor blink
    private Runnable blinkAction = new Runnable() {

        @Override
        public void run() {
            // TODO: Implement this method
            showCursor = !showCursor;
            postDelayed(blinkAction, 500);

            postInvalidate();
        }
    };

    // water drop
    private Runnable waterDropAction = new Runnable(){

        @Override
        public void run() {
            // TODO: Implement this method
            showWaterDrop = false;
        }
    };

    public void startBlink() {
        // TODO: Implement this method
        postDelayed(blinkAction, 1000);
    }


    public void stopBlink(boolean show) {
        removeCallbacks(blinkAction);
        // show cursor
        showCursor = show;
    }

    public void showWaterDrop(boolean show) {
        removeCallbacks(waterDropAction);
        showWaterDrop = show;
    }

    public void hideWaterDrop() {
        postDelayed(waterDropAction, 3000);
    }

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

    public GestureDetector getGestureDetector() {
        return mGestureDetector;
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

//        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//
//        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);

        int width = getTextMaxWidth() + screenWidth / 4;
        int height = getLineCount() * getLineHeight() + screenHeight / 4;
        setMeasuredDimension(width, height);
    }

    // draw line background
    public void drawLineBackground(Canvas canvas) {
        if(!hasSelectText) {
            // draw current line background
            canvas.drawRect(getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT,
                            getPaddingTop() + mCursorPosY,
                            getTextMaxWidth() + screenWidth / 4,
                            mCursorPosY + getLineHeight(),
                            mPaint
                            );
        } else {
            // draw select text background
            //Path path = new Path();
            //path.moveTo(selectHandleLeftX, selectHandleLeftY - getLineHeight());
            mPaint.setColor(Color.YELLOW);

            int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;
            // get the space width
            int spaceWidth = mTextBuffer.getCharWidth(' ');
            int lineHeight = getLineHeight();

            int start = selectHandleLeftY / getLineHeight();
            int end = selectHandleRightY / getLineHeight();

            // start line < end line
            if(start != end) {
                for(int i=start; i <= end; ++i) {
                    int lineWidth = getLineWidth(i) + spaceWidth;
                    if(i == start) {
                        canvas.drawRect(selectHandleLeftX, selectHandleLeftY - lineHeight,
                                        left + lineWidth, selectHandleLeftY, mPaint);
                    } else if(i == end) {
                        canvas.drawRect(left, selectHandleRightY - lineHeight,
                                        selectHandleRightX, selectHandleRightY, mPaint);
                    } else {
                        canvas.drawRect(left, (i - 1) * lineHeight,
                                        left + lineWidth, i * lineHeight, mPaint);
                    }
                }
            } else {
                // start line = end line
                canvas.drawRect(selectHandleLeftX, selectHandleLeftY - getLineHeight(),
                                selectHandleRightX, selectHandleRightY, mPaint);
            }

            mPaint.setColor(Color.GREEN);
        }
    }

    // draw text select handle
    public void drawSelectHandle(Canvas canvas) {
        if(hasSelectText) {
            // left water drop
            mTextSelectHandleLeftRes.setBounds(selectHandleLeftX - selectHandleWidth + selectHandleWidth / 4,
                                               selectHandleLeftY,
                                               selectHandleLeftX + selectHandleWidth / 4,
                                               selectHandleLeftY + selectHandleHeight
                                               );
            mTextSelectHandleLeftRes.draw(canvas);

            // right water drop
            mTextSelectHandleRightRes.setBounds(selectHandleRightX - selectHandleWidth / 4,
                                                selectHandleRightY,
                                                selectHandleRightX + selectHandleWidth - selectHandleWidth / 4,
                                                selectHandleRightY + selectHandleHeight
                                                );
            mTextSelectHandleRightRes.draw(canvas);
        }
    }

    // draw cursor
    public void drawCursor(Canvas canvas) {
        if(showCursor) {

            int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;
            int half = 0;
            if(mCursorPosX > left) {
                half = mCursorWidth / 2;
            }

            // draw text cursor 
            mDrawableCursorRes.setBounds(mCursorPosX - half,
                                         getPaddingTop() + mCursorPosY,
                                         mCursorPosX - half + mCursorWidth,
                                         mCursorPosY + getLineHeight()
                                         );
            mDrawableCursorRes.draw(canvas);
        }

        if(showWaterDrop) {
            // draw text select handle middle 
            mTextSelectHandleMiddleRes.setBounds(mCursorPosX - waterDropWidth / 2,
                                                 mCursorPosY + getLineHeight(),
                                                 mCursorPosX + waterDropWidth / 2,
                                                 mCursorPosY + getLineHeight() + waterDropHeight
                                                 );
            mTextSelectHandleMiddleRes.draw(canvas);
        }
    }

    // draw content text
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

        // draw background
        Drawable background = getBackground();
        if(background != null) {
            background.draw(canvas);
        }

        drawLineBackground(canvas);

        // draw content text
        drawEditableText(canvas);

        drawSelectHandle(canvas);

        drawCursor(canvas);
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        // TODO: Implement this method
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO: Implement this method
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            getParent().requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_UP:
            //getParent().requestDisallowInterceptTouchEvent(false);
            mGestureListener.onUp(event);
            break;
        }

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
        stopBlink(true);
        showWaterDrop(false);

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
        stopBlink(true);
        showWaterDrop(false);
        --mCursorIndex;

        // cursor x at first position
        if(mCursorIndex < 0) {
            mCursorIndex = 0;
            startBlink();
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

//    private void adjustCursorPositionY() {
//        if(mCursorPosY < getPaddingTop())
//            mCursorPosY = getPaddingTop();
//    }

    // set cursor position
    public void setCursorPosition(float x, float y) {
        // calculation the cursor y coordinate
        mCursorPosY = (int)y / getLineHeight() * getLineHeight();
        int bottom = getLineCount() * getLineHeight();

        if(mCursorPosY < getPaddingTop())
            mCursorPosY = getPaddingTop();

        if(mCursorPosY > bottom - getLineHeight())
            mCursorPosY = bottom - getLineHeight();

        // estimate the cursor x position
        int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;

        int prev = left;
        int next = left;

        mCursorLine = mCursorPosY / getLineHeight() + 1;
        mCursorIndex = getLineStart(mCursorLine);

        String text = mTextBuffer.getLine(mCursorLine);
        int length = text.length();

        float[] widths = new float[length];
        mTextPaint.getTextWidths(text, widths);

        for(int i=0; next < x && i < length; ++i) {
            if(i > 0) {
                prev += widths[i - 1];
            }
            next += widths[i];
        }

        // calculation the cursor x coordinate
        if(Math.abs(x - prev) <= Math.abs(next - x)) {
            mCursorPosX = prev;
        } else {
            mCursorPosX = next;
        }

        // calculation the cursor index
        if(mCursorPosX > left) {
            for(int j=0; left < mCursorPosX && j < length; ++j) {
                left += widths[j];
                ++mCursorIndex;
            }
        }
    }

    // toogle soft keyboard
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

    // 
    private void findNearestWord() {
        int left = getPaddingLeft() + getLineNumberWidth() + MARGIN_LEFT;

        String text = mTextBuffer.getLine(mCursorLine);
        int start = getLineStart(mCursorLine);
        int end = start + text.length() - 1;

        // select text start index
        for(selectStart = mCursorIndex; selectStart >= start; --selectStart) {
            char c = mTextBuffer.getCharAt(selectStart);
            if(!Character.isJavaIdentifierPart(c))
                break;
        }

        // select text end index
        for(selectEnd = mCursorIndex; selectEnd <= end; ++selectEnd) {
            char c = mTextBuffer.getCharAt(selectEnd);
            if(!Character.isJavaIdentifierPart(c))
                break;
        }

        // select start index need add
        ++selectStart;
        if(selectStart <= selectEnd) {
            // get the select word
            String selectWord = text.substring(selectStart - start, selectEnd - start);
            //Log.i(TAG, "word:" + selectWord);

            if(selectWord != null && !selectWord.equals("")) {
                hasSelectText = true;
                // select handle left (x y)
                selectHandleLeftX = left + (int)mTextPaint.measureText(text.substring(0, selectStart - start));

                selectHandleRightX = left + (int)mTextPaint.measureText(text.substring(0, selectEnd - start));

                selectHandleLeftY = selectHandleRightY = mCursorPosY + getLineHeight();
            }
        }
    }



    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean touchOnSelectHandleMiddle = false;
        private boolean touchOnSelectHandleLeft = false;
        private boolean touchOnSelectHandleRight = false;

        // exchange text select handle left and right
        private void swapSelectHandle() {

            selectHandleLeftX = selectHandleLeftX ^ selectHandleRightX;
            selectHandleRightX = selectHandleLeftX ^ selectHandleRightX;
            selectHandleLeftX = selectHandleLeftX ^ selectHandleRightX;
            
            selectHandleLeftY = selectHandleLeftY ^ selectHandleRightY;
            selectHandleRightY = selectHandleLeftY ^ selectHandleRightY;
            selectHandleLeftY = selectHandleLeftY ^ selectHandleRightY;

            selectStart = selectStart ^ selectEnd;
            selectEnd = selectStart ^ selectEnd;
            selectStart = selectStart ^ selectEnd;
            
            touchOnSelectHandleLeft = !touchOnSelectHandleLeft;
            touchOnSelectHandleRight = !touchOnSelectHandleRight;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO: Implement this method
            float x = e.getX();
            float y = e.getY();

            // touch middle water drop
            if(showWaterDrop && x >= mCursorPosX - waterDropWidth / 2 && x <= mCursorPosX + waterDropWidth / 2
               && y >= mCursorPosY + getLineHeight() && y <= mCursorPosY + getLineHeight() + waterDropHeight) {

                touchOnSelectHandleMiddle = true;
                stopBlink(true);
                showWaterDrop(true);
            }

            // touch left water drop
            if(hasSelectText && x >= selectHandleLeftX - selectHandleWidth + selectHandleWidth / 4 
               && x <= selectHandleLeftX + selectHandleWidth / 4 
               && y >= selectHandleLeftY && y <= selectHandleLeftY + selectHandleHeight) {

                touchOnSelectHandleLeft = true;
                stopBlink(false);
                showWaterDrop(false);
            }

            // touch right water drop
            if(hasSelectText && x >= selectHandleRightX - selectHandleWidth / 4 
               && x <= selectHandleRightX + selectHandleWidth - selectHandleWidth / 4 
               && y >= selectHandleRightY && y <= selectHandleRightY + selectHandleHeight) {

                touchOnSelectHandleRight = true;
                stopBlink(false);
                showWaterDrop(false);
            }

            return super.onDown(e);
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO: Implement this method
            showSoftInput(true);
            // stop cursor blink
            stopBlink(true);
            // show water drop
            showWaterDrop(true);

            setCursorPosition(e.getX(), e.getY());
            //Log.i(TAG, "mCursorIndex: " + mCursorIndex);
            postInvalidate();
            // cursor start blink
            startBlink();
            // hide water drop
            hideWaterDrop();

            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(touchOnSelectHandleMiddle) {
                // Y = getY() - getLineHeight() - getLineHeight() / 2
                // e2.getY()现在按下的位置要比光标所有位置大(按在水滴上)
                // 所以实际光标所在位置等于e2.getY()减去一些距离
                setCursorPosition(e2.getX(), e2.getY() - getLineHeight() * 3 / 2);

            } else if(touchOnSelectHandleLeft) {
                // calculation select handle left coordinate and index
                setCursorPosition(e2.getX(), e2.getY() - getLineHeight() * 3 / 2);
                selectHandleLeftX = mCursorPosX;
                selectHandleLeftY = mCursorPosY + getLineHeight();
                selectStart = mCursorIndex;

            } else if(touchOnSelectHandleRight) {
                // calculation select handle right coordinate and index
                setCursorPosition(e2.getX(), e2.getY() - getLineHeight() * 3 / 2);
                selectHandleRightX = mCursorPosX;
                selectHandleRightY = mCursorPosY + getLineHeight();
                selectEnd = mCursorIndex;

            } else {
                onUp(e2);
            }
            
            if(hasSelectText && ((selectHandleLeftY > selectHandleRightY) 
               || (selectHandleLeftY == selectHandleRightY 
               && selectHandleLeftX > selectHandleRightX))) {
               // exchange
               swapSelectHandle();
           }

            postInvalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }


        @Override
        public void onLongPress(MotionEvent e) {
            // TODO: Implement this method
            super.onLongPress(e);
            if(!touchOnSelectHandleMiddle) {
                setCursorPosition(e.getX(), e.getY());
                findNearestWord();
            } else {
                onUp(e);
            }
            postInvalidate();
        }

        // 
        public void onUp(MotionEvent e) {
            HighlightTextView.this.getParent().requestDisallowInterceptTouchEvent(false);

            if(touchOnSelectHandleMiddle || touchOnSelectHandleLeft 
               || touchOnSelectHandleRight) {

                touchOnSelectHandleMiddle = false;
                touchOnSelectHandleLeft = false;
                touchOnSelectHandleRight = false;
                startBlink();
                hideWaterDrop();
            }
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

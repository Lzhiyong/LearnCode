package com.text.edit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.util.Collections;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.util.Log;


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
    private int screenWidth, screenHeight;

    private int selectHandleWidth, selectHandleHeight;
    private int selectHandleLeftX, selectHandleLeftY;
    private int selectHandleRightX, selectHandleRightY;

    private int selectionStart, selectionEnd;

    private int tabWidth, spaceWidth;
    private int mTextWidth, mTextHeight;

    private TextBuffer mTextBuffer;

    private TextScrollView mScrollView;
    private TextHorizontalScrollView mHorizontalScrollView;

    private GestureDetector mGestureDetector;
    private GestureListener mGestureListener;

    private ClipboardManager mClipboard;

    private long lastTapTime = 0L;
    private boolean showCursor = true;
    private boolean showWaterDrop = false;
    private boolean hasSelectText = false;

    private final int SPACEING = 100;

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

        screenWidth = ScreenUtils.getScreenWidth(context);
        screenHeight = ScreenUtils.getScreenHeight(context);

        mDrawableCursorRes = context.getDrawable(R.drawable.abc_text_cursor_material);
        mDrawableCursorRes.setTint(Color.MAGENTA);

        mCursorWidth = mDrawableCursorRes.getIntrinsicWidth();
        mCursorHeight = mDrawableCursorRes.getIntrinsicHeight();

        // set cursor width
        if(mCursorWidth > 5) mCursorWidth = 5;

        // left water
        mTextSelectHandleLeftRes = context.getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark);
        mTextSelectHandleLeftRes.setTint(Color.MAGENTA);
        mTextSelectHandleLeftRes.setColorFilter(Color.MAGENTA, PorterDuff.Mode.SRC_IN);

        selectHandleWidth = mTextSelectHandleLeftRes.getIntrinsicWidth();
        selectHandleHeight = mTextSelectHandleLeftRes.getIntrinsicHeight();

        // right water
        mTextSelectHandleRightRes = context.getDrawable(R.drawable.abc_text_select_handle_right_mtrl_dark);
        mTextSelectHandleRightRes.setTint(Color.MAGENTA);

        // middle water
        mTextSelectHandleMiddleRes = context.getDrawable(R.drawable.abc_text_select_handle_middle_mtrl_dark);
        mTextSelectHandleMiddleRes.setTint(Color.MAGENTA);
        waterDropWidth = mTextSelectHandleMiddleRes.getIntrinsicWidth();
        waterDropHeight = mTextSelectHandleMiddleRes.getIntrinsicHeight();

        mGestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(context, mGestureListener);
        //mGestureDetector.setIsLongpressEnabled(false);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        setTextSize(18);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(10);

        mClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        spaceWidth = (int) mTextPaint.measureText(String.valueOf(' '));
        tabWidth = spaceWidth * 4;

        mCursorIndex = 0;
        mCursorLine = 1;

        requestFocus();
        setFocusable(true);
        postDelayed(blinkAction, 1000);
    }

    // cursor blink
    private Runnable blinkAction = new Runnable() {

        @Override
        public void run() {
            // TODO: Implement this method
            showCursor = !showCursor;
            postDelayed(blinkAction, 500);
            if(System.currentTimeMillis() - lastTapTime >= 3000) {
                showWaterDrop = false;
            }
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

    //
    public void setScrollView(TextScrollView scrollView, 
                              TextHorizontalScrollView horizontalScrollView) {
        mScrollView = scrollView;
        mHorizontalScrollView = horizontalScrollView;

        if(mScrollView != null) {
            mScrollView.setScrollListener(this);
            mScrollView.setSmoothScrollingEnabled(true);
        }

        if(mHorizontalScrollView != null) {
            mHorizontalScrollView.setScrollListener(this);
            mHorizontalScrollView.setSmoothScrollingEnabled(true);
        }
    }

    // get width list max item
    private int getTextWidth() {
        return Collections.max(mTextBuffer.getWidthList());
    }

    private int getTextHeight() {
        return getLineCount() * getLineHeight();
    }

    public int getLeftSpace() {
        return getPaddingLeft() + getLineNumberWidth() + SPACEING;
    }


    // ===========================================
    // TextBuffer method
    private int getLineCount() {
        return mTextBuffer.getLineCount();
    }

    private int getLineHeight() {
        return mTextBuffer.getLineHeight();
    }

    private int getCharWidth(int index) {
        return mTextBuffer.getCharWidth(mTextBuffer.getCharAt(index));
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

    // ===========================================


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

    // Get the maximum scrollable width
    public int getMaxScrollX() {
        return Math.max(screenWidth, getLeftSpace() + getTextWidth() + spaceWidth * 4);
    }

    // Get the maximum scrollable height
    public int getMaxScrollY() {
        return Math.max(screenHeight / 2, getTextHeight() + getLineHeight() * 2);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(getMaxScrollX(), getMaxScrollY());
    }


    // draw line background
    public void drawLineBackground(Canvas canvas) {

        if(!hasSelectText) {
            // draw current line background
            int left = getLeftSpace();
            canvas.drawRect(left,
                            getPaddingTop() + mCursorPosY,
                            Math.max(left + mTextWidth + spaceWidth * 4, screenWidth),
                            mCursorPosY + getLineHeight(),
                            mPaint
                            );
        } else {
            // draw select text background
            //Path path = new Path();
            //path.moveTo(selectHandleLeftX, selectHandleLeftY - getLineHeight());
            mPaint.setColor(Color.YELLOW);

            int left = getLeftSpace();
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
            // select handle left
            mTextSelectHandleLeftRes.setBounds(selectHandleLeftX - selectHandleWidth + selectHandleWidth / 4,
                                               selectHandleLeftY,
                                               selectHandleLeftX + selectHandleWidth / 4,
                                               selectHandleLeftY + selectHandleHeight
                                               );
            mTextSelectHandleLeftRes.draw(canvas);

            // select handle right
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
            int left = getLeftSpace();
            int half = 0;
            if(mCursorPosX >= left) {
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

        int startLine = Math.max(mScrollY / getLineHeight(), 1);

        int endLine = Math.min((mScrollY + mScrollView.getHeight()) / getLineHeight() + 1, getLineCount());

        int lineNumWidth = getLineNumberWidth();

        for(int i=startLine; i <= endLine; ++i) {

            int textX = getPaddingLeft();
            // baseline
            int textY =  i * getLineHeight() - (int)mTextPaint.descent();

            // draw line number
            mTextPaint.setColor(Color.GRAY);
            canvas.drawText(String.valueOf(i), textX, textY, mTextPaint);

            // draw vertical line
            canvas.drawLine(lineNumWidth + SPACEING / 2,  (i - 1) * getLineHeight(), lineNumWidth + SPACEING / 2, i * getLineHeight(), mPaint);

            // draw content text
            textX += (lineNumWidth + SPACEING);
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
                if(!hasSelectText) {
                    // delete char at cursor index
                    delete();
                } else {
                    delete(selectionStart, selectionEnd);
                    hasSelectText = false;
                }
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // when the text has changed, you need to re-layout
    public void onTextChanged() {
        int left = getLeftSpace();
        int width = getTextWidth();
        if((mTextWidth < width && left + width >=  screenWidth - spaceWidth * 2)
           || (mCursorPosX == left && mTextHeight <= screenHeight / 2)
           || (mTextHeight < getTextHeight())
           || (mCursorPosY - mScrollY < 0 && mCursorPosY - mScrollY >= -getLineHeight())) {
            // to re-layout
            requestLayout();
        } else {
            scrollToVisable(spaceWidth * 3, 0, spaceWidth * 2, getLineHeight());
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO: Implement this method
        super.onLayout(changed, left, top, right, bottom);

        mTextWidth = getTextWidth();
        mTextHeight = getTextHeight();
        // after layout needs to scroll
        scrollToVisable(spaceWidth * 3, 0, spaceWidth * 2, getLineHeight());
    }

    /**
     * @param left: left margin
     * @param top: top margin
     * @param right: right margin
     * @param bottom: bottom margin
     */
    private void scrollToVisable(int left, int top, int right, int bottom) {
        // horizontal direction
        int dx = 0;
        if(mCursorPosX - mScrollX <= left) 
            dx = mCursorPosX - mScrollX - left;
        else if(mCursorPosX - mScrollX >= screenWidth - right) 
            dx = mCursorPosX - screenWidth - mScrollX + right;

        mHorizontalScrollView.smoothScrollBy(dx, 0); 

        // vertical direction
        int dy = 0;
        if(mCursorPosY - mScrollY <= top)
            dy = mCursorPosY - mScrollY - top;
        else if(mCursorPosY - mScrollY >= mScrollView.getHeight() - bottom)
            dy = mCursorPosY - mScrollY - mScrollView.getHeight() + bottom;

        mScrollView.smoothScrollBy(0, dy);

    }


    // Insert char
    private void insert(char c) {
        removeCallbacks(blinkAction);
        showCursor = true;
        showWaterDrop = false;

        mTextBuffer.insert(mCursorIndex, mCursorLine, c);

        ++mCursorIndex;

        if(c == '\n') {
            mCursorPosX = getLeftSpace();
            mCursorPosY += getLineHeight();
            ++mCursorLine;
        } else {
            adjustCursorPositionX();
        }

        postInvalidate();
        onTextChanged();
        postDelayed(blinkAction, 1000);
    }

    // Insert text
    public void insert(String text) {
        int length = text.length();

        for(int i=0; i < length; ++i) {
            insert(text.charAt(i));
        }
    }

    // delete text
    private void delete() {
        removeCallbacks(blinkAction);
        showCursor = true;
        showWaterDrop = false;

        --mCursorIndex;
        // cursor x at first position
        if(mCursorIndex < 0) {
            mCursorIndex = 0;
            postDelayed(blinkAction, 1000);
            return;	// no need to delete
        }

        // get delete char
        char c = mTextBuffer.getCharAt(mCursorIndex);

        if(c == '\n') {
            int left = 0;
            if(mCursorLine == getLineCount()) {
                left = getPaddingLeft()  + SPACEING
                    + String.valueOf(mCursorLine - 1).length() * mTextBuffer.getCharWidth('0');
            } else {
                left = getLeftSpace();
            }

            mCursorPosX = left + getLineWidth(mCursorLine - 1);
            mCursorPosY -= getLineHeight();
            --mCursorLine;
        } else {
            adjustCursorPositionX();
        }

        mTextBuffer.delete(mCursorIndex, mCursorLine);

        postInvalidate();
        onTextChanged();
        postDelayed(blinkAction, 1000);
    }

    // delete char at index
    public void delete(int index){
        mCursorIndex = index + 1;
        // delete char at cursor index
        delete();
    }
    
    // delete text at index[start..end)
    public void delete(int start, int end) {
        // cursor index == end index
        mCursorIndex = end;
        for(int i=end; i > start; --i) {
            // delete char at cursor index
            delete();
        }
    }

    // copy text
    public void copy() {
        String text = getSelectText();
        if(text != null && !text.equals("")) {
            ClipData data = ClipData.newPlainText("content", text);
            mClipboard.setPrimaryClip(data);
        }
    }

    // cut text
    public void cut() {
        copy();
        delete(selectionStart, selectionEnd);
        hasSelectText = false;
    }

    // paste text
    public void paste() {
        if(mClipboard.hasPrimaryClip()) {
            String text = null;
            ClipDescription description = mClipboard.getPrimaryClipDescription();
            
            if(description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                ClipData data = mClipboard.getPrimaryClip();
                ClipData.Item item = data.getItemAt(0);
                text = item.getText().toString();
            }
            
            if(hasSelectText) {
                delete(selectionStart, selectionEnd);
                hasSelectText = false;
            }
            insert(text);
        }
    }

    // find text
    public void find(String str) {

    }

    // replace text
    public void replace(String str, String regex) {

    }

    public void selectAll() {
        removeCallbacks(blinkAction);
        showCursor = showWaterDrop = false;
        hasSelectText = true;

        selectionStart = 0;
        selectionEnd = mTextBuffer.getLength() - 1;

        selectHandleLeftX = getLeftSpace();
        selectHandleLeftY = getLineHeight();

        selectHandleRightX = getLeftSpace() + getLineWidth(getLineCount());
        selectHandleRightY = getLineCount() * getLineHeight();

        postInvalidate();
    }

    public String getSelectText() {
        if(hasSelectText) {
            return mTextBuffer.getText(selectionStart, selectionEnd);
        }
        return null;
    }

    // goto line
    public void gotoLine(int line) {
        if(line < 1) line = 1;

        if(line > getLineCount()) 
            line = getLineCount();

        mCursorIndex = getLineStart(line);
        mCursorLine = line;
        mCursorPosX = getLeftSpace();
        mCursorPosY = (line - 1) * getLineHeight();

        mHorizontalScrollView.smoothScrollTo(0, mScrollY);
        mScrollView.smoothScrollTo(0, Math.max(mCursorPosY - screenHeight / 3, 0));
    }

    public void redo() {

    }

    public void undo() {

    }

    private void adjustCursorPositionX() {
        int start = getLineStart(mCursorLine);

        String text = mTextBuffer.getLine(mCursorLine).substring(0, mCursorIndex - start);

        mCursorPosX = getLeftSpace() + (int)mTextPaint.measureText(text);
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
        int left = getLeftSpace();

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
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(show)
            imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        else
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // TODO: Implement this method

        outAttrs.inputType = InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            | EditorInfo.IME_ACTION_DONE
            | EditorInfo.IME_FLAG_NO_EXTRACT_UI;


//        outAttrs.inputType = InputType.TYPE_NULL;
//        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
//        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;

        return new TextInputConnection(this, true);
    }


    // auto scroll select handle and cursor
    private void onMove(int slopX, int slopY) {
        if(mCursorPosX - mScrollX <= slopX) {
            // left scroll
            mHorizontalScrollView.scrollBy(-getCharWidth(mCursorIndex), 0);
        } else if(mCursorPosX - mScrollX >= screenWidth - slopX) {
            // right scroll
            mHorizontalScrollView.scrollBy(getCharWidth(mCursorIndex + 1), 0);
        } else if(mCursorPosY - mScrollY <= 0) {
            // up scroll
            mScrollView.scrollBy(0, -getLineHeight());
        } else if(mCursorPosY - mScrollY >= mScrollView.getHeight() - slopY) {
            // down scroll
            mScrollView.scrollBy(0, getLineHeight());
        }
    }  

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean touchOnSelectHandleMiddle = false;
        private boolean touchOnSelectHandleLeft = false;
        private boolean touchOnSelectHandleRight = false;

        // for auto scroll select handle
        private Runnable moveAction = new Runnable() {

            @Override
            public void run() {
                // TODO: Implement this method
                onMove(spaceWidth * 4, getLineHeight());
                postDelayed(moveAction, 250);
            }
        };

        // when on long press to select a word
        private void findNearestWord() {
            int left = getLeftSpace();

            String text = mTextBuffer.getLine(mCursorLine);
            int start = getLineStart(mCursorLine);
            int end = start + text.length() - 1;

            // select text start index
            for(selectionStart = mCursorIndex; selectionStart >= start; --selectionStart) {
                char c = mTextBuffer.getCharAt(selectionStart);
                if(!Character.isJavaIdentifierPart(c))
                    break;
            }

            // select text end index
            for(selectionEnd = mCursorIndex; selectionEnd <= end; ++selectionEnd) {
                char c = mTextBuffer.getCharAt(selectionEnd);
                if(!Character.isJavaIdentifierPart(c))
                    break;
            }

            // select start index needs to be incremented by 1
            ++selectionStart;
            if(selectionStart < selectionEnd) {
                removeCallbacks(blinkAction);
                showCursor = showWaterDrop = false;
                hasSelectText = true;
                // select handle left (x y)
                selectHandleLeftX = left + (int)mTextPaint.measureText(text.substring(0, selectionStart - start));

                selectHandleRightX = left + (int)mTextPaint.measureText(text.substring(0, selectionEnd - start));

                selectHandleLeftY = selectHandleRightY = mCursorPosY + getLineHeight();
            }
        }


        // swap text select handle left and right
        private void swapSelection() {

            selectHandleLeftX = selectHandleLeftX ^ selectHandleRightX;
            selectHandleRightX = selectHandleLeftX ^ selectHandleRightX;
            selectHandleLeftX = selectHandleLeftX ^ selectHandleRightX;

            selectHandleLeftY = selectHandleLeftY ^ selectHandleRightY;
            selectHandleRightY = selectHandleLeftY ^ selectHandleRightY;
            selectHandleLeftY = selectHandleLeftY ^ selectHandleRightY;

            selectionStart = selectionStart ^ selectionEnd;
            selectionEnd = selectionStart ^ selectionEnd;
            selectionStart = selectionStart ^ selectionEnd;

            touchOnSelectHandleLeft = !touchOnSelectHandleLeft;
            touchOnSelectHandleRight = !touchOnSelectHandleRight;
        }

        // when single tap to check the select region
        private boolean checkSelectRegion(float x, float y) {

            if(y < selectHandleLeftY - getLineHeight() || y > selectHandleRightY)
                return false;

            // on the same line
            if(selectHandleLeftY == selectHandleRightY) {
                if(x < selectHandleLeftX || x > selectHandleRightX)
                    return false;
            } else {
                // not on the same line
                int left = getLeftSpace();
                int line = (int)y / getLineHeight() + 1;
                int width = getLineWidth(line) + spaceWidth;
                // select start line
                if(line == selectHandleLeftY / getLineHeight()) {
                    if(x < selectHandleLeftX || x > left + width)
                        return false;
                } else if(line == selectHandleRightY / getLineHeight()) {
                    // select end line
                    if(x < left || x > selectHandleRightX)
                        return false;
                } else {
                    if(x < left || x > left + width) 
                        return false;
                }
            }

            return true;
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
                removeCallbacks(blinkAction);
                showCursor = showWaterDrop = true;
            }

            // touch left water drop
            if(hasSelectText && x >= selectHandleLeftX - selectHandleWidth + selectHandleWidth / 4 
               && x <= selectHandleLeftX + selectHandleWidth / 4 
               && y >= selectHandleLeftY && y <= selectHandleLeftY + selectHandleHeight) {

                touchOnSelectHandleLeft = true;
                removeCallbacks(blinkAction);
                showCursor = showWaterDrop = false;
            }

            // touch right water drop
            if(hasSelectText && x >= selectHandleRightX - selectHandleWidth / 4 
               && x <= selectHandleRightX + selectHandleWidth - selectHandleWidth / 4 
               && y >= selectHandleRightY && y <= selectHandleRightY + selectHandleHeight) {

                touchOnSelectHandleRight = true;
                removeCallbacks(blinkAction);
                showCursor = showWaterDrop = false;
            }

            return super.onDown(e);
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO: Implement this method
            float x = e.getX();
            float y = e.getY();

            showSoftInput(true);

            if(!hasSelectText || !checkSelectRegion(x, y)) {
                // stop cursor blink
                removeCallbacks(blinkAction);
                showCursor = showWaterDrop = true;
                hasSelectText = false;

                setCursorPosition(x, y);
                //Log.i(TAG, "mCursorIndex: " + mCursorIndex);
                postInvalidate();
                lastTapTime = System.currentTimeMillis();
                // cursor start blink
                postDelayed(blinkAction, 1000);
            } 

            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(touchOnSelectHandleMiddle) {
                // calculation select handle middle coordinate and index
                removeCallbacks(moveAction);
                post(moveAction);
                setCursorPosition(e2.getX(), 
                                  e2.getY() - getLineHeight() - Math.min(getLineHeight(), selectHandleHeight) / 2);

            } else if(touchOnSelectHandleLeft) {
                removeCallbacks(moveAction);
                post(moveAction);
                // calculation select handle left coordinate and index
                setCursorPosition(e2.getX(), 
                                  e2.getY() - getLineHeight() - Math.min(getLineHeight(), selectHandleHeight) / 2);
                selectHandleLeftX = mCursorPosX;
                selectHandleLeftY = mCursorPosY + getLineHeight();
                selectionStart = mCursorIndex;

            } else if(touchOnSelectHandleRight) {
                removeCallbacks(moveAction);
                post(moveAction);
                // calculation select handle right coordinate and index
                setCursorPosition(e2.getX(), 
                                  e2.getY() - getLineHeight() - Math.min(getLineHeight(), selectHandleHeight) / 2);
                selectHandleRightX = mCursorPosX;
                selectHandleRightY = mCursorPosY + getLineHeight();
                selectionEnd = mCursorIndex;

            } else {
                onUp(e2);
            }

            if(hasSelectText && ((selectHandleLeftY > selectHandleRightY) 
               || (selectHandleLeftY == selectHandleRightY 
               && selectHandleLeftX > selectHandleRightX))) {
                // swap selection handle
                swapSelection();
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

                removeCallbacks(moveAction);
                touchOnSelectHandleMiddle = false;
                touchOnSelectHandleLeft = false;
                touchOnSelectHandleRight = false;
                if(!hasSelectText) {
                    lastTapTime = System.currentTimeMillis();
                    postDelayed(blinkAction, 1000);
                }
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
            return super.commitText(text, newCursorPosition);
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

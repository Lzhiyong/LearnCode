package com.text.edit;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TextBuffer implements Serializable {

    // save the text content
    private StringBuilder strBuilder = new StringBuilder();

    // the start index of each line text
    private ArrayList<Integer> indexList = new ArrayList<>();

    // the width of each line text
    private ArrayList<Integer> widthList = new ArrayList<>();

    // load text dynamically if the text content is too large
    // implement a loading progress bar
    public static int tempCount = 0;
    // the text max width
    public static int tempWidth = 0;

    private int visLine, delta;

    // the text read finish
    public static boolean onReadFinish = false;
    // the text write finish
    public static boolean onWriteFinish = false;

    private final String TAG = this.getClass().getSimpleName();
    ExecutorService exec =  Executors.newFixedThreadPool(2);
    private Handler handler = new Handler(Looper.getMainLooper());

    public TextBuffer() {
        // nothing to do
    }

    public TextBuffer(CharSequence c) {
        setBuffer(c);
    }

    public void setBuffer(CharSequence c) {
        emptyBuffer();
        // add a dafault new line
        strBuilder.append(c + "\n");
        // line start index
        int start = 0;
        for(int i=0; i < getLength(); ++i) {           
            if(getCharAt(i) == '\n') {
                if(indexList.size() == 0) {
                    // add first index 0
                    indexList.add(0);
                }
                indexList.add(i + 1);

                // the text width
                String text = getText(start, i + 1);
                int width = HighlightTextView.getTextMeasureWidth(text);
                widthList.add(width);
                if(width > tempWidth)
                    tempWidth = width;

                start = i + 1;
                ++tempCount;
            }
        }
        // remove the last index of '\n'
        indexList.remove(indexList.size() - 1);
        onReadFinish = true;
        Log.i(TAG, "size: " + indexList.size());
    }

    public void setBuffer(StringBuilder strBuilder) {
        emptyBuffer();
        this.strBuilder = strBuilder;
    }

    public StringBuilder getBuffer() {
        return strBuilder;
    }

    // empty the string builder
    public void emptyBuffer() {
        if(strBuilder.length() > 0) {
            indexList.clear();
            widthList.clear();
            strBuilder.delete(0, strBuilder.length());
            onReadFinish = onWriteFinish = false;
            tempCount = tempWidth = 0;
        }
    }

    public int getLength() {
        return strBuilder.length();
    }

    // Get the text line count
    public int getLineCount() {
        // don't use the lineCount
        return onReadFinish ? indexList.size(): tempCount;
    }

    // return the text max width
    public int getMaxWidth() {
        return tempWidth;
    }  

    // Set the text line index lists
    public void setIndexList(ArrayList<Integer> list) {
        indexList = list;
    }

    public ArrayList<Integer> getIndexList() {
        return indexList;
    }

    // Set the text line width lists
    public void setWidthList(ArrayList<Integer> list) {
        widthList = list;
    }

    public ArrayList<Integer> getWidthList() {
        return widthList;
    }

    // find the line where the cursor is by binary search
    public int getOffsetLine(int index) {
        int low = 0;
        int line = 0;
        int high = getLineCount() + 1;

        while(high - low > 1) {
            line = (low + high) >> 1; 
            // cursor index at middle line
            if(line == getLineCount() || index >= indexList.get(line - 1) 
               && index < indexList.get(line))
                break;
            if(index < indexList.get(line - 1))
                high = line;
            else
                low = line;
        }

        // find the cursor line
        return line;
    }

    public int getLineWidth(int line) {
        return widthList.get(line - 1);
    }

    // start index of the text line
    public int getLineStart(int line) {
        return indexList.get(line - 1);
    }

    // end index of the text line
    public int getLineEnd(int line) {
        int start = getLineStart(line);
        int length = indexOfLineText(start).length();
        return start + length - 1;
    }

    public char getCharAt(int index) {
        return strBuilder.charAt(index);
    }

    // get line text by index
    public String indexOfLineText(int start) {
        int end = start;
        while(getCharAt(end) != '\n'
              && getCharAt(end) != '\uFFFF') {
            ++end;
        }
        // line text index[start..end]
        return strBuilder.substring(start, end);
    }  

    // Get a line of text
    public String getLine(int line) {

        int lineStart= getLineStart(line);

        return indexOfLineText(lineStart);
    }

    // get text by index[start..end]
    public String getText(int start, int end) {
        return strBuilder.substring(start, end);
    }

    /**
     * insert text
     *
     * @parama index: the cursor index
     * @parama line: the cursor line
     * @parama vline: the visable line on screen
     */
    public synchronized void insert(int index, CharSequence c, int line) {
        // real insert text
        strBuilder.insert(index, c);

        int length = c.length();
        int start = getLineStart(line);

        // calculate the line width
        String text = indexOfLineText(start);
        int width = HighlightTextView.getTextMeasureWidth(text);
        if(width > tempWidth) {
            tempWidth = width;
        }
        widthList.set(line - 1, width);

        for(int i=index; i < index + length; ++i) {
            if(strBuilder.charAt(i) == '\n') {
                start = i + 1;
                text = indexOfLineText(start);
                // text line width
                width = HighlightTextView.getTextMeasureWidth(text);
                if(width > tempWidth) {
                    tempWidth = width;
                }
                indexList.add(line, start);
                widthList.add(line, width);
                ++tempCount;
                ++line;
            }
        }

        visLine = line + 100;
        delta += length;

        // calculation the line start index
        for(int i=line; i < visLine && i < getLineCount(); ++i) {
            indexList.set(i, indexList.get(i) + length);
        }

        handler.removeCallbacks(calculate);
        handler.postDelayed(calculate, 5);
    }

    // delete text
    public synchronized void delete(int start, int end, int line) {   
        int length = end - start;
        for(int i=start; i < end; ++i) {
            if(strBuilder.charAt(i) == '\n') {
                indexList.remove(line - 1);
                widthList.remove(line - 1);
                --tempCount;
                --line;
            }
        }

        // real delete text
        strBuilder.delete(start, end);

        // calculate the line width
        String text = getLine(line);
        int width = HighlightTextView.getTextMeasureWidth(text);
        if(width > tempWidth)
            tempWidth = width;
        widthList.set(line - 1, width);

        visLine = line + 100;
        delta -= length;
        
        // calculation the line start index
        for(int i=line; i < visLine && i < getLineCount(); ++i) {
            indexList.set(i, indexList.get(i) - length);
        }

        handler.removeCallbacks(calculate);
        handler.postDelayed(calculate, 5);
    }

    // replace text
    public synchronized void replace(int start, int end, 
                                     String replacement, 
                                     int line, int delta) {
        if(replacement.contains("\n")) {
            // the lists needs add new line
            // replace = delete + insert
            strBuilder.delete(start, end);
            insert(start, replacement, line);
        } else {
            // real replace
            strBuilder.replace(start, end, replacement);
            // recalculate the lists
            if(delta != 0) {
                for(int i=line; i < getLineCount(); ++i) {
                    indexList.set(i, indexList.get(i) + delta);
                }
            }
        }
    }

    private Runnable calculate = new Runnable() {
        @Override
        public void run() {
            // TODO: Implement this method
            // calculation the line start index
            for(int i=visLine; i < getLineCount(); ++i) 
                indexList.set(i, indexList.get(i) + delta);
            delta = 0;
            // calculate the line width
            tempWidth = Collections.max(widthList);
        }
    };
}

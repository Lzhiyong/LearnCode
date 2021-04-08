package com.text.edit;

import android.text.TextPaint;
import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TextBuffer implements Serializable {

    // the text line height
    private int lineHeight;

    // the total count of lines of text
    private int lineCount;

    // maximum width of text line
    private int maxWidth, maxHeight;

    // text content
    private StringBuilder strBuilder;

    // start index of each line of the text
    private ArrayList<Integer> indexList;

    private TextPaint textPaint;

    private final String TAG = this.getClass().getSimpleName();

    public TextBuffer(TextPaint textPaint) {
        this.textPaint = textPaint;

        TextPaint.FontMetricsInt metrics = textPaint.getFontMetricsInt();
        lineHeight = metrics.bottom - metrics.top;
        Log.i(TAG, "line height: " + lineHeight);
    }

    public synchronized void setBuffer(StringBuilder strBuilder) {
        if(strBuilder != null)
            this.strBuilder = strBuilder;
        else
            this.strBuilder = new StringBuilder();
    }


    public synchronized void setBuffer(String text) {

        int length = text.length();
        for(int i=0; i < length; ++i) {
            char c = text.charAt(i);
            if(c == '\n') {
                ++lineCount;
                indexList.add(i);
            }
            strBuilder.append(c);
        }
    }


    public int getLength() {
        return strBuilder.length();
    }

    public char getCharAt(int index) {
        if(index < 0 || index > getLength() - 1) {
            return '\0';
        }

        return strBuilder.charAt(index);
    }

    public int getCharWidth(char c) {
        return (int)textPaint.measureText(String.valueOf(c));
    }


    public int getLineNumberWidth() {
        //(int)textPaint.measureText(String.valueOf(getLineCount()));

        return String.valueOf(getLineCount()).length() * getCharWidth('0');
    }


    public void setMaxWidth(int textWidth) {
        maxWidth = textWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxHeight(int textHeight) {
        maxHeight = textHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }


    // Set the text line index list
    public void setIndexList(ArrayList<Integer> indexList) {
        if(indexList != null)
            this.indexList = indexList;
        else
            this.indexList = new ArrayList<>();

        if(indexList.size() == 0)
            indexList.add(0);
    }

    // start index of the text line
    public int getLineStart(int line) {
        return indexList.get(line - 1);
    }

    // end index of the text line
    public int getLineEnd(int line) {
        int start = getLineStart(line);
        int length = getLine(line).length();
        return start + length - 1;
    }

    public int getLineWidth(int line) {
        return (int)textPaint.measureText(getLine(line));
    }

    // Set the text line count
    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    // Get the text line count
    public int getLineCount() {
        return lineCount;
    }

    // Get the text line height
    public int getLineHeight() {
        return lineHeight;
    }

    // Get a line of text
    public synchronized String getLine(int line) {

        int startIndex = getLineStart(line);
        int endIndex = startIndex;

        while(strBuilder.charAt(endIndex) != '\n'
              && strBuilder.charAt(endIndex) != '\uFFFF') {
            ++endIndex;
        }

        int length = getLength();
        if(endIndex >= length) 
            endIndex = length - 1;

        return strBuilder.substring(startIndex, endIndex);
    }

    private void resetMaxWidth(int line) {
        int width = getLineWidth(line);

        if(width > maxWidth) {
            maxWidth = width;
        }
    }

    private void resetMaxHeight() {
        maxHeight = lineCount * lineHeight;
    }


    // Insert text
    public synchronized void insert(int index, int line, char c) {

        strBuilder.insert(index, c);

        if(c == '\n') {
            ++lineCount;
            indexList.add(line, index);
            resetMaxHeight();
        }

        // calculation text max width
        resetMaxWidth(line);

        // calculation line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) + 1);
        }
    }

    // Delete text
    public synchronized void delete(int index, int line) {

        char c = strBuilder.charAt(index);
        strBuilder.deleteCharAt(index);

        if(c == '\n') {
            --lineCount;
            indexList.remove(line);
            resetMaxHeight();
        } 

        // calculation text max width
        resetMaxWidth(line);

        // calculation line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) - 1);
        }
    }

    public synchronized void delete(int start, int end, int line) {
        strBuilder.delete(start, end);
    }

    class TextTask implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            // TODO: Implement this method
            int width = 0;
            int textWidth = 0;
            for(int i=1; i <= lineCount; ++i) {
                textWidth = getLineWidth(i);
                if(textWidth > width) {
                    width = textWidth;
                }
            }

            return width;
        }
    }
}

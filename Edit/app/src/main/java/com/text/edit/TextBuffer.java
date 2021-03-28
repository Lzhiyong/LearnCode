package com.text.edit;

import android.text.TextPaint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import android.util.Log;

public class TextBuffer implements Serializable {

    // the text line height
    private int lineHeight;

    private int lineCount;

    private int textMaxWidth;

    // text content
    private StringBuilder strBuffer;

    private ArrayList<Integer> indexList;

    private TextPaint textPaint;

    private final String TAG = this.getClass().getSimpleName();

    public TextBuffer(TextPaint textPaint) {
        this.textPaint = textPaint;
        TextPaint.FontMetrics metrics = textPaint.getFontMetrics();
        lineHeight = (int) (metrics.descent - metrics.ascent);
        Log.i(TAG, "line height: " + lineHeight);
    }

    public synchronized void setBuffer(StringBuilder strBuilder) {
        if(strBuilder != null)
            this.strBuffer = strBuilder;
        else
            this.strBuffer = new StringBuilder();
    }


    public synchronized void setBuffer(String text) {

        int length = text.length();
        for(int i=0; i<length; ++i) {
            char c = text.charAt(i);
            if(c == '\n') {
                ++lineCount;
                indexList.add(i);
            }
            strBuffer.append(c);
        }
    }

    public int getLength() {
        return strBuffer.length() - 1;
    }

    public char getCharAt(int index) {
        if(index < 0 || index > getLength()) {
            return '\0';
        }

        return strBuffer.charAt(index);
    }

    private int getCharWidth() {
        return (int)textPaint.measureText("0");
    }

    public int getLineNumberWidth() {
        return String.valueOf(getLineCount()).length() * getCharWidth();
    }

    public void setTextMaxWidth(int textWidth) {
        this.textMaxWidth = textWidth;
    }

    public int getTextMaxWidth() {
        return textMaxWidth;
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

    public int getLineStart(int line) {
        return indexList.get(line - 1);
    }

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

        int length = strBuffer.length();

        while(strBuffer.charAt(endIndex) != '\n'
                && strBuffer.charAt(endIndex) != '\uFFFF') {
            ++endIndex;
        }

        if(endIndex >= length)
            endIndex = length - 1;

        return strBuffer.substring(startIndex, endIndex);
    }

    // Insert text
    public synchronized void insert(int index, int line, char c) {

        if(c == '\n') {
            ++lineCount;
            indexList.add(line, index);
        }

        for(int i=line; i < getLineCount(); ++i) {
            indexList.set(i, indexList.get(i) + 1);
        }

        strBuffer.insert(index, c);
    }

    // Delete text
    public synchronized void delete(int index, int line) {

        if(strBuffer.charAt(index) == '\n') {
            --lineCount;
            indexList.remove(line);
        }

        for(int i=line; i < getLineCount(); ++i) {
            indexList.set(i, indexList.get(i) - 1);
        }

        strBuffer.deleteCharAt(index);
    }

    public synchronized void delete(int start, int end, int line) {
        strBuffer.delete(start, end);
    }
}

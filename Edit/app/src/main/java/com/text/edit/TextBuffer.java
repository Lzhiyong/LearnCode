package com.text.edit;

import android.text.TextPaint;
import java.io.Serializable;
import java.util.ArrayList;

public class TextBuffer implements Serializable {

    // the text line height
    private int lineHeight;

    // the total count of lines of text
    private int lineCount;

    // text content
    private StringBuilder strBuilder;

    // start index of each line of the text
    private ArrayList<Integer> indexList;

    private ArrayList<Integer> widthList;

    private TextPaint textPaint;

    private final String TAG = this.getClass().getSimpleName();

    public TextBuffer(TextPaint textPaint) {
        strBuilder = new StringBuilder();

        this.textPaint = textPaint;

        TextPaint.FontMetricsInt metrics = textPaint.getFontMetricsInt();
        lineHeight = metrics.bottom - metrics.top;

        indexList = new ArrayList<>();
        // add first index 0
        indexList.add(0);

        widthList = new ArrayList<>();
    }

    public void setBuffer(StringBuilder strBuilder) {
        this.strBuilder = strBuilder;
    }

    public StringBuilder getBuffer() {
        return strBuilder;
    }

    public void setBuffer(String text) {

        int length = text.length();
        for(int i=0; i < length; ++i) {
            char c = text.charAt(i);
            if(c == '\n') {
                ++lineCount;
                indexList.add(i);
            }
            strBuilder.append(c);
        }
        // remove the last index of '\n'
        indexList.remove(lineCount);
    }

    public String getText(int start, int end){
        return strBuilder.substring(start, end);
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

    // Set the text line index list
    public void setIndexList(ArrayList<Integer> list) {
        indexList = list;
    }

    public ArrayList<Integer> getIndexList() {
        return indexList;
    }

    public void setWidthList(ArrayList<Integer> list) {
        widthList = list;
    }

    public ArrayList<Integer> getWidthList() {
        return widthList;
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


    // Insert text
    public synchronized void insert(int index, int line, char c) {

        // insert char
        strBuilder.insert(index, c);

        if(c == '\n') {
            ++lineCount;
            // add current line
            indexList.add(line, index);
        } 

        // calculation line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) + 1);
        }

        // calculation text line width
        if(c == '\n') 
            widthList.add(line, getLineWidth(line + 1));
        else
            widthList.set(line - 1, getLineWidth(line));
    }

    // Delete text
    public synchronized void delete(int index, int line) {

        // delete char
        char c = strBuilder.charAt(index);
        strBuilder.deleteCharAt(index);

        if(c == '\n') {
            --lineCount;
            // remove current line
            indexList.remove(line);
            widthList.remove(line);
        } 

        // calculation line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) - 1);
        }

        // calculation text line width
        widthList.set(line - 1, getLineWidth(line));
    }


    public synchronized void delete(int start, int end, int line) {
        for(int i=start; i <= end; ++i) {
            delete(i, line);
        }
    }
}

package com.text.edit;

import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;

public class TextBuffer implements Serializable {

    // the total count of lines of text
    private int lineCount;

    // text content
    private StringBuilder strBuilder;

    // the start index of each line text
    private ArrayList<Integer> indexList;

    // the width of each line text
    private ArrayList<Integer> widthList;

    private final String TAG = this.getClass().getSimpleName();


    public TextBuffer() {
        strBuilder = new StringBuilder();

        indexList = new ArrayList<>();
        // add first index 0
        indexList.add(0);

        widthList = new ArrayList<>();
    }

    public void setBuffer(String text) {
        int length = text.length();
        for(int i=0; i < length; ++i) {
            char c = text.charAt(i);
            if(c == '\n') {
                ++lineCount;
                indexList.add(i + 1);
            }
            strBuilder.append(c);
        }
        Log.i(TAG, "line count: " + lineCount);
        // remove the last index of '\n'
        indexList.remove(lineCount);
    }

    public void setBuffer(StringBuilder strBuilder) {
        this.strBuilder = strBuilder;
    }

    public StringBuilder getBuffer() {
        return strBuilder;
    }

    public int getLength() {
        return strBuilder.length();
    }

    // Set the text line count
    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    // Get the text line count
    public int getLineCount() {
        return lineCount;
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

    // Get cursor line by cursor index
    public int getOffsetLine(int index) {
        int low = 0;
        int line = 0;
        int high = lineCount + 1;

        while(high - low > 1) {
            line = (low + high) >> 1; 
            // cursor index at middle line
            if(line == lineCount || index >= indexList.get(line - 1) 
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

    public synchronized char getCharAt(int index) {
        return strBuilder.charAt(index);
    }

    // get line text by index
    public synchronized String indexOfLineText(int start) {
        int end = start;
        while(getCharAt(end) != '\n'
              && getCharAt(end) != '\uFFFF') {
            ++end;
        }
        // line text index[start..end]
        return strBuilder.substring(start, end);
    }  

    // Get a line of text
    public synchronized String getLine(int line) {

        int lineStart= getLineStart(line);

        return indexOfLineText(lineStart);
    }

    // get text by index[start..end]
    public synchronized String getText(int start, int end) {
        return strBuilder.substring(start, end);
    }


    // insert text
    public synchronized void insert(int index, String s, int line) {
        // real insert text
        strBuilder.insert(index, s);

        int lineStart = getLineStart(line);
        int length = s.length();

        // calculate the line width
        String text = indexOfLineText(lineStart);
        int lineWidth = HighlightTextView.getTextMeasureWidth(text);
        widthList.set(line - 1, lineWidth);

        for(int i=index; i < index + length; ++i) {
            if(strBuilder.charAt(i) == '\n') {
                lineStart = i + 1;
                text = indexOfLineText(lineStart);
                lineWidth = HighlightTextView.getTextMeasureWidth(text);

                indexList.add(line, lineStart);
                widthList.add(line, lineWidth);

                ++lineCount;
                ++line;
            }
        }

        // calculation the line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) + length);
        }
    }

    // Delete text
    public synchronized void delete(int start, int end, int line) {   
        int length = end - start;

        for(int i=start; i < end; ++i) {
            if(strBuilder.charAt(i) == '\n') {
                indexList.remove(line - 1);
                widthList.remove(line - 1);
                --lineCount;
                --line;
            }
        }

        // calculation the line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) - length);
        }

        // real delete text
        strBuilder.delete(start, end);

        // calculate the line width
        String text = getLine(line);
        int lineWidth = HighlightTextView.getTextMeasureWidth(text);
        widthList.set(line - 1, lineWidth);
    }

    // replace text
    public synchronized void replace(int start, int end, 
                                     String replacement, int line, int delta) {

        if(!replacement.contains("\n")) {                             
            strBuilder.replace(start, end, replacement);
            // recalculate the lists
            if(delta != 0) {
                for(int i=line; i < lineCount; ++i) {
                    indexList.set(i, indexList.get(i) + delta);
                }
            }
        } else {
            strBuilder.delete(start, end);
            insert(start, replacement, line);
        }
    }
}

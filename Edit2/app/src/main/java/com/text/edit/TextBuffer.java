package com.text.edit;

import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;

public class TextBuffer implements Serializable {

    // text content
    private StringBuilder strBuilder = new StringBuilder();

    // the start index of each line text
    private ArrayList<Integer> indexList = new ArrayList<>();

    // the width of each line text
    private ArrayList<Integer> widthList = new ArrayList<>();

    private final String TAG = this.getClass().getSimpleName();

    public TextBuffer() {
        // add first index 0
        indexList.add(0);
    }

    public TextBuffer(CharSequence c) {
        this();
        setBuffer(c);
    }

    public void setBuffer(CharSequence c) {
        // add a dafault new line
        strBuilder.append(c + "\n");
        int lineCount = 0;
        for(int i=0; i < getLength(); ++i) {
            char ch = getCharAt(i);
            if(ch == '\n') {
                ++lineCount;
                int width = HighlightTextView.getTextMeasureWidth(getLine(lineCount));
                widthList.add(lineCount - 1, width);
                indexList.add(i + 1);
            }
        }
        // remove the last index of '\n'
        indexList.remove(indexList.size() - 1);
        Log.i(TAG, "size: " + indexList.size());
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

    // Get the text line count
    public int getLineCount() {
        return indexList.size();
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
        int length = getLine(line).length();
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

    // insert text
    public synchronized void insert(int index, CharSequence c, int line) {
        // real insert text
        strBuilder.insert(index, c);

        int length = c.length();
        int lineStart = getLineStart(line);

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
                //++tempLineCount;
                ++line;
            }
        }

        // calculation the line start index
        for(int i=line; i < getLineCount(); ++i) {
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
                //--tempLineCount;
                --line;
            }
        }

        // calculation the line start index
        for(int i=line; i < getLineCount(); ++i) {
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
        if(replacement.contains("\n")) {                             
            strBuilder.delete(start, end);
            insert(start, replacement, line);
        } else {
            strBuilder.replace(start, end, replacement);
            // recalculate the lists
            if(delta != 0) {
                for(int i=line; i < getLineCount(); ++i) {
                    indexList.set(i, indexList.get(i) + delta);
                }
            }
        }
    }
}

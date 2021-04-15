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

    // modify an item from the width lists
    public static final int OP_SET = 1; 
    // add an item for the width lists
    public static final int OP_ADD = 2;
    // delete an item from the width lists
    public static final int OP_DEL = 3;
    
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
                indexList.add(i);
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
            // cursor index at mid line
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
    public synchronized String getLine(int line) {

        int lineStart= getLineStart(line);

        return indexOfLineText(lineStart);
    }

    // get text by index[start..end]
    public synchronized String getText(int start, int end) {
        return strBuilder.substring(start, end);
    }

    // recalculate the width lists
    public void resetWidthList(int line, int lineWidth, int option) {
        if(option == OP_SET) {
            widthList.set(line - 1, lineWidth);
        } else if(option == OP_ADD) {
            widthList.add(line - 1, lineWidth);
        } else {
            widthList.remove(line - 1);
        }
    }

    // recalculate the index lists
    public void resetIndexList(int line, int delta) {
        // calculation line start index
        for(int i=line; i < lineCount; ++i) {
            indexList.set(i, indexList.get(i) + delta);
        }
    }

    // Insert char
    public synchronized void insert(int index, char c, int line) {
        // insert char
        strBuilder.insert(index, c);

        if(c == '\n') {
            ++lineCount;
            // add current line
            indexList.add(line, index);
        } 

        // recalculate the lists
        resetIndexList(line, 1);
    }


    // Delete char
    public synchronized void delete(int index, int line) {
        // delete char
        char c = strBuilder.charAt(index);
        strBuilder.deleteCharAt(index);

        if(c == '\n') {
            --lineCount;
            // remove current line
            indexList.remove(line);
        } 

        // recalculate the lists
        resetIndexList(line, -1);
    }

    // delete text at index[start..end]
    public synchronized void delete(int start, int end, int line) {
        for(int i=start; i <= end; ++i) {
            delete(i, line);
        }
    }

    public synchronized void replace(int line, int start, int end, 
                                     int delta, String replacement) {
        strBuilder.replace(start, end, replacement);
        // recalculate the lists
        if(delta != 0) resetIndexList(line, delta);
    }
}

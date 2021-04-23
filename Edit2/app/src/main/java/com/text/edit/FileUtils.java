package com.text.edit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FileUtils {
    
    // get the line count of the file
    public static int getLineNumber(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);

            lineNumberReader.skip(Integer.MAX_VALUE);
            int lines = lineNumberReader.getLineNumber() + 1;

            fileReader.close();
            lineNumberReader.close();
            // return total lines of the file
            return lines;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

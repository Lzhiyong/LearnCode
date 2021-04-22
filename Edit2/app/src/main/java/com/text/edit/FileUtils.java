package com.text.edit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FileUtils {
    // 
    public static long getLineNumber(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);

            lineNumberReader.skip(Long.MAX_VALUE);
            long lines = lineNumberReader.getLineNumber() + 1;

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

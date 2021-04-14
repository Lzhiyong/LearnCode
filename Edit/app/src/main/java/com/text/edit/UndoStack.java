package com.text.edit;

import java.util.Stack;

public class UndoStack {

    // undo stack
    Stack<Action> undoStack = new Stack<>();

    // redo stack
    Stack<Action> redoStack = new Stack<>();

    // add action
    public void add(Action action) {
        undoStack.push(action);
        // auto remove the first item
        removeFirst();
    }

    // undo operator
    public Action undo() {
        if(canUndo()) {
            Action action = undoStack.pop();
            redoStack.push(action);
            return action;
        }
        return null;
    }

    // redo operator
    public Action redo() {
        if(canRedo()) {
            Action action = redoStack.pop();
            undoStack.push(action);
            return action;
        }
        return null;
    }

    public boolean canUndo() {
        return undoStack.size() > 0;
    }

    public boolean canRedo() {
        return redoStack.size() > 0;
    }

    // when size > 50 remove the first item
    public void removeFirst() {
        if(undoStack.size() > 50)
            undoStack.remove(0);
            
        if(redoStack.size() > 50)
            redoStack.remove(0);
    }
    
    // empty the undo and redo stack
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
    
    // total size of stack
    public int size() {
        return undoStack.size() + redoStack.size();
    }

    // record the insert and delete action
    static class Action {

        // start and end index for insert text
        public int insertStart, insertEnd;
        // start and end index for delete text
        public int deleteStart, deleteEnd;

        // inserted and deleted text
        public String insertText, deleteText;

        public boolean isSelectMode;

        // select text
        public int selectionStart, selectionEnd;

        // select handle left
        public int handleLeftX, handleLeftY;
        // select handle right
        public int handleRightX, handleRightY;
    }
}

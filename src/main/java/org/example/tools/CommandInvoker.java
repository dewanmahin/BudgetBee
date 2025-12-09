package org.example.tools;

import java.util.Stack;

public class CommandInvoker {
    private Stack<Command> undoStack = new Stack<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
        }
    }
}

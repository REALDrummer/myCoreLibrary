package REALDrummer.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import static REALDrummer.utils.StringUtilities.*;

public class myBSetting extends mySetting {
    private String question, true_status_message, false_status_message;

    public myBSetting(String target, String key, String question, String true_status_message, String false_status_message, boolean initial_value) {
        this.target = target;
        this.key = key;
        this.question = question;
        this.true_status_message = true_status_message;
        this.false_status_message = false_status_message;
        this.value = initial_value;
    }

    // getters
    public String getFalseStatusMessage() {
        return false_status_message;
    }

    public String getQuestion() {
        return question;
    }

    public String getStatusMessage() {
        return getStatusMessage(booleanValue());
    }

    public String getStatusMessage(boolean value) {
        if (value)
            return true_status_message;
        else
            return false_status_message;
    }

    public String getTrueStatusMessage() {
        return true_status_message;
    }

    public boolean getValue() {
        return (boolean) value;
    }

    // readers and writers
    @Override
    public boolean read(BufferedReader in) {
        try {
            String question_line = in.readLine(), status_line = in.readLine();

            // if the next line isn't the question line, return false to indicate an error
            if (question_line == null || !question.trim().toLowerCase().startsWith(question_line.toLowerCase()))
                return false;

            // attempt to read answers at the end of the question
            Boolean read_value = readResponse(question_line.substring(question_line.lastIndexOf('?')));

            // if the question was not answered, check the current status line
            if (read_value == null) {
                // if the question wasn't answered and there's no status line,
                if (status_line == null || status_line.trim().equals(""))
                    return true;
                // if there is a status line, compare it to the true_status_message
                else
                    read_value = status_line.toLowerCase().startsWith(true_status_message.toLowerCase());
            }

            value = (boolean) read_value;
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public boolean write(BufferedWriter out) {
        try {
            out.write("    " + (target.equals("\\server") ? "" : "    ") + question + " ");
            out.newLine();
            out.write("    " + (target.equals("\\server") ? "" : "    ") + "  " + getStatusMessage(getValue()));
            out.newLine();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}

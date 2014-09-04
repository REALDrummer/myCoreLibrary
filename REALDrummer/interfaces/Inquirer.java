package REALDrummer.interfaces;

import REALDrummer.myQuestion;

public interface Inquirer {
    public void questionAnswered(myQuestion question, String answer_message, boolean answer);

    public void questionCancelled(myQuestion question);
}

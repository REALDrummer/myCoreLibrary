package REALDrummer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Question implements Listener, ActionListener {
    public static ArrayList<Question> questions = new ArrayList<Question>();
    public static ArrayList<Requestable> responders = new ArrayList<Requestable>();

    public String player, question_ID, initial_question, relog_reminder, cancel, responder;
    // [0] = 15-second; [1] = 30-second; [2] = 45-second
    public String[] timed_reminders = new String[4];
    public Timer timer;
    public byte time_counter = 0;
    public boolean success = true;

    public Question(String player, String question_ID, String initial_question, String reminder_15_seconds, String reminder_30_seconds, String reminder_45_seconds,
            String relog_reminder, String cancellation_message, String responder) {
        // make sure there's not another question from the same player with the same I.D.
        if (questions.contains(this)) {
            success = false;
            return;
        }

        // initialize the Question's properties
        this.player = player;
        this.question_ID = question_ID;
        this.initial_question = initial_question;
        timed_reminders = new String[] { relog_reminder, reminder_15_seconds, reminder_30_seconds, reminder_45_seconds };
        cancel = cancellation_message;
        this.responder = responder;

        // add the question to the question list
        questions.add(this);

        ask(false);
    }

    public Question(Player player, String question_ID, String initial_question, String reminder_15_seconds, String reminder_30_seconds, String reminder_45_seconds,
            String relog_reminder, String cancellation_message, String responder) {
        new Question(player.getName(), question_ID, initial_question, reminder_15_seconds, reminder_30_seconds, reminder_45_seconds, relog_reminder, cancellation_message,
                responder);
    }

    public Question(Player player, String question_ID, String initial_question, String reminder_15_seconds, String reminder_30_seconds, String reminder_45_seconds,
            String relog_reminder, String cancellation_message, Requestable responder) {
        new Question(player.getName(), question_ID, initial_question, reminder_15_seconds, reminder_30_seconds, reminder_45_seconds, relog_reminder, cancellation_message,
                responder.getClass().getName());
        if (!responders.contains(responder))
            responders.add(responder);
    }

    public Question(String player, String question_ID, String initial_question, String reminder_15_seconds, String reminder_30_seconds, String reminder_45_seconds,
            String relog_reminder, String cancellation_message, Requestable responder) {
        new Question(player, question_ID, initial_question, reminder_15_seconds, reminder_30_seconds, reminder_45_seconds, relog_reminder, cancellation_message, responder
                .getClass().getName());
        if (!responders.contains(responder))
            responders.add(responder);
    }

    public void ask(boolean relog) {
        // start the reminder/cancellation timer for this question if responder and player are available
        Player target = mCL.server.getPlayerExact(player);
        if (responder != null && target != null) {
            mCL.debug("responder and player found; asking question...");
            target.sendMessage(SU.colorCode(relog ? relog_reminder : initial_question));
            if (timer != null && timer.isRunning())
                timer.stop();
            timer = new Timer(15000, this);
            timer.start();
        } else if (responder == null && target == null)
            mCL.debug("neither responder " + responder + " nor player " + player + " found; postponing question...");
        else if (target == null)
            mCL.debug(player + " not found; postponing question...");
        else
            mCL.debug(responder + " not found; postponing question...");
    }

    // list searchers
    public static Question getNextPendingQuestion(String player) {
        for (Question question : questions)
            if (player.equals(question.player))
                return question;
        return null;
    }

    public static Question getNextPendingQuestion(Player player) {
        return getNextPendingQuestion(player.getName());
    }

    // listeners
    @Override
    public void actionPerformed(ActionEvent event) {
        // try to find the player to ask
        Player target = mCL.server.getPlayerExact(player);
        if (target == null) {
            mCL.debug(player + " not found for reminder " + time_counter + " questioning; postponing question...");
            timer.stop();
            time_counter = 0;
            return;
        }

        // ask the next reminder question
        target.sendMessage(timed_reminders[time_counter]);

        time_counter++;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public static void listenForAnswers(AsyncPlayerChatEvent event) {
        // see if the message is an answer to a question
        Boolean answer = SU.readResponse(event.getMessage());
        if (answer == null)
            return;

        // find out if this player has any questions pending
        Question question = getNextPendingQuestion(event.getPlayer());
        if (question == null)
            return;

        // if a question is pending, retrieve the question's Requestable responder class
        Requestable requestable = null;
        for (Requestable responder : responders)
            if (responder.getClass().getName().equals(responder)) {
                requestable = responder;
                break;
            }
        if (requestable == null) {
            MU.tellOps(ChatColor.DARK_RED + "I couldn't find the Requestable to return this answer to!\nThe Requestable's name is \"" + question.responder
                    + "\" and the question was \"" + question.initial_question + "\" (" + question.question_ID + ").", true);
            return;
        }

        // send the question's answer back to the Requestable class and terminate the question
        requestable.questionAnswered(question.question_ID, answer.booleanValue());
        questions.remove(question);

        // see if this player has any other questions pending
        question = getNextPendingQuestion(event.getPlayer());
        if (question == null)
            return;

        // if any other questions are pending, ask the next one
        question.ask(false);
    }

    @EventHandler
    public static void onPlayerLeaveWaitForThemToReturn(PlayerQuitEvent event) {
        // find out if this player has any questions pending
        Question question = getNextPendingQuestion(event.getPlayer());
        if (question == null)
            return;

        // if any questions are pending, stop their timers and wait for the player to return
        question.timer.stop();
        question.time_counter = 0;
    }

    @EventHandler
    public void onPlayerReturnContinueQuestioning(PlayerJoinEvent event) {
        // find any pending questions for this player
        Question question = getNextPendingQuestion(event.getPlayer());

        // if this player has a pending question, ask using the relog reminder
        if (question != null)
            ask(true);
    }

    // Object overrides
    @Override
    public boolean equals(Object object) {
        if (object instanceof Question && ((Question) object).player.equals(player) && ((Question) object).question_ID.equals(question_ID))
            return true;
        return false;
    }

    @Override
    public String toString() {
        if (!success)
            return "";
        else
            return responder.getClass().getName() + " asked " + player + " \"" + initial_question + "\" (" + question_ID + "). After 15 seconds, I'll ask \""
                    + timed_reminders[0] + "\"; after 30 seconds, \"" + timed_reminders[1] + "\"; after 45, \"" + timed_reminders[2] + "\"; after 60, \"" + cancel
                    + "\"; and on relog, \"" + relog_reminder + "\".";
    }
}

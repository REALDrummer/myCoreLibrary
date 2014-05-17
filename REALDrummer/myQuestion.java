package REALDrummer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.Timer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static REALDrummer.utils.ArrayUtilities.*;
import static REALDrummer.utils.ColorUtilities.colorCode;
import static REALDrummer.utils.MessageUtilities.*;
import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.StringUtilities.readResponse;

public class myQuestion implements Listener, ActionListener, Comparable<myQuestion>, Matchable {
    public static myList<myQuestion> questions = new myList<myQuestion>();
    public static ArrayList<Inquirer> inquirers = new ArrayList<Inquirer>();

    public String player, question_ID, initial_question, relog_reminder, cancel_message, inquirer;
    public String[] timed_reminders;
    public Timer timer;
    public int time_counter = 0, reminder_timing;
    public long time_asked;
    public boolean success = true;

    // TODO: make myQuestions work with the console commands
    // TODO: make myQuestions accept commands as well as chat answers

    public myQuestion() {
        // this default constructor is just here to allow me to register this class as a Listener in myCoreLibrary's myEnable() without creating a whole functional question
    }

    public myQuestion(Inquirer inquirer, String player, String question_ID, String initial_question, String relog_reminder, String cancel_message, int reminder_timing,
            String... timed_reminders) {
        /* make sure 1) the reminder timing is not less than or equal to 500 not only because this conflicts with the checker for preventing accidental answering of a question
         * that just popped up, but also because having a reminders every half second or less is rude anyway and 2) that there's not another question for the same player with
         * the same I.D. to prevent spamming the same question */
        if (reminder_timing <= 500 || questions.contains(this)) {
            success = false;
            return;
        }

        // initialize the myQuestion's properties
        this.player = player;
        this.question_ID = question_ID;
        this.initial_question = initial_question;
        this.timed_reminders = timed_reminders;
        this.cancel_message = cancel_message;
        this.inquirer = inquirer.getClass().getName();
        this.reminder_timing = reminder_timing;
        time_asked = Calendar.getInstance().getTimeInMillis();

        // add the question to the question list
        questions.add(this);

        ask(false);
    }

    public myQuestion(Inquirer inquirer, Player player, String question_ID, String initial_question, String relog_reminder, String cancel_message, int reminder_timing,
            String... timed_reminders) {
        new myQuestion(inquirer, player.getName(), question_ID, initial_question, relog_reminder, cancel_message, reminder_timing, timed_reminders);
    }

    public void ask(boolean relog) {
        // start the reminder/cancellation timer for this question if responder and player are available
        Player target = mCL.getServer().getPlayerExact(player);
        if (inquirer != null && target != null) {
            mCL.debug("responder and player found; asking question...");
            target.sendMessage(colorCode(relog ? relog_reminder : initial_question));
            if (timer != null && timer.isRunning())
                timer.stop();
            // it will be 500ms before any responses are accepted to avoid accidental answering of a question that popped up as a message was sent
            timer = new Timer(500, this);
            timer.start();
        } else if (inquirer == null && target == null)
            mCL.debug("neither responder " + inquirer + " nor player " + player + " found; postponing question...");
        else if (target == null)
            mCL.debug(player + " not found; postponing question...");
        else
            mCL.debug(inquirer + " not found; postponing question...");
    }

    // listeners
    @EventHandler(priority = EventPriority.LOWEST)
    public static void listenForAnswersToQuestions(AsyncPlayerChatEvent event) {
        // find out if this player has any questions pending
        myQuestion question = myQuestion.getNextPendingQuestion(event.getPlayer());
        if (question == null)
            return;

        // if the timer for this question is still set to 500ms, then no answers should be accepted at this time
        if (question.timer.getDelay() == 500)
            return;

        // find the Inquirer
        Inquirer this_inquirer = null;
        for (Inquirer _inquirer : myQuestion.inquirers)
            if (_inquirer.getClass().getName().equals(question.inquirer)) {
                this_inquirer = _inquirer;
                break;
            }
        if (this_inquirer == null) {
            err(mCL, "I couldn't find the Inquirer associated with this question!", "missing Inquirer", "question.inquirer=\"" + question.inquirer + "\"", question);
            myQuestion.questions.remove(question);
            return;
        }

        // send the results of the question to the Inquirer's questionAnswered method and remove the question
        this_inquirer.questionAnswered(question, event.getMessage(), readResponse(event.getMessage()));
        myQuestion.questions.remove(question);

        // see if this player has any other questions pending
        question = myQuestion.getNextPendingQuestion(event.getPlayer());
        if (question == null)
            return;

        // if any other questions are pending, ask the next one
        question.ask(false);
    }

    @EventHandler
    public static void onPlayerLeaveWaitForThemToReturn(PlayerQuitEvent event) {
        // find out if this player has any questions pending
        myQuestion question = myQuestion.getNextPendingQuestion(event.getPlayer());
        if (question == null)
            return;

        // if any questions are pending, stop their timers and wait for the player to return
        question.timer.stop();
        question.time_counter = 0;
    }

    @EventHandler
    public static void onPlayerReturnContinuemyQuestioning(PlayerJoinEvent event) {
        // find any pending questions for this player
        myQuestion question = myQuestion.getNextPendingQuestion(event.getPlayer());

        // if this player has a pending question, ask using the relog reminder
        if (question != null)
            question.ask(true);
    }

    // static utilities
    public static myQuestion getNextPendingQuestion(String player) {
        for (myQuestion question : questions)
            if (player.equals(question.player))
                return question;
        return null;
    }

    public static myQuestion getNextPendingQuestion(Player player) {
        return getNextPendingQuestion(player.getName());
    }

    // timer listener
    @Override
    public void actionPerformed(ActionEvent event) {
        // if the timer was set for 500ms, then this is the first time this method is being called this cycle, so we must just set the timer to go off in another 14,500ms
        if (timer.getDelay() == 500) {
            timer.setDelay(reminder_timing - 500);
            return;
        }

        // if the timer was set for 14,500ms, then this is the second time this method is being called this cycle, so set the next time to 15,000ms and continue
        if (timer.getDelay() == reminder_timing - 500)
            timer.setDelay(reminder_timing);

        // try to find the player to ask
        Player target = mCL.getServer().getPlayerExact(player);
        if (target == null) {
            mCL.debug(player + " not found for reminder " + time_counter + " questioning; postponing question...");
            timer.stop();
            time_counter = 0;
            return;
        }

        // if we have gone through all the timed reminders, cancel the question
        if (time_counter > 3) {
            target.sendMessage(colorCode(cancel_message));
            timer.stop();

            // find the Inquirer
            Inquirer this_inquirer = null;
            for (Inquirer _inquirer : inquirers)
                if (_inquirer.getClass().getName().equals(inquirer)) {
                    this_inquirer = _inquirer;
                    break;
                }
            if (this_inquirer == null) {
                err(mCL, "I couldn't find the Inquirer associated with this question!", "missing Inquirer", "inquirer=\"" + inquirer + "\"", this);
                questions.remove(this);
                return;
            }

            // inform the Inquirer of the cancellation of this question
            this_inquirer.questionCancelled(this);
        } // if we have not gone through all the timed reminders, ask the next reminder question
        else {
            target.sendMessage(colorCode(timed_reminders[time_counter]));
            time_counter++;
        }
    }

    // overrides
    @Override
    public int compareTo(myQuestion question) {
        // start by comparing the players' names
        int player_compare = player.compareTo(question.player);

        if (player_compare == 0)
            // if the player is the same, figure out which question came first
            return (int) (question.time_asked - time_asked);
        else
            return player_compare;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof myQuestion && ((myQuestion) object).player.equals(player) && ((myQuestion) object).question_ID.equals(question_ID))
            return true;
        return false;
    }

    @Override
    public int matchTo(String... match_parameters) {
        return compare(new String[] { player, String.valueOf(time_asked) }, match_parameters);
    }

    @Override
    public String toString() {
        if (!success)
            return "";
        else
            return inquirer.getClass().getName() + " asked " + player + " \"" + initial_question + "\" (" + question_ID + "). Every " + reminder_timing
                    + "ms, I'll ask the next question in this sequence: " + writeArray(timed_reminders, ", ", "and", "\"", "\"") + ".";
    }

}

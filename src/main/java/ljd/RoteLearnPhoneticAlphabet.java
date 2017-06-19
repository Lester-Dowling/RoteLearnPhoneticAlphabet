// Run command:
// java -jar RoteLearnPhoneticAlphabet.jar 3

package ljd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;

// See http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadLocalRandom.html
// import java.util.concurrent.ThreadLocalRandom;

public class RoteLearnPhoneticAlphabet {

  static private List<String> canonical_list;


  static private class RandomCanonical
  {
    private static int progressive_idx = -1;
    private static ArrayList<Integer> shuffled_index_list = new ArrayList<Integer>();

    static void init_shuffle(int trials_count)
    {
      final int canonical_size = canonical_list.size();
      while (shuffled_index_list.size() < trials_count) {
        for (int idx = 0; idx < canonical_size; ++idx) {
          shuffled_index_list.add(idx);
        }
      }
      Collections.shuffle(shuffled_index_list);
      for (int shuffled_idx = 0; shuffled_idx < shuffled_index_list.size(); ++shuffled_idx) {
        final int canonical_idx = shuffled_index_list.get(shuffled_idx);
        System.out.printf("%d --> %s%n", shuffled_idx, canonical_list.get(canonical_idx));
      }
    }

    RandomCanonical() {
      ++progressive_idx;
      if (progressive_idx >= shuffled_index_list.size()) {
        throw new RuntimeException("Progressive index into shuffled array has become too large");
      }
    }

    String phonetic()
    {
      final int canonical_idx = shuffled_index_list.get(progressive_idx);
      return canonical_list.get(canonical_idx);
    }

    boolean equalsIgnoreCase(String rhs)
    {
      final String[] possible_spellings = phonetic().split(" ");
      for (String possible_spelling : possible_spellings) {
        if (possible_spelling.equalsIgnoreCase(rhs)) {
          return true;
        }
      }
      return false;
    }

    int letterIdx()
    {
      final int canonical_idx = shuffled_index_list.get(progressive_idx);
      return canonical_idx + 1;
    }
  }



  static private class AnswerAttempt
  {
    boolean correct_answer;

    AnswerAttempt(RandomCanonical trial)
    {
      System.out.printf("What is the phonetic for letter #%d?%n",trial.letterIdx());
      final String user_answer = System.console().readLine();
      correct_answer = trial.equalsIgnoreCase(user_answer);
    }

    boolean correctAnswer() { return correct_answer; }
  }


  static boolean do_attempt(int attempt_number, final RandomCanonical trial)
  {
    final AnswerAttempt attempt = new AnswerAttempt(trial);
    if (!attempt.correctAnswer()) {
      ++attempt_number;
      if (attempt_number < 3) {
        System.out.printf("Nope.%n");
        System.out.printf("Try again!  Attempt #%d:%n", attempt_number);
      }
      else if (attempt_number == 3) {
        System.out.printf("Nope.%n");
        System.out.printf("Try again!  Last Attempt!%n");
      }
      else {
        System.out.printf("Wrong.%n");
      }
      return false;
    }
    System.out.printf("Correct!!!%n");
    return true;
  }


  static boolean do_trial(int trial_number, int trials_count)
  {
    System.out.printf("%n%nTrial #%d of %d%n", trial_number, trials_count);
    final RandomCanonical trial = new RandomCanonical();
    for (int attempt_number = 1; attempt_number <= 3; ++attempt_number) {
      if (do_attempt(attempt_number, trial)) return true;
    }
    return false;
  }


  static private final int DEFAULT_TRIALS_COUNT = 3;


  public static void main(String[] args)
  {
    try {

      // Load the list of phonetic words:
      File word_list_file = new File("Phonetic-Alphabet.txt");
      canonical_list = FileUtils.readLines(word_list_file, "UTF-8");

      for (String phonetic : canonical_list) {
        System.out.println(phonetic);
      }

    } catch (java.io.IOException e) {
      System.err.println("*** [I/O Exception] Failed to load word list file.");
      e.printStackTrace();
      return;
    }


    int trials_count = DEFAULT_TRIALS_COUNT;
    if (0 < args.length) {
      trials_count = Integer.parseInt(args[0]);
    }
    trials_count *= canonical_list.size();

    if (trials_count <= 0 || 100 < trials_count) {
      System.out.println("*** [Sanity Check] Excessive trial count.");
      return;
    }

    RandomCanonical.init_shuffle(trials_count);

    int correct_count = 0;
    for (int trial_idx = 1; trial_idx <= trials_count; ++trial_idx) {
      if (do_trial(trial_idx, trials_count)) ++correct_count;
    }
    System.out.printf("%n%n*** Finish.  You scored %d correct out of %d:  ", correct_count, trials_count);
    if (correct_count == trials_count) {
      System.out.printf("100%% !!!%n%n");
    }
    else {
      final double numerator = correct_count;
      final double demoninator = trials_count;
      final double percentage = 100 * numerator / demoninator;
      final char percent_sign = '%';
      System.out.printf("%,.0f%c %n%n", percentage, percent_sign);
    }
  }
}

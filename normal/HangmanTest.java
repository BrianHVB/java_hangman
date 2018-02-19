import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Suite of tests for the Hangman class. Uses JUnit 4.
 *
 * @author Brian von Behren
 * @version 1.0
 */
public class HangmanTest {

   /**
    * Path to a text file containing a short list of 5-letter words.
    */
   private final String SHORT_DICTIONARY = "shortDictionary.txt";

   /**
    * Path to a large text file containing a list of legal Scrabble words.
    */
   private static final String FULL_DICTIONARY = "dictionary.txt";

   /**
    * Default number of allowed guesses.
    */
   private final int GUESS_LIMIT = 7;

   /**
    * Default goal length.
    */
   private static final int GOAL_LENGTH = 5;

   /**
    * Rule for testing that specified exceptions are thrown.
    */
   @Rule
   public final ExpectedException exception = ExpectedException.none();


   /**
    * Sets-up a Hangman manger by creating a dictionary of strings from a text
    * file.
    *
    * @param filename    path of the dictionary file. Words must be separated by
    *                    lines
    * @param validLength the length of the goal word
    * @param guessLimit  the number of allowed guesses
    * @return a HangmanManager instance
    */
   private HangmanManager setupBasicManager(final String filename,
                                            final int validLength,
                                            final int guessLimit) {
      List<String> words = null;
      try {
         words = Files.readAllLines(FileSystems.getDefault().getPath(filename));
      } catch (IOException e) {
         e.printStackTrace();
      }
      return new Hangman(words, validLength, guessLimit);
   }

   /**
    * Sets-up a Hangman manager that has a single word in its dictionary.
    *
    * @param word       the goal word
    * @param guessLimit the number of wrong guesses allowed
    * @return a HangmanManager instance
    */
   private HangmanManager setupSingleWordManager(final String word,
                                                 final int guessLimit) {
      List<String> words = new ArrayList<>();
      words.add(word);

      return new Hangman(words, word.length(), guessLimit);
   }

   /**
    * Multi-faceted test for checking the state of the Hangman class. Given a
    * goalWord and letters to guess, tests that the patterns returned match what
    * is expected; also tests that the number of remaining guesses corresponds
    * to the number of incorrect guesses. Also checks that each letter
    * inputted as a guess appears in list returned by guesses().
    *
    * @param goalWord         a single goal word to test against
    * @param guesses          a list of characters to guess
    * @param expectedPatterns a list of strings corresponding to the expected
    *                         pattern after each guess
    * @param expectedCounts   a list of the expected number of letters revealed
    *                         by each guess
    */
   private void testWordPatternsAndGuessesCount(final String goalWord,
                                                final List<Character> guesses,
                                                final List<String>
                                                      expectedPatterns,
                                                final List<Integer>
                                                      expectedCounts) {

      HangmanManager manager = setupSingleWordManager(goalWord, GUESS_LIMIT);

      for (int i = 0; i < guesses.size(); i++) {
         int count = manager.record(guesses.get(i));
         assertThat(manager.pattern(), equalTo(expectedPatterns.get(i)));
         assertThat(count, equalTo(expectedCounts.get(i)));
      }

      int wrongGuessTotal = (int) expectedCounts.stream().filter(n -> n == 0)
                                                .count();
      assertThat(manager.guessesLeft(), equalTo(GUESS_LIMIT - wrongGuessTotal));

      guesses.forEach(c -> assertTrue(manager.guesses().contains(c)));
   }

   /**
    * Tests that the Hangman constructor throws an IllegalArgumentException
    * when any of the following is true:
    * <ul>
    * <li>The dictionary is empty</li>
    * <li>The length of the goal word is zero</li>
    * <li>The number of allowed guesses is zero</li>
    * </ul>
    */
   @Test
   public void hangmanConstructorThrowsErrorWhenAnArgumentIsOutOfRange() {
      List<String> invalidWordList = new ArrayList<>();
      int invalidLength = 0;
      int invalidWrongGuessLimit = -1;

      List<String> validWordLst = new ArrayList<>();
      validWordLst.add("hello");
      int validLength = 1;
      int validWrongGuessLimit = 0;

      try {
         HangmanManager manager = new Hangman(invalidWordList, validLength,
                                              validWrongGuessLimit);
         fail("IllegalArgumentException should have been thrown but wasn't");
      } catch (Exception e) {
         assertThat(e, instanceOf(IllegalArgumentException.class));
      }

      try {
         HangmanManager manager = new Hangman(validWordLst, invalidLength,
                                              validWrongGuessLimit);
         fail("IllegalArgumentException should have been thrown but wasn't");
      } catch (Exception e) {
         assertThat(e, instanceOf(IllegalArgumentException.class));
      }

      try {
         HangmanManager manager = new Hangman(validWordLst, validLength,
                                              invalidWrongGuessLimit);
         fail("IllegalArgumentException should have been thrown but wasn't");
      } catch (Exception e) {
         assertThat(e, instanceOf(IllegalArgumentException.class));
      }

   }


   /**
    * Tests that all candidate goal words are of the correct size.
    */
   @Test
   public void wordListContainsWordsOfCorrectSize() {
      int goalLength = 5;
      HangmanManager manager = setupBasicManager(SHORT_DICTIONARY, goalLength,
                                                 GUESS_LIMIT);

      assertThat(manager.words().stream()
                        .allMatch(word -> word.length() == goalLength),
                 is(true));
   }

   /**
    * Tests that list returned by words() is of size zero if no words
    * in the dictionary match the goalWord length.
    * Currently ignored because this state causes an exception to be thrown
    * during construction of the Hangman class.
    * @see Hangman
    */
   @Test
   @Ignore("Not possible to test due to constructor throwing error when no goal word of specified size exists")
   public void wordListReturnsSizeOfZeroWhenNoWordsMatchGivenLength() {
      int goalLength = 25;
      HangmanManager manager = setupBasicManager(SHORT_DICTIONARY, goalLength,
                                                 GUESS_LIMIT);
      assertThat(manager.words().size(), equalTo(0));
   }

   /**
    * Tests that the patterns returned for a five- and three- letter goal word
    * is "- - - - -" and "- - -" when no letters have been guessed.
    */
   @Test
   public void whenNoLettersHaveBeenGuessedReturnPatternIsStringOfDashes() {
      HangmanManager manager = setupBasicManager(SHORT_DICTIONARY, GOAL_LENGTH,
                                                 GUESS_LIMIT);
      String pattern = manager.pattern();
      assertThat(pattern, equalTo("- - - - -"));

      manager = setupBasicManager(FULL_DICTIONARY, 3, GUESS_LIMIT);
      assertThat(manager.pattern(), equalTo("- - -"));

   }

   /**
    * Tests that the set of goalWords contains "hello" when
    * the dictionary contains only the word "hello".
    */
   @Test
   public void singleWordDictionaryReturnsCorrectGoalWord() {
      String singleWord = "hello";
      HangmanManager manager = setupSingleWordManager(singleWord, GUESS_LIMIT);
      String word = manager.words().stream().findFirst().orElse("");

      assertThat(word, equalTo("hello"));
      assertThat(manager.words().size(), equalTo(1));
   }

   /**
    * When the goal word is "hello", tests that the pattern and state is correct
    * after each guess of the letters 'h', 'e', 'l', and 'o'. Note: All guesses
    * reveal letters. Please see {@link #testWordPatternsAndGuessesCount(String,
    * List, List, List)}
    */
   @Test
   public void helloReturnsCorrectPatternAfterSuccessiveCorrectGuesses() {
      String goalWord = "hello";
      List<Character> guessList = Arrays.asList('h', 'e', 'l', 'o');
      List<String> expectedPatterns = Arrays
            .asList("h - - - -", "h e - - -", "h e l l -", "h e l l o");
      List<Integer> expectedCounts = Arrays.asList(1, 1, 2, 1);

      testWordPatternsAndGuessesCount(goalWord, guessList, expectedPatterns,
                                      expectedCounts);
   }

   /**
    * When the goal word is "hello", tests that the pattern and state is correct
    * after each guess of the letters 'r', 's', 't', 'l', 'n', and 'e' Note: Mix
    * of correct and incorrect guesses Please see {@link
    * #testWordPatternsAndGuessesCount(String, List, List, List)}.
    */
   @Test
   public void helloReturnsCorrectPatternsAndStateAfterMixOfCorrectAndIncorrectGuesses() {
      String goalWord = "hello";
      List<Character> guessList = Arrays.asList('r', 's', 't', 'l', 'n', 'e');
      List<String> expectedPatterns = Arrays
            .asList("- - - - -", "- - - - -", "- - - - -", "- - l l -",
                    "- - l l -",
                    "- e l l -");
      List<Integer> expectedCounts = Arrays.asList(0, 0, 0, 2, 0, 1);

      testWordPatternsAndGuessesCount(goalWord, guessList, expectedPatterns,
                                      expectedCounts);

   }

   /**
    * Tests that an IllegalStateException is thrown if an attempt is made
    * to guess a letter when remaining guesses is zero.
    */
   @Test
   public void guessingALetterWhenNoGuessesRemainThrowsError() {
      HangmanManager manager = setupSingleWordManager("hello", 1);
      manager.record('a');

      exception.expect(IllegalStateException.class);
      manager.record('b');
   }

   /**
    * Tests that an IllegalStateException is thrown if a guess is made when no
    * the list of goal words is empty. This exception is required by the
    * HangmanManager interface, but under the current Hangman implementation it
    * is not possible to reach this state since a similar exception is thrown in
    * the constructor.
    *
    * @see Hangman
    */
   @Test
   @Ignore("Not possible to test due to constructor throwing an error when no goal words of specified length")
   public void guessingALetterWhenNoGoalWordThrowsError() {
      HangmanManager manager = setupBasicManager(SHORT_DICTIONARY, 100,
                                                 GUESS_LIMIT);

      exception.expect(IllegalStateException.class);
      manager.record('a');
   }

   /**
    * Tests that an IllegalArgumentException is thrown if the same
    * letter is guessed more than once.
    */
   @Test
   public void guessingTheSameLetterTwiceThrowsError() {
      HangmanManager manager = setupSingleWordManager("hello", GUESS_LIMIT);
      manager.record('a');

      exception.expect(IllegalArgumentException.class);
      exception.expectMessage("The letter a has already been guessed");
      manager.record('a');

   }




}

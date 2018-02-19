import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * The Hangman class keeps track of the state of a game of hangman.
 * Methods provide access to the current set of guesses,
 * number of wrong guesses remaining, and the current pattern
 * to be displayed to the user.
 * The <CODE>record</CODE> method updates state by recording a new guess
 * and updating the other values appropriately.
 *
 * @author Brian von Behren
 * @version 1.2
 * @see HangmanManager
 */

public class Hangman implements HangmanManager {

   /**
    * List of potential goal words.
    */
   private List<String> candidateDictionary;

   /**
    * Length of the goal word.
    */
   private int goalLength;

   /**
    * The number of wrong guesses that results in a player loss.
    */
   private int guessLimit;

   /**
    * Current list of potential goal words. Size will be 1 unless
    * DEVIOUS flag is set.
    */
   private Set<String> goalWords;

   /**
    * The number of wrong guesses that result in a loss for the player given
    * the current state of the game.
    */
   private int guessesLeft;

   /**
    * Sorted set of all letters guessed by the player.
    */
   private SortedSet<Character> guessedLetters = new TreeSet<>();

   /**
    * The default no-parameter constructor is not publicly available.
    */
   private Hangman() {

   }

   /**
    * Standard constructor for the Hangman game. Initialized state based on
    * parameters.
    *
    * @param dictionary      the list of potential goal words
    * @param length          the length of the goal word
    * @param wrongGuessLimit the number of wrong guesses that will result in a
    *                        loss
    * @throws IllegalArgumentException if candidateDictionary is empty
    * @throws IllegalArgumentException if goalLength is less than 1
    * @throws IllegalArgumentException if guessLimit is less than 1
    * @throws IllegalArgumentException if there are no words in the
    *                                  candidateDictionary with size equal to
    *                                  goalLength
    */
   public Hangman(final List<String> dictionary, final int length,
                  final int wrongGuessLimit) {

      if (dictionary.size() == 0) {
         throw new IllegalArgumentException("Dictionary must not be empty.");
      }

      if (length < 1) {
         throw new IllegalArgumentException("Word goalLength must be greater"
                                                  + "than zero");
      }

      if (wrongGuessLimit < 1) {
         throw new IllegalArgumentException("Wrong guess limit must greater "
                                                  + "than or equal to one");
      }

      if (dictionary.stream().filter(word -> word.length() == length)
                    .count() < 1) {
         throw new IllegalArgumentException(
               "No word in the candidateDictionary is of the specified length");
      }

      this.candidateDictionary = dictionary;
      this.goalLength = length;
      this.guessLimit = wrongGuessLimit;
      this.guessesLeft = wrongGuessLimit;
   }


   /**
    * Access the set of candidate words;
    * if size == 1, contents are the actual goal word.
    *
    * @return the goal word or the candidate goal words
    */
   @Override
   public Set<String> words() {
      if (goalWords != null) {
         return goalWords;
      }

      this.goalWords = candidateDictionary.stream()
                                    .filter(word -> word.length() == goalLength)
                                    .collect(Collectors.toSet());
      return goalWords;
   }


   /**
    * Access the limit on wrong guesses.
    *
    * @return the number of wrong guesses that results in a player loss
    */
   @Override
   public int wrongGuessLimit() {

      return this.guessLimit;
   }


   /**
    * Access the number of wrong guesses that result in a loss
    * for the player given the current state of the game.
    *
    * @return the number of wrong guesses that would result in a loss
    */
   @Override
   public int guessesLeft() {
      return this.guessesLeft;
   }

   /**
    * Access the set of letters already guessed by the player.
    *
    * @return the set of letters guessed by the player
    */
   @Override
   public SortedSet<Character> guesses() {
      return Collections.unmodifiableSortedSet(guessedLetters);
   }


   /**
    * Return the hangman-style display pattern of letters and dashes
    * (with interpolated spaces) appropriate to the current state
    * based on the letters already guessed and the goal.
    *
    * @return the hangman-style pattern to be displayed to the user
    * @throws IllegalStateException if there is no goal word
    */
   @Override
   public String pattern() {
      String goalWord = getFirstGoalWord();

      try {
         if (goalWord.equals("")) {
            throw new IllegalStateException(
                  "List returned by words() is empty");
         }
      } catch (IllegalStateException e) {
         e.printStackTrace();
      }

      return getPattern(goalWord, guessedLetters);
   }


   /**
    * Record state changes based on new letter guessed, using the rules for
    * a "devious" CPU.
    *
    * @param guess the letter being guessed [Precondition: guess must be
    *              lower-case letter] [Precondition: guess must not be among
    *              letters already guessed]
    * @return the number of occurrences of the guessed letter in the goal
    * @throws IllegalStateException    if no guesses left or no goal word
    * @throws IllegalArgumentException if letter is already guessed
    */
   public int record(final char guess) {
      if ((guessesLeft <= 0)) {
         throw new IllegalStateException("GuessesLeft is 0 or less");
      }
      if ((words().size() == 0)) {
         throw new IllegalStateException("List returned by words() is empty");
      }
      if (guessedLetters.contains(guess)) {
         throw new IllegalArgumentException(
               String.format("The letter %1$s has already been guessed",
                             guess));
      }

      guessedLetters.add(guess);

      HashMap<String, Set<String>> wordFamilies = new HashMap<>();

      words().forEach(word -> {
         String pattern = getPattern(word, guessedLetters);
         Set<String> wordSet = wordFamilies.getOrDefault(pattern,
                                                         new TreeSet<>());
         wordSet.add(word);
         wordFamilies.put(pattern, wordSet);
      });

      goalWords = wordFamilies.values().stream()
                              .max(Comparator.comparingInt(Set::size))
                              .orElse(new TreeSet<>());

      int numRevealed = (int) getFirstGoalWord().chars().filter(c -> c == guess)
                                                .count();

      if (numRevealed == 0) {
         guessesLeft--;
      }

      return numRevealed;
   }


   /**
    * Creates a hangman style pattern. Works by converting the goalWord into
    * a stream of characters, and then mapping each character to either a '-'
    * or a letter depending on if the character has been guessed. The result
    * is then joined into a string, separated by spaces.
    *
    * @param forWord the goalWord to generate the pattern for
    * @param guesses the letters guessed so far
    * @return a hangman style string representing the pattern of guessed and
    *         unknown letters
    */
   private String getPattern(final String forWord,
                             final SortedSet<Character> guesses) {

      //PATTERN GENERATION
      return forWord.chars().mapToObj(c -> (char) c).map(c ->
            guesses.contains(c) ? String.valueOf(c) : "-")
           .collect(Collectors.joining(" "));

   }

   /**
    * Obtains the first word of the set of goal words or an empty string if the
    * set is empty. For the normal rules, the set will always have a single
    * item.
    *
    * @return a single goal word or an empty string.
    */
   private String getFirstGoalWord() {
      return words().stream().findFirst().orElse("");
   }
}

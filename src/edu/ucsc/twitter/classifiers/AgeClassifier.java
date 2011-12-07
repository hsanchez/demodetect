package edu.ucsc.twitter.classifiers;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import edu.ucsc.cli.IterableFileReader;
import edu.ucsc.cli.util.Console;
import edu.ucsc.cli.util.Strings;
import edu.ucsc.twitter.DemographicsClassifier;
import edu.ucsc.twitter.util.TwitterEnvironment;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The age of a twitter user is difficult to ascertain without a direct survey. To inferr it
 * we trained a classifier with examples containing phrases such as "I am 23", "Im 23" or "I'm 23"
 * that didn't contain mentions of "today", "tomorrow" or "birthday" and other representative phrases.
 * This was done to reduce the skew of people announcing birthdays.
 *
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class AgeClassifier extends DemographicsClassifier<JointClassification> {
  // minor is legally defined as a person under the age of 18
  // adult is legally defined as a person above the age of 18 (inclusive)
  public static final String[] CATEGORIES = {"minor", "adult"};

  private static final Set<String> STOP_WORDS;
  private static final Set<String> KEYWORDS;
  static {
    final Set<String> words = new HashSet<String>();
    words.add(":)");
    words.add(":(");
    words.add(":-)");
    words.add(":-(");
    words.add(")");
    words.add("(");
    words.add("}");
    words.add("{");
    words.add("=");
    words.add("=)");
    words.add("^_^");
    words.add("-");
    words.add("$lt;3");
    words.add(":/");
    words.add(":|");
    words.add("www");
    words.add("&quot;");
    words.add("?");
    words.add(",");
    words.add("--");
    words.add("&gt;");
    words.add(";)");
    words.add(":'(");
    words.add(";/");
    words.add(";\\");
    words.add("'");
    words.add("=d");
    words.add("in");
    words.add("the");
    words.add(":d");
    words.add("!!");
    words.add("+");
    words.add("!!!");
    words.add(":p");
    words.add("&gt");
    words.add("&gt:");
    words.add(":o");
    words.add(";o");
    words.add("&lt;3");
    words.add("- ");
    STOP_WORDS = Collections.unmodifiableSet(words);


    final Set<String> keywordsToRemove = Sets.newHashSet();
    keywordsToRemove.add("I am 8");
    keywordsToRemove.add("Im 8");
    keywordsToRemove.add("I'm 8");
    keywordsToRemove.add("I am 9");
    keywordsToRemove.add("Im 9");
    keywordsToRemove.add("I'm 9");
    keywordsToRemove.add("I am 10");
    keywordsToRemove.add("Im 10");
    keywordsToRemove.add("I'm 10");
    keywordsToRemove.add("I am 11");
    keywordsToRemove.add("Im 11");
    keywordsToRemove.add("I'm 11");
    keywordsToRemove.add("I am 12");
    keywordsToRemove.add("Im 12");
    keywordsToRemove.add("I'm 12");
    keywordsToRemove.add("I am 13");
    keywordsToRemove.add("Im 13");
    keywordsToRemove.add("I'm 13");
    keywordsToRemove.add("I am 14");
    keywordsToRemove.add("Im 14");
    keywordsToRemove.add("I'm 14");
    keywordsToRemove.add("I am 15");
    keywordsToRemove.add("Im 15");
    keywordsToRemove.add("I'm 15");
    keywordsToRemove.add("I am 11");
    keywordsToRemove.add("Im 11");
    keywordsToRemove.add("I'm 11");
    keywordsToRemove.add("I am 23");
    keywordsToRemove.add("Im 23");
    keywordsToRemove.add("I'm 23");

    KEYWORDS = Collections.unmodifiableSet(keywordsToRemove);
  }


  private final NaiveBayesClassifier classifier;

  public AgeClassifier() {
    this(new NaiveBayesClassifier(CATEGORIES, createStopWordsTokenizer()));
  }

  AgeClassifier(NaiveBayesClassifier classifier){
    super();
    this.classifier = classifier;
  }

  @Override public JointClassification classify(String text) {
    final JointClassification cls = classifier.classify(text);
    info(String.format(
        "%s - minor(%f) adult(%f): %s\n",
        cls.bestCategory(),
        cls.conditionalProbability("minor"),
        cls.conditionalProbability("adult"),
        text
    ));
    return cls;
  }

  @Override public void train() {
    int numTrainingCases = 0;
    int numTrainingChars = 0;

    final String basepath = TwitterEnvironment.getInstance().getOutputFoldername();
    for (String eachCategory : CATEGORIES){
      final IterableFileReader inFile = IterableFileReader.openSilently(
          String.format("%s/%s.yml", basepath, eachCategory));
      for(String eachLine : inFile){
        if(Strings.contains(eachLine, "---")) continue;
        if(Strings.contains(eachLine, ":userid:")) continue;
        if(Strings.contains(eachLine, ":date:")) continue;
        if(Strings.contains(eachLine, ":username:")) continue;
        if(Strings.contains(eachLine, ":firstname:")) continue;
        if(Strings.contains(eachLine, ":hint:")) continue;

        final int lastIndexOf = eachLine.lastIndexOf("s:");
        final String filteredLine = eachLine.substring(lastIndexOf, eachLine.length()).trim();
        ++numTrainingCases;
        numTrainingChars += filteredLine.length();

        final Classification classification = new Classification(eachCategory);
        final Classified<CharSequence> classified = new Classified<CharSequence>(filteredLine,
            classification);
        classifier.handle(classified);

      }
    }

    info("  # Training Cases=" + numTrainingCases);
    info("  # Training Chars=" + numTrainingChars);

  }

  @Override public void evaluate() {
    train();
    info("AgeClassifier#train() - basic evaluation....");

    int[] numTests   = new int[CATEGORIES.length];
    int[] numCorrect = new int[CATEGORIES.length];
    int[] numWrong   = new int[CATEGORIES.length];

    final String basepath = TwitterEnvironment.getInstance().getOutputFoldername();

    for (int idx = 0; idx < CATEGORIES.length; ++idx) {
      final IterableFileReader inFile = IterableFileReader.openSilently(
          String.format("%s/%s.yml", basepath, CATEGORIES[idx]));
      for (String eachLine : inFile) {
        ++numTests[idx];
        JointClassification jointClassification = classifier.classify(eachLine);
        info(String.format(
            "%s - minor(%f) adult(%f): %s\n",
            jointClassification.bestCategory(),
            jointClassification.conditionalProbability("minor"),
            jointClassification.conditionalProbability("adult"),
            eachLine
        ));

        if (Objects.equal(jointClassification.bestCategory(), CATEGORIES[idx])) {
          ++numCorrect[idx];
        } else {
          ++numWrong[idx];
        }
      }
    }

    printEvaluationResults(numTests, numCorrect, numWrong);

  }

  public void generateConfusionMatrix() throws IOException, ClassNotFoundException {
    final String basepath = TwitterEnvironment.getInstance().getOutputFoldername();
    JointClassifier<CharSequence> compiledClassifier = castToJointClassifier(classifier);
    JointClassifierEvaluator<CharSequence> evaluator = new JointClassifierEvaluator<CharSequence>(
        compiledClassifier, CATEGORIES, false);

    info("AgeClassifier#generateConfusionMatrix() - evaluation....");
    for (String eachCategory : CATEGORIES) {
      final Classification classification   = new Classification(eachCategory);
      final IterableFileReader inFile = IterableFileReader.openSilently(
          String.format("%s/%s.yml", basepath, eachCategory));

      for (String eachLine : inFile) {
        final Classified<CharSequence> classified = new Classified<CharSequence>(eachLine,
            classification);
        evaluator.handle(classified);
      }
    }

    final ConfusionMatrix confusionMatrix = evaluator.confusionMatrix();
    info(confusionMatrix.toString());
  }

  public boolean isMinorSayingthis(String text){
    final JointClassification cls = classify(text);

    // we want to print only negative ones.
    final int result = Double
        .compare(cls.conditionalProbability("minor"), cls.conditionalProbability("adult"));
    return result > 0;
  }

  private static JointClassifier<CharSequence> castToJointClassifier(NaiveBayesClassifier classifier) {
    try {
      //noinspection unchecked
      return (JointClassifier<CharSequence>) AbstractExternalizable.compile(classifier);  // unchecked warning
    } catch (Exception e) {
      Console.streaming().error("unable to create joint classifier", e);
      throw new RuntimeException(e);
    }
  }

  private void printEvaluationResults(int[] numTests, int[] numCorrect, int[] numWrong) {
    int totalTests = 0, totalCorrect = 0, totalWrong = 0;
    for (int i = 0; i < numTests.length; ++i) {
      info(String.format("  # Test Cases for %s=%d\n", CATEGORIES[i], numTests[i]));
      info(String.format("  # Correct=%d\n", numCorrect[i]));
      info(String.format("  %% Correct=%f\n", ((double) numCorrect[i]) / (double) numTests[i]));
      info(String.format("  %% Wrong=%f\n", ((double) numWrong[i]) / (double) numTests[i]));
      totalTests   += numTests[i];
      totalCorrect += numCorrect[i];
      totalWrong   += numWrong[i];
    }


    double accuracy  = ((double) totalCorrect) / (double) totalTests;
    double errorRate = ((double) totalWrong) / (double) totalTests;

    info(String.format("\n  # Total Test Cases =%d\n", totalTests));
    info(String.format("  # Correct=%d\n", totalCorrect));
    info(String.format("  %% Accuracy=%f\n", accuracy));
    info(String.format("  %% Error Rate=%f\n", errorRate));
    //As usual, we compute 95% confidence intervals for a given accuracy of p over N trials
    // using the binomial distribution bionmial(p,N), which is just the distribution corresponding
    // to N independent trials each with a p chance of success. The deviation of the binomial
    // distribution is:
    // dev(binomial(p,N)) = sqrt(p*(1-p)/N)
    info(String.format("  %% Confidence Interval=%f\n", Math.sqrt(accuracy * (1.0 - accuracy)/((double )totalTests))));
  }

  protected static TokenizerFactory createStopWordsTokenizer() {
    final TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
    final TokenizerFactory f2 = new LowerCaseTokenizerFactory(f1);
    return new StopTokenizerFactory(f2, Sets.union(STOP_WORDS, KEYWORDS));
  }

  public static void main(String[] args) throws ClassNotFoundException, IOException {
    final AgeClassifier
        classifier = new AgeClassifier();

    // evaluate classifier
    classifier.evaluate();

    // evaluate classifier - generate confusion matrix
    classifier.generateConfusionMatrix();

    //classifier.train();
    System.out.println("You are " + (classifier.isMinorSayingthis(
        "man, you suck you piece of shit.") ? " a minor" : " an adult"));

    final String minorSaying = "hi Labrith I Am your biggest ran please write back you are amazing i am Amisha";
    System.out.println("You are " + (classifier.isMinorSayingthis(
    minorSaying) ? " a minor" : " an adult"));
  }
}

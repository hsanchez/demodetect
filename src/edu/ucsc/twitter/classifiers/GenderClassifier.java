package edu.ucsc.twitter.classifiers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import edu.ucsc.cli.IterableFileReader;
import edu.ucsc.cli.util.Console;
import edu.ucsc.cli.util.Pair;
import edu.ucsc.cli.util.Strings;
import edu.ucsc.twitter.DemographicsClassifier;
import edu.ucsc.twitter.Gender;
import java.io.IOException;
import java.util.Map;

/**
 * Twitter users do not specify their gender on registration. To deduce the gender of each
 * user, we compared their full name (if provided) against US Census Data of first names (which was
 * manually updated to include more recent names for obvious omissions). If a name could be either
 * gender, we chose whichever gender had a higher popularity (frequency or commulative frequency) of
 * that name.
 *
 * The idea was given by {@code http://www.boxuk.com/blog/twitter-user-demographics}
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class GenderClassifier extends DemographicsClassifier<Gender> {
  private final PersonNameRegistry registry;
  public GenderClassifier(){
    super();
    registry = PersonNameRegistry.INSTANCE;
  }

  @Override public Gender classify(String person) {
    final Pair<Gender, Double> inferredGender = registry.findGenderOf(person);
    return inferredGender.getLeft();
  }

  private static String lowercaseGender(Gender gender){
    return gender.toString().toLowerCase();
  }

  private static IterableFileReader openGenderfile(Gender gender){
    try {
      return getIterableFileReaders("male", "female").get(lowercaseGender(gender));
    } catch (IOException e) {
      Console.streaming().error("unable to open female/male file", e);
      throw new RuntimeException();
    }
  }

  /**
   * a record class that keeps information about the statistics of names found
   * in US Census's name files.
   *
   * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
   */
  static class Namestats {
    double freq;
    double commulativeFreq;
    int    rank;
  }

  /**
   * A singleton class representing the name files in memory.
   * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
   */
  enum PersonNameRegistry {
    INSTANCE;
    private static final ImmutableMap<String, Namestats> MALE_NAMES;
    private static final ImmutableMap<String, Namestats> FEMALE_NAMES;
    static {
      final Map<String,Namestats> female = Maps.newHashMap();
      for(String each : openGenderfile(Gender.FEMALE)){
        final Namestats stats = new Namestats();
        final String[]  split = Strings.splits(each, ' ');
        final String    name  = split[0].toLowerCase();

        stats.freq            = Double.valueOf(split[1]);
        stats.commulativeFreq = Double.valueOf(split[2]);
        stats.rank            = Integer.valueOf(split[3]);

        female.put(name, stats);
      }

      FEMALE_NAMES = ImmutableMap.copyOf(female);

      final Map<String,Namestats> male = Maps.newHashMap();
      for(String each : openGenderfile(Gender.MALE)){
        final Namestats stats = new Namestats();
        final String[]  split = Strings.splits(each, ' ');
        final String    name  = split[0].toLowerCase();

        stats.freq            = Double.valueOf(split[1]);
        stats.commulativeFreq = Double.valueOf(split[2]);
        stats.rank            = Integer.valueOf(split[3]);

        male.put(name, stats);
      }

      MALE_NAMES   = ImmutableMap.copyOf(male);
    }

    public Pair<Gender, Double> findGenderOf(String person){
      final String nonNullName = Preconditions.checkNotNull(person).toLowerCase();
      final Namestats male   = MALE_NAMES.get(nonNullName);
      final Namestats female = FEMALE_NAMES.get(nonNullName);
      if(male == null)   return Pair.of(Gender.FEMALE, 1.0);
      if(female == null) return Pair.of(Gender.MALE, 1.0);

      final double freqByMale   = male.freq;
      final double freqByFemale = female.freq;

      if(Doubles.compare(freqByMale, freqByFemale) == 0) {
        final double cummulativeFreqMale   = male.commulativeFreq;
        final double commulativeFreqFemale = female.commulativeFreq;
        if(Double.compare(cummulativeFreqMale, commulativeFreqFemale) > 0){
          return Pair.of(Gender.MALE, 1.0);
        } else {
          return Pair.of(Gender.FEMALE, 1.0);
        }


      } else {
        if(Double.compare(freqByMale, freqByFemale) > 0) {
          return Pair.of(Gender.MALE, 1.0);
        } else {
          return Pair.of(Gender.FEMALE, 1.0);
        }
      }

    }

  }

  public static void main(String[] args) {
    final String maleTest   = "Mark";
    final String femaleTest = "Mary";
    final GenderClassifier
        classifier = new GenderClassifier();
    final Gender maleResult   = classifier.classify(maleTest);
    final Gender femaleResult = classifier.classify(femaleTest);

    System.out.println(String.format("The expected result is male, and we got? %s",
        (Gender.isMale(maleResult) ? "male, Yeah!!" : "female, Yeah!!")));

    System.out.println(String.format("The expected result is female, and we got? %s",
        (Gender.isMale(femaleResult) ? "male, Yeah!!" : "female, Yeah!!")));
  }

}

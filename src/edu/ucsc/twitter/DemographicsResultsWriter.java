/*
 * Copyright (C) 2011 Huascar A. Sanchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucsc.twitter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import edu.ucsc.broadcast.EventSubscriber;
import edu.ucsc.twitter.ResultPackage.Kind;
import edu.ucsc.cli.util.Console;
import edu.ucsc.cli.util.Strings;
import edu.ucsc.twitter.util.TwitterEnvironment;
import edu.ucsc.twitter.util.YmlWriter;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DemographicsResultsWriter implements EventSubscriber<ResultPackage> {
  private final Set<TweetPackage> collectedPackages;

  private static Set<String> KEYWORDS;
  static {

    final Set<String> irrelevant = Sets.newHashSet();
    irrelevant.add("feel");
    irrelevant.add("like");
    irrelevant.add("mins");
    irrelevant.add("minutes");
    irrelevant.add("secs");
    irrelevant.add("seconds");
    irrelevant.add("hrs");
    irrelevant.add("hours");
    irrelevant.add("away");
    irrelevant.add("day");
    irrelevant.add("days");
    irrelevant.add("out of");
    irrelevant.add("times");
    irrelevant.add("time");
    irrelevant.add("by the");
    irrelevant.add("by");
    irrelevant.add("the");
    irrelevant.add("weeks!");
    irrelevant.add("weeks");
    irrelevant.add("away");
    irrelevant.add("today");
    irrelevant.add("tomorrow");
    irrelevant.add("birthday");
    irrelevant.add("again");
    irrelevant.add("away");
    irrelevant.add("till");
    irrelevant.add("until");
    irrelevant.add("think Im");
    irrelevant.add("think I'm");
    irrelevant.add("think I am");
    irrelevant.add("thinks Im");
    irrelevant.add("thinks I'm");
    irrelevant.add("thinks I am");
    irrelevant.add("class");
    irrelevant.add("who");
    irrelevant.add("-");
    irrelevant.add("(8)");
    irrelevant.add("(9)");
    irrelevant.add("(10)");
    irrelevant.add("(11)");
    irrelevant.add("(12)");
    irrelevant.add("(13)");
    irrelevant.add("(14)");
    irrelevant.add("(15)");
    irrelevant.add("(16)");
    irrelevant.add("(17)");
    irrelevant.add("(18)");
    irrelevant.add("(19)");
    irrelevant.add("(20)");
    irrelevant.add("(21)");
    irrelevant.add("(22)");
    irrelevant.add("(23)");
    irrelevant.add("(24)");
    irrelevant.add("(25)");
    irrelevant.add("(26)");
    irrelevant.add("(27)");
    irrelevant.add("(28)");
    irrelevant.add("(29)");
    irrelevant.add("(30)");
    irrelevant.add("cents");
    irrelevant.add("under");
    irrelevant.add("dollar");
    irrelevant.add("dollars");
    irrelevant.add("hundred");
    irrelevant.add("hundreds");
    irrelevant.add("thousand");
    irrelevant.add("thousands");
    irrelevant.add("million");
    irrelevant.add("millions");
    irrelevant.add("steps");
    irrelevant.add("ahead");
    KEYWORDS = ImmutableSet.copyOf(irrelevant);
  }

  public DemographicsResultsWriter() {
    this(Sets.<TweetPackage>newHashSet());
  }

  DemographicsResultsWriter(Set<TweetPackage> collectedPackages) {
    this.collectedPackages = collectedPackages;
  }

  private int currentSize() {
    return collectedPackages.size();
  }

  @Override public void onEvent(ResultPackage resultPackage) {
    synchronized (collectedPackages) {
      final List<Object> tweets = resultPackage.get(Kind.TWEET);
      copy(collectedPackages, tweets);
      trackProgress(currentSize(), TwitterEnvironment.getInstance().getMaxTweetsTobeExtracted());

      final boolean isPrintingResults = currentSize() >= TwitterEnvironment.getInstance()
          .getMaxTweetsTobeExtracted();
      System.out.println("info: DemographicsResultsWriter#onEvent says that we are "
          + (isPrintingResults ? "about to print results." : "not printing results yet.")
      );

      if (isPrintingResults) {
        writeYmlFile(collectedPackages);
        throw new IllegalStateException();
      }
    }
  }

  private static void writeYmlFile(Set<TweetPackage> collectedPackages) {
    final Date now = new Date();
    System.out.println("info: Finishing periodic task......");

    try {
      YmlWriter ymlWriter = null;


      int count = 1;
      for (TweetPackage each : collectedPackages) {
        if(isIrrelevant(each)) continue;

        if (count == 1) {
          final File directory = new File(TwitterEnvironment.getInstance().getOutputFoldername());
          final int fileCounter = directory.listFiles().length + 1;
          final String filename = TwitterEnvironment.getInstance().getOutputFoldername() + (
              fileCounter
                  + "-" + now.getTime() + "-" + "demographics-tweets.yml");
          ymlWriter = new YmlWriter(filename);
          ymlWriter.begin();
        }

        final String name = Strings.isEmpty(each.getFullname()) ? "n/a" : each.getFullname().split(" ")[0];
        Console.streaming().info(String.format("the name was: %s\n", name));
        ymlWriter.writeEntry(
            Strings.toString(each.getTweetCreationDate().getTime()),
            Strings.toString(each.getUserid()),
            each.getUsername(),
            name,
            each.getHint().replace(",", ""),
            each.getUserCurrentStatus()
        );

        count++;
        if (count > TwitterEnvironment.getInstance().getMaxTweetPerFile()) {
          ymlWriter.end();
          ymlWriter.finish();
          count = 1;
        }
      }

      if (count <= 40) {
        assert ymlWriter != null;
        ymlWriter.end();
        ymlWriter.finish();
        count = 1;
      }
    } catch (Exception e) {
      System.err.println("error: #writeYmlFile(List<TweetsPackage>): Unable to write to file.");
      e.printStackTrace();
    }
  }

  private static boolean isIrrelevant(TweetPackage content){
    return isIrrelevant(content.getUserCurrentStatus());
  }

  private static boolean isIrrelevant(String message){
    double THRESHOLD = 5.0;
    int count = 0;
    for(String words : KEYWORDS){
      if(Strings.contains(message.toLowerCase(), words)){
        count++;
      }
    }

    final double score = Math.min(((count) / THRESHOLD), 1.0); // max score = 1.0
    return Double.compare(score, 0.5) > 0; // is score greater than 0.5
  }

  private static void trackProgress(int actual, int possible) {
    final BigDecimal percent = BigDecimal.valueOf(((actual / possible) * 100));
    final String message = "We have collected (" + actual + ") tweets this far, progress("
        + calculatePercentage(percent) + "% full).";
    System.out.println("info: " + message);
  }

  private static BigDecimal calculatePercentage(final BigDecimal decimalPercent) {
    if (decimalPercent != null) {
      return decimalPercent.setScale(4, BigDecimal.ROUND_HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }

  private static void copy(Set<TweetPackage> dst, List<Object> src) {
    for (Object each : src) {
      dst.add((TweetPackage) each);
    }
  }

  public static void main(String[] args) {
    System.out.println(isIrrelevant(
        "I feel like I&apos;m 8 years old again with my double zipped pink tripple sectioned coooler lunch bag ;)"));
  }
}

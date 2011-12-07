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

import com.google.common.collect.Sets;
import edu.ucsc.broadcast.EventSubscriber;
import edu.ucsc.twitter.ResultPackage.Kind;
import edu.ucsc.cli.util.Strings;
import edu.ucsc.twitter.util.TwitterEnvironment;
import edu.ucsc.twitter.util.XmlWriter;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class BullyingResultsWriter implements EventSubscriber <ResultPackage> {
  private final Set<TweetPackage> collectedPackages;

  public BullyingResultsWriter(){
    this(Sets.<TweetPackage>newHashSet());
  }

  BullyingResultsWriter(Set<TweetPackage> collectedPackages){
    this.collectedPackages = collectedPackages;
  }

  private int currentSize(){
    return collectedPackages.size();
  }

  @Override public void onEvent(ResultPackage resultPackage) {
    synchronized (collectedPackages){
      final List<Object> tweets = resultPackage.get(Kind.TWEET);
      copy(collectedPackages, tweets);
      trackProgress(currentSize(), TwitterEnvironment.getInstance().getMaxTweetPerFile());

      final boolean isPrintingResults = currentSize() >= TwitterEnvironment.getInstance().getMaxTweetPerFile();
      System.out.println("info: ResultsWriter#onEvent says that we are "
          + (isPrintingResults ? "about to print results." : "not printing results yet.")
      );

      if(isPrintingResults) {
        writeXmlFile(collectedPackages);
        throw new IllegalStateException();
      }
    }
  }

  private static void writeXmlFile(Set<TweetPackage> collectedPackages) {
      final Date now = new Date();
      System.out.println("info: Finishing periodic task......");

      final File directory      = new File(TwitterEnvironment.getInstance().getOutputFoldername());
      final int  fileCounter    = directory.listFiles().length + 1;

    try {
      final String filename = TwitterEnvironment.getInstance().getOutputFoldername() + (fileCounter + "-"  + now.getTime() + "-" + "tweets.xml");
      final XmlWriter xmlWriter = new XmlWriter(filename);
      xmlWriter.begin();
      xmlWriter.start("twitter_events");
      for(TweetPackage each : collectedPackages){
        if(each.getUserMentions().isEmpty()){
          xmlWriter.tag(
              "event",
              new String[]{"date","id","aggressor","victim", "status"},
              new String[]{
                  Strings.toString(each.getTweetCreationDate().getTime()),
                  Strings.toString(each.getUserid()),
                  each.getUsername(),
                  "n/a",
                  each.getUserCurrentStatus()},
              5
          );
        } else {
          final StringBuilder mentions = new StringBuilder();
          for(Iterator<String> itr = each.getUserMentions().iterator(); itr.hasNext();){
            mentions.append(itr.next());
            if(itr.hasNext()){
              mentions.append(", ");
            }
          }
          xmlWriter.tag(
              "event",
              new String[]{"date","id","aggressor","victim", "status"},
              new String[]{
                  Strings.toString(each.getTweetCreationDate().getTime()),
                  Strings.toString(each.getUserid()),
                  each.getUsername(),
                  mentions.toString(),
                  each.getUserCurrentStatus()},
              5
          );
        }
      }
      xmlWriter.end();
      xmlWriter.finish();
    } catch (Exception e){
      System.err.println("error: #writeXmlFile(List<TweetsContext>): Unable to write to file.");
      e.printStackTrace();
    }
  }

  private static void trackProgress(int actual, int possible){
    final BigDecimal percent = BigDecimal.valueOf(((actual / possible)*100));
    final String message  = "We have collected (" + actual + ") tweets this far, progress(" + calculatePercentage(percent) + "% full).";
    System.out.println("info: " + message);
  }

  private static BigDecimal calculatePercentage(final BigDecimal decimalPercent){
    if (decimalPercent != null) {
        return decimalPercent.setScale(4, BigDecimal.ROUND_HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }
  private static void copy(Set<TweetPackage> dst, List<Object> src){
    for(Object each : src){
      dst.add((TweetPackage) each);
    }
  }
}

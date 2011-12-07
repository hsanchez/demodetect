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
package edu.ucsc.twitter.util;

import edu.ucsc.cli.IterableFileReader;
import edu.ucsc.cli.util.Environment;
import edu.ucsc.cli.util.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class TwitterEnvironment extends Environment {
  public TwitterEnvironment() throws IOException {
    this(
        new PropertiesConfiguration(
            getDefaultProperties(),
            TwitterEnvironmentProperties.FILE
        )
    );
  }

  TwitterEnvironment(Configuration configuration) {
    super(configuration);
  }

  
  public void changeRandomSampleArity(int val){
    getAbstractConfiguration().setProperty(TwitterEnvironmentProperties.RANDOM_SAMPLE_ARITY, String.valueOf(val));
  }

  public void changeMaxNumberOfTweetsPerFile(int val) {
    getAbstractConfiguration().setProperty(TwitterEnvironmentProperties.MAX_NUMBER_TWEETS_PER_FILE, String.valueOf(val));
  }

  public void changeOutputFolder(String newOutputFolderPath){
    getAbstractConfiguration().setProperty(TwitterEnvironmentProperties.OUTPUT_FOLDERNAME, newOutputFolderPath);
  }

  public void changeMaxTweetsToBeExtracted(int maxNumber){
    getAbstractConfiguration().setProperty(TwitterEnvironmentProperties.MAX_NUMBER_TWEETS, String.valueOf(maxNumber));
  }  
  
  
  @Override public AbstractConfiguration getAbstractConfiguration(){
    return (AbstractConfiguration) getConfiguration();
  }

  private static Properties getDefaultProperties() {
    return new Properties() {
      private static final long serialVersionUID = 1L;

      {
        setProperty(TwitterEnvironmentProperties.KEYWORDS,
            System.getProperty("user.dir") + "/extensions/config/keywords.cfg");
        setProperty(TwitterEnvironmentProperties.TWITTER_SCREENNAME, "none");
        setProperty(TwitterEnvironmentProperties.TWITTER_PASSWORD, "none");
        setProperty(TwitterEnvironmentProperties.OUTPUT_FOLDERNAME,
            System.getProperty("user.dir") + "/extensions/twitter/output/");
        setProperty(TwitterEnvironmentProperties.MAX_NUMBER_TWEETS_PER_FILE, String.valueOf(40));
        setProperty(TwitterEnvironmentProperties.RANDOM_SAMPLE_ARITY, String.valueOf(3));
        setProperty(TwitterEnvironmentProperties.MAX_NUMBER_TWEETS, String.valueOf(300));
      }
    };
  }
  

  /**
   * @return the environment singleton.
   */
  public static TwitterEnvironment getInstance() {
    return Installer.INSTANCE;
  }

  /**
   * @return {@link TwitterEnvironmentProperties#OUTPUT_FOLDERNAME}
   */
  public String getKeywordsFile() {
    return System.getProperty("user.dir") + "/" + Strings.toString(getConfiguration().getProperty(
        TwitterEnvironmentProperties.KEYWORDS));
  }

  /**
   * @return {@link TwitterEnvironmentProperties#MAX_NUMBER_TWEETS_PER_FILE}
   */
  public int getMaxTweetPerFile() {
    return Integer.valueOf(
        String.valueOf(getConfiguration().getProperty(TwitterEnvironmentProperties.MAX_NUMBER_TWEETS_PER_FILE)));
  }

  public int getMaxTweetsTobeExtracted(){
    return Integer.valueOf(
        String.valueOf(getConfiguration().getProperty(
            TwitterEnvironmentProperties.MAX_NUMBER_TWEETS)));
  }

  /**
   * @return {@link TwitterEnvironmentProperties#OUTPUT_FOLDERNAME}
   */
  public String getOutputFoldername() {
    return System.getProperty("user.dir") + "/" + Strings.toString(getConfiguration().getProperty(
        TwitterEnvironmentProperties.OUTPUT_FOLDERNAME));
  }

  /**
   * @return {@link TwitterEnvironmentProperties#TWITTER_PASSWORD}
   */
  public String getPassword() {
    return Strings
        .toString(getConfiguration().getProperty(TwitterEnvironmentProperties.TWITTER_PASSWORD));
  }

  public static Set<String> generateRandomSampleOfKeywords() {
    final Set<String> keywordsStore = new HashSet<String>();
    try {
      preprocessKeywords(keywordsStore);
    } catch (IOException e) {
      return keywordsStore;
    }
    return keywordsStore;
  }

  /**
   * @return {@link TwitterEnvironmentProperties#TWITTER_SCREENNAME}
   */
  public String getScreenname() {
    return Strings.toString(
        getConfiguration().getProperty(TwitterEnvironmentProperties.TWITTER_SCREENNAME));
  }
  
  public static Twitter getTwitterService(){
    return Installer.TWITTER;
  }

  public static void preprocessKeywords(Set<String> out) throws IOException {
    final TwitterEnvironment env = TwitterEnvironment
        .getInstance();
    final IterableFileReader keywords = new IterableFileReader(env.getKeywordsFile());
    final List<String> keywordsStore = new ArrayList<String>();
    for (String each : keywords) {
      keywordsStore.add(each);
    }

    out.addAll(randomSampleUsingFloydsAlgorithm(keywordsStore, 3));
  }

  public static <T> Set<T> randomSampleUsingFloydsAlgorithm(List<T> items, int m) {
    final Set<T> res = new HashSet<T>(m);
    final int n = items.size();

    final Random rnd = new Random();
    for (int i = n - m; i < n; i++) {
      final int pos = rnd.nextInt(i + 1);
      T item = items.get(pos);
      if (res.contains(item)) {
        res.add(items.get(i));
      } else {
        res.add(item);
      }
    }

    return res;
  }

  /**
   * Lazy-constructed singleton, which is thread safe
   */
  static class Installer {
    static final TwitterEnvironment INSTANCE;
    static final Twitter TWITTER;

    static {
      try {
        INSTANCE = new TwitterEnvironment();
        TWITTER  = new TwitterFactory().getInstance();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}

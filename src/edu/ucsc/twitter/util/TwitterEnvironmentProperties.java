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

import edu.ucsc.cli.util.EnvironmentProperties;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class TwitterEnvironmentProperties extends EnvironmentProperties {
  /**
   * Never invoked
   */
  private TwitterEnvironmentProperties() {
    super();
  }

  /**
   * Name of the configuration properties file.
   */
  public static final String FILE = System.getProperty("user.dir") + "/extensions/twitter/config/runtime.cfg";

  /**
   * Developer's twitter username.
   */
  public static final String TWITTER_SCREENNAME         = "twitter.screenname";

  /**
   * Developer's twitter password
   */
  public static final String TWITTER_PASSWORD           = "twitter.password";

  /**
   * max number of collected tweets to be placed on a file, before creating another file.
   */
  public static final String MAX_NUMBER_TWEETS_PER_FILE = "max.tweets.per.file";

  /**
   * Name of the configuration properties file containing all keywords to be queried.
   */
  public static final String KEYWORDS                   = "file.keywords";

  /**
   * Folder for storing the results of queries.
   */
  public static final String OUTPUT_FOLDERNAME          = "dir.output";

  /**
   * the size of each random subset of words.
   */
  public static final String RANDOM_SAMPLE_ARITY        = "random.sample.of.size";

  /**
   * Max number tweets to be extracted.
   */
  public static final String MAX_NUMBER_TWEETS          = "max.tweets.extract";
}

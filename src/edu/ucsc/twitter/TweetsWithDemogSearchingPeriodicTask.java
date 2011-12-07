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

import edu.ucsc.broadcast.EventService;
import edu.ucsc.twitter.util.TwitterEnvironment;
import java.util.HashSet;
import java.util.Set;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class TweetsWithDemogSearchingPeriodicTask extends PeriodicTask {
  private final TweetsSearch                    search;
  private final int                             maxNumberOfTweet;

  private static final Set<String> DEFAULT = new HashSet<String>();
  static {
    DEFAULT.add("I'm 18");
    DEFAULT.add("I'm 17");
    DEFAULT.add("I'm 16");
    DEFAULT.add("I'm 15");
    DEFAULT.add("I'm 14");
  }

  public TweetsWithDemogSearchingPeriodicTask(TweetsSearch search, EventService eventBus){
    super(eventBus);
    this.search           = search;
    this.maxNumberOfTweet = TwitterEnvironment.getInstance().getMaxTweetsTobeExtracted();
  }

  public TweetsWithDemogSearchingPeriodicTask(EventService eventBus){
    this(new PeriodicTweetsSearch(), eventBus);
  }

  @Override public PeriodicTask getInstance() {
    return new HostileTweetsSearchingPeriodicTask(this.search, getEventBus());
  }

  @Override public Runnable getRunnable() {
    return new Runnable() {
      @Override public void run() {
        final Set<String>   randomKeywords = TwitterEnvironment.generateRandomSampleOfKeywords();
        final ResultPackage results = search
            .search(maxNumberOfTweet, randomKeywords.isEmpty() ? DEFAULT : randomKeywords);
        publish(results);
      }
    };
  }
}

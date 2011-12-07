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

import java.util.Date;
import java.util.Set;

/**
 * A container of information related to a tweet, which will allow us to set
 * the context under which was posted.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface TweetPackage {
  /**
   * @return the date when this tweet was posted.
   */
  Date getTweetCreationDate();

  /**
   * @return {@link #getUsername()}'s last seen location.
   */
  String getUserLastSeenLocation();

  /**
   * @return the name of twitter user.
   */
  String getFullname();

  /**
   * @return a multiset of users being referenced in the tweets made by {@link #getUsername()}.
   */
  Set<String> getUserMentions();

  /**
   * @return some sort of additional way for identifying this tweet.
   */
  String getHint();

  /**
   * @return {@link #getUsername()}'s tweet.
   */
  String getUserCurrentStatus();

  /**
   * @return the entity who posted the {@link #getUserCurrentStatus() tweet}.
   */
  String getUsername();

  /**
   * @return list of urls in the tweet.
   */
  Set<String> getUrls();

  /**
   * @return {@link #getUsername()}'s id.
   */
  long getUserid();

  // this suggest that this method should be implemented in subclasses.
  @Override String toString();
}

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

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import edu.ucsc.cli.util.Console;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RetrievedTweetPackage implements TweetPackage {
  private final Builder builder;

  RetrievedTweetPackage(Builder builder){
    this.builder = builder;
  }

  @Override public Date getTweetCreationDate() {
    return builder.createdAt;
  }

  @Override public String getUserLastSeenLocation() {
    return builder.lastSeen;
  }

  @Override public String getFullname() {
    return builder.fullname;
  }

  @Override public Set<String> getUserMentions() {
    return Sets.newHashSet(builder.mentions);
  }

  @Override public String getHint() {
    return builder.hint;
  }

  @Override public String getUserCurrentStatus() {
    return builder.message;
  }

  @Override public String getUsername() {
    return builder.author;
  }

  @Override public Set<String> getUrls() {
    return Sets.newHashSet(builder.urls);
  }

  @Override public long getUserid() {
    return builder.authorId;
  }

  public static class Builder implements Supplier<TweetPackage> {
    private final String     author;
    private final long       authorId;
    private String           message;
    private String           lastSeen;
    private List<String>     urls;
    private List<String>     mentions;
    private Date             createdAt;
    private String           hint;
    private String           fullname;

    public Builder(long userId, String username){
      this.authorId = userId;
      this.author   = username;

    }

    public Builder createdAt(Date val){
      this.createdAt = val;
      return this;
    }

    public Builder status(String message){
      this.message = message;
      return this;
    }

    public Builder fullname(String val){
      Console.streaming().info(String.format("the name was retrieved: %s\n", val));
      fullname = val;
      return this;
    }

    public Builder hint(String label){
      hint = label;
      return this;
    }

    public Builder lastseen(String location){
      this.lastSeen = location;
      return this;
    }

    public Builder urls(List<String> vals){
      this.urls = vals;
      return this;
    }

    public Builder mentions(List<String> vals){
      this.mentions = vals;
      return this;
    }

    @Override public TweetPackage get() {
      return new RetrievedTweetPackage(this);
    }
  }

}

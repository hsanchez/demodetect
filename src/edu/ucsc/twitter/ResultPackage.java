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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ResultPackage {
  private final Map<Kind, List<Object>> tweets;
  private ResultPackage(Map<Kind, List<Object>> tweets){
    this.tweets = tweets;
  }

  public static ResultPackage emptyTweetsPackage(){
    return emptyPackage(Kind.TWEET);
  }

  public static ResultPackage emptyPackage(Kind kind){
    final Map<Kind, List<Object>> seed = Maps.newEnumMap(Kind.class);
    seed.put(kind, Lists.<Object>newArrayList());
    return new ResultPackage(seed);
  }

  public ResultPackage putAllTweetResults(List<?> findings){
    return putAll(Kind.TWEET, findings);
  }

  public List<Object> get(Kind kind){
    return ImmutableList.copyOf(tweets.get(kind));
  }

  public ResultPackage putAll(Kind kind, List<?> findings) {
    for(Object each : findings ){
      put(kind, each);
    }
    return this;
  }

  public ResultPackage put(Kind kind, Object context) {
    final Object castObject = kind.cast(Preconditions.checkNotNull(context));
    if(!tweets.get(kind).contains(castObject)){
      tweets.get(kind).add(castObject);
    }

    return this;
  }

  public enum Kind {
    TWEET(TweetPackage.class);
    private final Class<?> cast;
    Kind(Class<?> cast){
      this.cast = cast;
    }

    Object cast(Object that){
      return cast.cast(that);
    }
  }
}

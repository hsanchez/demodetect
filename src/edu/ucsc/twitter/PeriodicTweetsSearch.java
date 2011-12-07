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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.ucsc.twitter.RetrievedTweetPackage.Builder;
import edu.ucsc.twitter.circuitbreaker.BasicCircuitBreaker;
import edu.ucsc.twitter.circuitbreaker.CircuitBreaker;
import edu.ucsc.cli.util.Strings;
import edu.ucsc.twitter.util.Tweets;
import edu.ucsc.twitter.util.TwitterEnvironment;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.User;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class PeriodicTweetsSearch implements TweetsSearch {
  private static final CircuitBreaker<RuntimeException> BREAKER
      = new BasicCircuitBreaker<RuntimeException>();

  private static final CircuitBreaker<RuntimeException> SECOND_BREAKER
      = new BasicCircuitBreaker<RuntimeException>();

  private static final Tweets UTIL = new Tweets();

  @Override public ResultPackage search(int limit, Set<String> keywords) {
    final Query tweetsQuery = buildOrQuery(keywords, "en");
    final ResultPackage result = ResultPackage.emptyTweetsPackage();
    int accumulator = 0;
    int page = 1;
    final Set<Tweet> totalRetrieved = Sets.newHashSet();
    final Set<Tweet> bunch = fetch(tweetsQuery, page);

    while (bunch.size() > 0 && accumulator < limit) {
      for (Tweet each : bunch) {
        if (!totalRetrieved.contains(each)) {
          accumulator++;
          totalRetrieved.add(each);
        }
      }

      bunch.clear();
      bunch.addAll(fetch(tweetsQuery, page++));
    }

    return result.putAllTweetResults(compileFindings(totalRetrieved, keywords));
  }

  private Set<Tweet> fetch(Query tweetsQuery, int page) {
    breaker().callStarted();
    tweetsQuery.setPage(page);
    try {
      final QueryResult result = TwitterEnvironment.getTwitterService().search(tweetsQuery);
      breaker().callSucceeded();
      return Sets.newHashSet(result.getTweets());
    } catch (Exception cause) {
      System.err.println("twitter4j API failed");
      breaker().callFailed(new RuntimeException(cause));
      return ImmutableSet.of();
    }
  }

  private static CircuitBreaker<RuntimeException> breaker() {
    return BREAKER;
  }

  private static CircuitBreaker<RuntimeException> secondBreaker(){
    return SECOND_BREAKER;
  }

  private List<TweetPackage> compileFindings(Set<Tweet> totalRetrieved, Set<String> keywords) {
    final List<TweetPackage> tweetPackages = Lists.newArrayList();

    for (Tweet each : totalRetrieved) {
      final String content = each.getText().replaceAll("[\r\n]+", " ");
      final String username = each.getFromUser();
      final long userId = each.getFromUserId();
      final String lastseen = each.getPlace() == null
          ? (Strings.isEmpty(each.getLocation()) ? "n/a" : each.getLocation())
          : each.getPlace().getName();
      final List<String> urls = UTIL.extractURLs(content);
      final List<String> mentions = UTIL.extractMentionedScreennames(content);
      final String hint  = Strings.findMembers(content, keywords);
      final Date created = each.getCreatedAt();


      final String fullname = getUserFirstname(userId);


      tweetPackages.add(new Builder(userId, username)
          .status(content).lastseen(lastseen)
          .urls(urls)
          .mentions(mentions)
          .hint(hint)
          .fullname(fullname)
          .createdAt(created).get()
      );
    }

    return tweetPackages;
  }


  private static String getUserFirstname(long userID) {
    String result = "n/a";
    while (Strings.same(result, "n/a")){
      try {
        secondBreaker().callStarted();
        try {
          final ResponseList<Status> status = TwitterEnvironment.getTwitterService().getUserTimeline(userID);
          secondBreaker().callSucceeded();
          if(!status.isEmpty()){
            for(Status each : status){
              final User user = each.getUser();
              if(user == null) continue;
              final String firstname  = user.getName();
              final String screenname = user.getScreenName();
              final boolean firstNameIsEmpty = Strings.isEmpty(firstname);
              final boolean screenmaeIsEmpty = Strings.isEmpty(screenname);
              if(firstNameIsEmpty && screenmaeIsEmpty)  return "n/a";
              if(firstNameIsEmpty && !screenmaeIsEmpty) return dealWithCamelCasing(screenname);
              if(screenmaeIsEmpty) return dealWithCamelCasing(firstname);
              return dealWithCamelCasing(firstname);
            }
           } else {
            return "none";
          }

        } catch (Exception e){
          secondBreaker().callFailed(new RuntimeException(e));
          result = "n/a";
        }

      } catch (Exception e) {
        result = "n/a";
      }

    }

    return result;
  }

  /**
   * Converts a camelCase to a more human form, with spaces. E.g. 'Camel case'
   * @param word to be inspected.
   * @return the first word in a camel cased string.
   */
  private static String dealWithCamelCasing(String word) {
    Pattern pattern = Pattern.compile("([A-Z]|[a-z])[a-z]*");

    List<String> tokens = Lists.newArrayList();
    Matcher matcher = pattern.matcher(word);
    String acronym = "";
    while (matcher.find()) {
      String found = matcher.group();
      if (found.matches("^[A-Z]$")) {
        acronym += found;
      } else {
        if (acronym.length() > 0) {
          //we have an acronym to add before we continue
          tokens.add(acronym);
          acronym = "";
        }
        tokens.add(found.toLowerCase());
      }
    }
    if (acronym.length() > 0) {
      tokens.add(acronym);
    }
    if (tokens.size() > 0) {
      final String targetString = tokens.remove(0);
      String humanisedString = Character.toUpperCase(targetString.charAt(0)) + targetString
          .substring(1);
      for (String s : tokens) {
        humanisedString += " " + s;
      }
      return humanisedString.split(" ")[0];
    }

    return word.split(" ")[0];
  }

  private Query buildOrQuery(Set<String> keywords, String language) {
    final StringBuilder queryString = new StringBuilder();
    for (Iterator<String> itr = keywords.iterator(); itr.hasNext(); ) {
      queryString.append(String.format("\"%s\"", itr.next()));
      if (itr.hasNext()) {
        queryString.append(" OR ");
      }
    }

    final Query query = new Query(queryString.toString() + " OR " + buildAndQuery(keywords));
    query.setLang(language);
    return query;
  }

  private String buildAndQuery(Set<String> keywords) {
    final StringBuilder queryString = new StringBuilder();
    for (Iterator<String> itr = keywords.iterator(); itr.hasNext(); ) {
      queryString.append(itr.next());
      if (itr.hasNext()) {
        queryString.append(" AND ");
      }
    }

    return "(" + queryString.toString() + ")";
  }

  public static void main(String[] args) {
    final String test = "huascarsanchez";
    System.out.println(dealWithCamelCasing(test));
  }
}

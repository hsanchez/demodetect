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

import com.google.common.base.Joiner;
import edu.ucsc.cli.util.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * Based on Twitter's Common API.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Tweets {
  private static final List<String> EMPTY_LIST;
  private static final List<Index> EMPTY_INDEX;
  private static final String EMPTY_STR;
  private static final List<String> UNICODE_SPACE_RANGES = Arrays.asList(
      "\\u0009-\\u000d",      //  # White_Space # Cc   [5] <control-0009>..<control-000D>
      "\\u0020",              // White_Space # Zs       SPACE
      "\\u0085",              // White_Space # Cc       <control-0085>
      "\\u00a0",              // White_Space # Zs       NO-BREAK SPACE
      "\\u1680",              // White_Space # Zs       OGHAM SPACE MARK
      "\\u180E",              // White_Space # Zs       MONGOLIAN VOWEL SEPARATOR
      "\\u2000-\\u200a",      // # White_Space # Zs  [11] EN QUAD..HAIR SPACE
      "\\u2028",              // White_Space # Zl       LINE SEPARATOR
      "\\u2029",              // White_Space # Zp       PARAGRAPH SEPARATOR
      "\\u202F",              // White_Space # Zs       NARROW NO-BREAK SPACE
      "\\u205F",              // White_Space # Zs       MEDIUM MATHEMATICAL SPACE
      "\\u3000"              // White_Space # Zs       IDEOGRAPHIC SPACE
  );

  private static final String SPACE_RANGES;

  static {
    EMPTY_LIST = Collections.unmodifiableList(Collections.<String>emptyList());
    EMPTY_INDEX = Collections.unmodifiableList(Collections.<Index>emptyList());
    EMPTY_STR = "";
    SPACE_RANGES = Joiner.on("").join(UNICODE_SPACE_RANGES);
  }

  public Tweets() {
  }


  public List<String> extractMentionedScreennames(String text) {
    if (Strings.isEmpty(text)) { return EMPTY_LIST; }

    final LinkedList<String> mentions = new LinkedList<String>();
    final Matcher matcher = Patterns.EXTRACT_MENTIONS.matcher(text);
    while (matcher.find()) {
      if (!Patterns.SCREEN_NAME_MATCH_END.matcher(
          matcher.group(Patterns.EXTRACT_MENTIONS_GROUP_AFTER)).matches()
          ) {
        mentions.add(matcher.group(Patterns.EXTRACT_MENTIONS_GROUP_USERNAME));
      }
    }

    return Collections.unmodifiableList(mentions);
  }


  public List<Index> indexMentionedScreennames(String text) {
    if (Strings.isEmpty(text)) { return EMPTY_INDEX; }
    final LinkedList<Index> mentions = new LinkedList<Index>();
    final Matcher matcher = Patterns.EXTRACT_MENTIONS.matcher(text);
    while (matcher.find()) {
      if (!Patterns.SCREEN_NAME_MATCH_END.matcher(
          matcher.group(Patterns.EXTRACT_MENTIONS_GROUP_AFTER)).matches()
          ) {
        mentions.add(new Index(matcher, "mention"));
      }
    }

    return Collections.unmodifiableList(mentions);
  }

  /**
   * Extract a @username reference from the beginning of Tweet text. A reply is an occurance of
   * {@literal @}username at the beginning of a Tweet, preceded by 0 or more spaces.
   *
   * @param text of the tweet from which to extract the replied to username
   * @return username referenced, if any (without the leading @ sign). Returns null if this is not a
   *         reply.
   */
  public String extractReplyScreenname(String text) {
    if (Strings.isEmpty(text)) { return EMPTY_STR; }
    Matcher matcher = Patterns.EXTRACT_REPLY.matcher(text);
    if (matcher.matches()) {
      return matcher.group(Patterns.EXTRACT_REPLY_GROUP_USERNAME);
    } else {
      return null;
    }
  }

  /**
   * Helper method for extracting multiple matches from Tweet text.
   *
   * @param pattern to match and use for extraction
   * @param text of the Tweet to extract from
   * @param groupNumber the capturing group of the pattern that should be added to the list.
   * @return list of extracted values, or an empty list if there were none.
   */
  private List<String> extractMultipleMatches(Pattern pattern, String text, int groupNumber) {
    final LinkedList<String> matches = new LinkedList<String>();
    final Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      matches.add(matcher.group(groupNumber));
    }

    return matches;
  }

  private List<Index> extractMultipleMatchesWithIndeces(Pattern pattern, String text,
      int groupNumber, String valueType) {
    List<Index> extracted = new LinkedList<Index>();
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      extracted.add(new Index(matcher, valueType, groupNumber));
    }
    return extracted;
  }

  /**
   * Extract #hashtag references from Tweet text.
   *
   * @param text of the tweet from which to extract hashtags
   * @return List of hashtags referenced (without the leading # sign)
   */
  public List<String> extractHashtags(String text) {
    if (Strings.isEmpty(text)) { return EMPTY_LIST; }

    return extractMultipleMatches(
        Patterns.AUTO_LINK_HASHTAGS,
        text,
        Patterns.AUTO_LINK_HASHTAGS_GROUP_TAG
    );
  }


  /**
   * Extract URL references from Tweet text.
   *
   * @param text of the tweet from which to extract URLs
   * @return List of URLs referenced.
   */
  public List<String> extractURLs(String text) {
    if (text == null) {
      return null;
    }

    List<String> urls = new ArrayList<String>();

    Matcher matcher = Patterns.VALID_URL.matcher(text);
    while (matcher.find()) {
      String protocol = matcher.group(Patterns.VALID_URL_GROUP_PROTOCOL);
      if (!protocol.isEmpty()) {
        urls.add(matcher.group(Patterns.VALID_URL_GROUP_URL));
      }
    }

    return urls;
  }

  /**
   * Extract URL references from Tweet text.
   *
   * @param text of the tweet from which to extract URLs
   * @return List of URLs referenced.
   */
  public List<Index> extractURLsWithIndices(String text) {
    if (text == null) {
      return null;
    }

    List<Index> urls = new ArrayList<Index>();

    Matcher matcher = Patterns.VALID_URL.matcher(text);
    while (matcher.find()) {
      String protocol = matcher.group(Patterns.VALID_URL_GROUP_PROTOCOL);
      if (!protocol.isEmpty()) {
        urls.add(new Index(matcher, "url", Patterns.VALID_URL_GROUP_URL, 0));
      }
    }

    return urls;
  }

  /**
   * Extract #hashtag references from Tweet text.
   *
   * @param text of the tweet from which to extract hashtags
   * @return List of hashtags referenced (without the leading # sign)
   */
  public List<Index> extractHashtagsWithIndices(String text) {
    if (Strings.isEmpty(text)) { return EMPTY_INDEX; }

    return extractMultipleMatchesWithIndeces(Patterns.AUTO_LINK_HASHTAGS, text,
        Patterns.AUTO_LINK_HASHTAGS_GROUP_TAG, "hashtag");
  }

  /**
   * Index object.
   */
  public static class Index {
    private int start;
    private int end;
    private String value;
    private String type;

    public Index(Matcher matcher, String valueType) {
      // Offset -1 on start index to include @, # symbols for mentions and hashtags
      this(matcher, valueType, Patterns.EXTRACT_MENTIONS_GROUP_USERNAME);
    }

    public Index(Matcher matcher, String valueType, int groupNumber) {
      this(matcher, valueType, groupNumber, -1);
    }

    public Index(Matcher matcher, String valueType, int groupNumber, int startOffset) {
      this.start = matcher.start(groupNumber) + startOffset; // 0-indexed
      this.end = matcher.end(groupNumber);
      this.value = matcher.group(groupNumber);
      this.type = valueType;
    }

    @Override public int hashCode() {
      return this.type.hashCode()
          + this.value.hashCode()
          + this.start
          + this.end;
    }

    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof Index)) {
        return false;
      }

      Index other = (Index) o;

      return this.type.equals(other.type) &&
          this.start == other.start &&
          this.end == other.end &&
          this.value.equals(other.value);
    }

    @Override public String toString() {
      return String.format(
          "Index(start=%d, end=%d, val=%s, type=%s)",
          start, end, value, type
      );
    }
  }

  private static class Patterns {
    private static final String AT_SIGNS_CHARS = "@\uFF20";
    private static final String LATIN_ACCENTS_CHARS
        = "\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff";
    private static final Pattern AT_SIGNS = compile("[" + AT_SIGNS_CHARS + "]");
    private static final Pattern EXTRACT_MENTIONS = compile(
        "(^|[^a-z0-9_])" + AT_SIGNS + "([a-z0-9_]{1,20})(?=(.|$))",
        CASE_INSENSITIVE
    );

    private static final Pattern EXTRACT_REPLY = Pattern
        .compile("^(?:[" + SPACE_RANGES + "])*" + AT_SIGNS + "([a-z0-9_]{1,20}).*",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SCREEN_NAME_MATCH_END = Pattern.compile(
        "^(?:[" + AT_SIGNS_CHARS + LATIN_ACCENTS_CHARS + "]|://)");

    private static final String HASHTAG_CHARACTERS
        = "[a-z0-9_\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff]";
    private static final Pattern AUTO_LINK_HASHTAGS = Pattern
        .compile("(^|[^0-9A-Z&/]+)(#|\uFF03)([0-9A-Z_]*[A-Z_]+" + HASHTAG_CHARACTERS + "*)",
            Pattern.CASE_INSENSITIVE);

    /* URL related hash regex collection */
    private static final String URL_VALID_PRECEEDING_CHARS = "(?:[^\\-/\"':!=A-Z0-9_@＠]+|^|\\:)";
    private static final String URL_VALID_DOMAIN
        = "(?:[^\\p{Punct}\\s][\\.-](?=[^\\p{Punct}\\s])|[^\\p{Punct}\\s]){1,}\\.[a-z]{2,}(?::[0-9]+)?";

    private static final String URL_VALID_GENERAL_PATH_CHARS
        = "[a-z0-9!\\*';:=\\+\\$/%#\\[\\]\\-_,~]";
    private static final String URL_VALID_PATH_CHARS_WITHOUT_SLASH = "["
        + URL_VALID_GENERAL_PATH_CHARS + "&&[^/]]";
    private static final String URL_VALID_PATH_CHARS_WITHOUT_COMMA = "["
        + URL_VALID_GENERAL_PATH_CHARS + "&&[^,]]";

    /**
     * Allow URL paths to contain balanced parens 1. Used in Wikipedia URLs like /Primer_(film) 2.
     * Used in IIS sessions like /S(dfd346)/
     */
    private static final String URL_BALANCE_PARENS = "(?:\\(" + URL_VALID_GENERAL_PATH_CHARS
        + "+\\))";
    private static final String URL_VALID_URL_PATH_CHARS = "(?:" +
        URL_BALANCE_PARENS +
        "|@" + URL_VALID_PATH_CHARS_WITHOUT_SLASH + "++/" +
        "|(?:[.,]*+" + URL_VALID_PATH_CHARS_WITHOUT_COMMA + ")++" +
        ")";

    /**
     * Valid end-of-path chracters (so /foo. does not gobble the period). 2. Allow =&# for empty URL
     * parameters and other URL-join artifacts
     */

    private static final String URL_VALID_URL_PATH_ENDING_CHARS = "(?:[a-z0-9=_#/\\-\\+]+|"
        + URL_BALANCE_PARENS + ")";
    private static final String URL_VALID_URL_QUERY_CHARS
        = "[a-z0-9!\\*'\\(\\);:&=\\+\\$/%#\\[\\]\\-_\\.,~]";
    private static final String URL_VALID_URL_QUERY_ENDING_CHARS = "[a-z0-9_&=#/]";
    private static final String VALID_URL_PATTERN_STRING =
        "(" +                                                            //  $1 total match
            "(" + URL_VALID_PRECEEDING_CHARS + ")" +
            //  $2 Preceeding chracter
            "(" +                                                          //  $3 URL
            "(https?://)" +                                              //  $4 Protocol
            "(" + URL_VALID_DOMAIN + ")" +
            //  $5 Domain(s) and optional port number
            "(/" +
            "(?:" +
            URL_VALID_URL_PATH_CHARS + "+|" +
            //     1+ path chars and a valid last char
            URL_VALID_URL_PATH_ENDING_CHARS +                        //     Just a # case
            ")?" +
            ")?" +                                                       //  $6 URL Path and anchor
            "(\\?" + URL_VALID_URL_QUERY_CHARS + "*" +                   //  $7 Query String
            URL_VALID_URL_QUERY_ENDING_CHARS + ")?" +
            ")" +
            ")";

    public static final Pattern VALID_URL = Pattern
        .compile(VALID_URL_PATTERN_STRING, Pattern.CASE_INSENSITIVE);
    public static final int VALID_URL_GROUP_ALL = 1;
    public static final int VALID_URL_GROUP_BEFORE = 2;
    public static final int VALID_URL_GROUP_URL = 3;
    public static final int VALID_URL_GROUP_PROTOCOL = 4;
    public static final int VALID_URL_GROUP_DOMAIN = 5;
    public static final int VALID_URL_GROUP_PATH = 6;
    public static final int VALID_URL_GROUP_QUERY_STRING = 7;

    private static final int EXTRACT_MENTIONS_GROUP_AFTER = 3;
    private static final int EXTRACT_MENTIONS_GROUP_USERNAME = 2;
    private static final int EXTRACT_REPLY_GROUP_USERNAME = 1;
    private static final int AUTO_LINK_HASHTAGS_GROUP_TAG = 3;
  }
}

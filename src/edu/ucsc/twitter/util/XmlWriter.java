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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for writing XML files. This class provides convenience methods for creating XML
 * documents, such as starting and ending tags, and adding content and comments. This class handles
 * correct XML formatting and will properly escape text to ensure that the text remains valid XML.
 *
 * <p>To use this class, create a new instance with the desired [Print]FileWriter to write the XML
 * to. Call the {@link #begin()} or {@link #begin(String, int)} method when ready to start
 * outputting XML. Then use the provided methods to generate the XML file. Finally, call either the
 * {@link #finish()} or {@link #finish(String)} methods to signal the completion of the file.</p>
 */

public class XmlWriter {
  private FileWriter out;
  private int bias = 0;
  private int tab;
  private List<String> tagStack = new ArrayList<String>();

  public XmlWriter(String filename) {
    this(makeWriter(filename));
  }

  private static FileWriter makeWriter(String filename) {
    FileWriter out = null;
    try { out = new FileWriter(new File(filename), true); } catch (IOException e) {
      e.printStackTrace();
    }
    return out;
  }

  /**
   * Create a new XMLWriter.
   *
   * @param out the  FileWriter to write the XML to
   */
  public XmlWriter(FileWriter out) {
    this(out, 2);
  }

  /**
   * Create a new XMLWriter.
   *
   * @param out the FileWriter to write the XML to
   * @param tabLength the number of spaces to use for each level of indentation in the XML file
   */
  public XmlWriter(FileWriter out, int tabLength) {
    this.out = out;
    tab = tabLength;
  }

  /**
   * Write <em>unescaped</em> text into the XML file. To write escaped text, use the {@link
   * #content(String)} method instead.
   *
   * @param s the text to write. This String will not be escaped.
   */
  public void write(String s) throws IOException {
    out.write(s);
  }

  /**
   * Write <em>unescaped</em> text into the XML file, followed by a newline. To write escaped
   * text, use the {@link #content(String)} method instead.
   *
   * @param s the text to write. This String will not be escaped.
   */
  public void writeln(String s) throws IOException {
    out.write(s);
    out.write("\n");
  }

  /**
   * Write a newline into the XML file.
   */
  public void writeln() throws IOException {
    out.write("\n");
  }

  /**
   * Begin the XML document. This must be called before any other formatting methods. This method
   * writes an XML header into the top of the output stream.
   */
  public void begin() throws IOException {
    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writeln();
  }

  /**
   * Begin the XML document. This must be called before any other formatting methods. This method
   * writes an XML header into the top of the output stream, plus additional header text provided
   * by the client
   *
   * @param header header text to insert into the document
   * @param bias the spacing bias to use for all subsequent indenting
   */
  public void begin(String header, int bias) throws IOException {
    begin();
    out.write(header);
    this.bias = bias;
  }

  /**
   * Write a comment in the XML document. The comment will be written according to the current
   * spacing and followed by a newline.
   *
   * @param comment the comment text
   */
  public void comment(String comment) throws IOException {
    spacing();
    out.write("<!-- ");
    out.write(comment);
    out.write(" -->");
    writeln();
  }

  /**
   * Internal method for writing a tag with attributes.
   *
   * @param tag the tag name
   * @param names the names of the attributes
   * @param values the values of the attributes
   * @param nattr the number of attributes
   * @param close true to close the tag, false to leave it open and adjust the spacing
   * @throws IOException unable to perform task for the stated reasons.
   */
  protected void tag(String tag, String[] names, String[] values,
      int nattr, boolean close) throws IOException {
    spacing();
    out.write('<');
    out.write(tag);
    for (int i = 0; i < nattr; ++i) {
      out.write(' ');
      out.write(names[i]);
      out.write('=');
      out.write('\"');
      escapeString(values[i]);
      out.write('\"');
    }
    if (close) { out.write('/'); }
    out.write('>');
    writeln();

    if (!close) {
      tagStack.add(tag);
    }
  }

  /**
   * Write a closed tag with attributes. The tag will be followed by a newline.
   *
   * @param tag the tag name
   * @param names the names of the attributes
   * @param values the values of the attributes
   * @param nattr the number of attributes
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void tag(String tag, String[] names, String[] values, int nattr) throws IOException {
    tag(tag, names, values, nattr, true);
  }

  /**
   * Write a start tag with attributes. The tag will be followed by a newline, and the indentation
   * level will be increased.
   *
   * @param tag the tag name
   * @param names the names of the attributes
   * @param values the values of the attributes
   * @param nattr the number of attributes
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void start(String tag, String[] names, String[] values, int nattr) throws IOException {
    tag(tag, names, values, nattr, false);
  }

  /**
   * Write a new attribut to an existing tag.  The attribute will be followed by a newline.
   *
   * @param name the name of the attribute
   * @param value the value of the attribute
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void addAttribute(String name, String value) throws IOException {
    spacing();
    out.write(name);
    out.write('=');
    out.write('\"');
    escapeString(value);
    out.write('\"');
    writeln();
  }

  /**
   * Internal method for writing a tag with a single attribute.
   *
   * @param tag the tag name
   * @param name the name of the attribute
   * @param value the value of the attribute
   * @param close true to close the tag, false to leave it open and adjust the spacing
   * @throws IOException unable to perform task for the stated reasons.
   */
  protected void tag(String tag, String name, String value, boolean close) throws IOException {
    spacing();
    out.write('<');
    out.write(tag);
    out.write(' ');
    out.write(name);
    out.write('=');
    out.write('\"');
    escapeString(value);
    out.write('\"');
    if (close) { out.write('/'); }
    out.write('>');
    writeln();

    if (!close) {
      tagStack.add(tag);
    }
  }

  /**
   * Write a closed tag with one attribute. The tag will be followed by a newline.
   *
   * @param tag the tag name
   * @param name the name of the attribute
   * @param value the value of the attribute
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void tag(String tag, String name, String value) throws IOException {
    tag(tag, name, value, true);
  }

  /**
   * Write a start tag with one attribute. The tag will be followed by a newline, and the
   * indentation level will be increased.
   *
   * @param tag the tag name
   * @param name the name of the attribute
   * @param value the value of the attribute
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void start(String tag, String name, String value) throws IOException {
    tag(tag, name, value, false);
  }

  /**
   * Internal method for writing a tag with attributes.
   *
   * @param tag the tag name
   * @param names the names of the attributes
   * @param values the values of the attributes
   * @param nattr the number of attributes
   * @param close true to close the tag, false to leave it open and adjust the spacing
   * @throws IOException unable to perform task for the stated reasons.
   */
  protected void tag(String tag, List<String> names, List<String> values,
      int nattr, boolean close) throws IOException {
    spacing();
    out.write('<');
    out.write(tag);
    for (int i = 0; i < nattr; ++i) {
      out.write(' ');
      out.write(names.get(i));
      out.write('=');
      out.write('\"');
      escapeString(values.get(i));
      out.write('\"');
    }
    if (close) { out.write('/'); }
    out.write('>');
    writeln();

    if (!close) {
      tagStack.add(tag);
    }
  }

  /**
   * Write a closed tag with attributes. The tag will be followed by a newline.
   *
   * @param tag the tag name
   * @param names the names of the attributes
   * @param values the values of the attributes
   * @param nattr the number of attributes
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void tag(String tag, List<String> names, List<String> values, int nattr)
      throws IOException {
    tag(tag, names, values, nattr, true);
  }

  /**
   * Write a start tag with attributes. The tag will be followed by a newline, and the indentation
   * level will be increased.
   *
   * @param tag the tag name
   * @param names the names of the attributes
   * @param values the values of the attributes
   * @param nattr the number of attributes
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void start(String tag, List<String> names, List<String> values, int nattr)
      throws IOException {
    tag(tag, names, values, nattr, false);
  }

  /**
   * Write a start tag without attributes. The tag will be followed by a newline, and the
   * indentation level will be increased.
   *
   * @param tag the tag name
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void start(String tag) throws IOException {
    tag(tag, (String[]) null, null, 0, false);
  }

  /**
   * Close the most recently opened tag. The tag will be followed by a newline, and the
   * indentation level will be decreased.
   *
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void end() throws IOException {
    String tag = tagStack.remove(tagStack.size() - 1);
    spacing();
    out.write('<');
    out.write('/');
    out.write(tag);
    out.write('>');
    writeln();
  }

  /**
   * Write a new content tag with a single attribute, consisting of an open tag, content text, and
   * a closing tag, all on one line.
   *
   * @param tag the tag name
   * @param name the name of the attribute
   * @param value the value of the attribute, this text will be escaped
   * @param content the text content, this text will be escaped
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void contentTag(String tag, String name, String value, String content) throws IOException {
    spacing();
    out.write('<');
    out.write(tag);
    out.write(' ');
    out.write(name);
    out.write('=');
    out.write('\"');
    escapeString(value);
    out.write('\"');
    out.write('>');
    escapeString(content);
    out.write('<');
    out.write('/');
    out.write(tag);
    out.write('>');
    writeln();
  }

  /**
   * Write a new content tag with no attributes, consisting of an open tag, content text, and a
   * closing tag, all on one line.
   *
   * @param tag the tag name
   * @param content the text content, this text will be escaped
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void contentTag(String tag, String content) throws IOException {
    spacing();
    out.write('<');
    out.write(tag);
    out.write('>');
    escapeString(content);
    out.write('<');
    out.write('/');
    out.write(tag);
    out.write('>');
    writeln();
  }

  /**
   * Write content text.
   *
   * @param content the content text, this text will be escaped
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void content(String content) throws IOException {
    escapeString(content);
  }

  /**
   * Finish the XML document.
   *
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void finish() throws IOException {
    bias = 0;
    out.flush();
  }

  /**
   * Finish the XML document, writing the given footer text at the end of the document.
   *
   * @param footer the footer text, this will not be escaped
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void finish(String footer) throws IOException {
    bias = 0;
    out.write(footer);
    out.flush();
  }

  /**
   * Write the current spacing (determined by the indentation level) into the document. This
   * method is used by many of the other formatting methods, and so should only need to be called
   * in the case of custom text writing outside the mechanisms provided by this class.
   *
   * @throws IOException unable to perform task for the stated reasons.
   */
  public void spacing() throws IOException {
    int len = bias + tagStack.size() * tab;
    for (int i = 0; i < len; ++i) { out.write(' '); }
  }

  // ------------------------------------------------------------------------
  // Escape Text

  // unicode ranges and valid/invalid characters
  private static final char LOWER_RANGE = 0x20;
  private static final char UPPER_RANGE = 0x7f;
  private static final char[] VALID_CHARS = {0x9, 0xA, 0xD};

  private static final char[] INVALID = {'<', '>', '"', '\'', '&'};
  private static final String[] VALID =
      {"&lt;", "&gt;", "&quot;", "&apos;", "&amp;"};

  /**
   * Escape a string such that it is safe to use in an XML document.
   *
   * @param str the string to escape
   * @throws IOException unable to perform task for the stated reasons.
   */
  protected void escapeString(String str) throws IOException {
    if (str == null) {
      out.write("null");
      return;
    }

    int len = str.length();
    for (int i = 0; i < len; ++i) {
      char c = str.charAt(i);

      if ((c < LOWER_RANGE && c != VALID_CHARS[0] &&
          c != VALID_CHARS[1] && c != VALID_CHARS[2])
          || (c > UPPER_RANGE)) {
        // character out of range, escape with character value
        out.write("&#");
        out.write(Integer.toString(c));
        out.write(';');
      } else {
        boolean valid = true;
        // check for invalid characters (e.g., "<", "&", etc)
        for (int j = INVALID.length - 1; j >= 0; --j) {
          if (INVALID[j] == c) {
            valid = false;
            out.write(VALID[j]);
            break;
          }
        }
        // if character is valid, don't escape
        if (valid) {
          out.write(c);
        }
      }
    }
  }
}

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

import edu.ucsc.cli.util.OutputWriter;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class YmlWriter {
  private final OutputWriter writer;

  public YmlWriter(String filename){
    writer = new OutputWriter(filename);
  }

  public void begin() {
    writer.println("---");
  }

  public void writeEntry(String date, String userID, String userName, String firstName, String hint,
      String status) {
    writer.println(String.format("- :date: %s",       date));
    writer.println(String.format("  :userid: %s",     userID));
    writer.println(String.format("  :username: %s",   userName));
    writer.println(String.format("  :firstname: %s",  firstName));
    writer.println(String.format("  :hint: %s",       hint));
    writer.println(String.format("  :status: %s",     status));
  }

  public void end(){
    writer.println();
  }

  public void finish(){
    writer.close();
  }
}

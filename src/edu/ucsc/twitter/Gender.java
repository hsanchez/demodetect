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

import edu.ucsc.cli.util.Strings;
import java.util.NoSuchElementException;

/**
* A Person's gender.
*
* @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
*/
public enum Gender {
  MALE,
  FEMALE;

  public static boolean isFemale(Gender other){
    return FEMALE.isSame(other);
  }

  public static boolean isMale(Gender other){
    return !isFemale(other);
  }

  public boolean isSame(Gender other){
    return this == other;
  }

  public static Gender from(String name){
    for(Gender each : values()){
      if(Strings.same(name, each.toString().toLowerCase())){
        return each;
      }
    }

    throw new NoSuchElementException();
  }
}

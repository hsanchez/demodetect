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
import com.google.common.collect.Maps;
import edu.ucsc.cli.IterableFileReader;
import static edu.ucsc.cli.IterableFileReader.open;
import edu.ucsc.cli.util.Console;
import edu.ucsc.cli.util.StopWatch;
import edu.ucsc.twitter.util.TwitterEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A base class for all the demographics classifiers that will be
 * implemented in this project.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class DemographicsClassifier <R> {
  private final Console console;
  protected DemographicsClassifier(){
    console = Console.streaming();
  }

  /**
   * classifies a given text.
   * @param text
   *    text to be classified by classifier.
   * @return a hint indicating the class of the text.
   */
  public abstract R classify(String text);
  
  
  protected void error(String message){
    console.error(message);
  }
  
  protected void error(String message, Throwable cause){
    console.error(message, cause);
  }

  /**
   * evaluates the performance of {@code this} classifier.
   */
  public void evaluate(){}


  private static Map<String, File> getDatasetFiles(String... genders){
    final Map<String, File> files = Maps.newHashMap();
    final String basepath = TwitterEnvironment.getInstance().getOutputFoldername();
    for (String each : genders) {
      final String nonNullCategory = Preconditions.checkNotNull(each);
      final File file = new File(
          String.format(basepath + "/dist.%s.first", nonNullCategory));
      if (!file.exists()) {
        Console.streaming().error("unable to find file for " + nonNullCategory);
        throw new RuntimeException();
      }

      files.put(each, file);
    }

    return files;
  }

  protected static Map<String, IterableFileReader> getIterableFileReaders(String... categories)
      throws IOException {
    final Map<String, IterableFileReader> result = Maps.newHashMap();
    final Map<String, File>               files  = getDatasetFiles(categories);
    final StopWatch startTiming = new StopWatch();
    for (String each : categories) {
      final String nonNullCategory = Preconditions.checkNotNull(each);
      result.put(nonNullCategory, open(files.get(nonNullCategory)));
    }

    startTiming.resetAndLog("loading data");

    return result;
  }
  
  protected void info(String message){
    console.info(message);
  }

  /**
   * trains the classifier with representative examples before start classifying unknow text
   * instances.
   */
  public void train(){}  


}

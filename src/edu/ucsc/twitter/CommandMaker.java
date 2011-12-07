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
import edu.ucsc.broadcast.BasicEventService;
import edu.ucsc.broadcast.EventService;
import edu.ucsc.broadcast.SyncDeliveryStrategy;
import edu.ucsc.cli.Definition;
import edu.ucsc.cli.Executor;
import edu.ucsc.cli.ParsingResult;
import edu.ucsc.cli.util.Strings;
import edu.ucsc.twitter.util.TwitterEnvironment;
import java.util.concurrent.ExecutionException;

/**
 * Makes all the necessary commands that will allow us to deal with Twitter's
 * data collection.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
class CommandMaker {
  private final Definition definition;

  CommandMaker(Definition definition){
    this.definition = definition;
  }

  public void makeAllTwitterCommands(){
    definition.define("choose keyword samples of size <val:integer>")
        .documentedWith("sets the sample size of keywords that will be randomly selected.")
        .forExecutor(new ChangeRandomSampleSizeExecutor());
    definition.define("change <foldername:string> folder to <path:string>")
        .documentedWith("Changes the location of a given folder.")
        .forExecutor(new ChangeOutputFolderNameExecutor());
    definition.define("where is <foldername:string> folder?")
        .documentedWith("Prints out the location of a given folder.")
        .forExecutor(new PrintDestinationFolderExecutor());
    definition.define("search for hostile tweets <string> ...").documentedWith(
        "search Twitter's public timeline for matching keywords.")
        .forExecutor(new SearchForHostileTweetsExecutor());
    definition.define("search for tweets with demographics <string> ...").documentedWith(
        "search Twitter's public timeline for tweets disclosing demographics matching keywords.")
        .forExecutor(new SearchForTweetsContainingDemographics());
  }

  private static class ChangeRandomSampleSizeExecutor implements Executor {

    @Override public void execute(ParsingResult parsingResult) throws ExecutionException {
      final int val = (Integer)parsingResult.getParameterValue(0);
      if(val < 1) throw new IllegalStateException("invalid size for the random sample");
      TwitterEnvironment.getInstance().changeRandomSampleArity(val);
    }
  }

  private static class ChangeOutputFolderNameExecutor implements Executor {

    @Override public void execute(ParsingResult parsingResult) throws ExecutionException {
      final String fullyQualifiedNameOfOutputFolder = (String) parsingResult.getParameterValue(0);
      final String folderNewLocation = (String) parsingResult.getParameterValue(1);
      Preconditions
          .checkArgument(!Strings.isEmpty(folderNewLocation), "Invalid path");
      switch (FolderKind.from(fullyQualifiedNameOfOutputFolder)) {
        case OUTPUT:
          TwitterEnvironment.getInstance().changeOutputFolder(folderNewLocation);
          break;
        case KEYWORDS:
        default:
          System.out.println("Unknown folder destination!");
      }
    }
  }

  private static class PrintDestinationFolderExecutor implements Executor {
    @Override public void execute(ParsingResult parsingResult) throws ExecutionException {
      final String folder = (String) parsingResult.getParameterValue(0);
      Preconditions
          .checkArgument(!Strings.isEmpty(folder), "Invalid path");

      switch (FolderKind.from(folder)){
        case OUTPUT:
          System.out.println("Current Output folder is:\t" + TwitterEnvironment.getInstance().getOutputFoldername());
          break;
        case KEYWORDS:
          final int lastSlash              = TwitterEnvironment.getInstance().getKeywordsFile().lastIndexOf("/");
          final String keywordFileLocation = TwitterEnvironment.getInstance().getKeywordsFile();
          System.out.println("Keywords file is located at:\t" + keywordFileLocation.substring(0,
              lastSlash));
          break;
        default: System.out.println("Unknown folder destination!");
      }
    }
  }

  private static enum FolderKind {
    OUTPUT("output"),
    KEYWORDS("keywords"),
    UNKNOWN("unknown");

    private final String name;

    FolderKind(String name) {
      this.name = name;
    }

    static FolderKind from(String name) {
      for (FolderKind each : values()) {
        if (Strings.same(each.name, name)) { return each; }
      }

      return UNKNOWN;
    }
  }

  private static class SearchForTweetsContainingDemographics implements Executor {
    @Override public void execute(ParsingResult parsingResult) throws ExecutionException {
      final DemographicsResultsWriter writer  = new DemographicsResultsWriter();
      final EventService  service = new BasicEventService(new SyncDeliveryStrategy());
      final PeriodicTask  task    = new TweetsWithDemogSearchingPeriodicTask(service);
      task.subscribe("results", writer);
      final Watchdog dog = new TweetsCollectionWatchdog(task);
      dog.startWatching();
      while (true) {
        try {
          Thread.sleep(5000);
          if (dog.hasStoppedWatching()) {
            dog.stopWatching();
            break;
          }
        } catch (Throwable e) {
          System.err.println("Gathering tweets service is having a problem.");
          e.printStackTrace();
          break;
        }
      }
    }
  }

  private static class SearchForHostileTweetsExecutor implements Executor {
    @Override public void execute(ParsingResult parsingResult) throws ExecutionException {
      final BullyingResultsWriter writer  = new BullyingResultsWriter();
      final EventService  service = new BasicEventService(new SyncDeliveryStrategy());
      final PeriodicTask  task    = new HostileTweetsSearchingPeriodicTask(service);
      task.subscribe("results", writer);
      final Watchdog dog = new TweetsCollectionWatchdog(task);
      dog.startWatching();
      while (true) {
        try {
          Thread.sleep(5000);
          if (dog.hasStoppedWatching()) {
            dog.stopWatching();
            break;
          }
        } catch (Throwable e) {
          System.err.println("Gathering tweets service is having a problem.");
          e.printStackTrace();
          break;
        }
      }
    }
  }

}

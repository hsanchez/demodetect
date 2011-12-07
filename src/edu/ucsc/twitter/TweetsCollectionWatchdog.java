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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class TweetsCollectionWatchdog implements Watchdog {
  private static final long RETRY_THRESHOLD = 1000 * 60 * 12; // 12 minutes

  private PeriodicTask task;
  private final ExecutionConfig config;

  private final ScheduledExecutorService executor;
  private final AtomicBoolean isDone;
  private final AtomicLong nextTryTime = new AtomicLong();

  public TweetsCollectionWatchdog(PeriodicTask task, ExecutionConfig config) {
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.task = task;
    this.config = config;
    this.isDone = new AtomicBoolean(false);
  }

  public TweetsCollectionWatchdog(PeriodicTask task){
    this(task, new ExecutionConfig(0, 500, TimeUnit.MILLISECONDS));
  }

  private void retryTask() {
    task = retry();
    task.start();
  }

  @Override public ExecutionConfig getConfiguration() {
    return config;
  }

  @Override public PeriodicTask getTask() {
    return task;
  }

  @Override public boolean hasStoppedWatching() {
    return isDone.get();
  }

  @Override public PeriodicTask retry() {
    return getTask().getInstance();
  }

  @Override public void stopWatching() {
    if (hasStoppedWatching()) { return; }
    executor.shutdown();
    confirmStateChange();
  }

  private void confirmStateChange() {
    isDone.set(!isDone.get());
  }

  @Override public void startWatching() {
    nextTryTime.set(System.nanoTime() + RETRY_THRESHOLD);
    getTask().start();
    final long delay = getConfiguration().getDelay();
    final long period = getConfiguration().getPeriod();
    final TimeUnit unit = getConfiguration().getTimeunit();
    executor.scheduleAtFixedRate(new Watcher(), delay, period, unit);
  }

  private synchronized boolean areWeDoneYet() {
    final long currentNextTryTime = nextTryTime.get();
    final long currentTime = System.nanoTime();
    if (currentTime < currentNextTryTime) { return false; }
    stopWatching();
    return true;
  }

  @Override public String toString() {
    final long delay = getConfiguration().getDelay();
    final long period = getConfiguration().getPeriod();
    return String.format("Watchdog[watching=%s, delay=%d, period=%d]",
        hasStoppedWatching() ? "No" : "Yes", delay, period);
  }

  private class Watcher implements Runnable {
    @Override public void run() {
      while (true) {
        try {
          if (task.isDone()) {
            task.shutdownNow();

            if (areWeDoneYet()) {
              System.out.println("Task stopped - quitting watching duties...");
              break;
            }

            System.out.println("Task stopped - restarting...");
            // get new instance, restart it
            retryTask();
          }
        } catch (Exception e) {
          System.err.println("error: Watchdog is having trouble with its execution.");
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}

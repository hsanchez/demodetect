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

/**
 * A watchdog is an object assigned to a periodic task. It monitors
 * this task progress. So, for example, if the task fails, then the watchdog will re-initiate
 * this task with the last seen state of this task.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Watchdog {
  /**
   * @return a configuration that tells the watchdog how ofter and for how long
   *    a task should be monitored.
   */
  ExecutionConfig getConfiguration();

  /**
   * @return the watched/monitored task.
   */
  PeriodicTask getTask();

  /**
   * @return {@code true} if the watchdog has finished watching the task. {@code false} otherwise.
   */
  boolean hasStoppedWatching();

  /**
   * retry a periodic task which has been stopped.
   * @return the task to be retried.
   */
  PeriodicTask retry();

  /**
   * stops watching a task.
   */
  void stopWatching();

  /**
   * starts watching a task.
   */
  void startWatching();
}

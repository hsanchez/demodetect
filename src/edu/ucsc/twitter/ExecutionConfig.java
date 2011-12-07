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

import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ExecutionConfig {
  private final long     delay;
  private final long     period;
  private final TimeUnit timeunit;

  public ExecutionConfig(long delay, long period, TimeUnit timeunit){
    this.delay    = delay;
    this.period   = period;
    this.timeunit = timeunit;
  }

  public long getDelay() {
    return delay;
  }

  public long getPeriod() {
    return period;
  }

  public TimeUnit getTimeunit() {
    return timeunit;
  }
}

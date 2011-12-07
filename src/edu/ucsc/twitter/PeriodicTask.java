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
import com.google.common.collect.Sets;
import edu.ucsc.broadcast.EventService;
import edu.ucsc.broadcast.EventSubscriber;
import static edu.ucsc.broadcast.MatcherMaker.exactChannel;
import static edu.ucsc.broadcast.MatcherMaker.exactType;
import edu.ucsc.cli.util.Strings;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A task that gets called periodically.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class PeriodicTask {
  private final ExecutionConfig executionConfig;
  private final EventService  eventBus;
  private final Set<String>   channels;

  private final ScheduledExecutorService executor;
  private ScheduledFuture future;

  protected PeriodicTask(ExecutionConfig executionConfig, EventService eventBus) {
    this.executionConfig = executionConfig;
    this.eventBus = eventBus;
    this.executor = Executors.newSingleThreadScheduledExecutor();
    this.channels = Sets.newHashSet();
  }

  protected PeriodicTask(EventService eventBus) {
    this(new ExecutionConfig(0L, 3000L, TimeUnit.MILLISECONDS),
        eventBus);
  }

  public boolean cancel() {
    return isCancelled() || future != null && future.cancel(true);
  }

  public EventService getEventBus() {
    return eventBus;
  }

  public abstract PeriodicTask getInstance();

  public abstract Runnable getRunnable();

  public ExecutionConfig getExecutionConfig() {
    return executionConfig;
  }

  public boolean isDone() {
    return future != null && future.isDone();
  }

  public boolean isCancelled() {
    return future != null && future.isCancelled();
  }

  public void publish(ResultPackage event) {
    final ResultPackage nonNullEvent = Preconditions.checkNotNull(event);
    for (String channel : channels) {
      getEventBus().publish(channel, nonNullEvent);
    }
  }

  public void shutdown() {
    executor.shutdown();
    channels.clear();
  }

  public void shutdownNow(){
    cancel();
    shutdown();
  }

  public void start() {
    final long delay = getExecutionConfig().getDelay();
    final long period = getExecutionConfig().getPeriod();
    final TimeUnit unit = getExecutionConfig().getTimeunit();
    future = executor.scheduleAtFixedRate(getRunnable(), delay, period,
        unit);
  }

  public void subscribe(String channel,
      EventSubscriber<? extends ResultPackage>... listeners) {
    if (Arrays.asList(listeners).contains(null) || null == listeners) {
      throw new IllegalArgumentException();
    }
    Preconditions.checkArgument(!Strings.isEmpty(channel));
    channels.add(channel);
    for (EventSubscriber<?> each : listeners) {
      getEventBus().subscribe(exactChannel(channel), exactType(ResultPackage.class), each);
    }
  }
}

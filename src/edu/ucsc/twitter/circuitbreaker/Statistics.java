package edu.ucsc.twitter.circuitbreaker;

/**
 * It holds basic statistics of the circuit breaker's execution.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Statistics {
  /**
   * @return
   *    the avg time in which the circuit breaker resets itself.
   */
  long getAvgRecoveryTime();
}

//package org.emis.tayvs.jmh
//
//import scala.collection.breakOut
//
//import org.openjdk.jmh.annotations.Benchmark
//import org.openjdk.jmh.annotations.BenchmarkMode
//import org.openjdk.jmh.annotations.Mode
//import org.openjdk.jmh.annotations.OutputTimeUnit
//
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@BenchmarkMode(Array(Mode.Throughput))
//class BreakOutJMH {
//
//  def fromRangeMapInc2Set = (1 to 10).map(_ + 1).toSet
//  def fromRangeMapInc2SetBreakOut:Set[Int] = (1 to 10).map(_ + 1)(breakOut)
//
//}

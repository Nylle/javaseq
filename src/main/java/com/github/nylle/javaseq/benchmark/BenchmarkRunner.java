package com.github.nylle.javaseq.benchmark;

import com.github.nylle.javaseq.ISeq;
import com.github.nylle.javaseq.prototype.Seq;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BenchmarkRunner {
    // https://www.baeldung.com/java-microbenchmark-harness

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void iseq(Blackhole blackhole) {
        var result = ISeq.iterate(0, x -> x + 1)
                .filter(x -> x % 2 != 0)
                .map(x -> x.toString())
                .mapcat(x -> ISeq.sequence(x.split("")))
                .take(1000)
                .toList()
                .size();

        blackhole.consume(result);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void seq(Blackhole blackhole) {
        var result = Seq.iterate(0, x -> x + 1)
                .filter(x -> x % 2 != 0)
                .map(x -> x.toString())
                .mapcat(x -> Seq.sequence(x.split("")))
                .take(1000)
                .toList()
                .size();

        blackhole.consume(result);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    public void linkedList(Blackhole blackhole) {
        var infiniteIterator = Stream.iterate(0, x -> x + 1).iterator();

        var ints = new LinkedList<Integer>();
        while(ints.size() < 1000) {
            var x = infiniteIterator.next();
            if(x % 2 != 0) {
                ints.add(x);
            }
        }

        var strings = new LinkedList<String>();
        var n = 0;
        while(strings.size() <= 1000) {
            strings.addAll(Arrays.asList(ints.get(n).toString().split("")));
            n++;
        }

        var result = strings.size();

        blackhole.consume(result);
    }
}

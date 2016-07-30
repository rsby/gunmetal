package io.gunmetal.benchmarks;

import io.gunmetal.Components;
import io.gunmetal.ScopeDecorator;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author rees.byars
 */
@State(Scope.Benchmark)
public class Benchmark {

    Supplier<BumpkinComponent> componentSupplier;

    @Setup
    public void setUp() {
        componentSupplier = Components.newSupplier(BumpkinComponent.class, new ScopeDecorator());
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }

    @org.openjdk.jmh.annotations.Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(1)
    public void benchmarkSingle() {
        Components.newInstance(BumpkinComponent.class, new ScopeDecorator()).aa();
    }

    @org.openjdk.jmh.annotations.Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Fork(1)
    public void benchmarkSupplier() {
        componentSupplier.get().aa();
    }
}

package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Random;

public class XORShiftRandom extends Random {
    private static final long serialVersionUID = 1L;
    private long seed;
    private long state;

    public XORShiftRandom() {
        this(System.nanoTime());
    }

    public XORShiftRandom(long seed) {
        setSeed(seed);
    }

    private long XORShiftGen() {
        state ^= (state << 21);
        state ^= (state >>> 35);
        state ^= (state << 4);
        return state;
    }

    @Override
    protected int next(int bits) {
        return (int) (nextLong() & (1L << bits) - 1);
    }

    @Override
    public long nextLong() {
        return XORShiftGen();
    }

    public int nextIntRange(int min, int max) {
        if (max == Integer.MAX_VALUE) {
            max = max - 1;
        }

        return this.nextInt((max - min) + 1) + min;
    }

    public double nextDoubleRange(double min, double max) {
        if (max == Double.MAX_VALUE) {
            max = max - 1;
        }

        return this.nextDouble() * (max - min) + min;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        state = seed;
    }

    public long getSeed() {
        return this.seed;
    }
}

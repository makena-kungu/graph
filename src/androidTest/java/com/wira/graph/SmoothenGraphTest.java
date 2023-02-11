package com.wira.graph;

import androidx.annotation.NonNull;

import com.google.common.truth.Truth;
import com.wira.graph.GraphView.Plot;
import com.wira.graph.GraphView.Plot.SmoothingMode;

import org.junit.Test;

import java.util.Random;
import java.util.TreeSet;

public class SmoothenGraphTest {
    @Test
    public void testing_smoothing_graph() {
        TreeSet<GraphView.Coordinate> cs = genCs();
        Plot.smoothenGraph(cs, 5, SmoothingMode.MODE_MEDIAN);
        Truth.assertThat(cs.size()).isGreaterThan(0);
    }

    @NonNull
    private TreeSet<GraphView.Coordinate> genCs() {
        Random random = new Random();
        TreeSet<GraphView.Coordinate> cs = new TreeSet<>();
        int i = 0;
        while (i < 31) {
            cs.add(new GraphView.Coordinate(i++, ((float) random.nextGaussian()) * 1_000));
        }
        return cs;
    }
}
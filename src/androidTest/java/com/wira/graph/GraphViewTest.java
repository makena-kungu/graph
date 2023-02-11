package com.wira.graph;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;
import com.wira.graph.GraphView;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class GraphViewTest {

    private final GraphView.Coordinate test = new GraphView.Coordinate(0, 10);

    @Test
    public void findCoordinate() {
        List<GraphView.Coordinate> coordinates = gen();
        GraphView.Coordinate coordinate = GraphView.findCoordinate(coordinates, 9.7f);
        Truth.assertThat(coordinate).isEquivalentAccordingToCompareTo(test);
    }


    @NonNull
    private List<GraphView.Coordinate> gen() {
        TreeSet<GraphView.Coordinate> coordinates = new TreeSet<>();
        Random random = new Random();
        for (int i = 0; i < 3_000; i++) {
            coordinates.add(new GraphView.Coordinate(random.nextFloat(), random.nextFloat()));
        }
        return new ArrayList<>(coordinates);
    }
}
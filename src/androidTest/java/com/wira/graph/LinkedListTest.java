package com.wira.graph;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;
import com.google.common.truth.TruthJUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.TreeSet;

@RunWith(AndroidJUnit4.class)
public class LinkedListTest {

    @Test
    public void testWhetherItemsInALinkedListAreSortedOnInsertion() {
        TreeSet<GraphView.Coordinate> coordinates = new TreeSet<>();
        coordinates.add(new GraphView.Coordinate(4, 2));
        coordinates.add(new GraphView.Coordinate(3, 2));
        coordinates.add(new GraphView.Coordinate(2, 2));
        coordinates.add(new GraphView.Coordinate(8, 2));
        GraphView.Coordinate coordinate = coordinates.stream().findFirst().orElse(null);

        TruthJUnit.assume().that(coordinates).contains(coordinate);
        Truth.assertThat(coordinate).isNotNull();
        Truth.assertThat(coordinate.x).isEqualTo(2f);
    }

    @Test
    public void testWhetherTheSetDoesNotContainDuplicateCoordinates() {
        // duplication coordinates are those that do not have the same x value
        TreeSet<GraphView.Coordinate> coordinates = new TreeSet<>();
        GraphView.Coordinate c1 = new GraphView.Coordinate(2, 5);
        GraphView.Coordinate c2 = new GraphView.Coordinate(2, 4);

        coordinates.add(c1);
        coordinates.add(c2);
        Truth.assertThat(coordinates).contains(c1);
        Truth.assertThat(coordinates).doesNotContain(c2);
    }
}

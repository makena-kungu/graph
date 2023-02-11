package com.wira.graph;

import android.os.Parcel;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.truth.Truth;
import com.wira.graph.GraphView;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GenerateTest {

    @Test
    public void testWhetherTheCustomComparatorWorks() {
        GraphView.Coordinate coordinate = new GraphView.Coordinate(10, 6);
        GraphView.Coordinate coordinate1 = new GraphView.Coordinate(10, 5);
        Truth.assertThat(coordinate).isEquivalentAccordingToCompareTo(coordinate1);
    }

    @Test
    public void testWhetherCustomCoordinateHashCodeIsSameForEqualElements() {
        GraphView.Coordinate coordinate = new GraphView.Coordinate(10, 6);
        GraphView.Coordinate coordinate1 = new GraphView.Coordinate(10, 5);
        Truth.assertThat(coordinate.hashCode()).isEqualTo(coordinate1.hashCode());
    }

    @Test
    public void testWhetherParcelabilityWorks() {
        GraphView.Coordinate test = new GraphView.Coordinate(2, 3);
        Parcel p = Parcel.obtain();

        test.writeToParcel(p, 0);
        p.setDataPosition(0);
        GraphView.Coordinate createdfromparcel = GraphView.Coordinate.CREATOR.createFromParcel(p);
        Truth.assertThat(createdfromparcel.x).isEqualTo(2f);
        Truth.assertThat(createdfromparcel.y).isEqualTo(3f);
    }

}
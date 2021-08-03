package com.nemido.lib;

import android.text.SpannableString;
import android.text.style.BulletSpan;

import com.nemido.lib.utils.Period;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.nemido.lib.graph.GraphView.label;
import static java.lang.Math.ceil;
import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void suffix_works() {
        float val = 50;

        final String label = label(val);
        System.out.println(label);
        System.out.println(Float.parseFloat(label.replace("t", "")
                .replace("b", "")
                .replace("m", "")
                .replace("k", ""))%3);

        List<String> labels = new ArrayList<>();
        final float maximum = maxy(91);
        final String f = "0";
        labels.add(f);
        final float max = Float.parseFloat(label(maximum)
                .replace("t", "")
                .replace("b", "")
                .replace("m", "")
                .replace("k", ""));

        final int count = max%3 == 0 ? 3 : 2;

        System.out.println("==================Looping==================");
        float i = 1;
        while (i <= count) {
            System.out.println("i = " + i + " count = " + count);
            System.out.println(i/count);
            final float v = (i++/count)*maximum;
            labels.add(label(v));
        }

        System.out.println("\n====================Printing labels====================");
        for (String s : labels) {
            System.out.println(s);
        }
    }

    @Test
    public void test_maxDouble() {
        float f = 103.43f;
        System.out.println(Math.floor(0.99999999999));
        System.out.println(Math.ceil(.000000000000001));
        System.out.println(Float.floatToIntBits(f));
    }

    @Test
    public void test_maxY() {
        System.out.println(maxy(154));
    }

    public float maxy(float max) {
        float maxy;

        //Ceil the float into an integer so as to get the whole numbers size which
        //is achieved by converting the result int into a string, and consequently
        //obtaining the ten to power length
        final int length = String.valueOf((int) Math.ceil(max)).length();
        //The multiplicand is the value the max will be rounded to
        long multiplicand = (long) pow(10, length);

        //The max should also be a value that can lets say greater than the initial max but
        //if the difference is greater than a value for instance with the value 1102.0, the max
        //currently will be 2000 but 1200 would be an ideal actually a better value.
        //For a value 1509, 1600 would be the ideal maximum too.

        //The graph doesn't occupy 80% of the graph area
        do {
            multiplicand /= 10;
            maxy = (float) (ceil(max/multiplicand)*multiplicand);
        } while (max/maxy < 0.8);

        return maxy;
    }

    @Test
    public void testDate(){
        Period p = Period.ofQuarter();
        System.out.println(p.getDuration());

        Period p1 = Period.ofSemi();
        System.out.println(p1.getDuration());
    }

    @Test
    public void testBullet() {
        String s = "";
        final String some = "Some Text\n";
        int length = some.length();
        for (int i = 0; i < 10; i++) {
            s = s.concat(some);
        }

        SpannableString ss = new SpannableString(some);
        int j = 0;
        do {
            ss.setSpan(new BulletSpan(), j, j+length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
            j += length;
        }while(j<length);
        System.out.println(ss);
    }
}
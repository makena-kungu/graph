package com.wira.graph;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.BulletSpan;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.nemido.graphs.test", appContext.getPackageName());
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
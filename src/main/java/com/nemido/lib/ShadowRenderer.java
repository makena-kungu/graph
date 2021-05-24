package com.nemido.lib;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.core.graphics.ColorUtils;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;
import static android.graphics.Color.WHITE;

public class ShadowRenderer {
    private final RectF l1 = new RectF();
    private final RectF l2 = new RectF();
    private final RectF w1 = new RectF();
    private final RectF w2 = new RectF();

    private final RectF c1 = new RectF();
    private final RectF c2 = new RectF();
    private final RectF c3 = new RectF();
    private final RectF c4 = new RectF();

    private final Paint p = new Paint();
    private final Paint p1 = new Paint();
    private final Paint p2 = new Paint();

    public ShadowRenderer() {
        this(WHITE);
    }

    public ShadowRenderer(int color) {
        p.setAntiAlias(true);
        p.setColor(color);
        p1.set(p);
        p2.set(p);
    }

    public void drawRoundRectWithShadow(Canvas c, RectF o, final float radius, final float elevation) {
        drawRoundRectWithShadow(c, o, radius, elevation, BLACK);
    }

    public void drawRoundRectWithShadow(Canvas c, RectF o, final float radius, final float elevation, int shadowColor) {
        final int init = c.save();
        final float oRadius = elevation + radius;

        final int[] colors = new int[]{TRANSPARENT, ColorUtils.setAlphaComponent(shadowColor, 68), ColorUtils.setAlphaComponent(shadowColor, 20), ColorUtils.setAlphaComponent(shadowColor, 0)};
        float ratio = radius/oRadius;
        float elev = elevation/oRadius;
        final float[] pos = new float[]{ratio, ratio + (0f*elev), ratio + (.5f*elev), ratio + (1*elev)};
        final Shader.TileMode mode = Shader.TileMode.CLAMP;

        l1.set(o);
        l1.inset(radius, 0);
        l1.bottom = l1.top + oRadius;
        l1.offset(0, -elevation);
        l2.set(l1);
        l2.offsetTo(l2.left, o.bottom - radius);

        w1.set(o);
        w1.inset(0, radius);
        w1.right = w1.left + oRadius;
        w1.offset(-elevation, 0);
        w2.set(w1);
        w2.offsetTo(o.right - radius, w2.top);

        c1.set(w2.left, l2.top, w2.right, l2.bottom);
        final float w = c1.width()/2, h = c1.height()/2;
        c1.inset(-w, -h);
        c1.offset(-w, -h);
        c2.set(c1);
        c2.offsetTo(w1.left, c2.top);
        c3.set(c1);
        c3.offsetTo(w1.left, l1.top);
        c4.set(c1);
        c4.offsetTo(c4.left, l1.top);

        c.save();
        c.translate(l1.left, l1.top);
        l1.offsetTo(0, 0);
        p1.setShader(new LinearGradient(0, l1.bottom, 0, l1.top, colors, pos, mode));
        c.drawRect(l1, p1);
        c.restore();

        c.save();
        c.translate(l2.left, l2.top);
        l2.offsetTo(0, 0);
        p1.setShader(new LinearGradient(0, l2.top, 0, l2.bottom, colors, pos, mode));
        c.drawRect(l2, p1);
        c.restore();

        c.save();
        c.translate(w1.left, w1.top);
        w1.offsetTo(0, 0);
        p1.setShader(new LinearGradient(w1.right, 0, w1.left, 0, colors, pos, mode));
        c.drawRect(w1, p1);
        c.restore();

        c.save();
        c.translate(w2.left, w2.top);
        w2.offsetTo(0, 0);
        p1.setShader(new LinearGradient(w2.left, 0, w2.right, 0, colors, pos, mode));
        c.drawRect(w2, p1);
        c.restore();

        p2.setColor(shadowColor);
        p2.setShader(new RadialGradient(0, 0, oRadius, colors, pos, mode));

        c.save();
        c.translate(c1.centerX(), c1.centerY());
        c1.offsetTo(0, 0);
        c1.offset(-(.5f*c1.width()), -(.5f*c1.height()));
        c.drawArc(c1, 0, 90, true, p2);
        c.restore();

        c.save();
        c.translate(c2.centerX(), c2.centerY());
        c2.offsetTo(0, 0);
        c2.offset(-(.5f*c2.width()), -(.5f*c2.height()));
        c.drawArc(c2, 90, 90, true, p2);
        c.restore();

        c.save();
        c.translate(c3.centerX(), c3.centerY());
        c3.offsetTo(0, 0);
        c3.offset(-(.5f*c3.width()), -(.5f*c3.height()));
        c.drawArc(c3, 180, 90, true, p2);
        c.restore();

        c.save();
        c.translate(c4.centerX(), c4.centerY());
        c4.offsetTo(0, 0);
        c4.offset(-(.5f*c4.width()), -(.5f*c4.height()));
        c.drawArc(c4, 270, 90, true, p2);
        c.restore();

        p.setColor(WHITE);
        c.drawRoundRect(o, radius, radius, p);

        c.restoreToCount(init);
    }
}

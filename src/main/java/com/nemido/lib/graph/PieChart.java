package com.nemido.lib.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.nemido.lib.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.BLUE;
import static android.graphics.Color.CYAN;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.MAGENTA;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;
import static android.graphics.Color.parseColor;
import static java.lang.String.format;
import static java.util.Currency.getInstance;
import static java.util.Locale.getDefault;

public class PieChart extends View {
    private static final String TAG = "PieChart";
    public static final String DOUBLES_FORMAT = "%,.1f";
    private static final String CASH_FORMAT = "%s%,.2f";
    public static final int[] colors = new int[]{GREEN, BLUE, RED, CYAN, MAGENTA,
            parseColor("#FFC000"),
            parseColor("#2196F3"),
            YELLOW,
            parseColor("#FFC0CB"),//This is pink
            parseColor("#00C853")
    };

    private static final float SPACE = 8.0f;
    /**
     * The ratio of the height of pieBounds width to it's height
     */
    private static final float sRatio = 0.6f;

    private final float strokeWidth;
    private final float space;
    private final float keyBoundsHeight = 174;
    private final float largeTextSize;
    final float heightOfDivider = 1f;
    private float padding;

    private final AtomicReference<String> total = new AtomicReference<>("");

    private final Paint piePaint, keyPaint, textPaint, dividerPaint;
    private final RectF pieRect = new RectF(),
            pieBounds = new RectF(),
            miscellaneous = new RectF(),
            keyBounds = new RectF(),
            key = new RectF();
    private final Rect rect = new Rect();
    private final HashMap<String, Pie> data = new HashMap<>();

    public PieChart(Context context) {
        this(context, null);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        strokeWidth = 25.0f;
        padding = 10.0f;
        space = 5.0f;

        largeTextSize = getResources().getDimension(R.dimen.large_text_size);
        float mediumTextSize = getResources().getDimension(R.dimen.medium_text_size);

        piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        piePaint.setStyle(Paint.Style.STROKE);
        piePaint.setStrokeWidth(strokeWidth);
        piePaint.setStrokeJoin(Paint.Join.ROUND);
        piePaint.setStrokeCap(Paint.Cap.ROUND);

        keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        keyPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ColorUtils.setAlphaComponent(BLACK, 229));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(mediumTextSize);

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(ColorUtils.setAlphaComponent(BLACK, 200));
        dividerPaint.setStyle(Paint.Style.STROKE);

//        genDummy();
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    private void genDummy() {
        String label = "Label";
        List<Pie> pies = new ArrayList<>();
        int i = 0;
        for (int color : colors) {
            Pie pie = new Pie(label + " " + ++i, BigDecimal.valueOf(Math.random()*10000));
            pie.color = color;
            pies.add(pie);
        }

        add(pies);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int size = data.size();
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            //We obey what the parent says
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            //we calculate the height
            height = (int) Math.ceil(width*sRatio);
            height += (keyBoundsHeight + heightOfDivider)*size;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        pieBounds.set(0, 0, w / 2.0f, h);
//        miscellaneous.set(pieBounds);
//        miscellaneous.offsetTo(pieBounds.right, 0);
//        miscellaneous.inset(padding, padding);

        //The rest of the height depends on the size of the pie collection size
        pieBounds.set(0, 0, w, sRatio*w);
        miscellaneous.set(0, pieBounds.bottom, w, h);

        //We use the smallest dimension as the length of the sides of pieRect
        //since it should be a square the sides are therefore equal
        //From the center of the bounds find where the pieRect will lie w.r.t the length of the sides
        pieBounds.inset(padding, padding);
        float width = pieBounds.width(), height = pieBounds.height();
        float side = Math.min(height, width);
        pieRect.set(pieBounds);
        //Converting the pieRect into a square and centering it in the pieBounds
        pieRect.inset((width - side)*0.5f, (height - side)*0.5f);

        //adding some padding so as to accommodate the stroke width of the pie that we'll be drawing
        float inset = strokeWidth*2 + space;
        pieRect.inset(inset, inset);

        padding = -padding;
        pieBounds.inset(padding, padding);
        keyBounds.set(0, 0, 0, keyBoundsHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //From starting angle of zero we draw the pies clockwise
        //The sum of all angles should add up to 360
        float startAngle = 270;
        float maxWidth = 0, maxHeight = 0;
        Collection<Pie> pies = data.values();
        for (Pie pie : pies) {
            piePaint.setColor(pie.color);

            //we need to offset the arcs using the angles at their centers
            //We calculate the half angle then add it to the current start angle to get The angle
            //we'll be using
            canvas.drawArc(pieRect, startAngle, pie.angle, false, piePaint);
            startAngle += pie.angle + SPACE;
            textPaint.getTextBounds(pie.label, 0, pie.label.length(), rect);
            maxWidth = Math.max(maxWidth, rect.width());
            maxHeight = Math.max(maxHeight, rect.height());
        }

        //Drawing the key

        //Have a predefined interval height which'll be then updated if it won't
        //fit the predefined height of the whole pie-chart view
        int saveCount = canvas.save();
        float interval = keyBounds.height();
        keyBounds.bottom -= heightOfDivider;

        keyBounds.set(0, 0, 0, interval - heightOfDivider);
        canvas.translate(miscellaneous.left, miscellaneous.top);

        //Initialising the key box values
        final float size = keyBounds.height();
        key.set(0, 0, size, size);

        float centerX = key.centerX(), centerY = key.centerY();
        float kh = textPaint.getTextSize();
        key.right = key.bottom = kh;
        key.offset(centerX - key.centerX(), centerY - key.centerY());
        float r = kh*.35f;
        textPaint.setTextAlign(Paint.Align.LEFT);
        final int width = getWidth();
        int i = 0;
        for (Pie pie : pies) {
            if (i > 0)
                //Translate to the top of the next label
                canvas.translate(0, interval);
            else
                //translate to the top of the miscellaneous space
                canvas.translate(0, 0);

            keyPaint.setColor(pie.color);
            canvas.drawRoundRect(key, r, r, keyPaint);
            canvas.drawText(
                    pie.label,
                    interval,
                    key.centerY() + rect.height()*.5f,
                    textPaint
            );

            //We don't want to draw the bottom most divider hence we break the loop here
            if (i++ == pies.size() - 1) break;

            //drawing the divider
            canvas.save();
            canvas.translate(0, keyBounds.bottom + 0.5f*heightOfDivider);
            canvas.drawLine(0, 0, width, 0, dividerPaint);
            canvas.restore();
        }
        canvas.restoreToCount(saveCount);

        //Drawing the Total inside the pie-chart
        canvas.save();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(largeTextSize);
        textPaint.getTextBounds(total.get(), 0, total.get().length(), rect);
        canvas.translate(pieBounds.centerX(), pieBounds.centerY() + rect.height()*0.5f);
        canvas.drawText(total.get(), 0, 0, textPaint);
        canvas.restore();
    }

    public void add(@NonNull List<Pie> pies) {
        data.clear();
        add(pies.toArray(new Pie[0]));
    }

    public void add(@NonNull Pie... pies) {
        BigDecimal sum = BigDecimal.ZERO;
        Arrays.sort(pies);
        for (Pie pie : pies) {
            if (data.size() == 10) {
                Log.e(TAG, "add: ", new IllegalArgumentException("Can't exceed 10 entries"));
                break;
            }
            data.put(pie.getLabel(), pie);
            sum = sum.add(pie.getValue());
        }


        for (Pie pie : data.values()) {
            pie.setPercentage(
                    pie.getValue().divide(sum, RoundingMode.CEILING).floatValue(),
                    data.size()
            );
        }

        Locale l = getDefault();
        total.set(format(l, CASH_FORMAT, getInstance(l).getSymbol(), sum));
        invalidate();
        requestLayout();
    }

    public static class Pie implements Parcelable, Comparable<Pie> {
        private final String label;
        private final BigDecimal value;
        private float percentage;
        private float angle;
        private int color;

        private Pie(@NonNull Parcel in) {
            label = in.readString();
            value = new BigDecimal(in.readString());
            percentage = in.readFloat();
            angle = in.readFloat();
            color = in.readInt();
        }

        public Pie(String label, BigDecimal value) {
            this.value = value;
            Locale l = getDefault();
            this.label = format(
                    "%s - %s",
                    label,
                    format(l, CASH_FORMAT, getInstance(l).getSymbol(), this.value)
            );
        }

        public String getLabel() {
            return label;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setPercentage(float percentage, int count) {
            this.percentage = percentage;
            setAngle(count);
        }

        public void setAngle(int count) {
            angle = percentage*(360 - (SPACE*count));
        }

        public static final Parcelable.Creator<Pie> CREATOR = new Parcelable.Creator<Pie>() {
            @Override
            @NonNull
            public Pie createFromParcel(Parcel source) {
                return new Pie(source);
            }

            @Override
            @NonNull
            public Pie[] newArray(int size) {
                return new Pie[size];
            }
        };


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(label);
            dest.writeString(value.toString());
            dest.writeFloat(percentage);
            dest.writeFloat(angle);
            dest.writeInt(color);
        }

        @Override
        public int compareTo(@NonNull Pie o) {
            return this.getValue().compareTo(o.getValue());
        }
    }
}

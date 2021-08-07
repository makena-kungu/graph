package com.nemido.lib.graph;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.BLUE;
import static android.graphics.Color.CYAN;
import static android.graphics.Color.DKGRAY;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.MAGENTA;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;
import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.String.format;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.card.MaterialCardView;
import com.nemido.lib.R;
import com.nemido.lib.ShadowRenderer;
import com.nemido.lib.utils.Period;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * A user interface that displays a simple line-graph representation of data to the user.
 * </p>
 * <p>
 * The following code sample shows a typical use, with an XML layout
 * and code to modify the contents of the text view:
 * </p>
 *
 * <pre>
 *   &lt;LinearLayout
 *       xmlns:android="http://schemas.android.com/apk/res/android"
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"&gt;
 *      &lt;com.nemido.bizz.graphs.GraphView
 *          android:id="@+id/text_view_id"
 *          android:layout_height="wrap_content"
 *          android:layout_width="match_parent" /&gt;
 *   &lt;/LinearLayout&gt;
 *   </pre>
 *
 * <p>
 * A code sample demonstrating how to modify or add graph data into the graph view
 * as defined in the above XML layout:
 * </p>
 * <pre>
 *      public class SampleActivity extends AppCompatActivity {
 *
 *          protected void onCreate(@Nullable Bundle savedInstanceState) {
 *              super.onCreate(savedInstanceState);
 *              setContentView(R.layout.graph_layout);
 *              GraphView view = findViewById(R.id.m_graph_view);
 *
 *              List<Line> lines = new ArrayList<>();
 *              view.add(lines);
 *          }
 *      }
 *   </pre>
 *
 * <p>
 * It's recommended you use a {@code WRAP_CONTENT} height since it calculates it's own height.
 * As for the width use {@code MATCH_PARENT}.</br>
 * It's also commendable if you use a scrolling view in the hierarchy
 * </p>
 */
@SuppressWarnings("RedundantSuppression")
public class GraphView extends View {
    private static final String TAG = "GraphView";

    private static final int SPACE = 4;
    public static final String FORMAT = "%.1f%s";
    private static final float COS_45 = (float) Math.cos(Math.toRadians(45));

    private static final int NORMAL = 0;
    private static final int MINI = 1;
    private static final int CUSTOM = 2;

    public static final int NONE = -1;
    public static final int BIG = 0;
    public static final int SMALL = 1;

    private final boolean decoOn;
    private final int graphAreaHeight;
    private final int labelGraphSpace = 40;
    private final int size;
    private final int theHeight;
    private final float diameter;
    private final float elevation;
    private final float labelTextSize;
    /**
     * The Maximum y-axis raw value
     */
    private float maximum = 0;
    private final float padding;
    private float previousMax = Float.NaN;
    private final float strokeWidth;
    private final float textSize;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final float radius, touch;
    private final int[] colors = new int[]{GREEN, RED, BLUE, CYAN, MAGENTA, YELLOW, /*ORANGE*/Color.parseColor("#FFA500"), DKGRAY, GRAY};


    private final AtomicBoolean touched = new AtomicBoolean(false);
    private final AtomicInteger height = new AtomicInteger();
    private final AtomicInteger width = new AtomicInteger();
    private final AtomicInteger columnHeight = new AtomicInteger();
    private final AtomicInteger rowHeight = new AtomicInteger();
    private final AtomicReference<String> selectedKey = new AtomicReference<>();
    private final AtomicReference<Line> graph = new AtomicReference<>();

    private final List<Best> bests = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final List<String> tLabels = new ArrayList<>();

    private final PointF max = new PointF(0, 0);
    private final PointF multiplier = new PointF(0, 0);
    private final PointF touchedVal = new PointF(0, 0);

    private final Paint areaPaint;
    private final Paint linePaint;
    private final Paint cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mGuidesPaint;
    private final Paint mXAxisPaint;
    private final Paint mXLine;
    private final TextPaint cTextPaint;
    private final TextPaint gTextPaint;
    private final TextPaint mTextPaint;

    private final Map<String, Line> lineMap = new HashMap<>();
    private final Period period = Period.ofDay();

    private final Rect textRect = new Rect();
    private final Rect tRect = new Rect();
    private final Rect tBounds = new Rect();
    private final RectF bounds = new RectF();
    private final RectF columnBounds = new RectF();
    private final RectF indicator = new RectF();
    private final RectF mRect = new RectF();
    private final RectF rect = new RectF();
    private final RectF rowBounds = new RectF();
    private final RectF tRectF = new RectF();
    private final RectF weirdBounds = new RectF();
    private final ShadowRenderer s = new ShadowRenderer();

    private CompareListener listener;
    private OnDataChangedListener dataListener;
    private final GestureDetector detector;

    public GraphView(Context context) {
        this(context, null);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final int internalPadding = 10;
        setPadding(getPaddingLeft() + internalPadding,
                getPaddingTop() + internalPadding,
                getPaddingRight() + internalPadding,
                getPaddingBottom() + internalPadding
        );

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GraphView, defStyleAttr, 0);
        strokeWidth = a.getDimension(R.styleable.GraphView_strokeWidth, getResources().getDimension(R.dimen.default_stroke_width));
        radius = a.getDimension(R.styleable.GraphView_valuesRadius, getResources().getDimension(R.dimen.default_values_radius));
        padding = calcPadding(radius);
        touch = a.getFloat(R.styleable.GraphView_touch, -1);
        graphAreaHeight = getResources().getDimensionPixelSize(R.dimen.default_graphArea_height);
        labelTextSize = a.getDimension(R.styleable.GraphView_labelTextSize, getResources().getDimension(R.dimen.default_label_text_size));
        diameter = labelTextSize;
        textSize = a.getDimension(R.styleable.GraphView_android_textSize, getResources().getDimension(R.dimen.default_textSize));
        elevation = a.getDimension(R.styleable.GraphView_elevation, getResources().getDimension(R.dimen.default_elevation));
        final float clickedTextSize = a.getDimension(R.styleable.GraphView_clickedTextSize, getResources().getDimension(R.dimen.default_clickedTextSize));
        final float guidesStroke = a.getDimension(R.styleable.GraphView_guideStrokeWidth, getResources().getDimension(R.dimen.default_guideStrokeWidth));

        mGuidesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGuidesPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 30));
        mGuidesPaint.setStyle(Paint.Style.STROKE);
        mGuidesPaint.setStrokeWidth(guidesStroke);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);


        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(Color.LTGRAY);
        mTextPaint.setTextSize(20f);

        gTextPaint = new TextPaint(mTextPaint);
        gTextPaint.setAntiAlias(true);
        gTextPaint.setColor(ColorUtils.setAlphaComponent(DKGRAY, 200));
        gTextPaint.setTextSize(textSize);

        cTextPaint = new TextPaint(mTextPaint);
        cTextPaint.setAntiAlias(true);
        cTextPaint.setTextSize(clickedTextSize);
        cTextPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 180));

        mXAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXAxisPaint.setColor(Color.BLACK);
        mXAxisPaint.setStyle(Paint.Style.STROKE);
        mXAxisPaint.setStrokeWidth(a.getDimension(R.styleable.GraphView_xAxisWidth, getResources().getDimension(R.dimen.default_x_axis_width)));

        mXLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLine.setStrokeWidth(a.getDimension(R.styleable.GraphView_showStrokeWidth, getResources().getDimension(R.dimen.default_show_stroke_width)));
        mXLine.setStyle(Paint.Style.STROKE);
        mXLine.setColor(
                ColorUtils.
                        setAlphaComponent(
                                Color.BLACK,
                                a.getInt(R.styleable.GraphView_mAlpha, 75)
                        )
        );
        float dashWidth = a.getDimension(R.styleable.GraphView_android_dashWidth, getResources().getDimension(R.dimen.default_dashWidth)),
                dashGap = a.getDimension(R.styleable.GraphView_android_dashGap, getResources().getDimension(R.dimen.default_dashGap));
        mXLine.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));

        size = a.getInt(R.styleable.GraphView_size, NORMAL);
        theHeight = a.getInt(R.styleable.GraphView_height, NONE);
        decoOn = a.getBoolean(R.styleable.GraphView_decorationsOn,
                size != MINI || theHeight == NONE);

        a.recycle();

        detector = new GestureDetector(context, new GestureListener());
        areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaPaint.setStyle(Paint.Style.FILL);
    }

    /*void dummy(Period p) {
        final int duration = p.getDuration();
        setPeriod(p);
        final List<Line> lines = new ArrayList<>();
        int i = 0;
        int arr = 1;

        while (i < arr) {
            int j = 0;
            Line line = new Line("label" + i, i++%2 == 0);
            while (j < duration) {
                line.add(++j, Math.random()*1000);
            }
            lines.add(line);
        }

        add(lines);
        test = true;

    }*/

    @SuppressWarnings("unused")
    public void setCompareListener(CompareListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unused")
    public void setDataChangedListener(OnDataChangedListener listener) {
        dataListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //final int count = isSelected.get() ? 1 : lineList.size();
        //ensures multiplier is never greater than 3 which is the max number of rows allowed
        switch (size) {
            case NORMAL:
                final int m = 1;

                //finding the column height
                //firstly measure the height of any random text
                final String random = "This Sample typo";
                final float initialTS = mTextPaint.getTextSize();
                mTextPaint.setTextSize(labelTextSize);
                mTextPaint.getTextBounds(random, 0, random.length(), textRect);
                mTextPaint.setTextSize(initialTS);

                //we add a padding of 5 at the top an at the bottom
                rowHeight.set(textRect.height() + 20);
                columnHeight.set(rowHeight.get()*m);
                height.set(columnHeight.get() + graphAreaHeight + labelGraphSpace);
                width.set(MeasureSpec.getSize(widthMeasureSpec));
                break;
            case MINI:
                getDimension(width, height);
                width.set(resolveAdjustedSize(width.get(), widthMeasureSpec));
                height.set(resolveAdjustedSize(height.get(), heightMeasureSpec));
                break;
            case CUSTOM:
                if (theHeight == BIG) {
                    width.set(resolveAdjustedSize(width.get(), widthMeasureSpec));
                    height.set(resolveAdjustedSize(height.get(), heightMeasureSpec));
                } else if (theHeight == SMALL) {
                    final ViewGroup containerCard = findContainerCard(this);
                    int h = containerCard.getHeight();
                    h *= 2;
                    h /= 3;
                    //find the heights of all other components within the container and subtract them
                    // from the h to get the height of the graph
                    h -= removeHeight(containerCard);

                    width.set(resolveAdjustedSize(width.get(), widthMeasureSpec));
                    height.set(resolveAdjustedSize(h, heightMeasureSpec));
                }
                break;
        }
        setMeasuredDimension(width.get(), height.get());
    }

    private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result;

        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                //can be as big as we want
                result = desiredSize;
                break;
            case MeasureSpec.AT_MOST:
                //The views size is strictly constraint to this size by the parent view
                result = Math.min(desiredSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            default:
                throw new IllegalArgumentException();
        }

        return result;
    }

    private void getDimension(@NonNull AtomicInteger width, @NonNull AtomicInteger height) {
        Resources res = getResources();

        //if this method is called then the size ought to be MINI
        height.set(res.getDimensionPixelSize(R.dimen.mini_height));
        width.set(res.getDimensionPixelSize(R.dimen.mini_width));
    }

    private static ViewGroup findContainerCard(View view) {
        ViewGroup f;
        do {
            if (view instanceof MaterialCardView) {
                return (ViewGroup) view;
            } else {
                f = (ViewGroup) view;
            }

            //going up the hierarchy to find the container card
            final ViewParent parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        } while (view != null);

        return f;
    }

    private static int removeHeight(@NonNull View view) {
        int h = 0;

        //the initial view is the MaterialCardView so we go down the hierarchy again to find the
        // height of the views in the
        h += view.getResources().getDimensionPixelSize(R.dimen.heightOfValues);
        h += view.getResources().getDimensionPixelSize(R.dimen.initialPadding);
        h += view.getResources().getDimensionPixelSize(R.dimen.margin);
        if (view instanceof MaterialCardView) {
            MaterialCardView v = (MaterialCardView) view;
            h += v.getContentPaddingTop() +
                    v.getContentPaddingBottom();
        }

        return h;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: started");

        mRect.set(
                getPaddingLeft(),
                getPaddingTop(),
                (float) w - getPaddingRight(),
                h - getPaddingBottom()
        );

        //All the measuring'll take place here since all the addition has already occurred
        //set columns heights and widths
        if (size == NORMAL) {
            columnBounds.set(mRect);
            final long round = round(Math.floor(w*0.6f));
            columnBounds.right = columnBounds.left + round/2.0f;
            columnBounds.bottom = columnBounds.top + (columnHeight.get());
            rowBounds.set(0, 0, columnBounds.width(), rowHeight.get());

            float space = rowBounds.height();
            indicator.set(0, 0, space, space);
            //Center the indicator in the rowBounds
            final float v = (space - diameter)/2;
            indicator.inset(v, v);

            //add labels to the labels list
            //if the current max is the same as the previous one then no need to reallocate
        }
        computeBounds();
        labels();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (size == NORMAL) {
            boolean result = detector.onTouchEvent(event);
            if (result) {
                performClick();
                invalidate();
            }
            return result;
        } else return false;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.save();
        //canvas.rotate(180, width/2, height/2);

        final Iterator<Line> iterator = lineMap.values().iterator();
        if (iterator.hasNext()) {
            final Line line = iterator.next();
            if (line != null && lineMap.values().size() == 1) {
                line.color = (listener != null) ?
                        (listener.onCompare() ? GREEN : RED) :
                        GREEN;
                graph.set(line);
            }
        }

        if (size == NORMAL) {
            labels(canvas);

            guides(canvas);
        }

        graph(canvas);
    }

    void labels(@NonNull Canvas c) {
        final List<Line> lines = new ArrayList<>(lineMap.values());
        int count = lines.size();
        if (count <= 0) return;

        mTextPaint.setTextSize(labelTextSize);
        mTextPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 204));

        final int initialSave = c.save();
        c.translate(mRect.left, mRect.top);
        final int save = c.save();
        int i = 0;

        final float stroke = linePaint.getStrokeWidth();
        linePaint.setStrokeWidth(1.85f*stroke);
        do {
            //Translate to the next row but if its the first row in the respective column then we
            //shouldn't translate
            Line line = lines.get(i);
            if (i%3 != 0) {
                c.translate(0, rowBounds.height());
            }

            //translate to the text space
            c.save();
            final float y = indicator.centerY();
            linePaint.setColor(line.color);
            c.drawLine(indicator.left, y, indicator.right, y, linePaint);
            float labelBottom = indicator.bottom - 5;
            c.translate(indicator.width() + 30, labelBottom);
            c.drawText(line.label, 0, 0, mTextPaint);
            c.restore();

            //Translate to the second column
            if (i == 2) {
                c.restoreToCount(save);
                c.translate(columnBounds.width(), 0);
            }
            i++;
        } while (i < count);

        linePaint.setStrokeWidth(stroke);
        c.restoreToCount(initialSave);
    }

    void guides(Canvas c) {
        final ListIterator<String> iterator = labels.listIterator();
        if (!iterator.hasNext()) return;
        //generatePath();
        //NOTE: -THE DRAW IS FROM BOTTOM UPWARDS
        gTextPaint.setTextSize(textSize);

        final int saveCount = c.save();
        c.translate(bounds.left, bounds.bottom);
        //This accounts for the space in between two consecutive guides
        final int count = labels.size() - 1;
        final float interval = bounds.height()/count;
        final float width = bounds.width();
        do {
            final int i = iterator.nextIndex();
            final String l = iterator.next();
            gTextPaint.getTextBounds(l, 0, l.length(), textRect);
            textRect.offset(-textRect.right - SPACE, 0);
            c.drawText(l, textRect.left, textRect.height()/2f, gTextPaint);
            //insert a space of 8 btn the line and the text
            c.drawLine(0, 0, width, 0, i == 0 ? mXAxisPaint : mGuidesPaint);
            //translate relative to the last y point
            c.translate(0, -interval);
        } while (iterator.hasNext());

        c.restoreToCount(saveCount);
    }

    void graph(@NonNull Canvas c) {
        final List<Line> lineList = new ArrayList<>(lineMap.values());
        if (lineList.size() <= 0) {
            return;
        }
        //use path.quadTo();
        //c.drawPath
        //for offsetting the left use the updated bounds e.g. bounds.left.
        c.save();

        //The y coordinates of the points shall be inverted by multiplying it by -1 this means that
        //we'll have to translate to the bottom of the canvas or rather the graph area bounds and
        //the coordinates shall display the inverted graph
        c.translate(bounds.left, bounds.bottom);

        for (final Line l : lineMap.values()) {
            linePaint.setColor(l.color);
            c.drawPath(l.line, linePaint);
            areaPaint.setShader(
                    new LinearGradient(
                            weirdBounds.left,
                            weirdBounds.top,
                            weirdBounds.left,
                            weirdBounds.bottom,
                            new int[]{
                                    ColorUtils.setAlphaComponent(l.color, 103),
                                    ColorUtils.setAlphaComponent(l.color, 50),
                                    ColorUtils.setAlphaComponent(l.color, 20)
                            },
                            new float[]{0, .5f, 1},
                            Shader.TileMode.CLAMP
                    )
            );
            c.drawPath(l.areaUnderGraph, areaPaint);
        }

        float x = getWidth()*.25f;
        float y = getHeight()*-.25f;
        c.drawText("" + f, x, y, mTextPaint);

        touched(c);

        c.restore();
    }

    void touched(@NonNull Canvas c) {
        if (size != NORMAL) return;

        if (bests.isEmpty() || tLabels.isEmpty()) {
            return;
        }
        // Here we'll draw a rectangle, and on top of it some text showing where the user clicked
        // and a vertical line from the bottom of the triangle to the bottom x axis;
        final float x = x(touchedVal.x);

        //The vertical indicator will start from the point
        c.drawLine(x, 0, x, tRectF.bottom, mXLine);

        for (Best b : bests) {
            this.cPaint.setColor(b.line.color);
            c.drawCircle(x, y(b.my), strokeWidth*2.2f, this.cPaint);
        }

        //include shadow renderer but we'll draw a plain rect
        s.drawRoundRectWithShadow(c, rect, radius, elevation);

        tRect.offsetTo(0, 0);
        c.translate(tRectF.left, tRectF.top);

        int i = 0;
        final int size = tLabels.size();
        final float interval = tRectF.height()/tLabels.size();
        //final float textBottom = (tRectF.height()/tLabels.size() + 24)*.5f;
        while (i < size) {
            final String label = tLabels.get(i);
            cTextPaint.getTextBounds(label, 0, label.length(), tRect);
            tRect.offsetTo(0, 0);
            float diff = interval - tRect.height();
            diff /= 2;
            tRect.offset(0, (int) diff);
            c.translate(0, tRect.bottom);

            /*c.translate(0,
                    textBottom + (abs(textBottom - tRectF.height()/tLabels.size())*.5f));*/

            if (i == size - 1 || size == 2) {
                //drawing the date i.e. the first label
                //cTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
                cTextPaint.setTypeface(Typeface.DEFAULT);
                cPaint.setColor(BLACK);
                c.drawText(label, 0, 0, cTextPaint);
            } else {
                final String[] split = label.split(Best.splitter);
                split[0] = split[0].concat(Best.splitter);
                cTextPaint.getTextBounds(split[0], 0, split[0].length(), tBounds);
                split[0] = split[0].replace(Best.splitter, "");
                c.drawText(split[0], 0, 0, cTextPaint);
                cTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
                c.drawText(split[1], tBounds.right, 0, cTextPaint);
            }
            cTextPaint.setTypeface(null);
            i++;
        }
    }

    public void clear() {
        lineMap.clear();
        clearTouched();
        graph.set(null);
    }

    void clearTouched() {
        bests.clear();
        if (touched.get())
            touched.set(!touched.get());
    }

    public void add(@NonNull Collection<Line> lines) {
        clear();
        add(lines.toArray(new Line[0]));

    }

    int f;

    public void add(@NonNull Line... lines) {
        if (lines.length <= 0) return;

        int i = 0;
        final int len = lines.length;
        while (i < len) {
            final int mapSize = lineMap.size();
            Line line = lines[i];

            //indicates that there's more than one graph and therefore more colors should be used
            if (mapSize + lines.length > 1) {
                line.color = colors[i + mapSize - 1];
            }
            if (mapSize == 5) {
                Log.e(TAG, "add: Can't Have More than 5 Graphs", new UnsupportedOperationException());
                return;
            }

            //a data set of more than 30 units on the x axis tends to create an unclear line graph
            //thus divide a set into observable portions.

            if (size == MINI || size == CUSTOM) {
                if (period.getDuration() > Period.ofMonth().getDuration()) {
                    try {
                        final int countOfMonths = period.getDuration()/Period.ofMonth().getDuration();
                        final int length = line.points.size();
                        int j = 0;

                        List<PointF> points = new ArrayList<>();
                        while (j < length) {
                            int k = 0;
                            float sum = 0f;
                            while (k < countOfMonths) {
                                sum += line.points.get(j++).y;
                                k++;
                            }
                            points.add(new PointF(j, sum/countOfMonths));
                        }

                        line.points.clear();
                        line.points.addAll(points);
                        f = line.points.size();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (line.maxx > max.x) {
                max.x = line.maxx;
            }
            if (line.maxy > max.y) {
                max.y = line.maxy;
            }
            lineMap.put(line.label, line);
            i++;
        }


        computeBounds();
        invalidate();
        requestLayout();
    }

    private void computeBounds() {
        bounds.set(mRect);

        if (size == NORMAL) {
            bounds.top += columnHeight.get()*lineMap.size() + labelGraphSpace;
            bounds.bottom -= textSize + 5;
            //compensate for the transition that's made
            bounds.right -= ((float) getWidth() - bounds.right);
        }
    }

    public void setPeriod(Period p) {
        period.set(p);
        super.invalidate();
    }

    @Override
    public void invalidate() {
        if (dataListener != null) {
            dataListener.onChanged();
        }

        previousMax = maximum;
        float max = Collections.max(lineMap.values(), (o1, o2) -> Float.compare(o1.maxy, o2.maxy)).maxy;
        if (max != maximum)
            maximum = max;

        labels();
        generatePath();
        super.invalidate();
    }

    /**
     * Selects a certain graph from a list as per the liking of the user using a unique key that's
     * been submit
     *
     * @param key a unique that's used to get the graph from the map of graphs
     */
    @SuppressWarnings("unused")
    public void select(String key) {
        //One shouldn't be able to select a graph if the graph list contains only one graph
        if (lineMap.size() > 1) {
            if (!lineMap.containsKey(key)) {
                Log.e(TAG, "select: Unidentified Key: " + key, new IllegalArgumentException());
            } else {
                selectedKey.set(key);
                graph.set(lineMap.get(selectedKey.get()));

                invalidate();
                requestLayout();
            }
        }
    }

    @SuppressWarnings("unused")
    public List<String> keys() {
        return new ArrayList<>(lineMap.keySet());
    }

    /**
     * Generates a list of points closest to the touching point
     *
     * @param tx x-coordinate of the point touched on the screen
     */
    protected void bests(float tx) {
        clearTouched();
        touched.set(true);
        //Account for the translation made in starting to draw the graph which is bounds.left
        tx -= bounds.left;
        tx /= multiplier.x;
        //but this new value doesn't put into consideration that the graph begins at the bounds.left

        List<Line> lines = new ArrayList<>(lineMap.values());
        final Iterator<Line> it = lines.iterator();
        if (!it.hasNext())
            return;

        String max = "";
        String date = "";

        tLabels.clear();
        touchedVal.set(0, 0);

        for (Line line : lines) {
            final PointF point = findPoint(line.points, tx);

            if (date.isEmpty()) {
                date = date((int) point.x);
                if (date.length() > max.length())
                    max = date;
            }

            final Best best = new Best(line, point.y, lines.size());
            bests.add(best);

            if (point.y > touchedVal.y) {
                touchedVal.set(point);
            }
            if (best.label.length() > max.length()) max = best.label;

            tLabels.add(best.label);
        }
        tLabels.add(date);

        cTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        cTextPaint.getTextBounds(max, 0, max.length(), tRect);
        tRect.bottom += 5;//5 is the padding btn the vals i.e there'll be 2.5 space
        cTextPaint.setTypeface(null);

        final int count = tLabels.size();
        final float height = count*(tRect.height());
        final float width = tRect.width();
        final float cy = weirdBounds.centerY();
        tRectF.top = y(touchedVal.y) > cy ? cy : weirdBounds.top;
        tRectF.left = x(touchedVal.x);
        tRectF.right = tRectF.left + width;
        tRectF.bottom = tRectF.top + height/*- 5*/;//5 is removing the space at the bottom...

        //center the rect along the x-axis to the touched val
        tRectF.offset(-tRectF.width()*.5f, 0);
        //ensure the rect is within bounds

        //this is the "container used to draw the bounds with the shadow
        rect.set(tRectF);
        float v = 5, h = 10;
        rect.inset(-(padding + h), -(padding + v));

        if (rect.left < weirdBounds.left) {
            rect.offsetTo(0, rect.top);
        }
        if (rect.right > weirdBounds.width()) {
            rect.offset(-abs(rect.right - weirdBounds.width()), 0);
        }
        tRectF.set(rect);
        tRectF.inset(padding + h, padding + v);
    }

    protected final float calcPadding(float radius) {
        return (1 - COS_45)*radius;
    }

    private static int compare(@NonNull PointF o1, @NonNull PointF o2) {
        return Float.compare(o1.x, o2.x);
    }

    protected String date(int value) {
        switch (period.unit) {
            case DAY:
                return DateFormat.getTimeInstance(DateFormat.SHORT).format(period.time(value));
            case MAX:
                return DateFormat.getDateInstance(DateFormat.SHORT).format(period.time(value));
            default:
                return new SimpleDateFormat("dd MMM", Locale.getDefault())
                        .format(period.time(value));
        }
    }

    /**
     * <p>Finds the most suitable point in that it's the closest to the point of touch long the
     * x axis from a {@link Collection} of {@link PointF} items.
     * </p>
     * <pre>
     * Note:// This method works effectively if the collection supplied
     *          is sorted w.r.t the x-coordinates of the points as
     *          opposed to the y-coordinate
     * </pre>
     *
     * @param c the collection from which the point shall be estimated
     * @param x the x coordinate to be used in the estimation
     * @return The {@link PointF} with the closest {@link PointF#x} to the supplied x coordinate
     */
    public static PointF findPoint(@NonNull Collection<PointF> c, final float x) {
        //the point x should be rounded to the nearest whole number note rounding and not truncating
        final PointF unset = new PointF();

        final PointF[] arr = c.toArray(new PointF[0]);
        int min = 0;
        int max = arr.length - 1;
        int count;

        do {
            count = max - min;
            count++;

            if (count == 1 || min == max) {
                return arr[min];
            } else if (count == 2) {
                float a = abs(arr[min].x - x), b = abs(arr[max].x - x);
                return min(a, b) == a ? arr[min] : arr[max];
            }

            int middle = (max + min)/2;

            final PointF mid = arr[middle];

            if (x < mid.x && x > arr[min].x) {
                //minimum doesn't change if this conditions hole
                max = middle;
            } else if (mid.x == x) {
                return mid;
            } else if (x <= arr[min].x) {
                return arr[min];
            } else {
                //here the x is greater than the mid value so we increase the min value to be the
                //mid point
                if (x >= arr[max].x) {
                    return arr[max];
                } else if (x <= arr[++middle].x) {
                    float a = abs(mid.x - x), b = abs(arr[middle].x - x);
                    return min(a, b) == a ? mid : arr[middle];
                }
                //if the above conditions don't hold then we set the new min to be the middle
                min = middle;
            }
        } while (min < max);
        //When this loop terminates naturally we shall have a min value that'll help us get an array
        // with two values
        Log.e(TAG, "findPoint: ", new NoSuchElementException("No element found"));

        return unset;
    }

    void generatePath() {
        multiplier.x = bounds.width()/max.x;
        //find the inverse of the y values
        multiplier.y = -(bounds.height()/max.y);
        for (final Line o : lineMap.values()) {
            o.line.rewind();
            o.areaUnderGraph.rewind();
            final ListIterator<PointF> iterator = o.points.listIterator();
            final PointF firstPoint = iterator.next();
            o.line.moveTo(x(firstPoint.x), y(firstPoint.y));

            while (iterator.hasNext()) {
                final PointF p = iterator.next();
                o.line.lineTo(x(p.x), y(p.y));
            }

            o.areaUnderGraph.addPath(o.line);
            o.areaUnderGraph.lineTo(bounds.width(), 0);
            o.areaUnderGraph.lineTo(0, 0);
            o.areaUnderGraph.close();
        }
    }

    @NonNull
    public static String label(float val) {
        String suffix = "";
        final float t = (float) (Math.pow(10, 12));
        final float b = (float) (Math.pow(10, 9));
        final float m = (float) (Math.pow(10, 6));
        final float k = (float) (Math.pow(10, 3));
        if (val >= t) {
            val /= t;
            suffix = "t";
        } else if (val >= b) {
            val /= b;
            suffix = "b";
        } else if (val >= m) {
            val /= m;
            suffix = "m";
        } else if (val >= k) {
            val /= k;
            suffix = "k";
        }

        return format(Locale.getDefault(), FORMAT, val, suffix);
    }

    void labels() {
        if (size != NORMAL) return;

        if (Float.compare(previousMax, maximum) != 0) {
            labels.clear();
            final String f = "0";
            labels.add(f);
            final float max = Float.parseFloat(label(maximum)
                    .replace("t", "")
                    .replace("b", "")
                    .replace("m", "")
                    .replace("k", ""));

            final int count = max%3 == 0 ? 3 : 2;

            float i = 1;
            gTextPaint.getTextBounds(f, 0, f.length(), textRect);
            int width = textRect.width();
            while (i <= count) {
                final String label = label((i++/count)*maximum);
                gTextPaint.getTextBounds(label, 0, label.length(), textRect);
                labels.add(label);

                if (textRect.width() > width) {
                    width = textRect.width();
                }
            }
            width += SPACE;
            bounds.left = mRect.left + width;
            weirdBounds.set(bounds);
            weirdBounds.offset(0, -bounds.bottom);
        }
    }

    protected float x(float x) {
        return x*multiplier.x;
    }

    protected float y(float y) {
        return y*multiplier.y;
    }

    protected static class Best {
        public static final String splitter = "-";
        final String label;
        final float my;
        final boolean currency;
        final int color;
        final Line line;

        public Best(@NonNull Line line, float y, int count) {
            this.line = line;
            my = y;
            currency = line.hasCurrency;
            color = line.color;
            Locale l = Locale.getDefault();
            final BigDecimal val = BigDecimal.valueOf(my);
            label = (count == 1 ? "" : line.label + ": " + splitter)
                    .concat(currency ?
                            format(l, "%s%,.2f", Currency.getInstance(l).getSymbol(), val) :
                            format(l, "%.1f", val)
                    );

        }
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            if (touched.get()) {
                //Clears the touched data
                clearTouched();
                return true;
            }

            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: working out");
            bests(e.getX());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            float x = e2.getX();
            //If we are to use the animation method instead of the live-touch method
            // v = d / t
            // => t = d/v
            // use t as the time btn the two animations
            // where d = diff btn the end-point from the start-point
            bests(x);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            bests(e2.getX());
            return true;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            final boolean isValid = validate(e);
            if (!isValid)
                if (touched.get())
                    touched.set(false);

            return isValid;
        }

        /**
         * Validates that the touch point is within the bounds of the graph area
         *
         * @param e the {@link MotionEvent} containing the coordinates that are to be validated
         * @return true of the touch point is within the bounds false otherwise
         */
        boolean validate(@NonNull MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            return bounds.contains(x, y);
        }
    }

    /**
     * A utility class that holds values for a graph object that's used by the {@link GraphView}
     */
    public static class Line implements Parcelable {
        public boolean hasCurrency;//by default it's false
        public String label;
        private float maxx;
        private float maxy;
        private final List<PointF> points;

        private int color;

        private final Path line = new Path();
        private final Path areaUnderGraph = new Path();

        private Line(String label, boolean hasCurrency, List<PointF> points) {
            this.hasCurrency = hasCurrency;
            this.label = label;
            this.points = points;
        }

        protected Line(@NonNull Parcel in) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                hasCurrency = in.readBoolean();
            } else
                hasCurrency = in.readInt() != 0;
            label = in.readString();
            maxx = in.readFloat();
            maxy = in.readFloat();
            points = new ArrayList<>();
            in.readTypedList(points, PointF.CREATOR);
            color = in.readInt();
        }

        public void add(float x, float y) {
            points.add(new PointF(x, y));
        }

        @SuppressWarnings("unused")
        public void add(double x, double y) {
            add((float) x, (float) y);
        }

        //This method should be called once all the points are inserted
        /*void process() {
            //firstly find the max vals
            Collections.sort(tempPoints, GraphView::compare);
            clear();

            BigDecimal maxx = tempPoints.get(tempPoints.size() - 1).x;
            BigDecimal maxy = BigDecimal.ZERO;
            for (PointB b : tempPoints) {
                if (b.y.compareTo(maxy) > 0) {
                    maxx = b.y;
                }
            }

            ensureIsFloat(maxx, divisorX);
            ensureIsFloat(maxy, divisorY);

            if (divisorX.get() == 1 && divisorY.get() == 1)
                //enter the points as they are
                for (PointB b : tempPoints) {
                    points.add(new PointF(b.x.floatValue(), b.y.floatValue()));
                }
            else {
                //other wise divide all values with the respective divisor
                if (divisorX.get() == 1 && divisorY.get() > 1) {
                    for (PointB b : tempPoints) {
                        points.add(
                                new PointF(b.x.floatValue(),
                                        b.y.divide(BigDecimal.valueOf(divisorY.get()), DECIMAL32)
                                                .floatValue()
                                )
                        );
                    }
                } else if (divisorX.get() > 1 && divisorY.get() == 1) {
                    for (PointB b : tempPoints) {
                        points.add(
                                new PointF(
                                        b.x.divide(BigDecimal.valueOf(divisorX.get()), DECIMAL32)
                                                .floatValue(),
                                        b.y.floatValue()
                                )
                        );
                    }
                } else {
                    for (PointB b : tempPoints) {
                        points.add(
                                new PointF(
                                        b.x.divide(BigDecimal.valueOf(divisorX.get()), DECIMAL32)
                                                .floatValue(),
                                        b.y.divide(BigDecimal.valueOf(divisorY.get()), DECIMAL32)
                                                .floatValue()
                                )
                        );
                    }
                }
            }

            Collections.sort(points, GraphView::compare);

            maxx(maxx);
            maxy(maxy);
        }


         * Ensures that the supplied {@link BigDecimal} is converted to a float value using the
         * initial general divisor and if not updates the divisor to accommodate the float value
         *
         * @param v       the big-decimal value to be  converted to a float value
         * @param divisor a multiple of ten that ensures the number remains the same despite been
         *                converted to a float

        static void ensureIsFloat(BigDecimal v, @NonNull AtomicLong divisor) {
            float fv;
            long counter = -1;
            do {
                v = v.movePointLeft((int) ++counter);
                fv = v.floatValue();
            } while (Float.isInfinite(fv));
            divisor.set((long) Math.pow(10, counter));
        }*/

        private void maxx() {
            maxx = points.get(points.size() - 1).x;
        }

        private void maxy() {
            float max = Collections.max(points, (o1, o2) -> Float.compare(o1.y, o2.y)).y;

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

        }

        public static final Creator<Line> CREATOR = new Creator<Line>() {
            @Override
            @NonNull
            public Line createFromParcel(Parcel source) {
                return new Line(source);
            }

            @Override
            @NonNull
            public Line[] newArray(int size) {
                return new Line[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dest.writeBoolean(hasCurrency);
            } else {
                dest.writeInt(hasCurrency ? 1 : 0);
            }
            dest.writeString(label);
            dest.writeFloat(maxx);
            dest.writeFloat(maxy);
            dest.writeTypedList(points);
            dest.writeInt(color);
        }

        @SuppressWarnings("unused")
        public static class Builder {
            private String label;
            private int color;
            private boolean hasCurrency = true;

            private final List<PointF> ps = new ArrayList<>();

            @SuppressWarnings("UnusedReturnValue")
            public Builder setLabel(String label) {
                this.label = label;
                return this;
            }

            public Builder setColor(double percent) {
                color = percent < 0 ? Color.RED : GREEN;
                return this;
            }

            public Builder setHasCurrency() {
                return setHasCurrency(false);
            }

            public Builder setHasCurrency(boolean hasCurrency) {
                this.hasCurrency = hasCurrency;
                return this;
            }

            public Builder add(PointF p) {
                ps.add(p);
                return this;
            }

            public Builder add(double x, double y) {
                return add((float) x, (float) y);
            }

            public Builder add(float x, float y) {
                ps.add(new PointF(x, y));
                return this;
            }

            public Builder add(PointF... points) {
                Collections.addAll(ps, points);
                return this;
            }

            public Builder set(PointF... pointFS) {
                ps.clear();
                Collections.addAll(ps, pointFS);
                return this;
            }

            public Line build() {
                final Line line = new Line(label, hasCurrency, ps);

                line.color = color;

                Collections.sort(line.points, GraphView::compare);
                line.maxx();
                line.maxy();
                return line;
            }
        }
    }

    // For this two interfaces we assume that there's only one implementor and thus we don't need
    // more complex interfaces such as LiveData
    public interface CompareListener {
        /**
         * Compares the upward or downward trends of two data sets and returns true if the current
         * data set is on an upward trend compared to the former data set. This can be done by
         * comparing the means of the given data sets for instance.
         *
         * @return true if current data set if > the former data set.
         */
        boolean onCompare();
    }

    public interface OnDataChangedListener {
        /**
         * Notifies that the data in this view has changed incase the user wishes to do some
         * updating with the data from this class
         */
        void onChanged();
    }
}
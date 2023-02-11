package com.wira.graph;

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
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.card.MaterialCardView;
import com.wira.core.period.ChronoUnit;
import com.wira.core.period.Period;
import com.wira.graph.core.ShadowRenderer;

import org.jetbrains.annotations.Contract;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * <p>
 * A user interface that displays a simple line-graph representation of data to the user.
 * </p>
 *
 * <p>
 * To add data to the {@link GraphView}, use the {@link GraphView#initialise(Graph)} method.
 * To create an instance of {@link Graph}, use the {@link Graph.Builder}
 * </p>
 *
 * <p>
 * The following code sample shows a typical use, with an XML layout
 * and code to modify the contents of the text view:
 * </p>
 *
 * <pre>
 *   &lt;{@link MaterialCardView}
 *       xmlns:android="http://schemas.android.com/apk/res/android"
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"&gt;
 *      &lt;{@link GraphView}
 *          android:id="@+id/m_graph_view"
 *          android:layout_height="wrap_content"
 *          android:layout_width="match_parent" /&gt;
 *   &lt;/{@link MaterialCardView}&gt;
 *   </pre>
 *
 * <p>
 * A code sample demonstrating how to modify or add graph data into the graph view
 * as defined in the above XML layout:
 * </p>
 * <pre>
 *      public class SampleActivity extends {@link androidx.appcompat.app.AppCompatActivity} {
 *
 *          protected void onCreate(@Nullable Bundle savedInstanceState) {
 *              super.onCreate(savedInstanceState);
 *              setContentView(R.layout.graph_layout);
 *              {@link GraphView} view = findViewById(R.id.m_graph_view);
 *              view.{@link  GraphView#initialise(Graph)}
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

    private static final int SPACE = 4;
    public static final String FORMAT = "%.1f%s";
    private static final float COS_45 = (float) Math.cos(Math.toRadians(45));

    private static final int NORMAL = 0;
    private static final int MINI = 1;
    private static final int CUSTOM = 2;

    public static final int NONE = -1;
    public static final int BIG = 0;
    public static final int SMALL = 1;

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
    private final int xLabelIndicatorHeight;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final float radius, touch;


    private final AtomicBoolean touched = new AtomicBoolean(false);
    private final AtomicInteger height = new AtomicInteger();
    private final AtomicInteger width = new AtomicInteger();
    private final AtomicInteger columnHeight = new AtomicInteger();
    private final AtomicInteger rowHeight = new AtomicInteger();
    private final AtomicReference<String> selectedKey = new AtomicReference<>();
    private final AtomicReference<Plot> graph = new AtomicReference<>();

    private final List<Best> bests = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final List<String> tLabels = new ArrayList<>();

    private final Coordinate multiplier = new Coordinate(0, 0);
    private final Coordinate touchedVal = new Coordinate(0, 0);

    private final LinkedList<XAxisLabel> xAxisLabels = new LinkedList<>();

    private final Paint areaPaint;
    private final Paint linePaint;
    private final Paint cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mGuidesPaint;
    private final Paint xAxisLabelsPaint = new Paint();
    private final Paint mXAxisPaint;
    private final Paint mXLine;
    private final TextPaint cTextPaint;
    private final TextPaint gTextPaint;
    private final TextPaint mTextPaint;
    private final TextPaint mXLabelTextPaint;

    private Graph data = null;

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
        strokeWidth = a.getDimension(R.styleable.GraphView_graphStrokeWidth, getResources().getDimension(R.dimen.default_stroke_width));
        radius = a.getDimension(R.styleable.GraphView_graphValuesRadius, getResources().getDimension(R.dimen.default_values_radius));
        padding = calcPadding(radius);
        touch = a.getFloat(R.styleable.GraphView_graphTouch, -1);
        graphAreaHeight = getResources().getDimensionPixelSize(R.dimen.default_graphArea_height);

        xLabelIndicatorHeight = a.getDimensionPixelSize(R.styleable.GraphView_graphXAxisLabelIndicatorHeight, getResources().getDimensionPixelSize(R.dimen.default_graphXAxisLabelIndicatorHeight));
        labelTextSize = a.getDimension(R.styleable.GraphView_graphLabelTextSize, getResources().getDimension(R.dimen.default_label_text_size));
        diameter = labelTextSize;
        textSize = a.getDimension(R.styleable.GraphView_graphTextSize, getResources().getDimension(R.dimen.default_textSize));
        elevation = a.getDimension(R.styleable.GraphView_graphLabelElevation, getResources().getDimension(R.dimen.default_elevation));
        final float clickedTextSize = a.getDimension(R.styleable.GraphView_graphClickedTextSize, getResources().getDimension(R.dimen.default_clickedTextSize));
        final float guidesStroke = a.getDimension(R.styleable.GraphView_graphGuideStrokeWidth, getResources().getDimension(R.dimen.default_guideStrokeWidth));

        mGuidesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGuidesPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 30));
        mGuidesPaint.setStyle(Paint.Style.STROKE);
        mGuidesPaint.setStrokeWidth(guidesStroke);


        xAxisLabelsPaint.set(mGuidesPaint);
        xAxisLabelsPaint.setColor(a.getDimensionPixelSize(R.styleable.GraphView_graphXLabelsIndicatorColor, DKGRAY));
        xAxisLabelsPaint.setStrokeWidth(a.getDimensionPixelSize(R.styleable.GraphView_graphXLabelStrokeWidth, getResources().getDimensionPixelSize(R.dimen.default_xLabelStrokeWidth)));
        xAxisLabelsPaint.setStyle(Paint.Style.STROKE);

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

        mXLabelTextPaint = new TextPaint(mTextPaint);
        mXLabelTextPaint.setTextAlign(Paint.Align.CENTER);
        mXLabelTextPaint.setColor(a.getColor(R.styleable.GraphView_graphXLabelTextColor, DKGRAY));
        mTextPaint.setTextSize(a.getDimensionPixelSize(R.styleable.GraphView_graphLabelTextSize, R.dimen.default_label_text_size));

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
        mXAxisPaint.setStrokeWidth(a.getDimension(R.styleable.GraphView_graphXAxisWidth, getResources().getDimension(R.dimen.default_x_axis_width)));

        mXLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLine.setStrokeWidth(a.getDimension(R.styleable.GraphView_graphShowStrokeWidth, getResources().getDimension(R.dimen.default_show_stroke_width)));
        mXLine.setStyle(Paint.Style.STROKE);
        mXLine.setColor(
                ColorUtils.
                        setAlphaComponent(
                                Color.BLACK,
                                a.getInt(R.styleable.GraphView_graphAlpha, 75)
                        )
        );
        float dashWidth = a.getDimension(R.styleable.GraphView_graphDashWidth, getResources().getDimension(R.dimen.default_dashWidth)),
                dashGap = a.getDimension(R.styleable.GraphView_graphDashGap, getResources().getDimension(R.dimen.default_dashGap));
        mXLine.setPathEffect(new DashPathEffect(new float[]{dashWidth, dashGap}, 0));

        size = a.getInt(R.styleable.GraphView_graphSize, NORMAL);
        theHeight = a.getInt(R.styleable.GraphView_graphHeight, NONE);

        a.recycle();

        detector = new GestureDetector(context, new GestureListener());
        areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaPaint.setStyle(Paint.Style.FILL);
    }

    @SuppressWarnings("unused")
    void dummy(@NonNull Period p) {
        //this function generates sample data for preview.
        final int duration = p.getDuration();
        List<Plot> plots = new ArrayList<>();
        int i = 0;
        int arr = 2;
        Random r = new Random();
        int max = 2000;


        Date startdate = new Date(p.start());
        Date enddate = new Date(p.end());
        SimpleDateFormat dd_mm = new SimpleDateFormat("dd MM", Locale.getDefault());

        String start = dd_mm.format(startdate);
        String end = dd_mm.format(enddate);

        while (i < arr) {
            int j = 0;
            Plot.Builder builder = new Plot.Builder();
            builder.setColor(r.nextInt() * -1)
                    .setHasCurrency()
                    .setLabel("label " + (i++) + " ");
            for (Period period : p) {
                builder.add(period.index, next(max));
            }
            plots.add(builder.build());
        }
        initialise(Graph.Builder.setPeriod(p).set(plots.toArray(new Plot[0])).build());
    }

    Random r = new Random();

    private float next(int bound) {
        float x;
        do x = ((float) r.nextGaussian()) * bound; while (x < 0);
        return round(x);
    }

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
                columnHeight.set(rowHeight.get() * m);
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
            final long round = round(Math.floor(w * 0.6f));
            columnBounds.right = columnBounds.left + round / 2.0f;
            columnBounds.bottom = columnBounds.top + (columnHeight.get());
            rowBounds.set(0, 0, columnBounds.width(), rowHeight.get());

            float space = rowBounds.height();
            indicator.set(0, 0, space, space);
            //Center the indicator in the rowBounds
            final float v = (space - diameter) / 2;
            indicator.inset(v, v);

            //add labels to the labels list
            //if the current max is the same as the previous one then no need to reallocate
        }

        update();
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

        if (data == null) {
            return;
        }

        if (data.lines.values().size() > 1) {
            for (Plot plot : data.lines.values()) {
                if (plot == null) continue;

                plot.color = (listener != null) ? (listener.onCompare() ? GREEN : RED) : plot.color;
                graph.set(plot);
            }
        }

        if (size == NORMAL) {
            if (!data.isSingle)
                key(canvas);

            ListIterator<String> iterator = labels.listIterator();
            guides(canvas, iterator);
        }
        graph(canvas);
        if (size == NORMAL) xAxisLabels(canvas);
    }

    void key(@NonNull Canvas c) {
        final List<Plot> plots = new ArrayList<>(data.lines.values());
        int count = plots.size();
        if (count <= 0) return;

        mTextPaint.setTextSize(labelTextSize);
        mTextPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 204));

        final int initialSave = c.save();
        c.translate(mRect.left, mRect.top);
        final int save = c.save();
        int i = 0;

        final float stroke = linePaint.getStrokeWidth();
        linePaint.setStrokeWidth(1.85f * stroke);
        do {
            //Translate to the next row but if its the first row in the respective column then we
            //shouldn't translate
            Plot plot = plots.get(i);
            if (i % 3 != 0) {
                c.translate(0, rowBounds.height());
            }

            //translate to the text space
            c.save();
            final float y = indicator.centerY();
            linePaint.setColor(plot.color);
            c.drawLine(indicator.left, y, indicator.right, y, linePaint);
            float labelBottom = indicator.bottom - 5;
            c.translate(indicator.width() + 30, labelBottom);
            c.drawText(plot.label, 0, 0, mTextPaint);
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

    void guides(Canvas c, @NonNull final ListIterator<String> iterator) {
        if (!iterator.hasNext()) return;
        //generatePath();
        //NOTE: -THE DRAW IS FROM BOTTOM UPWARDS
        gTextPaint.setTextSize(textSize);

        final int saveCount = c.save();
        c.translate(bounds.left, bounds.bottom);
        //This accounts for the space in between two consecutive guides
        final int count = labels.size() - 1;
        final float interval = bounds.height() / count;
        final float width = bounds.width();
        do {
            final int i = iterator.nextIndex();
            final String l = iterator.next();
            gTextPaint.getTextBounds(l, 0, l.length(), textRect);
            textRect.offset(-textRect.right - SPACE, 0);
            c.drawText(l, textRect.left, textRect.height() / 2f, gTextPaint);
            //insert a space of 8 btn the line and the text
            c.drawLine(0, 0, width, 0, i == 0 ? mXAxisPaint : mGuidesPaint);
            //translate relative to the last y point
            c.translate(0, -interval);
        } while (iterator.hasNext());

        c.restoreToCount(saveCount);
    }

    void graph(@NonNull Canvas c) {
        Collection<Plot> timeSeries = data.lines.values();
        if (timeSeries.size() <= 0) {
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

        for (final Plot l : timeSeries) {
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

        touched(c);

        c.restore();
    }

    void xAxisLabels(Canvas canvas) {
        if (xAxisLabels.isEmpty()) {
            return;
        }

        int save = canvas.save();
        canvas.translate(bounds.left, bounds.bottom);

        for (XAxisLabel xAxisLabel : xAxisLabels) {
            int internalSave = canvas.save();
            float x = xAxisLabel.coordinate.x;
            canvas.translate(x, 0);
            canvas.drawLine(0,
                    0,
                    0,
                    xLabelIndicatorHeight,
                    xAxisLabelsPaint);
            canvas.translate(0, xLabelIndicatorHeight);
            canvas.drawText(xAxisLabel.value,
                    xAxisLabel.textStartX,
                    xLabelIndicatorHeight + 3,
                    mXLabelTextPaint);
            canvas.restoreToCount(internalSave);
        }

        canvas.restoreToCount(save);
        mTextPaint.setTextSize(labelTextSize);
        mTextPaint.setColor(ColorUtils.setAlphaComponent(Color.BLACK, 204));

    }

    void touched(@NonNull Canvas c) {
        if (size != NORMAL) return;

        if (bests.isEmpty() || tLabels.isEmpty()) {
            return;
        }
        // Here we'll draw a rectangle, and on top of it some text showing where the user clicked
        // and a vertical line from the bottom of the triangle to the bottom x axis;
        final float x = drawingX(touchedVal.x);

        //The vertical indicator will start from the point
        c.drawLine(x, 0, x, tRectF.bottom, mXLine);

        for (Best b : bests) {
            this.cPaint.setColor(b.plot.color);
            c.drawCircle(x, drawingY(b.my), strokeWidth * 2.2f, this.cPaint);
        }

        //include shadow renderer but we'll draw a plain rect
        s.drawRoundRectWithShadow(c, rect, radius, elevation);

        tRect.offsetTo(0, 0);
        c.translate(tRectF.left, tRectF.top);

        int i = 0;
        final int size = tLabels.size();
        final float interval = tRectF.height() / tLabels.size();
        //final float textBottom = (tRectF.height()/tLabels.size() + 24)*.5f;
        while (i < size) {
            final String label = tLabels.get(i);
            cTextPaint.getTextBounds(label, 0, label.length(), tRect);
            tRect.offsetTo(0, 0);
            float diff = interval - tRect.height();
            diff /= 2;
            tRect.offset(0, (int) diff);
            c.translate(0, tRect.bottom);

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

    void clearTouched() {
        bests.clear();
        if (touched.get())
            touched.set(!touched.get());
    }

    public void initialise(Graph data) {
        this.data = data;
        invalidate();
        requestLayout();
    }

    private void computeBounds() {
        bounds.set(mRect);

        if (size == NORMAL) {
            bounds.top += columnHeight.get() * data.lines.size() + labelGraphSpace;
            bounds.bottom -= textSize + xLabelIndicatorHeight;
            //compensate for the transition that's made
            bounds.right -= ((float) getWidth() - bounds.right);
        }
    }

    private void update() {
        update(false);
    }

    private void update(boolean isInvalidation) {
        // this method updates the graph area bounds, labels and generates the required paths that'll
        // be drawn in onDraw method.
        // It should be called when the views size changes in (onSizeChanged(int, int, int, int) and when new line
        // is added to the GraphView i.e. when the view is invalidated.
        //
        if (data == null) return;
        if (!isInvalidation)
            computeBounds();
        labels();
        generatePath();
        xAxisLabels();
    }

    @Override
    public void invalidate() {
        if (dataListener != null) {
            dataListener.onChanged();
        }

        previousMax = maximum;
        float max = Collections.max(data.lines.values(), (o1, o2) -> Float.compare(o1.maxy, o2.maxy)).maxy;
        if (max != maximum)
            maximum = max;

        update(true);
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
        if (data.lines.size() <= 1) return;
        if (!data.lines.containsKey(key)) {
            Timber.e(new IllegalArgumentException(), "select: Unidentified Key: %s", key);
            return;
        }

        selectedKey.set(key);
        graph.set(data.lines.get(selectedKey.get()));

        invalidate();
        requestLayout();
    }

    @SuppressWarnings("unused")
    public List<String> keys() {
        return new ArrayList<>(data.lines.keySet());
    }

    /**
     * Generates a list of points closest to the touching point
     *
     * @param touchedX x-coordinate of the point touched on the screen
     */
    protected void bests(float touchedX) {
        clearTouched();
        touched.set(true);
        //Account for the translation made in starting to draw the graph which is bounds.left
        touchedX -= bounds.left;
//        touchedX /= multiplier.x;
        float estimatedActualX = actualX(touchedX);
        //but this new value doesn't put into consideration that the graph begins at the bounds.left

        List<Plot> plots = new ArrayList<>(data.lines.values());
        final Iterator<Plot> it = plots.iterator();
        if (!it.hasNext())
            return;

        String max = "";
        String date = "";

        tLabels.clear();
        touchedVal.set(0, 0);

        for (Plot plot : plots) {
            final Coordinate coordinate = findCoordinate(new ArrayList<>(plot.coordinates), estimatedActualX);

            if (date.isEmpty()) {
                date = date((int) coordinate.x);
                if (date.length() > max.length())
                    max = date;
            }

            final Best best = new Best(plot, coordinate.y, plots.size());
            bests.add(best);

            if (coordinate.y > touchedVal.y) {
                touchedVal.set(coordinate);
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
        final float height = count * (tRect.height());
        final float width = tRect.width();
        final float cy = weirdBounds.centerY();
        tRectF.top = drawingY(touchedVal.y) > cy ? cy : weirdBounds.top;
        tRectF.left = drawingX(touchedVal.x);
        tRectF.right = tRectF.left + width;
        tRectF.bottom = tRectF.top + height/*- 5*/;//5 is removing the space at the bottom...

        //center the rect along the x-axis to the touched val
        tRectF.offset(-tRectF.width() * .5f, 0);
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
        return (1 - COS_45) * radius;
    }

    protected String date(int value) {
        if (data == null) {
            return null;
        }
        Period period = data.period;
        Period period1 = period.get(value);
        if (period1 == null) {
            return null;
        }
        long time = period1.timeInMillis();
        switch (period.unit) {
            case DAY:
                return DateFormat.getTimeInstance(DateFormat.SHORT).format(time);
            case WEEK:
                return new SimpleDateFormat("EEEE", Locale.getDefault()).format(time);
            case MAX:
                return DateFormat.getDateInstance(DateFormat.SHORT).format(time);
            default:
                return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(time);
        }
    }

    public static Coordinate findCoordinate(TreeSet<Coordinate> coordinates, float x) {
        return findCoordinate(new ArrayList<>(coordinates), x);
    }

    /**
     * Finds the {@link Coordinate} with {@link Coordinate#x} closest to {@param x}
     *
     * @param x the value used in finding the best {@link Coordinate}
     * @return the {@link Coordinate} with x values closest to parameter x
     */
    public static Coordinate findCoordinate(List<Coordinate> coordinates, float x) {
        return findCoordinate(coordinates, 0, coordinates.size() - 1, x);
    }

    private static Coordinate findCoordinate(@NonNull List<Coordinate> coordinates, int min, int max, float x) {
        Coordinate minimum = coordinates.get(min);
        Coordinate maximum = coordinates.get(max);
        float x1 = minimum.x;
        float x2 = maximum.x;

        if (x <= x1) return minimum;
        else if (x >= x2) return maximum;

        if (max < min) return minimum;

        int count = max - min + 1;

        if (count == 1) return minimum;
        else if (count == 2) {
            float a = abs(coordinates.get(min).x - x), b = abs(coordinates.get(max).x - x);
            return a <= b ? coordinates.get(min) : coordinates.get(max);
        }

        final int mid = (max + min) >> 1;
        final Coordinate middle = coordinates.get(mid);

        if (x > middle.x) return findCoordinate(coordinates, mid, max, x);
        else if (x < middle.x) return findCoordinate(coordinates, min, mid, x);
        else return middle;
    }

    void generatePath() {
        multiplier.x = bounds.width() / (data.max.x - data.minx);
        //find the inverse of the y values
        multiplier.y = -(bounds.height() / data.max.y);
        for (final Plot o : data.lines.values()) {
            o.line.rewind();
            o.areaUnderGraph.rewind();
            final Iterator<Coordinate> iterator = o.coordinates.iterator();
            final Coordinate firstPoint = iterator.next();
            o.line.moveTo(drawingX(firstPoint.x), drawingY(firstPoint.y));

            while (iterator.hasNext()) {
                final Coordinate p = iterator.next();
                o.line.lineTo(drawingX(p.x), drawingY(p.y));
            }

            o.areaUnderGraph.addPath(o.line);
            o.areaUnderGraph.lineTo(bounds.width(), 0);
            o.areaUnderGraph.lineTo(drawingX(firstPoint.x), 0);
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

    public static float parse(String label) {
        return Float.parseFloat(label.replaceAll("[tbmk]", ""));
    }

    void labels() {
        if (size != NORMAL) return;

        if (Float.compare(previousMax, maximum) != 0) {
            labels.clear();
            final String f = "0";
            labels.add(f);
            final float max = parse(label(maximum));

            final int count = max % 3 == 0 ? 3 : 2;

            float i = 1;
            gTextPaint.getTextBounds(f, 0, f.length(), textRect);
            int width = textRect.width();
            while (i <= count) {
                final String label = label((i++ / count) * maximum);
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

    void xAxisLabels() {
        if (size != NORMAL) {
            return;
        }

        float width = bounds.width();

        Plot firstPlot = data.lines.values().toArray(new Plot[0])[0];
        XAxisLabel first = new XAxisLabel(findCoordinate(firstPlot.coordinates, actualX(.3f * width)));
        XAxisLabel last = new XAxisLabel(findCoordinate(firstPlot.coordinates, actualX(.7f * width)));
        xAxisLabels.clear();
        xAxisLabels.add(first);
        xAxisLabels.add(last);
    }

    protected float drawingX(float actualX) {
        return (actualX - data.minx) * multiplier.x;
    }

    protected float actualX(float drawingX) {
        return data.minx + (drawingX / multiplier.x);
    }

    protected float drawingY(float actualY) {
        return actualY * multiplier.y;
    }

    protected static class Best {
        public static final String splitter = "-";
        final String label;
        final float my;
        final boolean currency;
        final int color;
        final Plot plot;

        public Best(@NonNull Plot plot, float y, int count) {
            this.plot = plot;
            my = y;
            currency = plot.hasCurrency;
            color = plot.color;
            Locale l = Locale.getDefault();
            final BigDecimal val = BigDecimal.valueOf(my);
            label = (count == 1 ? "" : plot.label + ": " + splitter)
                    .concat(currency ?
                            format(l, "%s%,.2f", Currency.getInstance(l).getSymbol(), val) :
                            format(l, "%.1f", val)
                    );

        }
    }

    public static class Coordinate extends PointF implements Comparable<Coordinate> {

        public Coordinate() {
        }

        @SuppressWarnings("unused")
        public Coordinate(@NonNull Coordinate coordinate) {
            super(coordinate.x, coordinate.y);
        }

        public Coordinate(float x, float y) {
            super(x, y);
        }

        @Override
        public int compareTo(Coordinate o) {
            return Float.compare(this.x, o.x);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            return Float.compare(((PointF) o).x, x) == 0;
        }

        @Override
        public int hashCode() {
            return x != 0.0f ? Float.floatToIntBits(x) : 0;
        }

        public static final Creator<Coordinate> CREATOR = new Creator<Coordinate>() {

            @NonNull
            @Override
            public Coordinate createFromParcel(Parcel source) {
                Coordinate c = new Coordinate();
                c.readFromParcel(source);
                return c;
            }

            @NonNull
            @Contract(value = "_ -> new", pure = true)
            @Override
            public Coordinate[] newArray(int size) {
                return new Coordinate[size];
            }
        };
    }

    /**
     * A utility class that holds the data used in the graph view.
     * To instantiate this view, use the {@link Graph.Builder}.
     */
    public static class Graph {
        private final Period period;
        private final Map<String, Plot> lines;
        private final Coordinate max = new Coordinate(Float.MIN_VALUE, Float.MIN_VALUE);
        private final float minx;
        private final boolean isSingle;

        protected Graph(@NonNull Period period, @NonNull Map<String, Plot> lines) {
            this.period = period;
            this.lines = lines;
            isSingle = lines.size() == 1;
            float minx = Float.MAX_VALUE;
            for (final Plot plot : lines.values()) {
                minx = Math.min(minx, plot.minx);
                max.x = Math.max(plot.maxx, max.x);
                max.y = Math.max(max.y, plot.maxy);
            }

            this.minx = minx;
        }

        /**
         * Creates an instance of the {@link Graph}. To create an instance of this Builder, use the
         * {@link #setPeriod(Period)} method.
         */
        public static final class Builder {
            private final int[] colors = new int[]{GREEN, BLUE, RED, CYAN, MAGENTA, YELLOW, /*ORANGE*/Color.parseColor("#FFA500"), DKGRAY, GRAY};

            private Period period = Period.empty();
            private final Map<String, Plot> lines = new HashMap<>();

            private Builder() {
            }

            @NonNull
            public static Builder setPeriod(@NonNull Period period) {
                Builder builder = new Builder();
                builder.period = period;
                return builder;
            }

            public Builder set(@NonNull List<Plot> plots) {
                return set(plots.toArray(new Plot[0]));
            }

            @Contract("_ -> this")
            public Builder set(@NonNull Plot... plots) {
                this.lines.clear();

                boolean shouldAddColour = plots.length > 1;
                for (int i = 0, linesLength = plots.length; i < linesLength; i++) {
                    Plot plot = plots[i];
                    if (shouldAddColour) plot.color = colors[i];
                    this.lines.put(plot.label, plot);
                }

                return this;
            }

            @NonNull
            @Contract(" -> new")
            public Graph build() {
                if (lines.isEmpty()) {
                    throw new IllegalArgumentException("The graph cannot contain empty lines!");
                }
                return new Graph(period, lines);
            }
        }
    }

    /**
     * A utility class that holds values for a graph object that's used by the {@link GraphView}
     */
    public static class Plot implements Parcelable {

        private final SmoothingMode smoothingMode;
        private final boolean smoothenGraph;
        public boolean hasCurrency;//by default it's false
        public String label;
        private float maxx;
        private float minx = Float.MAX_VALUE;
        private float maxy;
        private final TreeSet<Coordinate> coordinates;
        private final int smoothingThreshold;

        private int color;

        private final Path line = new Path();
        private final Path areaUnderGraph = new Path();

        private Plot(String label,
                     boolean hasCurrency,
                     @NonNull TreeSet<Coordinate> coordinates,
                     int smoothingThreshold,
                     SmoothingMode smoothingMode,
                     boolean smoothenGraph) {
            this.hasCurrency = hasCurrency;
            this.label = label;
            this.coordinates = coordinates;
            this.smoothingThreshold = smoothingThreshold;
            this.smoothingMode = smoothingMode;
            this.smoothenGraph = smoothenGraph;

            smoothenGraph();
        }

        protected Plot(Parcel in) {
            smoothingMode = (SmoothingMode) in.readSerializable();
            smoothenGraph = in.readByte() != 0;
            hasCurrency = in.readByte() != 0;
            label = in.readString();
            maxx = in.readFloat();
            minx = in.readFloat();
            maxy = in.readFloat();
            ArrayList<Coordinate> coordinates = new ArrayList<>();
            in.readTypedList(coordinates, Coordinate.CREATOR);
            this.coordinates = new TreeSet<>();
            this.coordinates.addAll(coordinates);
            smoothingThreshold = in.readInt();
            color = in.readInt();
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeByte((byte) (smoothenGraph ? 1 : 0));
            dest.writeByte((byte) (hasCurrency ? 1 : 0));
            dest.writeString(label);
            dest.writeFloat(maxx);
            dest.writeFloat(minx);
            dest.writeFloat(maxy);
            dest.writeInt(smoothingThreshold);
            dest.writeTypedList(new ArrayList<>(coordinates));
            dest.writeInt(color);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Plot> CREATOR = new Creator<Plot>() {
            @Override
            public Plot createFromParcel(Parcel in) {
                return new Plot(in);
            }

            @Override
            public Plot[] newArray(int size) {
                return new Plot[size];
            }
        };

        private void smoothenGraph() {
            if (!smoothenGraph) return;
            smoothenGraph(coordinates, smoothingThreshold, smoothingMode);
        }

        public static void smoothenGraph(@NonNull TreeSet<Coordinate> cs, int smoothingThreshold, SmoothingMode smoothingMode) {
            int size = cs.size();
            if (size < smoothingThreshold) return;

            Coordinate[] coordinates = cs.toArray(new Coordinate[0]);
            Arrays.sort(coordinates);
            cs.clear();
            final int stepSize = smoothingThreshold / 2;
            int i = stepSize;

            int last = size - (stepSize + 1);
            while (i < last) {
                float y = 0f;

                switch (smoothingMode) {
                    case MODE_MEAN:
                        y = averageOfRange(coordinates, i - stepSize, i + stepSize);
                        break;
                    case MODE_MEDIAN:
                        y = medianOfRange(coordinates, i - stepSize, i + stepSize);
                        break;
                }

                cs.add(new Coordinate(i++, y));
            }
        }

        private static float averageOfRange(Coordinate[] arr, int start, int end) {
            Coordinate[] floats = Arrays.copyOfRange(arr, start, end);
            return sum(floats) / floats.length;
        }

        private static float medianOfRange(Coordinate[] arr, int start, int end) {
            Coordinate[] coordinates = Arrays.copyOfRange(arr, start, end + 1);
            Float[] floats = Arrays.stream(coordinates).map(c -> c.y).toArray(Float[]::new);
            Arrays.sort(floats);
            return floats[floats.length / 2];
        }

        @Contract(pure = true)
        private static float sum(@NonNull Coordinate[] arr) {
            float sum = 0f;
            for (Coordinate v : arr) {
                sum += v.y;
            }
            return sum;
        }

        public void add(float x, float y) {
            coordinates.add(new Coordinate(x, y));
        }

        @SuppressWarnings("unused")
        public void add(double x, double y) {
            add((float) x, (float) y);
        }

        //This method should be called once all the points are inserted
        private void minx() {
            minx = coordinates.first().x;
        }

        //This method should be called once all the points are inserted
        private void maxx() {
            maxx = coordinates.last().x;
        }

        //This method should be called once all the points are inserted
        private void maxy() {
            float max = Collections.max(coordinates, (o1, o2) -> Float.compare(o1.y, o2.y)).y;

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
                maxy = (float) (ceil(max / multiplicand) * multiplicand);
            } while ((max / maxy) < 0.8);

        }

        @SuppressWarnings("unused")
        public static class Builder {
            private String label;
            private int color = GREEN;
            private boolean hasCurrency = true;
            private int smoothingThreshold = 3;
            private boolean smoothenGraph = false;

            private final TreeSet<Coordinate> coordinates = new TreeSet<>();
            private SmoothingMode smoothingMode = SmoothingMode.MODE_MEAN;

            @SuppressWarnings("UnusedReturnValue")
            public Builder setLabel(String label) {
                this.label = label;
                return this;
            }

            public Builder setColor(int percent) {
                color = percent < 0 ? Color.RED : GREEN;
                return this;
            }

            public Builder setHasCurrency() {
                return setHasCurrency(true);
            }

            public Builder setHasNoCurrency() {
                return setHasCurrency(false);
            }

            public Builder setHasCurrency(boolean hasCurrency) {
                this.hasCurrency = hasCurrency;
                return this;
            }

            public Builder setSmoothenGraph(boolean smoothenGraph) {
                this.smoothenGraph = smoothenGraph;
                return this;
            }

            public Builder setSmoothingThreshold(int smoothingThreshold) {
                if (smoothingThreshold % 2 == 0) smoothingThreshold++;
                this.smoothingThreshold = smoothingThreshold;
                return this;
            }

            public Builder setSmoothingMode(SmoothingMode smoothingMode) {
                this.smoothingMode = smoothingMode;
                return this;
            }

            private Builder add(Coordinate p) {
                this.coordinates.add(p);
                return this;
            }

            public Builder add(double x, double y) {
                return add((float) x, (float) y);
            }

            public Builder add(float x, float y) {
                return add(new Coordinate(x, y));
            }

            public Builder add(Coordinate... coordinates) {
                Collections.addAll(this.coordinates, coordinates);
                return this;
            }

            public Builder set(Coordinate... coordinates) {
                this.coordinates.clear();
                Collections.addAll(this.coordinates, coordinates);
                return this;
            }

            public Plot build() {
                final Plot plot = new Plot(label,
                        hasCurrency,
                        coordinates,
                        smoothingThreshold,
                        smoothingMode,
                        smoothenGraph);

                plot.color = color;
                plot.minx();
                plot.maxx();
                plot.maxy();
                return plot;
            }
        }

        public enum SmoothingMode {
            MODE_MEAN,
            MODE_MEDIAN,
        }
    }

    class XAxisLabel {
        private final Coordinate coordinate = new Coordinate();
        private final String value;
        private final float textStartX;

        XAxisLabel(@NonNull Coordinate coordinate) {
            this.coordinate.x = coordinate.x;
            this.coordinate.y = coordinate.y;
            Date date = data.period.time(round(this.coordinate.x));
            this.coordinate.x = drawingX(this.coordinate.x);
            /*this.value = DateTimeFormatter.ofPattern("hh:mm").format(date)*/
            if (data.period.unit == ChronoUnit.DAY)
                this.value = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
            else this.value = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date);
            textStartX();
            textStartX = r.left;
        }

        RectF r = new RectF();

        void textStartX() {
            Rect textBounds = new Rect();
            mXLabelTextPaint.getTextBounds(value, 0, value.length(), textBounds);
            r.set(textBounds);
//            r.offset(-r.centerX(), 0);
            r.offset(0, r.bottom);
        }

    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            bests(e.getX());
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public void onShowPress(@NonNull MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onContextClick(@NonNull MotionEvent e) {
            return super.onContextClick(e);
        }

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
            bests(e.getX());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            //If we are to use the animation method instead of the live-touch method
            // v = d / t
            // => t = d/v
            // use t as the time btn the two animations
            // where d = diff btn the end-point from the start-point
            bests(e2.getX());
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
         * Notifies that the data in this view has changed in-case the user wishes to do some
         * updating with the data from this class
         */
        void onChanged();
    }
}
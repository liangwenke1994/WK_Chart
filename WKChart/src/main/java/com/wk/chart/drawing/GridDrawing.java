
package com.wk.chart.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;

import com.wk.chart.compat.DisplayTypeUtils;
import com.wk.chart.compat.FontStyle;
import com.wk.chart.compat.Utils;
import com.wk.chart.compat.attribute.BaseAttribute;
import com.wk.chart.drawing.base.AbsDrawing;
import com.wk.chart.entry.AbsEntry;
import com.wk.chart.enumeration.GridLineStyle;
import com.wk.chart.module.base.AbsModule;
import com.wk.chart.render.CandleRender;

/**
 * >Grid轴绘制组件
 * <p>GridDrawing</p>
 */

public class GridDrawing extends AbsDrawing<CandleRender, AbsModule<AbsEntry>> {
    private static final String TAG = "GridDrawing";
    private BaseAttribute attribute;//配置文件
    private final TextPaint gridLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG); // Grid 轴标签的画笔
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // Grid 轴网格线画笔
    private float[] point;
    private String[] label;
    private final Rect rect = new Rect(); //用于测量文字的实际占用区域

    private float gridLabelY;//gridLabel的Y轴坐标

    private int position;//label下标

    @Override
    public void onInit(CandleRender render, AbsModule<AbsEntry> chartModule) {
        super.onInit(render, chartModule);
        attribute = render.getAttribute();

        gridLabelPaint.setTypeface(FontStyle.typeFace);
        gridLabelPaint.setTextSize(attribute.labelSize);
        gridLabelPaint.setColor(attribute.labelColor);
        gridLabelPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(attribute.lineWidth);
        gridPaint.setColor(attribute.lineColor);

        int size = attribute.gridCount + 3;
        point = new float[size * 2];
        label = new String[size];

        Utils.measureTextArea(gridLabelPaint, rect);
    }

    @Override
    public float[] onInitMargin() {
        margin[3] = attribute.gridLabelMarginTop
                + attribute.gridLabelMarginBottom
                + rect.height()
                + attribute.borderWidth
                + (attribute.gridLineStyle == GridLineStyle.GRADUATION ? attribute.gridLineLength : 0);
        return margin;
    }

    @Override
    public void readyComputation(Canvas canvas, int begin, int end, float[] extremum) {
        position = 0;
    }

    @Override
    public void onComputation(int begin, int end, int current, float[] extremum) {
        //每隔特定个 entry，记录一个 X 轴label的位置信息和值
        if (current == 0 || current == render.getAdapter().getLastPosition() || current % render.getInterval() != 0) {
            return;
        }
        point[position * 2] = current + 0.5f;
        label[position] = DisplayTypeUtils.format(render.getAdapter().getItem(current).getTime(),
                render.getAdapter().getTimeType());
        position++;
    }

    @Override
    public void onDraw(Canvas canvas, int begin, int end, float[] extremum) {
        render.mapPoints(render.getMainModule().getMatrix(), point);
        for (int i = 0; i < label.length; i++) {
            if (TextUtils.isEmpty(label[i])) {
                continue;
            }
            int xIndex = i * 2;
            canvas.drawText(label[i], point[xIndex], gridLabelY, gridLabelPaint);
            // 跳过超出显示区域的线
            if (attribute.gridLineStyle == GridLineStyle.NONE || point[xIndex] < viewRect.left || point[xIndex] > viewRect.right) {
                continue;
            }
            float x = point[xIndex];
            float y = render.getBottomModule().getRect().bottom;
            if (attribute.gridLineStyle == GridLineStyle.LINE) {
                canvas.drawLines(render.buildViewLRCoordinates(x, x), gridPaint);
            } else if (attribute.gridLineStyle == GridLineStyle.GRADUATION_INSIDE) {
                canvas.drawLine(x, y, x, y - attribute.gridLineLength, gridPaint);
            } else if (attribute.gridLineStyle == GridLineStyle.GRADUATION) {
                canvas.drawLine(x, y, x, y + attribute.gridLineLength + attribute.borderWidth, gridPaint);
            }
        }
    }

    @Override
    public void drawOver(Canvas canvas) {
    }

    @Override
    public void onViewChange() {
        gridLabelY = viewRect.bottom - attribute.borderWidth - attribute.gridLabelMarginBottom;
    }
}

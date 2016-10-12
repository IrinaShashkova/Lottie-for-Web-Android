package com.airbnb.lottie.utils;

import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.FloatRange;

import com.airbnb.lottie.model.LottieCubicCurveData;
import com.airbnb.lottie.model.LottieShapeData;

public class MiscUtils {

    public static PointF addPoints(PointF p1, PointF p2) {
        return new PointF(p1.x + p2.x, p1.y + p2.y);
    }

    public static void getPathFromData(LottieShapeData shapeData, Path outPath) {
        outPath.reset();
        PointF initialPoint = shapeData.getInitialPoint();
        outPath.moveTo(initialPoint.x, initialPoint.y);
        for (int i = 0; i < shapeData.getCurves().size(); i++) {
            LottieCubicCurveData curveData = shapeData.getCurves().get(i);
            outPath.cubicTo(curveData.getControlPoint1().x, curveData.getControlPoint1().y,
                    curveData.getControlPoint2().x, curveData.getControlPoint2().y,
                    curveData.getVertex().x, curveData.getVertex().y);
        }
    }

    public static float lerp(float a, float b, @FloatRange(from = 0f, to = 1f) float percentage) {
        return a + percentage * (b - a);
    }

    public static int lerp(int a, int b, @FloatRange(from = 0f, to = 1f) float percentage) {
        return (int) (a + percentage * (b - a));
    }
}

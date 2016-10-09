package com.airbnb.lotte.animation;

import android.graphics.Path;
import android.graphics.PointF;

import com.airbnb.lotte.model.LotteCubicCurveData;
import com.airbnb.lotte.model.LotteShapeData;
import com.airbnb.lotte.utils.LotteKeyframeAnimation;
import com.airbnb.lotte.utils.LotteShapeKeyframeAnimation;
import com.airbnb.lotte.utils.MiscUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.airbnb.lotte.utils.MiscUtils.addPoints;

@SuppressWarnings({"EmptyCatchBlock"})
public class LotteAnimatableShapeValue extends BaseLotteAnimatableValue<LotteShapeData, Path> {
    private final boolean closed;

    public LotteAnimatableShapeValue(JSONObject json, int frameRate, long compDuration, boolean closed) {
        super(null, frameRate, compDuration);
        this.closed = closed;
        init(json);
    }

    @Override
    protected LotteShapeData valueFromObject(Object object) throws JSONException {
        JSONObject pointsData = null;
        if (object instanceof JSONArray) {
            try {
                Object firstObject = ((JSONArray) object).get(0);
                if (firstObject instanceof JSONObject && ((JSONObject) firstObject).has("v")) {
                    pointsData = (JSONObject) firstObject;
                }
            } catch (JSONException e) {
                throw new IllegalStateException("Unable to get shape. " + object);
            }
        } else if (object instanceof JSONObject && ((JSONObject) object).has("v")) {
            pointsData = (JSONObject) object;
        }

        if (pointsData == null) {
            return null;
        }

        JSONArray pointsArray = null;
        JSONArray inTangents = null;
        JSONArray outTangents = null;
        try {
            pointsArray = pointsData.getJSONArray("v");
            inTangents = pointsData.getJSONArray("i");
            outTangents = pointsData.getJSONArray("o");
        } catch (JSONException e) { }

        if (pointsArray == null || inTangents == null || outTangents == null ||
                pointsArray.length() != inTangents.length() || pointsArray.length() != outTangents.length()) {
            throw new IllegalStateException("Unable to process points array or tangents. " + pointsData);
        }

        LotteShapeData shape = new LotteShapeData();

        PointF vertex = vertexAtIndex(0, pointsArray);
        shape.setInitialPoint(vertex);

        for (int i = 1; i < pointsArray.length(); i++) {
            vertex = vertexAtIndex(i, pointsArray);
            PointF previousVertex = vertexAtIndex(i - 1, pointsArray);
            PointF cp1 = vertexAtIndex(i - 1, outTangents);
            PointF cp2 = vertexAtIndex(i, inTangents);

            PointF shapeCp1 = addPoints(previousVertex, cp1);
            PointF shapeCp2 = addPoints(vertex, cp2);
            shape.addCurve(new LotteCubicCurveData(shapeCp1, shapeCp2, vertex));
        }

        if (closed) {
            vertex = vertexAtIndex(0, pointsArray);
            PointF previousVertex = vertexAtIndex(pointsArray.length() - 1, pointsArray);
            PointF cp1 = vertexAtIndex(pointsArray.length() - 1, outTangents);
            PointF cp2 = vertexAtIndex(0, inTangents);

            PointF shapeCp1 = addPoints(previousVertex, cp1);
            PointF shapeCp2 = addPoints(vertex, cp2);
            shape.addCurve(new LotteCubicCurveData(shapeCp1, shapeCp2, vertex));
        }

        return shape;

    }


    private PointF vertexAtIndex(int idx, JSONArray points) {
        if (idx >= points.length()) {
            throw new IllegalArgumentException("Invalid index " + idx + ". There are only " + points.length() + " points.");
        }

        try {
            JSONArray pointArray = points.getJSONArray(idx);
            Object x = pointArray.get(0);
            Object y = pointArray.get(1);
            return new PointF(
                    x instanceof Double ? new Float((Double) x) : (int) x,
                    y instanceof Double ? new Float((Double) y) : (int) y);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to get point.", e);
        }
    }

    @Override
    public LotteKeyframeAnimation animationForKeyPath() {
        LotteShapeKeyframeAnimation animation = new LotteShapeKeyframeAnimation(duration, compDuration, keyTimes, keyValues, interpolators);
        // TODO: use this
//        animation.setStartDelay(delay);
        animation.addUpdateListener(new LotteKeyframeAnimation.AnimationListener<Path>() {
            @Override
            public void onValueChanged(Path progress) {
                observable.setValue(progress);
            }
        });
        return animation;
    }

    @Override
    Path convertType(LotteShapeData shapeData) {
        if (observable.getValue() == null) {
            observable.setValue(new Path());
        }
        MiscUtils.getPathFromData(shapeData, observable.getValue());
        return observable.getValue();
    }
}

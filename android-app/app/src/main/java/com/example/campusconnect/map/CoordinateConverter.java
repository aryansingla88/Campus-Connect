package com.example.campusconnect.map;

import android.graphics.PointF;
import android.util.Log;

public class CoordinateConverter {

    private double a, b, c;
    private double d, e, f;
    private static final float OFFSET_X = 75f;
    private static final float OFFSET_Y = 71f;
    public CoordinateConverter(
            double lat1, double lng1, float x1, float y1,
            double lat2, double lng2, float x2, float y2,
            double lat3, double lng3, float x3, float y3
    ) {
        // Solve affine transform using 3 points

        double[][] A = {
                {lng1, lat1, 1},
                {lng2, lat2, 1},
                {lng3, lat3, 1}
        };

        double[] X = {x1, x2, x3};
        double[] Y = {y1, y2, y3};

        double[][] invA = invert3x3(A);

        double[] resX = multiply(invA, X);
        double[] resY = multiply(invA, Y);

        a = resX[0];
        b = resX[1];
        c = resX[2];

        d = resY[0];
        e = resY[1];
        f = resY[2];
    }

    public PointF toPixel(double lat, double lng) {

        float x = (float) (a * lng + b * lat + c);
        float y = (float) (d * lng + e * lat + f);

        // ✅ offset correction
        x += OFFSET_X;
        y += OFFSET_Y;

        return new PointF(x, y);
    }

    //yatharth(aryan)
    public double[] toLatLng(float x, float y) {

        // remove offset
        x -= OFFSET_X;
        y -= OFFSET_Y;

        double det = a * e - b * d;

        if (Math.abs(det) < 1e-6) {
            throw new IllegalStateException("Invalid transform (det = 0)");
        }

        double lng = (e * (x - c) - b * (y - f)) / det;
        double lat = (-d * (x - c) + a * (y - f)) / det;

        return new double[]{lat, lng};
    }
    //yatharth(aryan)

    // -------- matrix utils --------

    private double[][] invert3x3(double[][] m) {
        double det =
                m[0][0]*(m[1][1]*m[2][2] - m[1][2]*m[2][1]) -
                        m[0][1]*(m[1][0]*m[2][2] - m[1][2]*m[2][0]) +
                        m[0][2]*(m[1][0]*m[2][1] - m[1][1]*m[2][0]);

        double[][] inv = new double[3][3];

        inv[0][0] = (m[1][1]*m[2][2] - m[1][2]*m[2][1]) / det;
        inv[0][1] = (m[0][2]*m[2][1] - m[0][1]*m[2][2]) / det;
        inv[0][2] = (m[0][1]*m[1][2] - m[0][2]*m[1][1]) / det;

        inv[1][0] = (m[1][2]*m[2][0] - m[1][0]*m[2][2]) / det;
        inv[1][1] = (m[0][0]*m[2][2] - m[0][2]*m[2][0]) / det;
        inv[1][2] = (m[0][2]*m[1][0] - m[0][0]*m[1][2]) / det;

        inv[2][0] = (m[1][0]*m[2][1] - m[1][1]*m[2][0]) / det;
        inv[2][1] = (m[0][1]*m[2][0] - m[0][0]*m[2][1]) / det;
        inv[2][2] = (m[0][0]*m[1][1] - m[0][1]*m[1][0]) / det;

        return inv;
    }

    private double[] multiply(double[][] m, double[] v) {
        double[] res = new double[3];
        for (int i = 0; i < 3; i++) {
            res[i] = m[i][0]*v[0] + m[i][1]*v[1] + m[i][2]*v[2];
        }
        return res;
    }
}
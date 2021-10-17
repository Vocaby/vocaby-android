package com.vocaby.app.utils;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.TextView;

public class PaintUtil {
    public static void setGradient(TextView view, int topColor, int bottomColor) {
        Shader myShader = new LinearGradient(
                0, 0, 0, 40,
                topColor, bottomColor,
                Shader.TileMode.CLAMP );
        view.getPaint().setShader( myShader );
    }
}

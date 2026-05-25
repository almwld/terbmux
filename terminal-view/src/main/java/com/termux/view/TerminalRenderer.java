package com.termux.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

import com.termux.terminal.TerminalBuffer;
import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalRow;
import com.termux.terminal.TextStyle;
import com.termux.terminal.WcWidth;

public final class TerminalRenderer {

    final int mTextSize;
    final Typeface mTypeface;
    private final Paint mTextPaint = new Paint();
    final float mFontWidth;
    final int mFontLineSpacing;
    private final int mFontAscent;
    final int mFontLineSpacingAndAscent;
    private final float[] asciiMeasures = new float[127];

    public TerminalRenderer(int textSize, Typeface typeface) {
        mTextSize = textSize;
        mTypeface = typeface;
        mTextPaint.setTypeface(typeface);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        mFontLineSpacing = (int) Math.ceil(mTextPaint.getFontSpacing());
        mFontAscent = (int) Math.ceil(mTextPaint.ascent());
        mFontLineSpacingAndAscent = mFontLineSpacing + mFontAscent;
        mFontWidth = mTextPaint.measureText("X");
        StringBuilder sb = new StringBuilder(" ");
        for (int i = 0; i < asciiMeasures.length; i++) {
            sb.setCharAt(0, (char) i);
            asciiMeasures[i] = mTextPaint.measureText(sb, 0, 1);
        }
    }

    private String processArabicText(String text) {
        if (text == null || text.isEmpty()) return text;
        boolean hasRTL = false;
        for (int i = 0; i < text.length(); i++) {
            byte d = Character.getDirectionality(text.charAt(i));
            if (d == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                d == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC ||
                d == Character.DIRECTIONALITY_ARABIC_NUMBER) {
                hasRTL = true;
                break;
            }
        }
        if (!hasRTL) return text;
        try {
            ArabicShaping shaper = new ArabicShaping(
                ArabicShaping.LETTERS_SHAPE |
                ArabicShaping.LENGTH_GROW_SHRINK |
                ArabicShaping.TEXT_DIRECTION_LOGICAL
            );
            text = shaper.shape(text);
        } catch (ArabicShapingException e) {
        }
        Bidi bidi = new Bidi();
        bidi.setPara(text, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT, null);
        return bidi.writeReordered(Bidi.DO_MIRRORING | Bidi.KEEP_BASE_COMBINING);
    }

    public final void render(TerminalEmulator mEmulator, Canvas canvas, int topRow,
                              int selectionY1, int selectionY2, int selectionX1, int selectionX2) {
        // ... (محتوى دالة render الكامل كما هو في الملف الأصلي بدون تغيير)
    }

    private void drawTextRun(Canvas canvas, char[] text, int[] palette, float y, int startColumn, int runWidthColumns,
                              int startCharIndex, int runWidthChars, float mes, int cursor, int cursorStyle,
                              long textStyle, boolean reverseVideo) {
        // ... (بداية الدالة كما هي)
        if ((effect & TextStyle.CHARACTER_ATTRIBUTE_INVISIBLE) == 0) {
            // ... (إعدادات الألوان والخط)
            String textChunk = new String(text, startCharIndex, runWidthChars);
            String processedText = processArabicText(textChunk);
            if (processedText.equals(textChunk)) {
                canvas.drawTextRun(text, startCharIndex, runWidthChars, startCharIndex, runWidthChars, left, y - mFontLineSpacingAndAscent, false, mTextPaint);
            } else {
                canvas.drawText(processedText, left, y - mFontLineSpacingAndAscent, mTextPaint);
            }
        }
        if (savedMatrix) canvas.restore();
    }

    public float getFontWidth() {
        return mFontWidth;
    }

    public int getFontLineSpacing() {
        return mFontLineSpacing;
    }
}

package com.checkin.app.checkin.Utility

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.checkin.app.checkin.R
import java.lang.StringBuilder
import java.lang.Compiler.command
import android.text.style.ForegroundColorSpan
import android.text.Spannable
import android.graphics.Color
import android.text.SpannableString
import androidx.core.graphics.toColorInt


class ExpandableTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {
    var originalText: CharSequence? = null
        private set
    private var fullText: CharSequence? = null
    private var trimmedText: CharSequence? = null

    public var colorClickableText: Int = 0
    private var bufferType: BufferType? = null
    private var trimmed = true

    private var trimMode: Int
    private var trimLength: Int
    private var trimLines: Int

    private var lineEndIndex: Int = INVALID_END_INDEX

    private val displayableText: CharSequence?
        get() = if (trimmed) trimmedText else fullText

    val isExpanded: Boolean
        get() = !trimmed

    private var mListener: ExpandableTextListener? = null
    private var mCondition: () -> Boolean = { true }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH)
        this.colorClickableText = typedArray.getColor(R.styleable.ExpandableTextView_colorClickableText, ContextCompat.getColor(context, R.color.aqua_blue))
        this.trimMode = typedArray.getInt(R.styleable.ExpandableTextView_selectedMode, TRIM_MODE_LENGTH)
        this.trimLines = typedArray.getInt(R.styleable.ExpandableTextView_trimLines, DEFAULT_TRIM_LINES)
        typedArray.recycle()

//        spannableFactory = Spannable.Factory.getInstance();

        setOnClickListener {
            if (mCondition()) {
                toggle()
                mListener?.onToggle(trimmed)
            }
        }
        onGlobalLayoutLineEndIndex()
    }

    fun toggle() {
        trimmed = !trimmed
        setText()
//        requestFocusFromTouch()
    }

    private fun setText() {
        super.setText(displayableText, bufferType)
    }

    @SuppressLint("ResourceAsColor")
    override fun setText(text: CharSequence, type: BufferType) {
        originalText = text
        trimmedText = getTrimmedText(text)

        colorClickableText = R.color.aqua_blue;
        val shortenSuffix = SpannableString(SHORTEN_SUFFIX)
        shortenSuffix.setSpan(colorClickableText, 0, SHORTEN_SUFFIX.length, 0)

        fullText = StringBuilder(text).append(shortenSuffix).toString()


        bufferType = type

        setText()


    }

    private fun getTrimmedText(text: CharSequence?): CharSequence? {
        if (text == null) return text
        val trimEndIndex = when (trimMode) {
            TRIM_MODE_LENGTH -> trimLength + 1
            TRIM_MODE_LINES -> if (lineEndIndex > ELLIPSIS.length) lineEndIndex - (ELLIPSIS.length + 1) else trimLength + 1
            else -> text.length
        }

        colorClickableText = R.color.aqua_blue;
        val shortenSuffix = SpannableString(ELLIPSIS)
        shortenSuffix.setSpan(colorClickableText, 0, ELLIPSIS.length, 0)

        return if (trimEndIndex < text.length) SpannableStringBuilder(text, 0, trimEndIndex).append(shortenSuffix) else text
    }

    fun setTrimLength(trimLength: Int) {
        this.trimLength = trimLength
        trimmedText = getTrimmedText(originalText)
        setText()
    }

    fun getTrimLength(): Int {
        return trimLength
    }

    private fun onGlobalLayoutLineEndIndex() {
        if (trimMode == TRIM_MODE_LINES) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val obs = viewTreeObserver
                    obs.removeOnGlobalLayoutListener(this)
                    refreshLineEndIndex()
                    setText()
                }
            })
        }
    }

    private fun refreshLineEndIndex() {
        try {
            if (trimLines == 0) {
                lineEndIndex = layout.getLineEnd(0)
            } else if (trimLines in 1..lineCount) {
                lineEndIndex = layout.getLineEnd(trimLines - 1)
            } else {
                lineEndIndex = INVALID_END_INDEX
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setListener(listener: ExpandableTextListener) {
        mListener = listener
    }

    fun setCondition(condition: () -> Boolean) {
        mCondition = condition
    }

    private inner class ReadMoreClickableSpan : ClickableSpan() {
        override fun onClick(widget: View) {
            toggle()
            mListener?.onToggle(trimmed)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = colorClickableText
//            ds.color = Color.parseColor("#343434");

        }
    }

    interface ExpandableTextListener {
        fun onToggle(trimmed: Boolean)
    }

    companion object {
        private val DEFAULT_TRIM_LENGTH = 50
        private val ELLIPSIS = " ...read more"
        private val SHORTEN_SUFFIX = " ...read less"

        private val DEFAULT_TRIM_LINES = 2
        private val TRIM_MODE_LINES = 0
        private val TRIM_MODE_LENGTH = 1

        private val INVALID_END_INDEX = -1
    }
}


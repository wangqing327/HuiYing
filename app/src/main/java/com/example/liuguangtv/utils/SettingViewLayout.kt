package com.example.liuguangtv.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.liuguangtv.R

@SuppressLint("Recycle", "CustomViewStyleable", "InflateParams")
class SettingViewLayout : FrameLayout {
    private var checkedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private lateinit var title: TextView
    private lateinit var state: TextView
    private lateinit var checkBox: CheckBox
    private lateinit var stateOn: String
    private lateinit var stateOff: String

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) :
            this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        createView(attributeSet)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createView(attributeSet: AttributeSet?) {
        val view: View = View.inflate(context, R.layout.settingview, this)
        title = view.findViewById(R.id.settingviewtitle)
        state = view.findViewById(R.id.settingviewState)
        checkBox = view.findViewById(R.id.settingviewcheckBox)
        checkBox.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                state.text = stateOn
            } else {
                state.text = stateOff
            }
            checkedChangeListener?.onCheckedChanged(p0, p1)
        }

        if (attributeSet != null) {
            val typedArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.SettingViewLayout)
            //View.VISIBLE = 0, View.INVISIBLE = 1
            var visible = typedArray.getInt(R.styleable.SettingViewLayout_TitleVisibility, 0)
            title.visibility = visible

            visible = typedArray.getInt(R.styleable.SettingViewLayout_StateVisibility, 0)
            state.visibility = visible

            var titleText = typedArray.getString(R.styleable.SettingViewLayout_titleText)
            if (titleText == null) {
                titleText = "SettingViewLayout"
            }
            title.text = titleText


            val titleSize =
                typedArray.getDimension(R.styleable.SettingViewLayout_titleTextSize, 16f)
            title.textSize = titleSize

            var color: Int = ContextCompat.getColor(context, R.color.view_default)

            color = typedArray.getColor(R.styleable.SettingViewLayout_TitleColor, color)
            title.setTextColor(color)

            color = ContextCompat.getColor(context, R.color.view_default)
            color = typedArray.getColor(R.styleable.SettingViewLayout_stateTextColor, color)
            state.setTextColor(color)

            val stateSize =
                typedArray.getDimension(R.styleable.SettingViewLayout_stateTextSize, 12f)
            state.textSize = stateSize


            val tag = typedArray.getString(R.styleable.SettingViewLayout_buttonTag)
            checkBox.tag = tag

            var drawable = typedArray.getDrawable(R.styleable.SettingViewLayout_buttonDrawable)
            if (drawable == null) {
                drawable = resources.getDrawable(R.drawable.checkboxbackground,null)
            }
            checkBox.buttonDrawable = drawable
            //stateText赋值
            try {
                var text = typedArray.getString(R.styleable.SettingViewLayout_stateText)
                if (text == null) {
                    text = context.resources.getString(R.string.state1)
                }
                val array = text.split("|")
                stateOn = array[0]
                stateOff = array[1]
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val isChecked =
                typedArray.getBoolean(R.styleable.SettingViewLayout_checked, false)
            //如果为不选中状态，将直接赋值，因为不会执行选中状态被改变事件
            if (!isChecked) {
                state.text = stateOff
            }
            checkBox.isChecked = isChecked

            typedArray.recycle()
        }
    }

    fun getTitle(): CharSequence {
        return title.text
    }

    fun setTitle(title: CharSequence) {
        this.title.text = title
    }

    fun setTitle(resId: Int) {
        title.setText(resId)
    }

    fun setTitleTextSize(size: Float) {
        title.textSize = size
    }

    /**
     * @param visible 推荐View.VISIBLE或View.INVISIBLE
     */
    fun setTitleVisible(visible: Int) {
        title.visibility = visible
    }

    /**
     * @param visible 推荐View.VISIBLE或View.INVISIBLE
     */
    fun setStateVisible(visible: Int) {
        state.visibility = visible
    }

    fun getStateText(): CharSequence {
        return state.text
    }

    fun setStateTextSize(size: Float) {
        state.textSize = size
    }

    /**
     * @param state 可分解为数组的文本，示例为"打开|关闭"、"on|off"
     */
    fun setStateText(state: CharSequence) {
        try {
            val array = state.split("|")
            stateOn = array[0]
            stateOff = array[1]
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setStateText(resId: Int) {
        setStateText(context.resources.getString(resId))
    }

    fun getButtonSelected(): Boolean {
        return checkBox.isChecked
    }

    fun setButtonSelected(checked: Boolean) {
        checkBox.isChecked = checked
    }

    fun setButtonDrawable(drawable: Drawable) {
        checkBox.buttonDrawable = drawable
    }

    fun setButtonDrawable(resId: Int) {
        checkBox.setButtonDrawable(resId)
    }

    fun setOnCheckedChanged(listener: CompoundButton.OnCheckedChangeListener) {
        checkedChangeListener = listener
    }
}
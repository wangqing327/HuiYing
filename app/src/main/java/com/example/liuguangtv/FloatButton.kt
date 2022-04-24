package com.example.liuguangtv

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.liuguangtv.databinding.FloatbuttonBinding
import com.example.liuguangtv.settingactivitys.DownLoadFileInfo


class FloatButton : ConstraintLayout,View.OnClickListener {
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr) {
        initView(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet?)
            : this(context, attributeSet, 0)

    constructor(context: Context) : this(context, null)
    private lateinit var parent:ConstraintLayout
    /**
     * 图标
     */
    private lateinit var cardView1: CardView

    /**
     * 数目容器
     */
    private lateinit var cardView2: CardView

    /**
     * 视频数量显示
     */
    private lateinit var textCount: TextView

    /**
     * 视频数量统计
     */
    private var videoCount = 0

    private var videoLinks: MutableMap<Int, DownLoadFileInfo> = mutableMapOf()

    private lateinit var imgGif: ImageView

    private var clickCallBack: OnClickListener? = null

    private val anim = AnimationUtils.loadAnimation(context, R.anim.animdownload2)

    private fun initView(attributeSet: AttributeSet?) {
        val view: View = View.inflate(context, R.layout.floatbutton, this)
        val binding = FloatbuttonBinding.bind(view)
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
        layoutParams = lp
        cardView1 = binding.cardView1
        cardView2 = binding.cardView2
        imgGif = binding.imgDownload
        textCount = binding.textCount
        parent = cardView1.parent as ConstraintLayout
        cardView1.setOnClickListener(this::onClick)
        if (attributeSet != null) {
            val typedArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.FloatButton)

            val visible = typedArray.getInt(R.styleable.FloatButton_android_visibility, 0)
            parent.visibility = visible

            var color = ContextCompat.getColor(context, R.color.web_default)
            color = typedArray.getColor(R.styleable.FloatButton_firstDivBackground, color)
            cardView1.setCardBackgroundColor(color)

            color = ContextCompat.getColor(context, R.color.wcred2)
            color = typedArray.getColor(R.styleable.FloatButton_secondDivBackground, color)
            cardView2.setCardBackgroundColor(color)

            val textSize = typedArray.getDimension(R.styleable.FloatButton_showVideoTextSize, 10f)
            textCount.textSize = textSize

            val count = typedArray.getInt(R.styleable.FloatButton_videoText, 0)
            if (count > 99) {
                textCount.text = "99+"
            }

            if (count > 0) {
                cardView2.visibility = VISIBLE
            }

            typedArray.recycle()
        }
    }

    fun hide() {
        parent.visibility = View.GONE
    }

    fun show() {
        parent.visibility = View.VISIBLE
    }

    fun isShow(): Boolean {
        return visibility == View.VISIBLE
    }

    /**
     * 获取视频数量
     */
    fun getVideoCount(): Int {
        return videoCount
    }

    /**
     * 返回所有视频地址
     */
    fun getVideoLinks(): MutableList<DownLoadFileInfo> {
        val list = mutableListOf<DownLoadFileInfo>()
        videoLinks.forEach {
            list.add(it.value)
        }
        return list
    }

    /**
     * 设置显示的视频数量值
     * @param size 值
     * @param isPlayAnim 是否播放动画,默认true
     */
    private fun setVideCount(size: Int, isPlayAnim: Boolean = true) {
        videoCount = size
        if (size == 0) {
            visibility = GONE
        } else if (visibility != VISIBLE) {
            visibility = VISIBLE
        }
        if (size > 0 && cardView2.visibility != VISIBLE) {
            cardView2.visibility = View.VISIBLE
        }
        if (videoCount > 99) {
            textCount.text = "99+"
        } else {
            textCount.text = size.toString()
        }
        if (isPlayAnim)
            starAnim()
    }

    /**
     * 初始化值，将数量设为0 链接设为空，隐藏小红点
     */
    fun initSet() {
        setVideCount(0, false)
        videoLinks = mutableMapOf()
        cardView2.visibility = INVISIBLE
        visibility = GONE
    }

    /**
     * 开启动画
     */
    private fun starAnim() {
        imgGif.startAnimation(anim)
    }

    /**
     * video数量自增1
     */
    fun videoCountAdd(videoLink: DownLoadFileInfo) {
        videoLinks.forEach {
            //有重复的链接，只显示一个
            if (videoLink.link == it.value.link) {
                return
            }
        }
        setVideCount(++videoCount)
        videoLinks[videoCount] = videoLink
    }

    fun setClickCalled(callback: OnClickListener) {
        clickCallBack = callback
    }

/*    private var lastTime = 0L
    private var x = 0
    private var y = 0

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { view, event ->
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastTime = System.currentTimeMillis()
                val lp = layoutParams as ConstraintLayout.LayoutParams
               *//* x = lp.
                y = lp.topMargin*//*
                x = event.rawX.toInt()
                y = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val NowX = event.rawX.toInt()
                val NowY = event.rawY.toInt()
                val MovedX = NowX - x
                val MovedY = NowY - y
                x = NowX
                y = NowY
                val lp = layoutParams as ConstraintLayout.LayoutParams
                lp.setMargins(
                    setXhefa(lp.marginStart + MovedX),
                    setYhefa(lp.topMargin + MovedY),
                    0,
                    0
                )
                layoutParams = lp
                // requestLayout()
            }

            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - lastTime > 300) {
                    val cancelEvent = MotionEvent.obtain(event)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL or (event.actionIndex
                            shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                    view.onTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                    true
                }
            }

        }
        invalidate()
        false
    }


    *//**
     * 限定x坐标不能大于屏幕宽度，且不能小于0
     *//*
    private fun setXhefa(dx: Int): Int {
        var dx = dx
        if (dx < 0) {
            dx = 0
        } else {
            if (dx > Utils.getCurrentWidth(context) - width) {
                dx = Utils.getCurrentWidth(context) - width
            }
        }
        return dx
    }


    *//**
     * 限定y坐标不能大于屏幕宽度，且不能小于0
     *//*
    private fun setYhefa(dy: Int): Int {
        var dy = dy
        if (dy < 0) {
            dy = 0
        } else {
            val s: Int = (context as AppCompatActivity).window.decorView.height
            if (dy > s - height) {
                dy = s - height
            }
        }
        return dy
    }*/


    override fun onClick(p0: View?) {
        clickCallBack?.onClick(p0)
    }
}

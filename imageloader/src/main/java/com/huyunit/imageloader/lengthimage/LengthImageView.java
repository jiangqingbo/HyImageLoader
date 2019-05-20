package com.huyunit.imageloader.lengthimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;

/**
 * @author: bobo
 * @createtime: 2019/5/17 5:37 PM
 * @emial: jqbo84@163.com
 */
public class LengthImageView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener {
    private Context mContext;
    private Rect mRect;
    private BitmapFactory.Options mOptions;
    private GestureDetector mGestureDetector;
    private final Scroller mScroller;
    private BitmapRegionDecoder mDecoder;
    private Bitmap bitmap;

    private int mImageWidth;
    private int mImageHeight;

    private int mViewWidth;
    private int mViewHeight;
    //缩放比例
    private float mScale;

    public LengthImageView(Context context) {
        this(context, null, 0);
    }

    public LengthImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LengthImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context.getApplicationContext();
        //指定要加载的区域
        mRect = new Rect();
        //需要复用
        mOptions = new BitmapFactory.Options();
        //手势识别类
        mGestureDetector = new GestureDetector(context, this);
        //设置onTouchListener
        setOnTouchListener(this);
        //滑动帮助
        mScroller = new Scroller(context);
    }

    public void setImagePath(String imagePath) {
        setImageFile(new File(imagePath));
    }

    public void setImageFile(File file) {
        if (!file.exists()) {
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            setImage(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setImageAssetsString(String assetsString) {
        if (TextUtils.isEmpty(assetsString)) {
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = mContext.getAssets().open(assetsString);
            setImageStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setImageStream(InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        setImage(inputStream);
    }

    /**
     * 由用户set一张图片
     *
     * @param is
     */
    public void setImage(InputStream is) {
        //先读取原图片的信息   高，宽
        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, mOptions);
        mImageWidth = mOptions.outWidth;
        mImageHeight = mOptions.outHeight;
        //开启复用
        mOptions.inMutable = true;
        //设置格式成RGB_565
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mOptions.inJustDecodeBounds = false;

        //创建一个区域解码器
        try {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //重新绘制view
        requestLayout();
    }

    /**
     * 在测量的时候把我们需要的内存区域获取到  存入到mRect中
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取测量的view的大小
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();

        //确定要加载的图片的区域
        mRect.left = 0;
        mRect.top = 0;
        mRect.right = mImageWidth;
        //获取一个缩放因子
        mScale = mViewWidth / (float) mImageWidth;
        //高度就根据缩放比进行获取
        mRect.bottom = (int) (mViewHeight / mScale);
    }

    /**
     * 画出内容
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //如果解码器拿不到，表示没有设置过要显示的图片
        if (null == mDecoder) {
            return;
        }
        //复用上一张bitmap
        mOptions.inBitmap = bitmap;
        //解码指定的区域
        bitmap = mDecoder.decodeRegion(mRect, mOptions);
        //把得到的矩阵大小的内存进行缩放  得到view的大小
        Matrix matrix = new Matrix();
        matrix.setScale(mScale, mScale);
        //画出来
        canvas.drawBitmap(bitmap, matrix, null);
    }

    /**
     * 把滑动事件交给手势处理
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //交给手势处理
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 手按下的回调
     *
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        //如果移动还没有停止，强制停止
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        //继续接收后续事件
        return true;
    }

    /**
     * @param e1        手指按下去的事件，开始获取坐标
     * @param e2        当前手势事件， 获取当前坐标
     * @param distanceX x方向(即左右)移动时的距离
     * @param distanceY y方向(即上下)移动时的距离
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //上下移动的时候，需要改变显示区域   改mRect
        mRect.offset(0, (int) distanceY);
        //处理移动时已经移到了两个顶端的问题
        if (mRect.bottom > mImageHeight) {
            mRect.bottom = mImageHeight;
            mRect.top = mImageHeight - (int) (mViewHeight / mScale);
        }
        if (mRect.top < 0) {
            mRect.top = 0;
            mRect.bottom = (int) (mViewHeight / mScale);
        }
        invalidate();
        return false;
    }

    /**
     * 处理惯性问题
     *
     * @param e1
     * @param e2
     * @param velocityX 每秒移动的x点
     * @param velocityY
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //做计算
        mScroller.fling(0, mRect.top,
                0, (int) -velocityY,
                0, 0,
                0, mImageHeight - (int) (mViewHeight / mScale));
        return false;
    }

    /**
     * 使用上一个接口的计算结果
     */
    @Override
    public void computeScroll() {
        if (mScroller.isFinished()) {
            return;
        }
        //true 表示当前滑动还没有结束
        if (mScroller.computeScrollOffset()) {
            mRect.top = mScroller.getCurrY();
            mRect.bottom = mRect.top + (int) (mViewHeight / mScale);
            invalidate();
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }
}

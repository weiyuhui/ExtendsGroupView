package com.example.administrator.extendsgroupview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Gabby on 2018/12/10.
 */

public class HorizontalView extends ViewGroup {

    private int lastInterceptX;
    private int lastInterceptY;
    private int lastX;
    private int lastY;
    int currentIndex = 0;
    int childWidth = 0;
    private Scroller scroller;
    private VelocityTracker tracker;

    public HorizontalView(Context context){
        super(context);
        init();
    }

    private void init() {
        scroller = new Scroller(getContext());
        tracker = VelocityTracker.obtain();
    }

    public HorizontalView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public HorizontalView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heigthSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        if(getChildCount() == 0){
            setMeasuredDimension(0, 0);
        }else if(widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            View childOne = getChildAt(0);
            int childWith = childOne.getMeasuredWidth();
            int childHeight = childOne.getMeasuredHeight();
            setMeasuredDimension(childWith*getChildCount(), childHeight);
        }else if(widthMode == MeasureSpec.AT_MOST){
            int childWith = getChildAt(0).getMeasuredWidth();
            setMeasuredDimension(childWith*getChildCount(), heigthSize);
        }else if(heightMode == MeasureSpec.AT_MOST){
            int childHeight = getChildAt(0).getMeasuredHeight();
            setMeasuredDimension(widthSize, childHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int left = 0;
        View child;
        for(int i=0; i<childCount; i++){
            child = getChildAt(i);
            if(child.getVisibility() != View.GONE){
                int width = child.getMeasuredWidth();
                childWidth = width;
                child.layout(left, 0, left+width, child.getMeasuredHeight());
                left += width;
            }
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                //如果scroller没有执行完毕,则打断
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastInterceptX;
                int deltaY = y - lastInterceptY;
                //水平方向距离长  MOVE中返回true一次,后续的MOVE和UP都不会收到此请求
                if (Math.abs(deltaX) - Math.abs(deltaY) > 0) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        lastX = x;
        lastY = y;
        lastInterceptX = x;
        lastInterceptY = y;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        boolean intercept = false;
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!scroller.isFinished()){
                    scroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastInterceptX;
                scrollBy(-deltaX, 0);
                break;
            case MotionEvent.ACTION_UP:
                int distance = getScrollX() - currentIndex*childWidth;
                if(Math.abs(distance) > childWidth/2){
                    if(distance > 0){
                        currentIndex++;
                    }else{
                        currentIndex--;
                    }
                }else{
                    tracker.computeCurrentVelocity(1000);
                    float xV = tracker.getXVelocity();
                    if(Math.abs(xV) > 50){
                        if(xV > 0){
                            currentIndex--;
                        }else{
                            currentIndex++;
                        }
                    }
                }
                currentIndex = currentIndex<0 ? 0: currentIndex>getChildCount()-1 ? getChildCount()-1: currentIndex;
                smoothScrollTo(currentIndex * childWidth, 0);
                tracker.clear();
                break;
        }
        lastX = x;
        lastY = y;
        return true;
    }

    @Override
    public void computeScroll(){
        super.computeScroll();
        if(scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    private void smoothScrollTo(int destX, int destY) {
        scroller.startScroll(getScrollX(), getScrollY(), destX-getScrollX(), destY - getScrollY(), 1000);
        invalidate();
    }


}

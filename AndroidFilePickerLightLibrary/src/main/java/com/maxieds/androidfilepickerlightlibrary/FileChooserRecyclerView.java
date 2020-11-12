/*
        This program (the AndroidFilePickerLight library) is free software written by
        Maxie Dion Schmidt: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        The complete license provided with source distributions of this library is
        available at the following link:
        https://github.com/maxieds/AndroidFilePickerLight
*/

package com.maxieds.androidfilepickerlightlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class FileChooserRecyclerView extends RecyclerView {

    private static final String LOGTAG = FileChooserRecyclerView.class.getSimpleName();

    public FileChooserRecyclerView(Context layoutCtx) {
        super(layoutCtx);
        setupRecyclerViewLayout();
    }

    public FileChooserRecyclerView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    public FileChooserRecyclerView(Context context, AttributeSet attrSet, int defStyle) {
        super(context, attrSet, defStyle);
    }

    public void setupRecyclerViewLayout() {

        setHasFixedSize(true);
        setItemViewCacheSize(0);
        setNestedScrollingEnabled(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(layoutParams);
        FileChooserRecyclerView.LayoutManager rvLayoutManager = new FileChooserRecyclerView.LayoutManager(getContext());
        setLayoutManager((FileChooserRecyclerView.LayoutManager) rvLayoutManager);
        addItemDecoration(new FileChooserRecyclerView.CustomDividerItemDecoration(R.drawable.rview_file_item_divider));
        //addOnItemTouchListener(...);

        /*
         * This code fragment gets called when the RecyclerView layout is first displayed:
         */
        final FileChooserRecyclerView recyclerView = this;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayFragments displayFragmentsCtx = DisplayFragments.getInstance();
                if(!displayFragmentsCtx.viewportCapacityMesaured && recyclerView.getLayoutManager().getChildCount() != 0) {
                    displayFragmentsCtx.fileItemDisplayHeight = recyclerView.getLayoutManager().getChildAt(0).getMeasuredHeight();
                    if (displayFragmentsCtx.fileItemDisplayHeight > 0) {
                        displayFragmentsCtx.resetViewportMaxFilesCount(recyclerView);
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    // ??? TODO: ???
    /*
    @Override
    public boolean fling(int velocityX, int velocityY) {
        final LinearLayoutManager rvLayoutManager = (LinearLayoutManager) getLayoutManager();
        if (rvLayoutManager != null && rvLayoutManager instanceof FileChooserRecyclerView.LayoutManager) {
            super.smoothScrollToPosition(((FileChooserRecyclerView.LayoutManager) getLayoutManager()).getPositionForVelocity(velocityX, velocityY));
            return true;
        }
        return super.fling(velocityX, velocityY);
    }
    */

    // ??? TODO: ???
    /*
    @Override
    public boolean onTouchEvent(MotionEvent mevent) {
        final boolean returnStatus = super.onTouchEvent(mevent);
        final FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) getLayoutManager();
        if (rvLayoutManager instanceof FileChooserRecyclerView.LayoutManager && getScrollState() == SCROLL_STATE_IDLE &&
                (mevent.getAction() == MotionEvent.ACTION_UP || mevent.getAction() == MotionEvent.ACTION_CANCEL)) {
            //smoothScrollToPosition(((FileChooserRecyclerView.LayoutManager) rvLayoutManager).getScrollPositionToFixSnapState());
            smoothScrollToPosition(((FileChooserRecyclerView.LayoutManager) rvLayoutManager).findFirstVisibleItemPosition());
        }
        return returnStatus;
    }
    */

    public interface RecyclerViewSlidingContextWindow {

        void setWeightBufferSize(int size);

        int getActiveCountToBalanceTop();
        int getActiveTopBufferSize();
        int getActiveCountToBalanceBottom();
        int getActiveBottomBufferSize();

        int getLayoutVisibleDisplaySize();
        int getLayoutFirstVisibleItemIndex();
        int getLayoutLastVisibleItemIndex();
        int getActiveLayoutItemsCount();

    }

    /* See: https://developer.android.com/reference/androidx/recyclerview/widget/LinearSnapHelper */
    public static class LayoutManager extends LinearLayoutManager {

        private static LayoutManager localStaticInst = null;
        public static LayoutManager getInstance() { return localStaticInst; }

        private int nextPositionOffsetDiff;
        public int getNextPositionOffset() { return nextPositionOffsetDiff; }
        public void setNextPositionOffset(int newOffset) { nextPositionOffsetDiff = newOffset; }

        public LayoutManager(Context layoutCtx) {
            super(layoutCtx);
            setOrientation(LinearLayoutManager.VERTICAL);
            setAutoMeasureEnabled(true);
            setReverseLayout(false);
            setStackFromEnd(true);
            setSmoothScrollbarEnabled(true);
            localStaticInst = this;
            nextPositionOffsetDiff = 1;
        }

        @Override
        public boolean isAutoMeasureEnabled() {
            return true;
        }

        public void setInsertAtFrontMode() {
            setReverseLayout(true);
            setStackFromEnd(false);
        }

        public void setAppendToBackMode() {
            setReverseLayout(false);
            setStackFromEnd(true);
        }

        public void restoreDefaultMode() {
            setReverseLayout(false);
            setStackFromEnd(true);
        }

        public int getPositionForVelocity(int velocityX, int velocityY) {
            if (getChildCount() == 0) {
                return 0;
            }
            if (getOrientation() == HORIZONTAL) {
                return getPositionForVelocity(
                        velocityX,
                        getChildAt(0).getLeft(),
                        getChildAt(0).getWidth(),
                        getPosition(getChildAt(0))
                );
            }
            else if (getOrientation() == VERTICAL) {
                return getPositionForVelocity(
                        velocityY,
                        getChildAt(0).getTop(),
                        getChildAt(0).getHeight(),
                        getPosition(getChildAt(0))
                );
            }
            else {
                return 0;
            }
        }

        // We want it to move when flung and be responsive, but keep a constant rate of movement:
        public static final float SCROLLER_MILLISECONDS_PER_INCH = 45f; // larger values slow it down

        private int getPositionForVelocity(int velocity, int scrollPos, int childSize, int curPos) {
            final double distDelta = ViewConfiguration.getScrollFriction() * velocity * SCROLLER_MILLISECONDS_PER_INCH;
            final double nextScrollPos = scrollPos + (velocity > 0 ? distDelta : -distDelta);
            if (velocity < 0) {
                return (int) Math.max(0, curPos + nextScrollPos / childSize);
            } else {
                return (int) (curPos + (nextScrollPos / childSize) + getNextPositionOffset());
            }
        }

        /*@Override
        public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {

            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                private float distanceInPixels = 250;
                private float scrollDuration = 1.65f;

                protected int getHorizontalSnapPreference() {
                    return SNAP_TO_START;
                }

                protected int getVerticalSnapPreference() {
                    // This will scroll at the topmost position (which is the behavior we want):
                    return SNAP_TO_START;
                }

                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    return SCROLLER_MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                }

                //@Override
                //protected int calculateTimeForScrolling(int deltaX) {
                //    float alpha = (float) deltaX / distanceInPixels;
                //    return (int) (scrollDuration * alpha);
                //}

            };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);

        }

        public int getScrollPositionToFixSnapState() {
            if (getChildCount() == 0) {
                return 0;
            }
            final View child = getChildAt(0);
            final int childPosIndex = getPosition(child);
            if (getOrientation() == HORIZONTAL && Math.abs(child.getLeft()) > child.getMeasuredWidth() / 2) {
                // Scrolled first view is more than halfway offscreen
                return childPosIndex + 1;
            } else if (getOrientation() == VERTICAL && Math.abs(child.getTop()) > child.getMeasuredWidth() / 2) {
                // Scrolled first view is more than halfway offscreen
                return childPosIndex + 1;
            }
            // Keep it where it is located for now:
            return childPosIndex;
        }*/

    }

    public static class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] DIVIDER_DEFAULT_ATTRS = new int[]{
                android.R.attr.listDivider,
                android.R.attr.verticalDivider,
                android.R.attr.horizontalDivider
        };
        private Drawable listingsDivider;

        public static final int LIST_DIVIDER_STYLE_INDEX = 0;
        public static final int DEFAULT_DIVIDER_STYLE_INDEX = 1;

        public CustomDividerItemDecoration(Context ctx, int dividerTypeIndex, boolean dividerTypeIsVertical) {
            final TypedArray styledDefaultAttributes = ctx.obtainStyledAttributes(DIVIDER_DEFAULT_ATTRS);
            if(dividerTypeIndex != LIST_DIVIDER_STYLE_INDEX) {
                dividerTypeIndex = DEFAULT_DIVIDER_STYLE_INDEX + (dividerTypeIsVertical ? 0 : 1);
            }
            listingsDivider = styledDefaultAttributes.getDrawable(dividerTypeIndex);
            styledDefaultAttributes.recycle();
        }

        public CustomDividerItemDecoration(int resId) {
            listingsDivider = DisplayUtils.getDrawableFromResource(resId);
        }

        public static void setMarginAdjustments(int leftAdjust, int topAdjust, int rightAdjust, int bottomAdjust) {
            MARGIN_RIGHT_ADJUST = rightAdjust;
            MARGIN_LEFT_ADJUST = leftAdjust;
            MARGIN_TOP_ADJUST = topAdjust;
            MARGIN_BOTTOM_ADJUST = bottomAdjust;
        }

        private static int MARGIN_RIGHT_ADJUST = 35;
        private static int MARGIN_LEFT_ADJUST = 35;
        private static int MARGIN_TOP_ADJUST = 0;
        private static int MARGIN_BOTTOM_ADJUST = 0;

        @Override
        public void onDraw(Canvas displayCanvas, RecyclerView parentContainerView, RecyclerView.State rvState) {
            int leftMargin = parentContainerView.getPaddingLeft() + MARGIN_LEFT_ADJUST;
            int rightMargin = parentContainerView.getWidth() - parentContainerView.getPaddingRight() - MARGIN_RIGHT_ADJUST;
            for (int i = 0; i < parentContainerView.getChildCount(); i++) {
                View childView = parentContainerView.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                int topMargin = childView.getBottom() + params.bottomMargin + MARGIN_TOP_ADJUST;
                int bottomMargin = topMargin + listingsDivider.getIntrinsicHeight() + MARGIN_BOTTOM_ADJUST;
                listingsDivider.setBounds(leftMargin, topMargin, rightMargin, bottomMargin);
                listingsDivider.draw(displayCanvas);
            }
        }

    }

}
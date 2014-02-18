/**
 * Should use setBitmap init the Background First;
 */

package bos.consoar.smartcircularmenu;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class SmartCircularMenu extends View {
	private int isVisible;
	//bitmap res
	private Bitmap originalBitmap, mRoundHeadBitmap, mRoundNormalHeadBitmap,
			mRoundHeadClickedBitmap, mExpandBitmap;
	private int mAlphaNum = 168;
	// Static Access Variables
	public static final int VERTICAL_RIGHT = 0;
	public static final int VERTICAL_LEFT = 1;
	public static final int HORIZONTAL_TOP = 2;
	public static final int HORIZONTAL_BOTTOM = 3;
	// Private non-shared variables
	private boolean isBottom = false;
	private boolean isMenuVisible = false;
	private boolean isMenuTogglePressed = false;
	private boolean isMenuItemPressed = false;
	private boolean isExpand = false;
	private String mPressedMenuItemID = null;
	private int mDiameter = 0;
	private float mRadius = 0.0f;
	private int mStartAngle = 0;
	private RectF mMenuRect;
	private RectF mMenuCenterButtonRect;
	private Paint mRadialMenuPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mAnimationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mLogoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);;
	private Point mViewAnchorPoints;
	private HashMap<String, SmartCircularMenuItem> mMenuItems = new HashMap<String, SmartCircularMenuItem>();
	// Variables that can be user defined
	private float mShadowRadius = 5 * getResources().getDisplayMetrics().density;
	private boolean isShowMenuText = false;
	private boolean isAnimation = true;
	private int mOrientation = HORIZONTAL_BOTTOM;
	private int centerRadialColor = Color.WHITE;
	private int mShadowColor = 0x80888888;
	private String openMenuText = "Home";
	private String closeMenuText = "Close";
	private String centerMenuText = openMenuText;
	private int mToggleMenuTextColor = Color.DKGRAY;
	private float textSize = 12 * getResources().getDisplayMetrics().density;
	private int mOpenButtonScaleFactor = 4;

	// Animation need
	private float mScale, mScaleLast;
	private static final int DURATION_EXPAND = 200;
	private static final int DURATION_DELAY = 150;
	private int DURATION_DISAPLY = 200;
	private Matrix mScaleMatrix = new Matrix();
	private boolean mDelayCenterAnimating, mExpandAnimating,
			mExpandCancelAnimating, isDelayCenterAnimatingCancel;
	private AnimatorSet ExpandAnimationSet, ExpandCancelAnimationSet,
			DelayCenterAnimationSet;

	public interface onCircularClickListener {
		public void onCircularButtonClick(View v);
	}

	/**
	 * @param onClickListener
	 */
	public void setOnCircularClickListener(
			onCircularClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	/**
	 * 
	 */
	private onCircularClickListener onClickListener;

	public SmartCircularMenu(Context context) {
		super(context);
		init();
	}

	public SmartCircularMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SmartCircularMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setBitmap(Bitmap originalBitmap) {
		this.originalBitmap = originalBitmap;
		this.mRoundHeadBitmap = getRoundedCornerBitmap(originalBitmap);
		this.mRoundNormalHeadBitmap = mRoundHeadBitmap;
		this.mRoundHeadClickedBitmap = getRoundedCornerBitmap(drawBlackBitmap(originalBitmap));
	}

	public void setScale(float mScale) {
		this.mScale = mScale;
		invalidate();
	}

	public float getScale() {
		return mScale;
	}

	public Bitmap getmExpandBitmap() {
		return mExpandBitmap;
	}

	public void setmExpandBitmap(Bitmap mExpandBitmap) {
		this.mExpandBitmap = mExpandBitmap;
	}

	private void init() {
		isBottom = mOrientation == HORIZONTAL_BOTTOM ? true : false;
		mRadialMenuPaint.setAntiAlias(true);
		mRadialMenuPaint.setDither(true);
		mRadialMenuPaint.setFilterBitmap(true);
		mRadialMenuPaint.setTextSize(textSize);
		mRadialMenuPaint.setColor(Color.WHITE);
		mRadialMenuPaint.setAlpha(mAlphaNum);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setDither(true);
		mCirclePaint.setFilterBitmap(true);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setColor(Color.DKGRAY);
		mCirclePaint.setShadowLayer(1.0f, 0f, 0f, Color.BLACK);
		mAnimationPaint.setAntiAlias(true);
		mAnimationPaint.setDither(true);
		mAnimationPaint.setFilterBitmap(true);
		mLogoPaint.setAntiAlias(true);
		mLogoPaint.setDither(true);
		mLogoPaint.setFilterBitmap(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredHeight = measureHeight(heightMeasureSpec);
		int measuredWidth = measureWidth(widthMeasureSpec);
		if (measuredWidth < measuredHeight)
			setMeasuredDimension(measuredWidth, measuredWidth);
		else
			setMeasuredDimension(measuredHeight, measuredHeight);
	}

	private int measureHeight(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		// Default size if no limits are specified.
		int result = 200;
		if (specMode == MeasureSpec.AT_MOST) {
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			// If your control can fit within these bounds return that value.
			result = specSize;
		}
		return result;
	}

	private int measureWidth(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		// Default size if no limits are specified.
		int result = 200;
		if (specMode == MeasureSpec.AT_MOST) {
			result = specSize;
		} else if (specMode == MeasureSpec.EXACTLY) {
			// If your control can fit within these bounds return that value.
			result = specSize;
		}
		return result;
	}

	public void startExpandCancelAnimation() {
		if (mExpandCancelAnimating) {
			return;
		}
		// System.out.println("startExpandCancelAnimation mScale " +
		// mScaleLast);
		ObjectAnimator expandCancelScale = ObjectAnimator.ofFloat(this,
				"scale", mScaleLast, 0.0f);
		expandCancelScale.setDuration(DURATION_EXPAND);
		ObjectAnimator postExpandCancelScale = ObjectAnimator.ofFloat(this,
				"scale", 0.0f);
		postExpandCancelScale.setDuration(0);
		ExpandCancelAnimationSet = new AnimatorSet();
		ExpandCancelAnimationSet.setInterpolator(new LinearInterpolator());
		ExpandCancelAnimationSet.play(postExpandCancelScale).after(
				expandCancelScale);
		ExpandCancelAnimationSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub
				isMenuVisible = true;
				isExpand = true;
				mExpandCancelAnimating = true;
				// System.out.println("ExpandCancelAnimationStart");
			}

			@Override
			public void onAnimationRepeat(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub
				mExpandCancelAnimating = false;
				isMenuItemPressed = false;
				isExpand = false;
				// System.out.println("ExpandCancelAnimationEnd");
			}

			@Override
			public void onAnimationCancel(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub
				mExpandCancelAnimating = false;
				isMenuItemPressed = false;
				isExpand = false;
				// System.out.println("ExpandCancelAnimationCancel");
			}
		});
		ExpandCancelAnimationSet.start();
	}

	public void startExpandAnimation() {
		if (mExpandAnimating) {
			return;
		}
		// System.out.println("startExpandAnimation mScaleLast " + mScaleLast);
		ObjectAnimator expandScale = ObjectAnimator.ofFloat(this, "scale",
				mScaleLast, 1.0f);
		expandScale.setDuration(DURATION_EXPAND);
		ObjectAnimator postExpandScale = ObjectAnimator.ofFloat(this, "scale",
				1.0f);
		postExpandScale.setDuration(0);
		ExpandAnimationSet = new AnimatorSet();
		ExpandAnimationSet.setInterpolator(new LinearInterpolator());
		ExpandAnimationSet.play(postExpandScale).after(expandScale);
		ExpandAnimationSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub
				mExpandAnimating = true;
				// 震动提示
				Vibrator vibrator;
				vibrator = (Vibrator) getContext().getSystemService(
						Context.VIBRATOR_SERVICE);
				long[] pattern = { 100, 0, 0, 40 }; // 停止 开启 停止 开启
				vibrator.vibrate(pattern, -1);
				// System.out.println("ExpandAnimationStart");
			}

			@Override
			public void onAnimationRepeat(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub
				mExpandAnimating = false;
				// System.out.println("ExpandAnimationEnd");
			}

			@Override
			public void onAnimationCancel(
					com.nineoldandroids.animation.Animator arg0) {
				// TODO Auto-generated method stub
				mExpandAnimating = false;
				// System.out.println("ExpandAnimationCancel");
			}
		});
		ExpandAnimationSet.start();
	}

	public void startDelayCenterAnimation() {
		if (mDelayCenterAnimating) {
			return;
		}
		ObjectAnimator expandCenterScale = ObjectAnimator.ofFloat(this,
				"scale", 0.0f, 1.0f);
		expandCenterScale.setDuration(DURATION_DELAY);
		ObjectAnimator postExpandCenterScale = ObjectAnimator.ofFloat(this,
				"scale", 1.0f);
		postExpandCenterScale.setDuration(0);
		if (DelayCenterAnimationSet == null) {
			DelayCenterAnimationSet = new AnimatorSet();
			DelayCenterAnimationSet.play(postExpandCenterScale).after(
					expandCenterScale);
			DelayCenterAnimationSet.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(
						com.nineoldandroids.animation.Animator arg0) {
					// TODO Auto-generated method stub
					isExpand = false;
					isDelayCenterAnimatingCancel = false;
					// System.out.println("onDelayCenterAnimationStart");
				}

				@Override
				public void onAnimationRepeat(
						com.nineoldandroids.animation.Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(
						com.nineoldandroids.animation.Animator arg0) {
					// TODO Auto-generated method stub
					isExpand = true;
					if (isAnimation)
						startExpandAnimation();
					else {
						mExpandAnimating = false;
					}
					// System.out.println("onDelayCenterAnimationEnd");
				}

				@Override
				public void onAnimationCancel(
						com.nineoldandroids.animation.Animator arg0) {
					// TODO Auto-generated method stub
					isDelayCenterAnimatingCancel = true;
					isMenuTogglePressed = false;
					isExpand = false;
					// System.out.println("onDelayCenterAnimationCancel");

				}
			});
		}
		DelayCenterAnimationSet.start();
	}

	private void drawNormalBitmap(Canvas canvas) {
		// System.out.println("drawNormalBitmap");
		if (mOrientation == HORIZONTAL_BOTTOM) {
			// mRadialMenuPaint.setShadowLayer(mShadowRadius,
			// 0.0f, 0.0f, Color.TRANSPARENT);
			// System.out.println("Draw normal menu HORIZONTAL_BOTTOM");
			if (mMenuItems.size() > 0) {
				if (mRoundHeadBitmap == null)
					canvas.drawArc(mMenuRect, mStartAngle, 360, true,
							mRadialMenuPaint);
				float mStart = 180;
				float mStartCenter = mStartAngle;
				// Get the sweep angles based on the number of menu
				// items
				float mSweep = 180 / mMenuItems.size();
				for (SmartCircularMenuItem item : mMenuItems.values()) {
					mRadialMenuPaint.setColor(item.getBackgroundColor());
					mRadialMenuPaint.setAlpha(mAlphaNum);
					item.setMenuPath(mMenuCenterButtonRect, mMenuRect, mStart,
							mStartCenter, mSweep, mRadius, mViewAnchorPoints,
							isBottom);
					canvas.drawPath(item.getMenuPath(), mRadialMenuPaint);
					if (isShowMenuText) {
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, Color.TRANSPARENT);
						mRadialMenuPaint.setColor(item.getTextColor());
						canvas.drawTextOnPath(item.getText(),
								item.getMenuPath(), 5, textSize,
								mRadialMenuPaint);
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, mShadowColor);
					}
					item.getIcon().draw(canvas);
					mStart += mSweep;
					mStartCenter += mSweep * 1.5;
				}
				mRadialMenuPaint.setStyle(Style.FILL);
			}
		} else {
			if (mMenuItems.size() > 0) {
				if (mRoundHeadBitmap == null)
					canvas.drawArc(mMenuRect, mStartAngle, 360, true,
							mRadialMenuPaint);
				float mStart = mStartAngle;
				float mStartCenter = mStartAngle;
				// Get the sweep angles based on the number of menu
				// items
				float mSweep = 180 / mMenuItems.size();
				for (SmartCircularMenuItem item : mMenuItems.values()) {
					mRadialMenuPaint.setColor(item.getBackgroundColor());
					mRadialMenuPaint.setAlpha(mAlphaNum);
					item.setMenuPath(mMenuCenterButtonRect, mMenuRect, mStart,
							mStartCenter, mSweep, mRadius, mViewAnchorPoints,
							isBottom);
					canvas.drawPath(item.getMenuPath(), mRadialMenuPaint);
					if (isShowMenuText) {
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, Color.TRANSPARENT);
						mRadialMenuPaint.setColor(item.getTextColor());
						canvas.drawTextOnPath(item.getText(),
								item.getMenuPath(), 5, textSize,
								mRadialMenuPaint);
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, mShadowColor);
					}
					item.getIcon().draw(canvas);
					mStart += mSweep;
					mStartCenter = mStart;
				}
				mRadialMenuPaint.setStyle(Style.FILL);
			}
		}
	}

	private void initExpandBitmap() {
		mExpandBitmap = Bitmap.createBitmap(getWidth(), getWidth(),
				Bitmap.Config.ARGB_8888);
		Canvas cv = new Canvas(mExpandBitmap);
		if (mOrientation == HORIZONTAL_BOTTOM) {
			// mRadialMenuPaint.setShadowLayer(mShadowRadius,
			// 0.0f, 0.0f, Color.TRANSPARENT);
			// System.out.println("Draw normal menu HORIZONTAL_BOTTOM");
			if (mMenuItems.size() > 0) {
				if (mRoundHeadBitmap == null)
					cv.drawArc(mMenuRect, mStartAngle, 360, true,
							mRadialMenuPaint);
				float mStart = 180;
				float mStartCenter = mStartAngle;
				// Get the sweep angles based on the number of menu
				// items
				float mSweep = 180 / mMenuItems.size();
				for (SmartCircularMenuItem item : mMenuItems.values()) {
					mRadialMenuPaint.setColor(item.getBackgroundColor());
					mRadialMenuPaint.setAlpha(mAlphaNum);
					item.setMenuPath(mMenuCenterButtonRect, mMenuRect, mStart,
							mStartCenter, mSweep, mRadius, mViewAnchorPoints,
							isBottom);
					cv.drawPath(item.getMenuPath(), mRadialMenuPaint);
					if (isShowMenuText) {
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, Color.TRANSPARENT);
						mRadialMenuPaint.setColor(item.getTextColor());
						cv.drawTextOnPath(item.getText(), item.getMenuPath(),
								5, textSize, mRadialMenuPaint);
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, mShadowColor);
					}
					item.getIcon().draw(cv);
					mStart += mSweep;
					mStartCenter += mSweep * 1.5;
				}
			}
		} else {
			if (mMenuItems.size() > 0) {
				if (mRoundHeadBitmap == null)
					cv.drawArc(mMenuRect, mStartAngle, 360, true,
							mRadialMenuPaint);
				float mStart = mStartAngle;
				float mStartCenter = mStartAngle;
				// Get the sweep angles based on the number of menu
				// items
				float mSweep = 180 / mMenuItems.size();
				for (SmartCircularMenuItem item : mMenuItems.values()) {
					mRadialMenuPaint.setColor(item.getBackgroundColor());
					mRadialMenuPaint.setAlpha(mAlphaNum);
					item.setMenuPath(mMenuCenterButtonRect, mMenuRect, mStart,
							mStartCenter, mSweep, mRadius, mViewAnchorPoints,
							isBottom);
					cv.drawPath(item.getMenuPath(), mRadialMenuPaint);
					if (isShowMenuText) {
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, Color.TRANSPARENT);
						mRadialMenuPaint.setColor(item.getTextColor());
						cv.drawTextOnPath(item.getText(), item.getMenuPath(),
								5, textSize, mRadialMenuPaint);
						mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f,
								0.0f, mShadowColor);
					}
					item.getIcon().draw(cv);
					mStart += mSweep;
					mStartCenter = mStart;
				}
			}
		}
	}

	private void drawExpandBitmap(Canvas canvas) {
		// System.out.println("drawExpandBitmap");
		if (mMenuItems.size() > 0 && mExpandBitmap == null) {
			initExpandBitmap();
		}
		// System.out.println("mScale " + mScale);
		switch (mOrientation) {
		case HORIZONTAL_BOTTOM:
			mScaleMatrix.setScale(mScale, mScale);
			mScaleMatrix.postTranslate(
					getWidth() / 2 - mExpandBitmap.getWidth() * mScale / 2,
					getHeight() - mExpandBitmap.getHeight() * mScale);
			canvas.drawBitmap(mExpandBitmap, mScaleMatrix, mAnimationPaint);
			break;
		case VERTICAL_RIGHT:
			mScaleMatrix.setScale(mScale, mScale);
			mScaleMatrix.postTranslate(getWidth() - mExpandBitmap.getWidth()
					* mScale, getHeight() / 2 - mExpandBitmap.getHeight()
					* mScale / 2);
			canvas.drawBitmap(mExpandBitmap, mScaleMatrix, mAnimationPaint);
			break;
		case HORIZONTAL_TOP:
			mScaleMatrix.setScale(mScale, mScale);
			mScaleMatrix.postTranslate(
					getWidth() / 2 - mExpandBitmap.getWidth() * mScale / 2, 0);
			canvas.drawBitmap(mExpandBitmap, mScaleMatrix, mAnimationPaint);
			break;
		case VERTICAL_LEFT:
			mScaleMatrix.setScale(mScale, mScale);
			mScaleMatrix.postTranslate(0,
					getHeight() / 2 - mExpandBitmap.getHeight() * mScale / 2);
			canvas.drawBitmap(mExpandBitmap, mScaleMatrix, mAnimationPaint);
			break;
		}
		mScaleLast = mScale;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// System.out.println("onDraw");
		// mRadialMenuPaint
		// .setShadowLayer(mShadowRadius, 0.0f, 0.0f, mShadowColor);
		mRadialMenuPaint.setStyle(Paint.Style.FILL);
		// Draw the menu if the menu is to be displayed.
		if (isMenuVisible && isExpand) {
			// System.out.println("isMenuVisible && isExpand");
			// See if there is any item in the collection
			// if (mOrientation == HORIZONTAL_BOTTOM) {
			if (mExpandAnimating || mExpandCancelAnimating) {
				drawExpandBitmap(canvas);
			} else {
				drawNormalBitmap(canvas);
			}
			// } else {
			// drawNormalBitmap(canvas);
			// }
		}
		// Draw the center menu toggle piece
		if (mRoundHeadBitmap == null) {
			mRadialMenuPaint.setColor(centerRadialColor);
			mRadialMenuPaint.setStyle(Paint.Style.FILL);
			if (mOrientation == HORIZONTAL_BOTTOM) {
				canvas.drawArc(mMenuCenterButtonRect, mStartAngle, 360, true,
						mRadialMenuPaint);
			} else {
				canvas.drawArc(mMenuCenterButtonRect, mStartAngle, 180, true,
						mRadialMenuPaint);
			}
			// mRadialMenuPaint.setShadowLayer(mShadowRadius, 0.0f, 0.0f,
			// Color.TRANSPARENT);
			// Draw the center text
			if (mRoundHeadBitmap == null)
				drawCenterText(canvas, mRadialMenuPaint);
		} else {
			canvas.drawBitmap(mRoundHeadBitmap, null, mMenuCenterButtonRect,
					mLogoPaint);
			// 画外圈
			// if (isMenuTogglePressed) {
			// mCirclePaint.setStrokeWidth(4.0F);
			// canvas.drawArc(mMenuCenterButtonRect, mStartAngle, 360, true,
			// mCirclePaint);
			// } else {
			// mCirclePaint.setStrokeWidth(2.0F);
			// canvas.drawArc(mMenuCenterButtonRect, mStartAngle, 360, true,
			// mCirclePaint);
			// }
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isVisible == View.GONE) {
			return super.onTouchEvent(event);
		}
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			if (ExpandCancelAnimationSet != null)
				ExpandCancelAnimationSet.cancel();
			// System.out.println("ACTION_DOWN");
			if (mMenuCenterButtonRect.contains(x, y)) {
				mRoundHeadBitmap = mRoundHeadClickedBitmap;
				centerRadialColor = SmartCircularMenuColors.HOLO_LIGHT_BLUE;
				isMenuTogglePressed = true;
				isMenuVisible = true;
				mExpandAnimating = false;
				mDelayCenterAnimating = false;
				centerMenuText = closeMenuText;
				for (SmartCircularMenuItem item : mMenuItems.values()) {
					mMenuItems.get(item.getMenuID()).setBackgroundColor(
							mMenuItems.get(item.getMenuID())
									.getMenuNormalColor());
				}
				// invalidate();
				startDelayCenterAnimation();
				return true;
			}
			return false;
		}
		case MotionEvent.ACTION_UP: {
			// System.out.println("ACTION_UP");
			if (DelayCenterAnimationSet != null)
				DelayCenterAnimationSet.cancel();
			if (ExpandAnimationSet != null) {
				// System.out.println("ExpandAnimationSet != null");
				ExpandAnimationSet.cancel();
			}
			mRoundHeadBitmap = mRoundNormalHeadBitmap;
			if (isDelayCenterAnimatingCancel && !mExpandAnimating) {
				System.out.println("OnClick");
				mScaleLast = 0;
				onClickListener.onCircularButtonClick(this);
				isMenuItemPressed = false;
				isMenuTogglePressed = false;
				isExpand = false;
				invalidate();
				return true;
			}
			if (isMenuTogglePressed) {
				centerRadialColor = Color.WHITE;
				if (isMenuVisible) {
					isMenuVisible = false;
					centerMenuText = openMenuText;
				} else {
					isMenuVisible = true;
					centerMenuText = closeMenuText;
				}
				isMenuTogglePressed = false;
				isExpand = false;
			}
			if (mPressedMenuItemID != null && isMenuItemPressed) {
				if (mMenuItems.get(mPressedMenuItemID).getCallback() != null) {
					mMenuItems.get(mPressedMenuItemID).getCallback()
							.onMenuItemPressed();
				}
				mMenuItems.get(mPressedMenuItemID)
						.setBackgroundColor(
								mMenuItems.get(mPressedMenuItemID)
										.getMenuNormalColor());
				isMenuItemPressed = false;
				mScaleLast = 0.0f;
			}
			if (mMenuCenterButtonRect.contains(x, y)
					&& mPressedMenuItemID == null && isAnimation) {
				// System.out.println("ExpandCancelAnimation");
				startExpandCancelAnimation();
				return true;
			}
			invalidate();
			return false;
		}
		case MotionEvent.ACTION_MOVE: {
			// System.out.println("ACTION_MOVE");
			if (isMenuVisible) {
				if (mMenuCenterButtonRect.contains(x, y)) {
					centerRadialColor = SmartCircularMenuColors.HOLO_LIGHT_BLUE;
					isMenuTogglePressed = true;
					isMenuVisible = true;
					mPressedMenuItemID = null;
					clearAllItemColor();
					invalidate();
					return true;
				}
				if (mMenuItems.size() > 0) {
					for (SmartCircularMenuItem item : mMenuItems.values()) {
						if (mMenuRect.contains((int) x, (int) y))
							if (item.getBounds().contains((int) x, (int) y)) {
								isMenuItemPressed = true;
								mPressedMenuItemID = item.getMenuID();
							}
					}
					if (mPressedMenuItemID != null) {
						clearAllItemColor();
						mMenuItems.get(mPressedMenuItemID).setBackgroundColor(
								mMenuItems.get(mPressedMenuItemID)
										.getMenuSelectedColor());
						invalidate();
						return false;
					}
					return false;
				}
			}
		}
		}

		return super.onTouchEvent(event);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Determine the diameter and the radius based on device orientation
		if (w > h) {
			mDiameter = h;
			mRadius = (int) (mDiameter * 0.409)
					- (getPaddingTop() + getPaddingBottom());
		} else {
			mDiameter = w;
			mRadius = (int) (mDiameter * 0.409)
					- (getPaddingLeft() + getPaddingRight());
		}
		// Init the draw arc Rect object
		mMenuRect = getRadialMenuRect(false, 0, 0);
		mMenuCenterButtonRect = getRadialMenuRect(true, 0, 0);
	}

	/**
	 * menu button color clear
	 */
	private void clearAllItemColor() {
		// TODO Auto-generated method stub
		for (SmartCircularMenuItem item : mMenuItems.values()) {
			mMenuItems.get(item.getMenuID()).setBackgroundColor(
					mMenuItems.get(item.getMenuID()).getMenuNormalColor());
		}
	}

	/**
	 * Draw the toggle menu button text.
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void drawCenterText(Canvas canvas, Paint paint) {
		paint.setColor(mToggleMenuTextColor);
		switch (mOrientation) {
		case VERTICAL_RIGHT:
			canvas.drawText(centerMenuText,
					getWidth() - paint.measureText(centerMenuText), getHeight()
							/ 2 + textSize / 2, paint);
			break;
		case VERTICAL_LEFT:
			canvas.drawText(centerMenuText, 1, getHeight() / 2 + textSize / 2,
					paint);
			break;
		case HORIZONTAL_TOP:
			canvas.drawText(centerMenuText,
					(getWidth() / 2) - (paint.measureText(centerMenuText) / 2),
					textSize, paint);
			break;
		case HORIZONTAL_BOTTOM:
			canvas.drawText(centerMenuText,
					(getWidth() / 2) - (paint.measureText(centerMenuText) / 2),
					getHeight()
							- (int) (mRadius / mOpenButtonScaleFactor * 0.707),
					paint);
			break;
		}
	}

	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(outBitmap);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPX = bitmap.getWidth() / 2;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return outBitmap;
	}

	public Bitmap drawBlackBitmap(Bitmap bitmap) {
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(outBitmap);
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		canvas.drawBitmap(bitmap, rect, rect, paint);
		paint.setAlpha(128);
		canvas.drawRect(rect, paint);
		return outBitmap;
	}

	/**
	 * Get the arc drawing rects
	 * 
	 * @param isCenterButton
	 * @return
	 */
	private RectF getRadialMenuRect(boolean isCenterButton, int width,
			int height) {
		int left, right, top, bottom;
		left = right = top = bottom = 0;
		if (width == 0)
			width = getWidth();
		if (height == 0)
			height = getHeight();
		switch (mOrientation) {
		case VERTICAL_RIGHT:
			if (isCenterButton) {
				left = width - (int) (mRadius / mOpenButtonScaleFactor);
				right = width + (int) (mRadius / mOpenButtonScaleFactor);
				top = (height / 2) - (int) (mRadius / mOpenButtonScaleFactor);
				bottom = (height / 2)
						+ (int) (mRadius / mOpenButtonScaleFactor);
			} else {
				left = width - (int) mRadius;
				right = width + (int) mRadius;
				top = (height / 2) - (int) mRadius;
				bottom = (height / 2) + (int) mRadius;
			}
			mStartAngle = 90;
			mViewAnchorPoints = new Point(width, height / 2);
			break;
		case VERTICAL_LEFT:
			if (isCenterButton) {
				left = -(int) (mRadius / mOpenButtonScaleFactor);
				right = (int) (mRadius / mOpenButtonScaleFactor);
				top = (height / 2) - (int) (mRadius / mOpenButtonScaleFactor);
				bottom = (height / 2)
						+ (int) (mRadius / mOpenButtonScaleFactor);
			} else {
				left = -(int) mRadius;
				right = (int) mRadius;
				top = (height / 2) - (int) mRadius;
				bottom = (height / 2) + (int) mRadius;
			}
			mStartAngle = 270;
			mViewAnchorPoints = new Point(0, height / 2);
			break;
		case HORIZONTAL_TOP:
			if (isCenterButton) {
				left = (width / 2) - (int) (mRadius / mOpenButtonScaleFactor);
				right = (width / 2) + (int) (mRadius / mOpenButtonScaleFactor);
				top = -(int) (mRadius / mOpenButtonScaleFactor);
				bottom = (int) (mRadius / mOpenButtonScaleFactor);
			} else {
				left = (width / 2) - (int) mRadius;
				right = (width / 2) + (int) mRadius;
				top = -(int) mRadius;
				bottom = (int) mRadius;
			}
			mStartAngle = 0;
			mViewAnchorPoints = new Point(width / 2, 0);
			break;
		case HORIZONTAL_BOTTOM:
			if (isCenterButton) {
				left = (width / 2) - (int) (mRadius / mOpenButtonScaleFactor);
				right = (width / 2) + (int) (mRadius / mOpenButtonScaleFactor);
				top = height - (int) (mRadius / mOpenButtonScaleFactor)
						- (int) (mRadius / mOpenButtonScaleFactor * 0.707);
				bottom = height
						+ (int) (mRadius / mOpenButtonScaleFactor * (1 - 0.707));
			} else {
				left = (width / 2) - (int) mRadius;
				right = (width / 2) + (int) mRadius;
				top = height - (int) mRadius;
				bottom = height + (int) mRadius;
			}
			mStartAngle = 135;
			mViewAnchorPoints = new Point(width / 2, height);
			break;
		}
		Rect rect = new Rect(left, top, right, bottom);
		Log.i(VIEW_LOG_TAG, " Top " + top + " Bottom " + bottom + " Left "
				+ left + "  Right " + right);
		return new RectF(rect);
	}

	/********************************************************************************************
	 * Getter and setter methods
	 ********************************************************************************************/

	/**
	 * Set the orientation the semi-circular radial menu. There are four
	 * possible orientations only VERTICAL_RIGHT , VERTICAL_LEFT ,
	 * HORIZONTAL_TOP, HORIZONTAL_BOTTOM
	 * 
	 * @param orientation
	 */
	public void setOrientation(int orientation) {
		mOrientation = orientation;
		mMenuRect = getRadialMenuRect(false, 0, 0);
		mMenuCenterButtonRect = getRadialMenuRect(true, 0, 0);
		isBottom = mOrientation == HORIZONTAL_BOTTOM ? true : false;
		invalidate();
	}

	/**
	 * Add a menu item with it's identifier tag
	 * 
	 * @param idTag
	 *            - Menu item identifier id
	 * @param mMenuItem
	 *            - RadialMenuItem object
	 */
	public void addMenuItem(String idTag, SmartCircularMenuItem mMenuItem) {
		mMenuItems.put(idTag, mMenuItem);
		invalidate();
	}

	/**
	 * Remove a menu item with it's identifier tag
	 * 
	 * @param idTag
	 *            - Menu item identifier id
	 */
	public void removeMenuItemById(String idTag) {
		mMenuItems.remove(idTag);
		invalidate();
	}

	/**
	 * Remove a all menu items
	 */
	public void removeAllMenuItems() {
		mMenuItems.clear();
		invalidate();
	}

	/**
	 * Dismiss an open menu.
	 */
	public void dismissMenu() {
		isMenuVisible = false;
		centerMenuText = openMenuText;
		invalidate();
	}

	/**
	 * @return the mShadowRadius
	 */
	public float getShadowRadius() {
		return mShadowRadius;
	}

	/**
	 * @param mShadowRadius
	 *            the mShadowRadius to set
	 */
	public void setShadowRadius(int mShadowRadius) {
		this.mShadowRadius = mShadowRadius
				* getResources().getDisplayMetrics().density;
		invalidate();
	}

	/**
	 * @return the isShowMenuText
	 */
	public boolean isShowMenuText() {
		return isShowMenuText;
	}

	/**
	 * @param isShowMenuText
	 *            the isShowMenuText to set
	 */
	public void setShowMenuText(boolean isShowMenuText) {
		this.isShowMenuText = isShowMenuText;
		invalidate();
	}

	/**
	 * @return the mOrientation
	 */
	public int getOrientation() {
		return mOrientation;
	}

	/**
	 * @return the centerRadialColor
	 */
	public int getCenterRadialColor() {
		return centerRadialColor;
	}

	/**
	 * @param centerRadialColor
	 *            the centerRadialColor to set
	 */
	public void setCenterRadialColor(int centerRadialColor) {
		this.centerRadialColor = centerRadialColor;
		invalidate();
	}

	/**
	 * @return the mShadowColor
	 */
	public int getShadowColor() {
		return mShadowColor;
	}

	/**
	 * @param mShadowColor
	 *            the mShadowColor to set
	 */
	public void setShadowColor(int mShadowColor) {
		this.mShadowColor = mShadowColor;
		invalidate();
	}

	/**
	 * @return the openMenuText
	 */
	public String getOpenMenuText() {
		return openMenuText;
	}

	/**
	 * @param openMenuText
	 *            the openMenuText to set
	 */
	public void setOpenMenuText(String openMenuText) {
		this.openMenuText = openMenuText;
		if (!isMenuTogglePressed)
			centerMenuText = openMenuText;
		invalidate();
	}

	/**
	 * @return the closeMenuText
	 */
	public String getCloseMenuText() {
		return closeMenuText;
	}

	/**
	 * @param closeMenuText
	 *            the closeMenuText to set
	 */
	public void setCloseMenuText(String closeMenuText) {
		this.closeMenuText = closeMenuText;
		if (isMenuTogglePressed)
			centerMenuText = closeMenuText;
		invalidate();
	}

	/**
	 * @return the mToggleMenuTextColor
	 */
	public int getToggleMenuTextColor() {
		return mToggleMenuTextColor;
	}

	/**
	 * @param mToggleMenuTextColor
	 *            the mToggleMenuTextColor to set
	 */
	public void setToggleMenuTextColor(int mToggleMenuTextColor) {
		this.mToggleMenuTextColor = mToggleMenuTextColor;
		invalidate();
	}

	/**
	 * @return the textSize
	 */
	public float getTextSize() {
		return textSize;
	}

	/**
	 * @param textSize
	 *            the textSize to set
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize * getResources().getDisplayMetrics().density;
		mRadialMenuPaint.setTextSize(this.textSize);
		invalidate();
	}

	/**
	 * @return the mOpenButtonScaleFactor
	 */
	public int getOpenButtonScaleFactor() {
		return mOpenButtonScaleFactor;
	}

	/**
	 * @param mOpenButtonScaleFactor
	 *            the mOpenButtonScaleFactor to set
	 */
	public void setOpenButtonScaleFactor(int mOpenButtonScaleFactor) {
		this.mOpenButtonScaleFactor = mOpenButtonScaleFactor;
		invalidate();
	}

	public boolean isAnimation() {
		return isAnimation;
	}

	public void setAnimation(boolean isAnimation) {
		this.isAnimation = isAnimation;
	}

	public void setVisit(int visibility) {
		int DURATION;
		if (isAnimation) {
			DURATION = DURATION_DISAPLY;
		} else
			DURATION = 0;
		if (visibility == View.GONE) {
			AnimatorSet set = new AnimatorSet();
			set.setInterpolator(new LinearInterpolator());
			set.playTogether(ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f));
			set.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					// TODO Auto-generated method stub
					isVisible = View.GONE;
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub

				}
			});
			set.setDuration(DURATION).start();
		}
		if (visibility == View.VISIBLE) {
			AnimatorSet set = new AnimatorSet();
			set.setInterpolator(new LinearInterpolator());
			set.playTogether(ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f));
			set.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
					// TODO Auto-generated method stub
					isVisible = View.VISIBLE;
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
					// TODO Auto-generated method stub

				}
			});
			set.setDuration(DURATION).start();
		}
	}
}
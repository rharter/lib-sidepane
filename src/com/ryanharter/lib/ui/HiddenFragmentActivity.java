package com.ryanharter.lib.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Activity class that enables Facebook-like side slide navigation.
 */
public class HiddenFragmentActivity extends FragmentActivity {
	private static final String TAG = "HiddenFragmentActivity";

	protected static final int ALIGN_LEFT = RelativeLayout.ALIGN_PARENT_LEFT;
	protected static final int ALIGN_RIGHT = RelativeLayout.ALIGN_PARENT_RIGHT;

	private Fragment hiddenFragment;
	private int mainLayoutResID;

	private FrameLayout backgroundLayout;
	private FrameLayout mainLayout;
	private int alignment = ALIGN_LEFT;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("OPEN", open);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		open = savedInstanceState.getBoolean("OPEN");
	}

	/**
	 * Set the main content view. This should only be called to change the
	 * initial view. Use "setContentViews" when the activity is first created.
	 * 
	 * @param layoutResID
	 */
	@Override
	public void setContentView(int layoutResID) {
		mainLayoutResID = layoutResID;
		buildContainerView();
	}

	/**
	 * Sets the fragment to be shown when the tray is opened. <br>
	 * The distance the tray opens is based on the width of the hidden fragment,
	 * so it should most likely not be set MATCH_PARENT.
	 */
	public void setHiddenFragment(Fragment fragment) {
		hiddenFragment = fragment;
	}

	/**
	 * Sets the alignment of the background view (and the slide direction) to
	 * the left or right. Use {@link #ALIGN_LEFT} and {@link #ALIGN_RIGHT}.
	 */
	public void setAlignment(int align) {
		this.alignment = align;
	}

	/**
	 * Just some ints to use for IDs for the layouts.
	 */
	private final static int BG_LAYOUT_ID = "BG_LAYOUT".hashCode();
	private final static int FG_LAYOUT_ID = "FG_LAYOUT".hashCode();

	/**
	 * Programmatically creates the container view for the main content view and
	 * it's hidden fragment. The fragment can be set to be on the left or right,
	 * but not both.
	 */
	public void buildContainerView() {

		// Create the containing layout
		RelativeLayout relativeLayout = new RelativeLayout(this);

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		// Create the background Fragment
		backgroundLayout = new FrameLayout(this);
		RelativeLayout.LayoutParams blp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		blp.addRule(alignment);
		backgroundLayout.setLayoutParams(blp);

		// Add the supplied fragment
		backgroundLayout.setId(BG_LAYOUT_ID);
		{
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(BG_LAYOUT_ID, hiddenFragment);
			fragmentTransaction.commit();
		}
		backgroundLayout.setVisibility(View.INVISIBLE);
		relativeLayout.addView(backgroundLayout);

		// Create the foreground layout
		mainLayout = new FrameLayout(this);
		RelativeLayout.LayoutParams flp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		inflater.inflate(mainLayoutResID, mainLayout);
		relativeLayout.addView(mainLayout);

		setContentView(relativeLayout, rlp);
	}

	/**
	 * Overrides the back button to close the pane if it's open.
	 */
	@Override
	public void onBackPressed() {
		if (open) {
			toggle();
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * Keeps track of when the side pane is open.
	 */
	private boolean open = false;

	/**
	 * Animations for the open and close of the side pane.
	 */
	private Animation openAnimation;
	private Animation closeAnimation;

	/**
	 * Toggles the side tray opening and closing. The distance that the main
	 * view moves is entirely based on the width of the hidden fragment.
	 */
	public void toggle() {
		if (open) {
			if (closeAnimation == null) {
				// initialize the close animation
				closeAnimation = new SidePaneAnimation(0f, -backgroundLayout.getWidth(), 0f, 0f);
				closeAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
				closeAnimation.setDuration(200);
				closeAnimation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						backgroundLayout.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationStart(Animation animation) {
					}
				});
			}
			mainLayout.startAnimation(closeAnimation);
		} else {
			if (openAnimation == null) {
				// initialize the open animation
				final HiddenFragmentActivity self = this;

				backgroundLayout.setVisibility(View.VISIBLE);
				openAnimation = new SidePaneAnimation(0f, backgroundLayout.getWidth(), 0f, 0f);
				openAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
				openAnimation.setDuration(200);
				openAnimation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationStart(Animation animation) {
						backgroundLayout.setVisibility(View.VISIBLE);
					}
				});
			}
			mainLayout.startAnimation(openAnimation);
		}
		open = !open;
	}

	/**
	 * Custom animation that duplicates the effect of a TranslateAnimation but,
	 * unlike the built in TranslateAnimation, actually moves the view, not just
	 * the pixels that are drawn. This allows the touch areas for Buttons and
	 * things like that to follow the view.
	 */
	private class SidePaneAnimation extends TranslateAnimation {
		private RelativeLayout.LayoutParams mainLayoutParams;

		private float startX, endX;

		public SidePaneAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
			super(fromXDelta, toXDelta, fromYDelta, toYDelta);
			mainLayoutParams = (RelativeLayout.LayoutParams) mainLayout.getLayoutParams();
			startX = mainLayoutParams.leftMargin + fromXDelta;
			endX = mainLayoutParams.leftMargin + toXDelta;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			mainLayoutParams.leftMargin = Math.round(startX + ((endX - startX) * interpolatedTime));
			mainLayoutParams.rightMargin = -mainLayoutParams.leftMargin;
			mainLayout.setLayoutParams(mainLayoutParams);
		}
	}
}
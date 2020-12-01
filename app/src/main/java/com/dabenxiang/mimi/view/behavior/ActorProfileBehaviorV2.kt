package com.dabenxiang.mimi.view.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.widget.collapsing.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.fragment_actor_videos.view.*
import kotlinx.android.synthetic.main.item_actor_collapsing.view.*

class ActorProfileBehaviorV2(
        context: Context?,
        attrs: AttributeSet?
) : BehaviorRules(context, attrs) {

    override fun calcAppbarHeight(child: View): Int = with(child) {
        return (height + pixels(R.dimen.toolbar_height)).toInt()
    }

    override fun View.provideAppbar(): AppBarLayout = app_bar
    override fun View.provideCollapsingToolbar(): CollapsingToolbarLayout = collapsing_toolbar_layout
    override fun canUpdateHeight(progress: Float): Boolean = progress >= GONE_VIEW_THRESHOLD

    override fun View.setUpViews(): List<RuledView> {
        val appearedUntil = 0.1f
        return listOf(
                RuledView(
                        iTopDetails,
                        BRuleYOffset(
                                min = pixels(R.dimen.zero),
                                max = pixels(R.dimen.toolbar_height)
                        )
                ),
                RuledView(
                        tv_name,
                        BRuleXOffset(
                                min = 0f,
                                max = pixels(R.dimen.big_margin),
                                interpolator = ReverseInterpolator(AccelerateInterpolator())
                        ),
                        BRuleYOffset(
                                min = pixels(R.dimen.zero),
                                max = pixels(R.dimen.dialog_padding),
                                interpolator = ReverseInterpolator(LinearInterpolator())
                        ),
                        BRuleAppear(appearedUntil),
                        BRuleAlpha(min = 0.6f, max = 1f).workInRange(from = appearedUntil, to = 1f),
                        BRuleScale(min = 0.8f, max = 1f)
                ),
                RuledView(
                        tv_total_click,
                        BRuleAppear(visibleUntil = GONE_VIEW_THRESHOLD)
                ),
                RuledView(
                        dot,
                        BRuleAppear(visibleUntil = GONE_VIEW_THRESHOLD)
                ),
                RuledView(
                        tv_total_video,
                        BRuleAppear(visibleUntil = GONE_VIEW_THRESHOLD)
                ),
                RuledView(
                        tvCollapsedTop,
                        BRuleAppear(appearedUntil, true)
                ),
                RuledView(
                        info_view,
                        BRuleAppear(visibleUntil = GONE_VIEW_THRESHOLD, animationDuration = 100L)
                ),
                imagesRuleFunc(iv_avatar, LinearInterpolator())
        )
    }

    private fun View.imagesRuleFunc(view: ImageView, interpolator: Interpolator) = RuledView(
            view,
            BRuleYOffset(
                    min = -(tv_name.y - tvCollapsedTop.y),
                    max = 0f,
                    interpolator = DecelerateInterpolator(1.5f)
            ),
            BRuleXOffset(
                    min = 0f,
                    max = tvCollapsedTop.width.toFloat() - pixels(R.dimen.huge_margin),
                    interpolator = ReverseInterpolator(interpolator)
            ),
            BRuleScale(min = 0.8f, max = 1f)
    )


    companion object {
        const val GONE_VIEW_THRESHOLD = 0.8f
    }
}

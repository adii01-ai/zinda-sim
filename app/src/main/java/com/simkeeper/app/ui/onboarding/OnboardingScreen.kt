package com.simkeeper.app.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.simkeeper.app.R
import com.simkeeper.app.ui.components.PrimaryButton
import com.simkeeper.app.ui.components.TextLink
import com.simkeeper.app.ui.theme.SimKeeperTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: Int, val body: Int)

private val pages = listOf(
    OnboardingPage(R.string.onboarding_1_title, R.string.onboarding_1_body),
    OnboardingPage(R.string.onboarding_2_title, R.string.onboarding_2_body),
    OnboardingPage(R.string.onboarding_3_title, R.string.onboarding_3_body),
)

/** Three-page pager. "Skip" and "Get started" both go to the Permission screen. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SimKeeperTheme.colors.background)
            .safeDrawingPadding()
            .padding(horizontal = spacing.screen, vertical = spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo(modifier = Modifier.weight(1f))
            TextLink(stringResource(R.string.action_skip), onClick = onDone)
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            OnboardingPageContent(index, pages[index])
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PageDots(pagerState, modifier = Modifier.weight(1f))
            val last = pagerState.currentPage == pages.lastIndex
            PrimaryButton(
                text = stringResource(if (last) R.string.onboarding_get_started else R.string.action_next),
                trailingIcon = R.drawable.ic_arrow_forward,
                fillWidth = false,
                onClick = {
                    if (last) onDone() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(index: Int, page: OnboardingPage) {
    val spacing = SimKeeperTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { OnboardingIllustration(page = index) }
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = stringResource(page.title),
            style = SimKeeperTheme.typography.headlineLarge,
            color = SimKeeperTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(page.body),
            style = SimKeeperTheme.typography.bodyLarge,
            color = SimKeeperTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(SimKeeperTheme.sizes.logoTile)
                .background(SimKeeperTheme.colors.primary, SimKeeperTheme.shapes.tag),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sim_card),
                contentDescription = null,
                tint = SimKeeperTheme.colors.onPrimary,
                modifier = Modifier.size(SimKeeperTheme.sizes.iconSmall),
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            style = SimKeeperTheme.typography.titleMedium,
            color = SimKeeperTheme.colors.onSurface,
        )
    }
}

@Composable
private fun PageDots(state: PagerState, modifier: Modifier = Modifier) {
    val sizes = SimKeeperTheme.sizes
    val description = stringResource(R.string.onboarding_page_a11y, state.currentPage + 1, state.pageCount)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(SimKeeperTheme.spacing.xs),
    ) {
        repeat(state.pageCount) { index ->
            val active = index == state.currentPage
            val width by animateDpAsState(if (active) sizes.pageDotActive else sizes.pageDot, label = "dot")
            Box(
                Modifier
                    .height(sizes.pageDot)
                    .width(width)
                    .background(
                        if (active) SimKeeperTheme.colors.primary else SimKeeperTheme.colors.outline,
                        SimKeeperTheme.shapes.pill,
                    ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingScreenPreview() {
    SimKeeperTheme { OnboardingScreen(onDone = {}) }
}

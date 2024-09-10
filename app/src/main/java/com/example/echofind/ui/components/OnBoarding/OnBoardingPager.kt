package com.example.echofind.ui.components.OnBoarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.navigation.NavController
import com.example.echofind.data.model.PageData
import com.example.echofind.data.model.dataStore.StoreBoarding

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingPager(
    item: List<PageData>,
    pagerState: PagerState,
    modifier: Modifier,
    navController: NavController,
    store: StoreBoarding
) {
    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize() // Ensure HorizontalPager occupies full space
        ) { page ->
            Column(
                modifier = Modifier
                    .padding(60.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoaderData(
                    modifier = Modifier
                        .size(200.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    item[page].image
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = item[page].tittle,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .fillMaxWidth(),
                    color = Color.Black,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item[page].desc,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    fontSize = 17.sp,
                    modifier = Modifier
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            PagerIndicator(item.size, pagerState.currentPage)
            Spacer(modifier = Modifier.height(80.dp)) // Add space between PagerIndicator and ButtonFinish
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            ButtonFinish(pagerState.currentPage, navController, store)
        }
    }
}
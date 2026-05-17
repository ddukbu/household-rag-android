package com.example.householdrag.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.model.AssetOut
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import com.example.householdrag.ui.theme.LemonDeep

@Composable
fun AssetSummaryCard(asset: AssetOut?) {
    // 테두리 네모 박스를 없애고 깔끔하게 글자들과 밑줄만 배치!
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = "현재 총 자산",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${String.format("%,d", asset?.current_asset ?: 0)}원",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 혜림 님이 원하신 포인트! 노란색 밑줄
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 3.dp,
            color = LemonDeep
        )
    }
}

// [중요] 기존에 있던 다른 프리뷰 함수는 지우고, 딱 이거 하나만 남겨두세요!
@Preview(showBackground = true, name = "노란 밑줄 자산 스타일 미리보기")
@Composable
fun AssetSummaryCardPreview() {
    HouseholdRAGTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AssetSummaryCard(
                asset = AssetOut(
                    initial_asset = 0,
                    current_asset = 4350000,
                    total_income = 0,
                    total_expense = 0,
                    updated_at = ""
                )
            )
        }
    }
}
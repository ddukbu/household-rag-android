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

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 3.dp,
            color = LemonDeep
        )
    }
}

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
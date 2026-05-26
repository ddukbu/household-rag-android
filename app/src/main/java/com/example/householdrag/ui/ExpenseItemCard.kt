package com.example.householdrag.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.householdrag.api.Expense
import com.example.householdrag.model.Income
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import com.example.householdrag.ui.theme.formatAmount

@Composable
fun ExpenseItemCard(
    item: Any,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // var isExpanded by remember { mutableStateOf(false) }

    val date: String
    val time: String
    val title: String
    val amount: Int
    val isIncome = item is Income
    // val subText: String // 카테고리, 결제수단(입금방법), 메모
    val detailedCategory: String
    val detailedMethod: String
    val memoText: String


    if (item is Expense) {
        date = item.date
        time = item.time
        title = item.place  // 사용처
        amount = item.amount
        detailedCategory = item.category
        detailedMethod = item.payment_method
        memoText = item.memo

//      subText =
//          "${item.category} · ${item.payment_method}${if (item.memo.isNotBlank()) " · ${item.memo}" else ""}"
    } else if (item is Income) {
        date = item.date
        time = item.time
        title = item.category   // 카테고리
        amount = item.amount
        detailedCategory = item.deposit_source
        detailedMethod = item.deposit_method ?: "미지정"
        memoText =
            "${if (!item.memo.isNullOrBlank()) " ${item.memo}" else ""}"

//      subText =
//          "${item.deposit_source ?: "미지정"}${if (!item.memo.isNullOrBlank()) "· ${item.deposit_method ?: "미지정"} · ${item.memo}" else "" }"

    } else {
        return
    }

    val amountSign = if (isIncome) "+" else "-"
    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color.Black
    val cardBackground = if (isIncome) Color(0xFFF9FBF9) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                // verticalAlignment = Alignment.CenterVertically
            ) {
                // 날짜 시간
                Text(
                    text = "$date  $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    // modifier = Modifier.width(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이름 가격
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$amountSign ${formatAmount(amount)}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
            }

            // 확장
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200))
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val combinedDetails = buildString {
                            append("$detailedCategory · $detailedMethod")
                            if (memoText.isNotBlank()) {
                                append(" · $memoText")
                            }
                        }

                        Text(
                            text = combinedDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onEditClick,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    "수정",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            TextButton(
                                onClick = onDeleteClick,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(
                                        0xFFE53935
                                    )
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("삭제", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "가계부 카드 미리보기")
@Composable
fun ExpenseItemCardPreview() {
    HouseholdRAGTheme {
        // 실제 데이터 대신 임시 데이터를 만들어서 넣어줍니다.
        val demoExpense = Expense(
            id = "1",
            place = "배달의 민족",
            amount = 18000,
            date = "2026-04-30",
            memo = "점심 햄버거 세트",
            category = "식비",
            payment_method = "카카오페이",
            time = "12:00",
            is_fixed_expense = false
        )
        val demoExpense2 = Expense(
            id = "2",
            place = "배달의 민족 떡볶이 앤 튀김 앤 순대 세트 대형 주문",
            amount = 18000,
            date = "2026-04-30",
            memo = "친구들이랑 홈파티 하려고 진짜 엄청 많이 주문함! 진짜 역대급으로 많이 먹은 날이고 너무너무 행복했음 대박 만족 식사!",
            category = "식비",
            payment_method = "카카오페이",
            time = "12:00",
            is_fixed_expense = false
        )
        val demoIncome = Income(
            id = "3",
            category = "월급",
            amount = 2500000,
            date = "2026-04-25",
            memo = "기분 좋은 날",
            deposit_method = "계좌이체",
            deposit_source = "주식회사 테스트",
            time = "10:00",
            is_fixed_income = false
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text("1. 지출 예시 피드", fontWeight = FontWeight.Bold)
            ExpenseItemCard(
                item = demoExpense2,
                isExpanded = true,
                onCardClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("2. 수입 예시 피드 (혜림 님 통합 버전 폼)", fontWeight = FontWeight.Bold)
            ExpenseItemCard(
                item = demoIncome,
                isExpanded = true,
                onCardClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}
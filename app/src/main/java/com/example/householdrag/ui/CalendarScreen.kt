package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.householdrag.api.Expense
import com.example.householdrag.model.Income
import com.example.householdrag.ui.theme.LemonMain
import com.example.householdrag.ui.theme.MonthSelector
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(transactions: List<Any>, onListClick: () -> Unit) {
    // 날짜 상태 관리
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // val currentMonth = remember { YearMonth.now() }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    val startMonth = remember { currentYearMonth.minusMonths(50) }
    val endMonth = remember { currentYearMonth.plusMonths(50) }
    val daysOfWeek = remember { daysOfWeek() }
    val scope = rememberCoroutineScope()

    // 지출이 있는 날짜들 (중복 제거)
    val spentDates = remember(transactions) {
        transactions.filterIsInstance<Expense>().mapNotNull {
            try {
                LocalDate.parse(it.date)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    // 수입이 있는 날짜들
    val incomeDates = remember(transactions) {
        transactions.filterIsInstance<Income>().mapNotNull {
            try { LocalDate.parse(it.date) } catch (e: Exception) { null }
        }.toSet()
    }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentYearMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    // 캘린더를 직접 넘겼을 때(스와이프) 상단 연/월 글자도 바뀌게 동기화
    LaunchedEffect(state.firstVisibleMonth) {
        currentYearMonth = state.firstVisibleMonth.yearMonth
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp )
    ) {
        // [상단] 캘린더 헤더 (연도/월 표시)
        MonthSelector(
            currentYM = currentYearMonth.toString(),
            onMonthChange = { newYMStr ->
                val newYM = YearMonth.parse(newYMStr)
                currentYearMonth = newYM
                // 화살표 누르면 캘린더도 해당 달로 스크롤!
                scope.launch {
                    state.animateScrollToMonth(newYM)
                }
            }
        )

        // [중앙] 요일 표시 (일~토)
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    text = dayOfWeek.name.take(1),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        // [본체] 라이브러리 캘린더
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayElement(
                    day = day,
                    isSelected = selectedDate == day.date,
                    hasExpense = spentDates.contains(day.date),
                    hasIncome = incomeDates.contains(day.date),
                    onClick = { selectedDate = it.date }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

        // [하단] 선택된 날짜의 통합 수입/지출 내역 필터링
        val filteredTransactions = transactions.filter {
            try {
                val dateStr = when (it) {
                    is Expense -> it.date
                    is Income -> it.date
                    else -> ""
                }
                LocalDate.parse(dateStr) == selectedDate
            } catch (e: Exception) {
                false
            }
        }

        var expandedCalItemId by remember { mutableStateOf<String?>(null) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "${selectedDate.dayOfMonth}일 기계부 내역",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            if (filteredTransactions.isEmpty()) {
                item { Text("가계부 기록이 없어요.", color = Color.Gray, fontSize = 14.sp) }
            } else {
                items(filteredTransactions) { trans ->
                    val calItemId = when (trans) {
                        is Expense -> trans.id
                        is Income -> trans.id
                        else -> ""
                    }

                    ExpenseItemCard(
                        item = trans,
                        isExpanded = expandedCalItemId == calItemId,
                        onCardClick = {
                            expandedCalItemId = if (expandedCalItemId == calItemId) null else calItemId
                        },
                        onEditClick = { /* 캘린더에서는 보기 전용으로 두거나 메인에서 처리 */ },
                        onDeleteClick = { }
                    )                }
            }
        }
    }
}

@Composable
fun DayElement(
    day: CalendarDay,
    isSelected: Boolean,
    hasExpense: Boolean,
    hasIncome: Boolean,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LemonMain else Color.Transparent)
            .clickable(enabled = day.position == DayPosition.MonthDate) { onClick(day) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    day.position != DayPosition.MonthDate -> Color.LightGray // 이전/다음 달 날짜
                    isSelected -> Color.Black
                    else -> Color.Black
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasExpense && day.position == DayPosition.MonthDate) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (hasExpense) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                        )
                    }
                    if (hasIncome) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                    }
                }
            }
        }
    }
}
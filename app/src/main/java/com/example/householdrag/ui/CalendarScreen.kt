package com.example.householdrag.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.householdrag.model.AssetHistoryItem
import com.example.householdrag.model.BudgetOut
import com.example.householdrag.model.Income
import com.example.householdrag.ui.theme.LemonMain
import com.example.householdrag.ui.theme.MonthSelector
import com.example.householdrag.ui.theme.formatAmount
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    transactions: List<Any>,
    assetHistory: List<AssetHistoryItem>,
    budgets: List<BudgetOut>,
    currentAsset: Int?,
    onAddClick: (date: LocalDate, category: String?) -> Unit,
    onListClick: () -> Unit
) {
    // 날짜 상태 관리
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedAssetYear by remember { mutableStateOf(YearMonth.now().year) }
    var calendarMode by remember { mutableStateOf("일별") }

    // 카테고리
    var selectedCategoryFilter by remember { mutableStateOf("전체") }
    var filterDropdownExpanded by remember { mutableStateOf(false) }
    val filterOptions =
        listOf("전체", "식비", "교통비", "쇼핑", "여가", "생활", "의료", "월세", "보험료", "월급", "용돈", "부수입", "기타")

    val startMonth = remember { currentYearMonth.minusMonths(50) }
    val endMonth = remember { currentYearMonth.plusMonths(50) }
    val daysOfWeek = remember { daysOfWeek() }
    val scope = rememberCoroutineScope()

    // 카테고리 필터링
    val filteredTransactionsByCategory = remember(transactions, selectedCategoryFilter) {
        if (selectedCategoryFilter == "전체") {
            transactions
        } else {
            transactions.filter {
                val itemCategory = when (it) {
                    is Expense -> it.category
                    is Income -> it.category
                    else -> ""
                }
                itemCategory == selectedCategoryFilter
            }
        }
    }

    // 지출이 있는 날짜들
    val spentDates = remember(filteredTransactionsByCategory) {
        filteredTransactionsByCategory.filterIsInstance<Expense>().mapNotNull {
            try {
                LocalDate.parse(it.date)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    // 수입이 있는 날짜들
    val incomeDates = remember(filteredTransactionsByCategory) {
        filteredTransactionsByCategory.filterIsInstance<Income>().mapNotNull {
            try {
                LocalDate.parse(it.date)
            } catch (e: Exception) {
                null
            }
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
            .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp)
    ) {
        // [상단] 캘린더 헤더 (연도/월 표시), 카테고리 드롭다운
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
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
            }

            // 카테고리
            Box(
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .clickable { filterDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategoryFilter,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedCategoryFilter == "전체") Color.DarkGray else Color.Black,
                        fontWeight = if (selectedCategoryFilter == "전체") FontWeight.Normal else FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("▾", fontSize = 12.sp, color = Color.Gray)
                }

                // 필터 클릭 시 팝업되는 메뉴 상자
                DropdownMenu(
                    expanded = filterDropdownExpanded,
                    onDismissRequest = { filterDropdownExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 14.sp) },
                            onClick = {
                                selectedCategoryFilter = option
                                filterDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F3F3))
                    .padding(3.dp)
            ) {
                CalendarModeChip(
                    text = "월별",
                    selected = calendarMode == "월별",
                    onClick = { calendarMode = "월별" }
                )
                CalendarModeChip(
                    text = "일별",
                    selected = calendarMode == "일별",
                    onClick = { calendarMode = "일별" }
                )
            }

            FilledTonalButton(
                onClick = onListClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("목록")
            }
        }

        if (calendarMode == "월별") {
            MonthlyAssetHistorySection(
                assetHistory = assetHistory,
                budgets = budgets,
                currentAsset = currentAsset,
                selectedYear = selectedAssetYear,
                onYearChange = { selectedAssetYear = it }
            )
            return@Column
        }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedDate.dayOfMonth}일 가계부 내역",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCategoryFilter != "전체") {
                            Text(
                                text = "[$selectedCategoryFilter]",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                onAddClick(
                                    selectedDate,
                                    selectedCategoryFilter.takeIf { it != "전체" }
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("추가")
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Text(
                        text = if (selectedCategoryFilter == "전체") "가계부 기록이 없어요." else "해당 날짜에 [$selectedCategoryFilter] 내역이 없어요.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
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
                            expandedCalItemId =
                                if (expandedCalItemId == calItemId) null else calItemId
                        },
                        onEditClick = { /* 캘린더에서는 보기 전용으로 두거나 메인에서 처리 */ },
                        onDeleteClick = { }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) LemonMain else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.Black else Color.Gray
        )
    }
}

@Composable
fun MonthlyAssetHistorySection(
    assetHistory: List<AssetHistoryItem>,
    budgets: List<BudgetOut>,
    currentAsset: Int?,
    selectedYear: Int,
    onYearChange: (Int) -> Unit
) {
    val historyByMonth = remember(assetHistory, selectedYear) {
        assetHistory
            .filter { it.year_month.startsWith("$selectedYear-") }
            .associateBy {
                it.year_month.substringAfter("-").toIntOrNull() ?: 0
            }
    }
    val budgetByMonth = remember(budgets, selectedYear) {
        budgets
            .filter { it.year_month.startsWith("$selectedYear-") }
            .associateBy {
                it.year_month.substringAfter("-").toIntOrNull() ?: 0
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CurrentAssetHeader(currentAsset = currentAsset)
        }

        item {
            YearSelector(
                year = selectedYear,
                onPreviousYear = { onYearChange(selectedYear - 1) },
                onNextYear = { onYearChange(selectedYear + 1) }
            )
        }

        items((1..12).chunked(4)) { monthRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                monthRow.forEach { month ->
                    MonthlyAssetMonthCell(
                        month = month,
                        item = historyByMonth[month],
                        budget = budgetByMonth[month],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentAssetHeader(currentAsset: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F0F7))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "현재 자산",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${formatAmount(currentAsset ?: 0)}원",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun YearSelector(
    year: Int,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${year}년",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row {
            IconButton(onClick = onPreviousYear) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "이전 연도"
                )
            }
            IconButton(onClick = onNextYear) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "다음 연도"
                )
            }
        }
    }
}

@Composable
fun MonthlyAssetMonthCell(
    month: Int,
    item: AssetHistoryItem?,
    budget: BudgetOut?,
    modifier: Modifier = Modifier
) {
    val hasRecord = item != null
    val stateLabel = when (budget?.state) {
        "good" -> "좋음"
        "warning" -> "경고"
        "bad" -> "나쁨"
        else -> null
    }
    val stateColor = when (budget?.state) {
        "good" -> Color(0xFF2E7D32)
        "warning" -> Color(0xFFF9A825)
        "bad" -> Color(0xFFE53935)
        else -> Color.Gray
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (hasRecord || stateLabel != null) Color(0xFFEAF4FF) else Color.Transparent)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = month.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (hasRecord) FontWeight.Bold else FontWeight.Normal,
            color = if (hasRecord) Color.Black else Color.DarkGray
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (item == null) {
            Text(
                text = "기록 없음",
                fontSize = 10.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = formatAmount(item.asset),
                fontSize = 10.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        if (stateLabel != null) {
            Text(
                text = stateLabel,
                fontSize = 10.sp,
                color = stateColor,
                fontWeight = FontWeight.Bold
            )
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

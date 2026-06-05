package com.example.householdrag.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.householdrag.api.ApiClient
import com.example.householdrag.api.ApiErrorHandler
import com.example.householdrag.api.Expense
import com.example.householdrag.api.FixedExpenseItem
import com.example.householdrag.api.FixedIncomeItem
import com.example.householdrag.auth.AuthRepository
import com.example.householdrag.auth.AuthTokenStore
import com.example.householdrag.model.AskRequest
import com.example.householdrag.model.AssetHistoryItem
import com.example.householdrag.model.AssetOut
import com.example.householdrag.model.BudgetDetailsUpdateRequest
import com.example.householdrag.model.BudgetDraftOut
import com.example.householdrag.model.BudgetDraftRequest
import com.example.householdrag.model.BudgetOut
import com.example.householdrag.model.ChatHistoryDto
import com.example.householdrag.model.FixedExpenseBudget
import com.example.householdrag.model.FixedIncomeBudget
import com.example.householdrag.model.Income
import com.example.householdrag.model.InitialAssetRequest
import com.example.householdrag.model.SavingUpdateRequest
import com.example.householdrag.ui.theme.HouseholdRAGTheme
import kotlinx.coroutines.launch

enum class Screen { LOGIN, SIGNUP, LIST, CALENDAR, ADD, ASK, BUDGET }
enum class ChatMode { GENERAL, BUDGET }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        setContent {
            HouseholdRAGTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HouseholdApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdApp() {
    val TAG = "HouseholdApp"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isAuthenticated by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) } // 기본은 LOGIN 테스트 시 변경
    var isLoading by remember { mutableStateOf(false) }
    var isChatLoading by remember { mutableStateOf(false) }

    // --- 데이터 상태 변수 ---
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var statusMessage by remember { mutableStateOf("준비됨") }
    var editId by remember { mutableStateOf<String?>(null) }

    // 지출 수익 통합
    var combinedTransactions by remember { mutableStateOf(listOf<Any>()) }

    // 자산 상태
    var assetData by remember { mutableStateOf<AssetOut?>(null) }
    var assetHistory by remember { mutableStateOf(listOf<AssetHistoryItem>()) }
    // 초기 자산 설정용 팝업 상태 변수
    var showAssetDialog by remember { mutableStateOf(false) }
    var inputInitialAsset by remember { mutableStateOf("") }

    var date by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var inputStartsAsExpense by remember { mutableStateOf(true) }
    var inputFormSession by remember { mutableStateOf(0) }
    var expandedItemId by remember { mutableStateOf<String?>(null) }

    var mainFixedItemId by remember { mutableStateOf("") }

    var currentQuestion by remember { mutableStateOf("") }
    var chatMode by remember { mutableStateOf(ChatMode.GENERAL) }
    var selectedBudgetMode by remember { mutableStateOf("balanced") }
    var pendingBudgetDraft by remember { mutableStateOf<BudgetDraftOut?>(null) }

    var currentYearMonth by remember {
        mutableStateOf(
            java.time.YearMonth.now().toString() // 기본값: 현재 연-월 ("2026-05")
        )
    }
    var budgetData by remember { mutableStateOf<BudgetOut?>(null) }
    var budgets by remember { mutableStateOf(listOf<BudgetOut>()) }
    var totalFixedIncome by remember { mutableStateOf(0) }
    var totalFixedExpense by remember { mutableStateOf(0) }
    var fixedIncomeList by remember { mutableStateOf(listOf<FixedIncomeItem>()) }
    var fixedExpenseList by remember { mutableStateOf(listOf<FixedExpenseItem>()) }
    var chatHistory by remember { mutableStateOf(listOf<ChatHistoryDto>()) }

    // --- 로직 함수 ---
    fun clearForm() {
        editId = null; date = ""; category = ""; amount = ""; paymentMethod = ""; place = ""; memo =
            ""
        inputStartsAsExpense = true
        inputFormSession += 1
    }

    fun openAddScreen(
        initialDate: String = "",
        initialCategory: String? = null
    ) {
        val incomeCategories = setOf("월급", "연금", "부수입", "용돈", "상여")
        clearForm()
        date = initialDate
        category = initialCategory.orEmpty()
        inputStartsAsExpense = initialCategory !in incomeCategories
        currentScreen = Screen.ADD
    }

    fun refreshBudget(yearMonth: String) {
        scope.launch {
            try {
                val budgetResponse = ApiClient.api.getBudget(yearMonth)
                budgetData = budgetResponse

                val fixedIncomes = ApiClient.api.getFixedIncomes(yearMonth)
                fixedIncomeList = fixedIncomes
                totalFixedIncome = fixedIncomes.sumOf { it.amount }

                val fixedExpenses = ApiClient.api.getFixedExpenses(yearMonth)
                fixedExpenseList = fixedExpenses
                totalFixedExpense = fixedExpenses.sumOf { it.amount }
            } catch (e: Exception) {
                statusMessage = "예산 데이터를 가져오지 못했습니다."
            }
        }
    }

    suspend fun refreshExpenses() {
        isLoading = true
        try {
            // expenses = ApiClient.api.getExpenses().reversed()
            val expenseList = ApiClient.api.getExpenses()
            expenses = expenseList

            val incomeList = ApiClient.api.getIncomes()

            combinedTransactions = (expenseList + incomeList).sortedWith(
                compareByDescending<Any> {
                    when (it) {
                        is Expense -> it.date
                        is com.example.householdrag.model.Income -> it.date
                        else -> ""
                    }
                }.thenByDescending {
                    when (it) {
                        is Expense -> it.time
                        is com.example.householdrag.model.Income -> it.time
                        else -> ""
                    }
                }
            )

            Log.d("P_TEST", "서버에서 받은 리스트 개수: ${expenses.size}")
            Log.d("P_TEST", "데이터 샘플: $expenses")
            statusMessage = "목록 업데이트 성공"
        } catch (e: Exception) {
            Log.e("P_TEST", "불러오기 실패 에러 메시지: ${e.message}", e)
            statusMessage = "불러오기 실패"
        } finally {
            isLoading = false
        }
    }

    // 자산 정보를 가져오는 함수
    fun refreshAssets() {
        scope.launch {
            try {
                val response = ApiClient.api.getAsset()
                assetData = response
                Log.d("ASSET", "자산 데이터 로드 성공: ${response.current_asset}")
            } catch (e: Exception) {
                Log.e("ASSET", "자산 데이터를 가져오지 못했습니다: ${e.message}")
                statusMessage = "자산 데이터 업데이트 실패"
            }
        }
    }

    // 화면 진입 시 서버에서 대화 기록을 가져오는 로직 추가
    fun refreshChatHistory() {
        scope.launch {
            try {
                val history = ApiClient.api.getChatHistory()
                // 정렬 기준: 서버가 어떤 순서로 내려주든 created_at 기준으로 오래된->최신 정렬하여 일관성 유지
                val serverList = history.sortedBy { it.created_at }
                chatHistory = serverList
            } catch (e: Exception) {
                Log.e("Chat", "대화 기록 로드 실패", e)
            }
        }
    }

    fun refreshAssetHistory() {
        scope.launch {
            try {
                val response = ApiClient.api.getAssetHistory()
                assetHistory = response.asset_history
            } catch (e: Exception) {
                Log.e("ASSET_HISTORY", "자산 기록을 가져오지 못했습니다: ${e.message}")
                statusMessage = "자산 기록 업데이트 실패"
            }
        }
    }

    fun refreshBudgets() {
        scope.launch {
            try {
                budgets = ApiClient.api.getBudgets()
            } catch (e: Exception) {
                Log.e("BUDGETS", "월별 예산 상태를 가져오지 못했습니다: ${e.message}")
                statusMessage = "월별 예산 상태 업데이트 실패"
            }
        }
    }

    fun askGeneralQuestion() {
        val submittedQuestion = currentQuestion.trim()
        if (submittedQuestion.isBlank()) return

        scope.launch {
            isChatLoading = true
            try {
                ApiClient.api.ask(AskRequest(submittedQuestion))
                Log.d(TAG, "AI 응답 수신 성공")
                currentQuestion = ""
                refreshChatHistory()
            } catch (e: Exception) {
                Log.e(TAG, "질문 API 호출 에러: ${e.message}", e)
                statusMessage = ApiErrorHandler.getUserMessage(e)
            } finally {
                isChatLoading = false
            }
        }
    }

    fun analyzeGeneralSpending() {
        val submittedQuestion = currentQuestion.trim()
        scope.launch {
            isChatLoading = true
            try {
                val requestText = submittedQuestion.ifBlank {
                    "최근 카테고리별 소비 패턴을 분석해줘."
                }
                ApiClient.api.analyzeSpending(AskRequest(requestText))
                currentQuestion = ""
                refreshChatHistory()
            } catch (e: Exception) {
                Log.e(TAG, "분석 API 호출 에러: ${e.message}", e)
                statusMessage = ApiErrorHandler.getUserMessage(e)
            } finally {
                isChatLoading = false
            }
        }
    }

    fun createBudgetDraftFromChat(
        mode: String,
        userMessage: String? = null
    ) {
        scope.launch {
            isChatLoading = true
            try {
                val trimmedUserMessage = userMessage?.trim()?.takeIf { it.isNotBlank() }
                val draft = ApiClient.api.createBudgetDraft(
                    currentYearMonth,
                    BudgetDraftRequest(
                        mode = mode,
                        user_message = trimmedUserMessage
                    )
                )
                pendingBudgetDraft = draft
                chatMode = ChatMode.BUDGET
                currentQuestion = ""
                refreshChatHistory()
            } catch (e: Exception) {
                Log.e(TAG, "예산안 draft 생성 에러: ${e.message}", e)
                statusMessage = ApiErrorHandler.getUserMessage(e)
            } finally {
                isChatLoading = false
            }
        }
    }

    fun applyBudgetDraft() {
        if (pendingBudgetDraft == null) return
        scope.launch {
            isChatLoading = true
            try {
                val result = ApiClient.api.applyBudgetDraft(currentYearMonth)
                budgetData = result.budget
                pendingBudgetDraft = null
                chatMode = ChatMode.GENERAL
                refreshChatHistory()
                refreshBudget(currentYearMonth)
            } catch (e: Exception) {
                Log.e(TAG, "예산안 적용 에러: ${e.message}", e)
                statusMessage = ApiErrorHandler.getUserMessage(e)
            } finally {
                isChatLoading = false
            }
        }
    }

    fun cancelBudgetDraft() {
        if (pendingBudgetDraft == null) return
        scope.launch {
            isChatLoading = true
            try {
                val result = ApiClient.api.cancelBudgetDraft(currentYearMonth)
                budgetData = result.budget
                pendingBudgetDraft = null
                chatMode = ChatMode.GENERAL
                refreshChatHistory()
                refreshBudget(currentYearMonth)
            } catch (e: Exception) {
                Log.e(TAG, "예산안 취소 에러: ${e.message}", e)
                statusMessage = ApiErrorHandler.getUserMessage(e)
            } finally {
                isChatLoading = false
            }
        }
    }

    // ASK 화면으로 전환될 때 기록 불러오기
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.ASK) {
            refreshChatHistory()
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.LIST) {
            refreshExpenses()
            refreshAssets()
            refreshBudget(currentYearMonth)
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.CALENDAR) {
            refreshExpenses()
            refreshAssets()
            refreshAssetHistory()
            refreshBudgets()
        }
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.ADD) {
            refreshBudget(currentYearMonth)
        }
    }

    // LaunchedEffect(Unit) { refreshExpenses() }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.SIGNUP) {
                TopAppBar(
                    title = { Text("MoneyMate") },
                    actions = {
                        TextButton(onClick = {
                            AuthTokenStore.clear(context)
                            currentScreen = Screen.LOGIN
                            statusMessage = "로그아웃 되었습니다."
                        }) {
                            Text("로그아웃", color = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { scope.launch { refreshExpenses() } }) {
                            Icon(Icons.Default.Refresh, contentDescription = "새로고침")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == Screen.LIST) {
                FloatingActionButton(
                    onClick = { clearForm(); currentScreen = Screen.ADD },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "추가")
                }
            }
        },
        bottomBar = {
            if (currentScreen != Screen.LOGIN && currentScreen != Screen.SIGNUP) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                        thickness = 0.5.dp
                    )
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.BUDGET,
                            onClick = { currentScreen = Screen.BUDGET },
                            label = { Text("예산") },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                indicatorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.LIST,
                            onClick = { currentScreen = Screen.LIST },
                            label = { Text("목록") },
                            icon = { Icon(Icons.Default.List, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.ASK,
                            onClick = { currentScreen = Screen.ASK },
                            label = { Text("채팅") },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.LOGIN -> LoginScreen(
                    onLoginClick = { email, pw ->
                        isLoading = true
                        AuthRepository.loginAndSetFirebaseIdToken(
                            context,
                            email.trim(),
                            pw
                        ) { success, error ->
                            scope.launch {
                                isLoading = false
                                if (success) {
                                    currentScreen = Screen.LIST; refreshExpenses()
                                } else {
                                    statusMessage = error ?: "로그인 실패"
                                }
                            }
                        }
                    },
                    onSignUpClick = { currentScreen = Screen.SIGNUP }
                )

                Screen.SIGNUP -> SignUpScreen(
                    onSignUpClick = { email, pw ->
                        isLoading = true
                        AuthRepository.signUpAndInitProfile(
                            context,
                            email.trim(),
                            pw
                        ) { success, error ->
                            scope.launch {
                                isLoading = false
                                if (success) currentScreen = Screen.LOGIN
                                else statusMessage = error ?: "회원가입 실패"
                            }
                        }
                    },
                    onBackToLogin = { currentScreen = Screen.LOGIN }
                )

                Screen.LIST -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Text("가계부 목록", style = MaterialTheme.typography.headlineSmall)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        // 현재 자산액을 모르면 빈칸, 알면 기본값으로 채워주기
                                        inputInitialAsset =
                                            (assetData?.initial_asset ?: 0).toString()
                                        showAssetDialog = true
                                    }) {
                                Text(
                                    text = "현재 총 자산",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${
                                        String.format(
                                            "%,d",
                                            assetData?.current_asset ?: 0
                                        )
                                    }원",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }

                            // 캘린더로 전환하는 아이콘 버튼
                            IconButton(onClick = { currentScreen = Screen.CALENDAR }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "캘린더 보기",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        //AssetSummaryCard(asset = assetData)

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(combinedTransactions) { transactionItem ->
                                val itemId = when (transactionItem) {
                                    is Expense -> transactionItem.id
                                    is Income -> transactionItem.id
                                    else -> ""
                                }

                                ExpenseItemCard(
                                    item = transactionItem,
                                    isExpanded = expandedItemId == itemId,
                                    onCardClick = {
                                        expandedItemId =
                                            if (expandedItemId == itemId) null else itemId
                                    },
                                    onEditClick = {
                                        // 수정 처리 로직 (기존 기믹 분기 매핑)
                                        if (transactionItem is Expense) {
                                            editId = transactionItem.id;
                                            date = transactionItem.date;
                                            category = transactionItem.category
                                            amount = transactionItem.amount.toString();
                                            paymentMethod = transactionItem.payment_method
                                            place = transactionItem.place;
                                            memo = transactionItem.memo
                                            mainFixedItemId =
                                                transactionItem.fixed_item_id.orEmpty()
                                            inputStartsAsExpense = true
                                            inputFormSession += 1
                                            currentScreen = Screen.ADD
                                        } else if (transactionItem is Income) {
                                            editId = transactionItem.id;
                                            date = transactionItem.date;
                                            category = transactionItem.category
                                            amount = transactionItem.amount.toString();
                                            memo = transactionItem.memo ?: ""
                                            place = transactionItem.deposit_source
                                                ?: "" // 수입은 입금처를 임시 보관
                                            mainFixedItemId =
                                                transactionItem.fixed_item_id.orEmpty()
                                            paymentMethod = transactionItem.deposit_method
                                            inputStartsAsExpense = false
                                            inputFormSession += 1
                                            currentScreen = Screen.ADD
                                        }
                                    },
                                    onDeleteClick = {
                                        scope.launch {
                                            if (transactionItem is Expense) {
                                                ApiClient.api.deleteExpense(transactionItem.id)
                                            } else if (transactionItem is Income) {
                                                ApiClient.api.deleteIncome(transactionItem.id)
                                            }
                                            refreshExpenses()
                                            refreshAssets()
                                            refreshAssetHistory()
                                            refreshBudgets()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Screen.ADD -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        ExpenseInputCard(
                            editId = editId,
                            initialIsExpenseMode = inputStartsAsExpense,
                            formSessionKey = inputFormSession,
                            date = date, onDateChange = { date = it },
                            category = category, onCategoryChange = { category = it },
                            amount = amount, onAmountChange = { amount = it },
                            paymentMethod = paymentMethod, onPaymentChange = { paymentMethod = it },
                            place = place, onPlaceChange = { place = it },
                            memo = memo, onMemoChange = { memo = it },
                            onResetClick = { clearForm(); currentScreen = Screen.LIST },

                            // 추가된 부분
                            fixedIncomeList = fixedIncomeList.filter { !it.is_recorded },
                            fixedExpenseList = fixedExpenseList.filter { !it.is_recorded },

                            onFixedItemSelect = { selectedCategory, selectedAmount, selectedMemo, selectedFixedItemId ->
                                category = selectedCategory
                                amount = selectedAmount
                                memo = selectedMemo
                                mainFixedItemId = selectedFixedItemId
                            },

                            // 지출 저장 로직
                            onSaveExpense = { expenseReq ->
                                scope.launch {
                                    try {
                                        val finalRequest = expenseReq.copy(fixed_item_id = mainFixedItemId)

                                        if (editId == null) {
                                            ApiClient.api.createExpense(expenseReq) // 신규 지출
                                        } else {
                                            editId?.let {
                                                ApiClient.api.updateExpense(
                                                    it,
                                                    expenseReq
                                                )
                                            }
                                        }
                                        clearForm()
                                        refreshExpenses()
                                        refreshAssets()
                                        refreshAssetHistory()
                                        refreshBudgets()
                                        currentScreen = Screen.LIST
                                    } catch (e: Exception) {
                                        statusMessage = "지출 저장 실패"
                                    }
                                }
                            },

                            // 수입 저장 로직
                            onSaveIncome = { incomeReq ->
                                scope.launch {
                                    try {
                                        val finalRequest = incomeReq.copy(fixed_item_id = mainFixedItemId)

                                        if (editId == null) {
                                            ApiClient.api.createIncome(incomeReq)
                                        } else {
                                            editId?.let {
                                                ApiClient.api.updateIncome(
                                                    it,
                                                    incomeReq
                                                )
                                            }
                                        }
                                        clearForm()
                                        refreshExpenses()
                                        refreshAssets()
                                        refreshAssetHistory()
                                        refreshBudgets()
                                        currentScreen = Screen.LIST
                                    } catch (e: Exception) {
                                        statusMessage = "수입 저장 실패"
                                    }
                                }
                            }
                        )
                    }
                }

                Screen.ASK -> {
                    AskSectionCard(
                        chatHistory = chatHistory,
                        currentQuestion = currentQuestion,
                        chatMode = chatMode,
                        isWaitingAnswer = isChatLoading,
                        hasPendingBudgetDraft = pendingBudgetDraft != null,
                        onQuestionChange = { currentQuestion = it },
                        onAskClick = { askGeneralQuestion() },
                        onAnalysisClick = { analyzeGeneralSpending() },
                        onBudgetModeClick = {
                            selectedBudgetMode = "balanced"
                            createBudgetDraftFromChat(
                                mode = "balanced",
                                userMessage = currentQuestion
                            )
                        },
                        onBudgetToneClick = { mode ->
                            selectedBudgetMode = mode
                            createBudgetDraftFromChat(
                                mode = mode,
                                userMessage = currentQuestion
                            )
                        },
                        onBudgetApplyClick = { applyBudgetDraft() },
                        onBudgetCancelClick = { cancelBudgetDraft() }
                    )
                }


                Screen.BUDGET -> {
                    LaunchedEffect(currentYearMonth) {
                        refreshBudget(currentYearMonth)
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        BudgetScreen(
                            currentYM = currentYearMonth,
                            budgetData = budgetData,
                            fixedIncomeTotal = totalFixedIncome,
                            fixedExpenseTotal = totalFixedExpense,
                            fixedIncomeList = fixedIncomeList,
                            fixedExpenseList = fixedExpenseList,

                            onIncomeRowClick = {
                                scope.launch {
                                    try {
                                        val fixedIncomes =
                                            ApiClient.api.getFixedIncomes(currentYearMonth)
                                        fixedIncomeList = fixedIncomes
                                        totalFixedIncome = fixedIncomes.sumOf { it.amount }
                                    } catch (e: Exception) {
                                        Log.e("BUDGET_CLICK", "고정 수입 목록 실시간 동기화 실패")
                                    }
                                }
                            },

                            onExpenseRowClick = {
                                scope.launch {
                                    try {
                                        val fixedExpenses =
                                            ApiClient.api.getFixedExpenses(currentYearMonth)
                                        fixedExpenseList = fixedExpenses
                                        totalFixedExpense = fixedExpenses.sumOf { it.amount }
                                    } catch (e: Exception) {
                                        Log.e("BUDGET_CLICK", "고정 지출 목록 실시간 동기화 실패")
                                    }
                                }
                            },

                            onAddFixedIncome = { category, amount, memo ->
                                scope.launch {
                                    try {
                                        isLoading = true
                                        ApiClient.api.createFixedIncome(
                                            yearMonth = currentYearMonth,
                                            request = FixedIncomeBudget(
                                                category = category,
                                                amount = amount,
                                                memo = memo
                                            )
                                        )
                                        val fixedIncomes =
                                            ApiClient.api.getFixedIncomes(currentYearMonth)
                                        fixedIncomeList = fixedIncomes
                                        totalFixedIncome = fixedIncomes.sumOf { it.amount }
                                        refreshBudget(currentYearMonth)
                                    } catch (e: Exception) {
                                        Log.e("FIXED_INC_ADD", "고정 수입 추가 실패: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            onAddFixedExpense = { category, amount, memo ->
                                scope.launch {
                                    try {
                                        isLoading = true
                                        ApiClient.api.createFixedExpense(
                                            yearMonth = currentYearMonth,
                                            request = FixedExpenseBudget(
                                                category = category,
                                                amount = amount,
                                                memo = memo
                                            )
                                        )
                                        val fixedExpenses =
                                            ApiClient.api.getFixedExpenses(currentYearMonth)
                                        fixedExpenseList = fixedExpenses
                                        totalFixedExpense = fixedExpenses.sumOf { it.amount }
                                        refreshBudget(currentYearMonth)
                                    } catch (e: Exception) {
                                        Log.e("FIXED_EXP_ADD", "고정 지출 추가 실패: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            // 고정 수입 항목 내용 수정
                            onUpdateFixedIncome = { id, category, amount, memo ->
                                scope.launch {
                                    try {
                                        isLoading = true
                                        ApiClient.api.updateFixedIncome(
                                            yearMonth = currentYearMonth,
                                            fixedIncomeId = id,
                                            request = FixedIncomeBudget(
                                                category = category,
                                                amount = amount,
                                                memo = memo
                                            )
                                        )
                                        refreshBudget(currentYearMonth) // 리로드 🔄
                                    } catch (e: Exception) {
                                        Log.e("FIXED_INC_UPDATE", "수정 실패: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            // 고정 수입 항목 제거
                            onDeleteFixedIncome = { id ->
                                scope.launch {
                                    try {
                                        isLoading = true
                                        ApiClient.api.deleteFixedIncome(
                                            yearMonth = currentYearMonth,
                                            fixedIncomeId = id
                                        )
                                        refreshBudget(currentYearMonth) // 리로드 🔄
                                    } catch (e: Exception) {
                                        Log.e("FIXED_INC_DELETE", "삭제 실패: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            // 고정 지출 항목 내용 수정
                            onUpdateFixedExpense = { id, category, amount, memo ->
                                scope.launch {
                                    try {
                                        isLoading = true
                                        ApiClient.api.updateFixedExpense(
                                            yearMonth = currentYearMonth,
                                            fixedExpenseId = id,
                                            request = FixedExpenseBudget(
                                                category = category,
                                                amount = amount,
                                                memo = memo
                                            )
                                        )
                                        refreshBudget(currentYearMonth) // 리로드 🔄
                                    } catch (e: Exception) {
                                        Log.e("FIXED_EXP_UPDATE", "수정 실패: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            // 고정 지출 항목 제거
                            onDeleteFixedExpense = { id ->
                                scope.launch {
                                    try {
                                        isLoading = true
                                        ApiClient.api.deleteFixedExpense(
                                            yearMonth = currentYearMonth,
                                            fixedExpenseId = id
                                        )
                                        refreshBudget(currentYearMonth) // 리로드 🔄
                                    } catch (e: Exception) {
                                        Log.e("FIXED_EXP_DELETE", "삭제 실패: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            onMonthChange = { newYM ->
                                currentYearMonth = newYM // 화살표 클릭 시 상태 업데이트
                            },

                            onUpdateCategoryBudget = { targetCategory, newLimitAmount ->
                                scope.launch {
                                    try {
                                        isLoading = true

                                        val currentDetails =
                                            budgetData?.budget_details?.toMutableMap()
                                                ?: mutableMapOf()
                                        currentDetails[targetCategory] = newLimitAmount

                                        val requestBody = BudgetDetailsUpdateRequest(
                                            budget_details = currentDetails
                                        )

                                        ApiClient.api.updateBudgetDetails(
                                            yearMonth = currentYearMonth,
                                            request = requestBody
                                        )

                                        refreshBudget(currentYearMonth)

                                    } catch (e: Exception) {
                                        Log.e("BUDGET_LIMIT_UPDATE", "한도 수정 실패: ${e.message}")
                                        statusMessage = "한도 수정에 실패했습니다."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },

                            onUpdateSaving = { newSavingAmount ->
                                scope.launch {
                                    try {
                                        isLoading = true

                                        val requestBody =
                                            SavingUpdateRequest(saving = newSavingAmount)

                                        ApiClient.api.updateSaving(
                                            yearMonth = currentYearMonth,
                                            request = requestBody
                                        )

                                        refreshBudget(currentYearMonth)
                                    } catch (e: Exception) {
                                        Log.e("BUDGET_SAVING_UPDATE", "목표 저축액 수정 실패: ${e.message}")
                                        statusMessage = "저축액 목표 수정에 실패했습니다."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }

                        )
                    }
                }

                Screen.CALENDAR -> CalendarScreen(
                    transactions = combinedTransactions,
                    assetHistory = assetHistory,
                    budgets = budgets,
                    currentAsset = assetData?.current_asset,
                    onAddClick = { selectedDate, selectedCategory ->
                        openAddScreen(
                            initialDate = selectedDate.toString(),
                            initialCategory = selectedCategory
                        )
                    },
                    onListClick = { currentScreen = Screen.LIST })
            }

            if (showAssetDialog) {
                AlertDialog(
                    onDismissRequest = { showAssetDialog = false },
                    containerColor = Color.White,
                    title = { Text("초기 자산 설정", fontWeight = FontWeight.ExtraBold) },
                    text = {
                        Column {
                            Text(
                                "앱을 시작할 때 가졌던 기준 자산을 입력해 주세요.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = inputInitialAsset,
                                onValueChange = { inputInitialAsset = it },
                                label = { Text("초기 자산 금액") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val assetAmount = inputInitialAsset.toIntOrNull() ?: 0
                                scope.launch {
                                    try {
                                        ApiClient.api.updateInitialAsset(
                                            InitialAssetRequest(
                                                initial_asset = assetAmount
                                            )
                                        )
                                        showAssetDialog = false
                                        refreshAssets()
                                        refreshAssetHistory()
                                    } catch (e: Exception) {
                                        Log.e("ASSET_DIALOG", "초기자산 업데이트 실패: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Text("저장")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAssetDialog = false }) {
                            Text("취소", color = Color.Gray)
                        }
                    }
                )
            }

            if (isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.23f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showSystemUi = true, name = "앱 화면 (기기 형태)")
@Composable
fun FullAppPreview() {
    HouseholdRAGTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HouseholdApp()
        }
    }
}
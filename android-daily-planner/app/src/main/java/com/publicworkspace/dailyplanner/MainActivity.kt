package com.publicworkspace.dailyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val recurrence: Recurrence
)

sealed interface Recurrence {
    data object Daily : Recurrence
    data class Weekly(val dayOfWeek: DayOfWeek) : Recurrence
    data class OneTime(val date: LocalDate) : Recurrence
}

private fun Task.isPlannedFor(date: LocalDate): Boolean = when (val r = recurrence) {
    Recurrence.Daily -> true
    is Recurrence.Weekly -> r.dayOfWeek == date.dayOfWeek
    is Recurrence.OneTime -> r.date == date
}

class PlannerViewModel : ViewModel() {
    private var tasks by mutableStateOf<List<Task>>(emptyList())

    fun addTask(title: String, recurrence: Recurrence) {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isBlank()) return
        tasks = tasks + Task(title = normalizedTitle, recurrence = recurrence)
    }

    fun removeTask(taskId: String) {
        tasks = tasks.filterNot { it.id == taskId }
    }

    fun tasksFor(date: LocalDate): List<Task> = tasks.filter { it.isPlannedFor(date) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                PlannerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlannerScreen(plannerViewModel: PlannerViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }
    var selectedDayOfWeek by remember { mutableStateOf(DayOfWeek.FRIDAY) }
    var oneTimeDate by remember { mutableStateOf(LocalDate.now()) }

    val tasksForSelectedDate = plannerViewModel.tasksFor(selectedDate)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda diária") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da tarefa") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Recorrência", style = MaterialTheme.typography.titleMedium)
            RecurrenceSelector(
                recurrenceType = recurrenceType,
                onRecurrenceSelected = { recurrenceType = it },
                selectedDayOfWeek = selectedDayOfWeek,
                onDaySelected = { selectedDayOfWeek = it }
            )

            if (recurrenceType == RecurrenceType.ONE_TIME) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Data da tarefa única", style = MaterialTheme.typography.titleSmall)
                DateNavigator(
                    date = oneTimeDate,
                    onDateChanged = { oneTimeDate = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                val recurrence = when (recurrenceType) {
                    RecurrenceType.DAILY -> Recurrence.Daily
                    RecurrenceType.FRIDAY_ONLY -> Recurrence.Weekly(DayOfWeek.FRIDAY)
                    RecurrenceType.WEEKDAY_CUSTOM -> Recurrence.Weekly(selectedDayOfWeek)
                    RecurrenceType.ONE_TIME -> Recurrence.OneTime(oneTimeDate)
                }
                plannerViewModel.addTask(title = title, recurrence = recurrence)
                title = ""
            }) {
                Text("Adicionar tarefa")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Filtrar agenda por data", style = MaterialTheme.typography.titleSmall)
            DateNavigator(
                date = selectedDate,
                onDateChanged = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tarefas para $selectedDate",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (tasksForSelectedDate.isEmpty()) {
                Text(
                    text = "Nenhuma tarefa para este dia.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasksForSelectedDate, key = { it.id }) { task ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    recurrenceLabel(task.recurrence),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = { plannerViewModel.removeTask(task.id) }) {
                                Text("Remover")
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class RecurrenceType {
    DAILY,
    FRIDAY_ONLY,
    WEEKDAY_CUSTOM,
    ONE_TIME
}

@Composable
private fun RecurrenceSelector(
    recurrenceType: RecurrenceType,
    onRecurrenceSelected: (RecurrenceType) -> Unit,
    selectedDayOfWeek: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit
) {
    val options = listOf(
        RecurrenceType.DAILY to "Diária",
        RecurrenceType.FRIDAY_ONLY to "Só sexta",
        RecurrenceType.WEEKDAY_CUSTOM to "Dia da semana",
        RecurrenceType.ONE_TIME to "Única"
    )

    options.forEach { (type, label) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = recurrenceType == type,
                onClick = { onRecurrenceSelected(type) }
            )
            Text(label)
        }
    }

    if (recurrenceType == RecurrenceType.WEEKDAY_CUSTOM) {
        DayOfWeek.entries.forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedDayOfWeek == day,
                    onClick = { onDaySelected(day) }
                )
                Text(dayLabel(day))
            }
        }
    }
}

@Composable
private fun DateNavigator(
    date: LocalDate,
    onDateChanged: (LocalDate) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { onDateChanged(date.minusDays(1)) }) { Text("-1 dia") }
        Text(date.toString())
        Button(onClick = { onDateChanged(date.plusDays(1)) }) { Text("+1 dia") }
    }
}

private fun recurrenceLabel(recurrence: Recurrence): String = when (recurrence) {
    Recurrence.Daily -> "Diária"
    is Recurrence.Weekly -> "Semanal: ${dayLabel(recurrence.dayOfWeek)}"
    is Recurrence.OneTime -> "Única: ${recurrence.date}"
}

private fun dayLabel(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Segunda"
    DayOfWeek.TUESDAY -> "Terça"
    DayOfWeek.WEDNESDAY -> "Quarta"
    DayOfWeek.THURSDAY -> "Quinta"
    DayOfWeek.FRIDAY -> "Sexta"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}

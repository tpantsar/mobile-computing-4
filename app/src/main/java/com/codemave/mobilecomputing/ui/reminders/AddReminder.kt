package com.codemave.mobilecomputing.ui.reminders

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.ui.login.SharedPreferences
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AddReminder(
    navController: NavController,
    context: Context,
    viewModel: ReminderViewModel = viewModel()
) {
    val timeContext = LocalContext.current

    val calendar = Calendar.getInstance()
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val time = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    val timePickerDialog = TimePickerDialog(
        timeContext,
        { _, hour: Int, minute: Int ->
            time.value = "%02d:%02d".format(hour, minute)
        },
        hour, minute, true
    )

    val datePickerDialog = DatePickerDialog(
        timeContext,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            date.value = "%02d.%02d.%04d".format(day, month + 1, year)
        },
        year, month, day
    )

    // Update time to current time
    timePickerDialog.updateTime(hour, minute)
    time.value = "%02d:%02d".format(hour, minute)
    datePickerDialog.updateDate(year, month, day)
    date.value = "%02d.%02d.%04d".format(day, month + 1, year)

    val reminderCalendar = Calendar.getInstance()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val coroutineScope = rememberCoroutineScope()
        val notificationTitle = rememberSaveable { mutableStateOf("") }
        val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)

        TopBar(
            backgroundColor = appBarColor
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.9f),
                value = notificationTitle.value,
                onValueChange = { notificationTitle.value = it },
                label = { Text(text = "Title") },
                shape = RoundedCornerShape(corner = CornerSize(50.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = RoundedCornerShape(corner = CornerSize(50.dp)),
                onClick = {
                    timePickerDialog.show()
                }
            ) {
                Text(text = "Time: " + time.value)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = RoundedCornerShape(corner = CornerSize(50.dp)),
                onClick = {
                    datePickerDialog.show()
                }
            ) {
                Text(text = "Date: " + date.value)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = RoundedCornerShape(corner = CornerSize(50.dp)),
                onClick = {
                    requestPermission(
                        context = context,
                        permission = Manifest.permission.ACCESS_FINE_LOCATION,
                        requestPermission = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
                    ).apply {
                        navController.navigate("map")
                    }
                }
            ) {
                Text(text = "Reminder location")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = RoundedCornerShape(corner = CornerSize(50.dp)),
                onClick = {
                    if (notificationTitle.value == "") {
                        notificationTitle.value = "Reminder"
                    }
                    val timeValues = time.value.split(":")
                    val dateValues = date.value.split(".")
                    val newYear = Integer.parseInt(dateValues[2])
                    val newMonth = Integer.parseInt(dateValues[1])
                    val newDay = Integer.parseInt(dateValues[0])
                    val newHour = Integer.parseInt(timeValues[0])
                    val newMinute = Integer.parseInt(timeValues[1])
                    reminderCalendar.set(newYear, newMonth - 1, newDay, newHour, newMinute)

                    coroutineScope.launch {
                        viewModel.saveReminder(
                            com.codemave.mobilecomputing.data.entity.Notification(
                                notificationTitle = notificationTitle.value,
                                notificationTime = time.value, // notificationTime.value,
                                notificationDate = date.value,
                                reminderTime = reminderCalendar.timeInMillis,
                                creationTime = Date().time,
                                creatorId = SharedPreferences(context).username,
                                notificationSeen = false,
                                locationX = null,
                                locationY = null
                            )
                        )
                    }
                    navController.popBackStack()
                    // }
                }
            ) {
                Text(text = "Save reminder")
            }
        }
    }
}

@Composable
private fun TopBar(
    backgroundColor: Color
) {
    TopAppBar(
        title = {
            Text(
                text = "New reminder",
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .heightIn(max = 24.dp)
            )
        },
        backgroundColor = backgroundColor
    )
}

private fun requestPermission(
    context: Context,
    permission: String,
    requestPermission: () -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        requestPermission()
    }
}
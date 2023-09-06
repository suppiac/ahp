package ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import org.koin.compose.koinInject
import viewmodel.AppAction
import viewmodel.AppViewModel

@Composable
fun App(vm: AppViewModel = koinInject()) {
    val uiState = vm.uiState.collectAsState().value
    
    Box(contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.width(640.dp).height(480.dp)) {
            // 输入框
            OutlinedTextField(
                value = uiState.fieldText,
                onValueChange = { vm.dispatch(AppAction.ChangeFieldText(it)) },
                textStyle = TextStyle(fontSize = 16.sp),
                label = { Text("输入矩阵：") },
                placeholder = { Text("e.g.:\n1 1 3\n1 1 3\n1/3 1/3 1") },
                modifier = Modifier.width(516.dp).height(240.dp).align(alignment = Alignment.CenterHorizontally),
            )
            
            // 按钮
            Button(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .width(516.dp)
                    .height(40.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                onClick = { vm.dispatch(AppAction.Calculate) }
            ) {
                Text("计算")
            }
            
            Divider(modifier = Modifier.width(516.dp).height(1.dp).align(alignment = Alignment.CenterHorizontally))
            
            // 矩阵权重
            Text(
                text = "权重矩阵W:${uiState.w}",
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .width(516.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            )
            
            // lambda_max CI CR
            Row(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .width(516.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                Text(text = "λm:${uiState.lambdaMax}", modifier = Modifier.width(172.dp))
                Text(text = "CI:${uiState.ci}", modifier = Modifier.width(172.dp))
                Text(text = "CR:${uiState.cr}", modifier = Modifier.width(172.dp))
            }
        }
    }
    
    DialogWindow(
        visible = uiState.error,
        onCloseRequest = { vm.dispatch(AppAction.SetErrorValue(false)) },
        title = "错误",
        state = rememberDialogState(width = 240.dp, height = 180.dp, position = WindowPosition(Alignment.Center)),
        resizable = false
    ) {
        Column(modifier = Modifier.width(270.dp).height(180.dp)) {
            Text(
                text = "输入数据不是矩阵！",
                modifier = Modifier
                    .width(210.dp)
                    .height(90.dp)
                    .padding(horizontal = 5.dp, vertical = 10.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.width(210.dp)
            ) {
                Button(
                    onClick = { vm.dispatch(AppAction.SetErrorValue(false)) },
                    modifier = Modifier.align(alignment = Alignment.BottomEnd)
                ) {
                    Text("确认")
                }
            }
        }
    }
}
package viewmodel

import Jama.Matrix
import constant.RI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.RoundingMode
import java.text.DecimalFormat

class AppViewModel {
    private val _uiSate = MutableStateFlow(AppState())
    val uiState = _uiSate.asStateFlow()
    
    private val format = DecimalFormat("#.####")
    
    fun dispatch(action: AppAction) {
        when (action) {
            is AppAction.ChangeFieldText -> changeFieldText(action.fieldText)
            is AppAction.Calculate -> calculate()
            is AppAction.SetErrorValue -> setErrorValue(action.error)
        }
    }
    
    private fun changeFieldText(fieldText: String) {
        _uiSate.value = uiState.value.copy(fieldText = fieldText)
    }
    
    private fun setErrorValue(error: Boolean) {
        _uiSate.value = uiState.value.copy(error = error)
    }
    
    private fun calculate() {
        format.roundingMode = RoundingMode.HALF_UP
        val strAList = uiState.value.fieldText.split('\n')
        val length = strAList.size
        
        // 计算输入矩阵A
        val a = Array(length) { FloatArray(length) }
        val aD = Array(length) { DoubleArray(length) }
        
        for (index in 0 until length) {
            val temp = strAList[index].split(' ')
            
            if (temp.size != length) {
                _uiSate.value = uiState.value.copy(error = true)
                break
            }
            
            for (i in temp.indices) {
                if (temp[i].isEmpty()) {
                    _uiSate.value = uiState.value.copy(error = true)
                    break
                }
                val numList = temp[i].split('/')
                if (numList.size > 1) {
                    a[index][i] = numList[0].toFloat() / numList[1].toFloat()
                    aD[index][i] = a[index][i].toDouble()
                } else {
                    a[index][i] = temp[i].toFloat()
                    aD[index][i] = temp[i].toDouble()
                }
            }
        }
        
        if (uiState.value.error) return
        
        // 矩阵A每列元素相加
        val sumRow = FloatArray(length)
        for (i in 0 until length) {
            for (j in 0 until length) {
                sumRow[i] += a[j][i]
            }
        }
        
        // A / sumRow
        val b = Array(length) { FloatArray(length) }
        for (i in 0 until length) {
            for (j in 0 until length) {
                b[j][i] = a[j][i] / sumRow[i]
            }
        }
        
        // 矩阵B每行元素相加
        val sumCol = FloatArray(length)
        for (i in 0 until length) {
            for (j in 0 until length) {
                sumCol[i] += b[i][j]
            }
        }
        
        // 计算权重矩阵
        val wArray = FloatArray(length)
        val wArrayD = Array(length) { DoubleArray(1) }
        for (i in 0 until length) {
            wArray[i] += sumCol[i] / sumCol.sum()
            wArrayD[i][0] += sumCol[i].toDouble() / sumCol.sum().toDouble()
        }
        var w = "["
        for (i in 0 until length) {
            w += format.format(wArray[i]).toString()
            if (i != length - 1) {
                w += ", "
            }
        }
        w += "]"
        
        // C dot W
        val cw = Matrix(aD).times(Matrix(wArrayD)).array
        
        // 计算最大特征根
        var lm = 0.0
        for (i in 0 until length) {
            lm += (cw[i][0].toFloat() / wArray[i])
        }
        lm /= length.toFloat()
        
        // ci
        val ciN = (lm - length) / (length - 1)
        
        _uiSate.value = uiState.value.copy(
            w = w,
            lambdaMax = format.format(lm).toString(),
            ci = format.format(ciN).toString(),
            cr = format.format(ciN / RI[length - 1]).toString()
        )
    }
}

data class AppState(
    val fieldText: String = "",
    val error: Boolean = false,
    val w: String = "",
    val lambdaMax: String = "",
    val ci: String = "",
    val cr: String = "",
)

sealed class AppAction {
    class ChangeFieldText(val fieldText: String) : AppAction()
    data object Calculate : AppAction()
    class SetErrorValue(val error: Boolean) : AppAction()
}
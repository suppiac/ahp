import org.koin.dsl.module
import viewmodel.AppViewModel

val appModule = module {
    single { AppViewModel() }
}
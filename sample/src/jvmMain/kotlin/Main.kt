import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pedrobneto.mockengine.network.KtorfitProvider
import com.pedrobneto.mockengine.network.SampleRepository
import com.pedrobneto.mockengine.network.createSampleApi
import com.pedrobneto.mockengine.ui.App
import com.pedrobneto.mockengine.ui.SampleViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun main() = application {
    startKoin {
        printLogger()
        modules(
            module {
                single {
                    Json {
                        encodeDefaults = true
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                }

                singleOf(KtorfitProvider::get)
                single { get<Ktorfit>().createSampleApi() }

                factoryOf(::SampleRepository)
                viewModelOf(::SampleViewModel)
            }
        )
    }

    Window(onCloseRequest = ::exitApplication, title = "Mock Engine") {
        App()
    }
}

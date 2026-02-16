package aldtoll.twiligihts.ui.screen.final_screen

import aldtoll.twiligihts.R
import aldtoll.twiligihts.logic.database.DatabaseInteractor.Companion.PREFIX
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FinalScreen : Fragment() {

    private val viewModel by viewModels<FinalScreenViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                FinalScreenCompose()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.finishBattle()
        viewModel.reinit()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_finalScreen_to_startScreenFragment)
        }
    }


    @Composable
    fun FinalScreenCompose(
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Бой завершен",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = { shareLogs() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
            ) {
                Text("Отправить лог")
            }
        }
    }

    private fun shareLogs(): Boolean {
        val events = viewModel.getEvents()
        if (events.isEmpty()) return false

        // Собираем все сообщения
        val logText = events.joinToString("\n") { it.message }

        try {
            // Создаем временный файл
            val file = File(requireContext().cacheDir, "battle_log_${PREFIX}.txt")
            file.writeText(logText)

            // Создаем URI для файла (для Android 7+ нужен FileProvider)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider", // Замените на ваш fileprovider authority
                file
            )

            // Создаем интент для шаринга
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Запускаем активити для выбора приложения
            requireContext().startActivity(Intent.createChooser(shareIntent, "Share battle log"))

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    @Preview
    @Composable
    fun SimpleComposablePreview() {
        FinalScreenCompose()
    }
}

package de.spiritcroc.cubescrambler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.caverock.androidsvg.SVG
import de.spiritcroc.cubescrambler.ui.theme.CubeScramblerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.worldcubeassociation.tnoodle.puzzle.CubePuzzle
import org.worldcubeassociation.tnoodle.scrambles.Puzzle
import kotlin.random.Random
import kotlin.random.asJavaRandom


val puzzles = mapOf(
    "2x2" to CubePuzzle(2),
    "3x3" to CubePuzzle(3),
    "4x4" to CubePuzzle(4),
    "5x5" to CubePuzzle(5),
    "6x6" to CubePuzzle(6),
    "7x7" to CubePuzzle(7),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel: ScramblerViewModel by viewModels()
        super.onCreate(savedInstanceState)
        setContent {
            CubeScramblerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    ScrambleArea(viewModel)
                }
            }
        }
    }
}

class ScramblerViewModel(val random: Random = Random(System.currentTimeMillis()),
                         initialScrambles: List<ScrambleInfo> = listOf()) : ViewModel() {
    val scrambles: MutableStateFlow<List<ScrambleInfo>> = MutableStateFlow(initialScrambles)
}

data class ScrambleInfo(val puzzle: Puzzle, val scramble: String, val svg: SVG)

fun generateScramble(puzzle: Puzzle, random: Random): ScrambleInfo {
    val scramble = puzzle.generateWcaScramble(random.asJavaRandom())
    val svg = SVG.getFromString(puzzle.drawScramble(scramble, null).toString())
    return ScrambleInfo(puzzle, scramble, svg)
}

@Composable
fun ScrambleButton(viewModel: ScramblerViewModel,
                   puzzleName: String,
                   puzzle: Puzzle,
                   listState: LazyListState) {
    val uiScope = rememberCoroutineScope()
    val bgScope = rememberCoroutineScope { Dispatchers.IO }
    Button(onClick = {
        bgScope.launch {
            viewModel.scrambles.emit(listOf(
                generateScramble(puzzle, viewModel.random),
                *viewModel.scrambles.value.toTypedArray()
            ))
            uiScope.launch {
                listState.animateScrollToItem(0, 0)
            }
        }
    }) {
        Text(puzzleName)

    }
}

@Composable
fun ScrambleSelection(viewModel: ScramblerViewModel, listState: LazyListState) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(4.dp)) {
        for (puzzle in puzzles) {
            Column(modifier = Modifier.padding(4.dp)) {
                ScrambleButton(viewModel, puzzle.key, puzzle.value, listState)
            }
        }
    }
}

@Composable
fun Scramble(scramble: ScrambleInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SelectionContainer {
            Text(scramble.scramble, fontSize = 30.sp, modifier = Modifier.padding(8.dp))
        }
        ScrambleImage(scramble)
    }
}

@Composable
fun ScrambleImage(scramble: ScrambleInfo) {
    Canvas(modifier = Modifier
        .aspectRatio(scramble.svg.documentAspectRatio)
        .fillMaxWidth(),
        onDraw = {
            this.drawIntoCanvas { canvas ->
                val scale = 900f/scramble.svg.documentWidth
                canvas.scale(scale, scale)
                scramble.svg.renderToCanvas(canvas.nativeCanvas)
            }
        }
    )
}

@Composable
fun Scrambles(viewModel: ScramblerViewModel, listState: LazyListState) {
    val currentList = viewModel.scrambles.asStateFlow().collectAsState()
    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        items(currentList.value) { scramble ->
            Scramble(scramble)
        }
    }
}

@Composable
fun ScrambleArea(viewModel: ScramblerViewModel) {
    val listState = rememberLazyListState()
    Column {
        ScrambleSelection(viewModel, listState)
        Scrambles(viewModel, listState)
    }
}

@Preview
@Composable
fun ScramblePreview() {
    val random = Random(0)
    val viewModel = ScramblerViewModel(
        random = random,
        /*
        listOf(
            generateScramblePair(puzzles["3x3"]!!, random),
            generateScramblePair(puzzles["3x3"]!!, random),
            generateScramblePair(puzzles["3x3"]!!, random),
            generateScramblePair(puzzles["3x3"]!!, random),
        )
         */
    )
    ScrambleArea(viewModel)
}
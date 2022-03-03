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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.worldcubeassociation.tnoodle.puzzle.CubePuzzle
import org.worldcubeassociation.tnoodle.scrambles.Puzzle
import org.worldcubeassociation.tnoodle.svglite.Svg
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
                         initialScrambles: List<Pair<Puzzle, String>> = listOf()) : ViewModel() {
    val scrambles: MutableStateFlow<List<Pair<Puzzle, String>>> = MutableStateFlow(initialScrambles)
}

fun generateScramblePair(puzzle: Puzzle, random: Random): Pair<Puzzle, String> {
    return puzzle to puzzle.generateWcaScramble(random.asJavaRandom())
}

@Composable
fun ScrambleButton(viewModel: ScramblerViewModel,
                   puzzleName: String,
                   puzzle: Puzzle,
                   listState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            viewModel.scrambles.emit(listOf(
                generateScramblePair(puzzle, viewModel.random),
                *viewModel.scrambles.value.toTypedArray()
            ))
            listState.animateScrollToItem(0, 0)
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
fun Scramble(puzzle: Puzzle, scramble: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SelectionContainer {
            Text(scramble, fontSize = 30.sp, modifier = Modifier.padding(8.dp))
        }
        ScrambleImage(puzzle, scramble)
    }
}

@Composable
fun ScrambleImage(puzzle: Puzzle, scramble: String) {
    val wcaSvg: Svg = puzzle.drawScramble(scramble, null)
    val svg: SVG = SVG.getFromString(wcaSvg.toString())
    Canvas(modifier = Modifier
        .aspectRatio(svg.documentAspectRatio)
        .fillMaxWidth(),
        onDraw = {
            this.drawIntoCanvas { canvas ->
                val scale = 900f/svg.documentWidth
                canvas.scale(scale, scale)
                svg.renderToCanvas(canvas.nativeCanvas)
            }
        }
    )
}

@Composable
fun Scrambles(viewModel: ScramblerViewModel, listState: LazyListState) {
    val currentList = viewModel.scrambles.asStateFlow().collectAsState()
    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        items(currentList.value) { ps ->
            Scramble(ps.first, ps.second)
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
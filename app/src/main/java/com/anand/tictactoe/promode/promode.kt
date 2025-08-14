package com.anand.tictactoe.promode

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.anand.tictactoe.R
import com.anand.tictactoe.databinding.FragmentPromodeBinding

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class promode : Fragment() {


    // Game state variables
    private var currentPlayer = "X"
    private var gameBoard = Array(3) { Array(3) { "" } }
    private var gameActive = true
    private var playerXScore = 0
    private var playerOScore = 0

    // Track player moves for removal (stores position of moves in order)
    private val playerXMoves = mutableListOf<Pair<Int, Int>>()
    private val playerOMoves = mutableListOf<Pair<Int, Int>>()

    // Track move order for visual feedback
    private var moveCounter = 0

    // UI elements
    private lateinit var statusText: TextView
    private lateinit var scoreX: TextView
    private lateinit var scoreO: TextView
    private lateinit var gameButtons: Array<Array<Button>>
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_promode, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initializeViews()
            initializeGame()
            setupClickListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Error initializing game: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initializeViews() {
        statusText = rootView.findViewById(R.id.statusText)
        scoreX = rootView.findViewById(R.id.scoreX)
        scoreO = rootView.findViewById(R.id.scoreO)

        // Initialize game buttons array with proper error checking
        gameButtons = Array(3) { row ->
            Array(3) { col ->
                val buttonId =
                    resources.getIdentifier("btn$row$col", "id", requireContext().packageName)
                if (buttonId == 0) {
                    throw IllegalStateException("Button btn$row$col not found in layout")
                }
                val button = rootView.findViewById<Button>(buttonId)
                button.tag = "$row,$col"
                button
            }
        }

        updateStatusText()
        updateScoreDisplay()
    }

    private fun setupClickListeners() {
        // Set up cell click listeners
        for (row in 0..2) {
            for (col in 0..2) {
                gameButtons[row][col].setOnClickListener { onCellClick(row, col) }
            }
        }

        // Set up button click listeners
        rootView.findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }
        rootView.findViewById<Button>(R.id.newGameButton).setOnClickListener { newGame() }
    }

    private fun initializeGame() {
        currentPlayer = "X"
        gameBoard = Array(3) { Array(3) { "" } }
        gameActive = true
        moveCounter = 0

        // Clear move tracking
        playerXMoves.clear()
        playerOMoves.clear()

        // Clear all buttons
        for (row in 0..2) {
            for (col in 0..2) {
                gameButtons[row][col].text = ""
                gameButtons[row][col].isEnabled = true
                gameButtons[row][col].setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
                // Reset text size to normal
                gameButtons[row][col].textSize = 36f
            }
        }

        updateStatusText()
    }

    private fun onCellClick(row: Int, col: Int) {
        if (!gameActive) return

        // Check if cell is occupied by current player (allow overwriting opponent's pieces)
        if (gameBoard[row][col] == currentPlayer) {
            Toast.makeText(requireContext(), "You already have a piece here!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        makeMove(row, col)
    }

    private fun makeMove(row: Int, col: Int) {
        moveCounter++

        // Remove opponent's piece if this cell was occupied
        if (gameBoard[row][col].isNotEmpty()) {
            val opponent = gameBoard[row][col]
            removeFromMoveList(row, col, opponent)
        }

        // Handle piece limit (3 pieces max per player)
        val currentMoves = if (currentPlayer == "X") playerXMoves else playerOMoves

        if (currentMoves.size >= 3) {
            // Remove the oldest piece
            val oldestMove = currentMoves.removeAt(0)
            val oldRow = oldestMove.first
            val oldCol = oldestMove.second

            // Clear the oldest piece from board and UI
            gameBoard[oldRow][oldCol] = ""
            gameButtons[oldRow][oldCol].text = ""
            gameButtons[oldRow][oldCol].setBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.white)
            )
            gameButtons[oldRow][oldCol].isEnabled = true
            gameButtons[oldRow][oldCol].textSize = 36f

            showRemovalFeedback(oldRow, oldCol, currentPlayer)
        }

        // Place new piece
        gameBoard[row][col] = currentPlayer
        currentMoves.add(Pair(row, col))

        // Update UI
        val button = gameButtons[row][col]
        button.text = currentPlayer
        button.isEnabled = false

        // Set color and visual feedback for new piece
        if (currentPlayer == "X") {
            button.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_red_dark
                )
            )
        } else {
            button.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_blue_dark
                )
            )
        }

        // Visual feedback for newest piece
        button.textSize = 40f
        button.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.holo_orange_light
            )
        )

        // Reset previous pieces' visual state
        updatePieceVisuals()

        // Check for win
        when {
            checkWinner() -> {
                gameActive = false
                statusText.text = "🎉 Player $currentPlayer Wins! 🎉"
                updateScore()
                highlightWinningCells()
                showGameOverToast("Player $currentPlayer Wins!")
            }

            else -> {
                // Switch player
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                updateStatusText()
            }
        }
    }

    private fun removeFromMoveList(row: Int, col: Int, player: String) {
        val moves = if (player == "X") playerXMoves else playerOMoves
        moves.removeIf { it.first == row && it.second == col }
    }

    private fun showRemovalFeedback(row: Int, col: Int, player: String) {
        Toast.makeText(
            requireContext(),
            "Player $player's oldest piece removed from (${row + 1}, ${col + 1})",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updatePieceVisuals() {
        // Reset all pieces to normal visual state first
        for (row in 0..2) {
            for (col in 0..2) {
                if (gameBoard[row][col].isNotEmpty()) {
                    gameButtons[row][col].textSize = 36f
                    gameButtons[row][col].setBackgroundColor(
                        ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
                    )
                }
            }
        }

        // Highlight newest pieces
        highlightNewestPieces(playerXMoves, "X")
        highlightNewestPieces(playerOMoves, "O")
    }

    private fun highlightNewestPieces(moves: List<Pair<Int, Int>>, player: String) {
        if (moves.isNotEmpty()) {
            val newestMove = moves.last()
            val row = newestMove.first
            val col = newestMove.second
            gameButtons[row][col].setBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light)
            )
        }
    }

    private fun checkWinner(): Boolean {
        // Check rows
        for (row in 0..2) {
            if (gameBoard[row][0] == currentPlayer &&
                gameBoard[row][1] == currentPlayer &&
                gameBoard[row][2] == currentPlayer
            ) {
                return true
            }
        }

        // Check columns
        for (col in 0..2) {
            if (gameBoard[0][col] == currentPlayer &&
                gameBoard[1][col] == currentPlayer &&
                gameBoard[2][col] == currentPlayer
            ) {
                return true
            }
        }

        // Check diagonals
        if (gameBoard[0][0] == currentPlayer &&
            gameBoard[1][1] == currentPlayer &&
            gameBoard[2][2] == currentPlayer
        ) {
            return true
        }

        if (gameBoard[0][2] == currentPlayer &&
            gameBoard[1][1] == currentPlayer &&
            gameBoard[2][0] == currentPlayer
        ) {
            return true
        }

        return false
    }

    private fun highlightWinningCells() {
        val winColor = ContextCompat.getColor(requireContext(), android.R.color.holo_green_light)

        // Check rows
        for (row in 0..2) {
            if (gameBoard[row][0] == currentPlayer &&
                gameBoard[row][1] == currentPlayer &&
                gameBoard[row][2] == currentPlayer
            ) {
                for (col in 0..2) {
                    gameButtons[row][col].setBackgroundColor(winColor)
                }
                return
            }
        }

        // Check columns
        for (col in 0..2) {
            if (gameBoard[0][col] == currentPlayer &&
                gameBoard[1][col] == currentPlayer &&
                gameBoard[2][col] == currentPlayer
            ) {
                for (row in 0..2) {
                    gameButtons[row][col].setBackgroundColor(winColor)
                }
                return
            }
        }

        // Check main diagonal
        if (gameBoard[0][0] == currentPlayer &&
            gameBoard[1][1] == currentPlayer &&
            gameBoard[2][2] == currentPlayer
        ) {
            for (i in 0..2) {
                gameButtons[i][i].setBackgroundColor(winColor)
            }
            return
        }

        // Check anti-diagonal
        if (gameBoard[0][2] == currentPlayer &&
            gameBoard[1][1] == currentPlayer &&
            gameBoard[2][0] == currentPlayer
        ) {
            gameButtons[0][2].setBackgroundColor(winColor)
            gameButtons[1][1].setBackgroundColor(winColor)
            gameButtons[2][0].setBackgroundColor(winColor)
        }
    }

    private fun updateScore() {
        if (currentPlayer == "X") {
            playerXScore++
        } else {
            playerOScore++
        }
        updateScoreDisplay()
    }

    private fun updateStatusText() {
        if (gameActive) {
            val currentMoves = if (currentPlayer == "X") playerXMoves else playerOMoves
            val piecesCount = currentMoves.size
            statusText.text = "Player $currentPlayer's Turn (${piecesCount}/3 pieces)"
        }
    }

    private fun updateScoreDisplay() {
        scoreX.text = playerXScore.toString()
        scoreO.text = playerOScore.toString()
    }

    private fun showGameOverToast(message: String) {
        Toast.makeText(
            requireContext(),
            "$message\nTap 'Reset Game' to play again",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun resetGame() {
        initializeGame()
        Toast.makeText(requireContext(), "Pro Mode Game Reset!", Toast.LENGTH_SHORT).show()
    }

    private fun newGame() {
        initializeGame()
        playerXScore = 0
        playerOScore = 0
        updateScoreDisplay()
        Toast.makeText(requireContext(), "New Pro Mode Game Started!", Toast.LENGTH_SHORT).show()
    }
}

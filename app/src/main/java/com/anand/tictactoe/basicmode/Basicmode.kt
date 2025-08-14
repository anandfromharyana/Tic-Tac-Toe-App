package com.anand.tictactoe.basicmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.anand.tictactoe.R

class Basicmode : Fragment() {

    // Game state variables
    private var currentPlayer = "X"
    private var gameBoard = Array(3) { Array(3) { "" } }
    private var gameActive = true
    private var playerXScore = 0
    private var playerOScore = 0

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
        rootView = inflater.inflate(R.layout.fragment_basicmode, container, false)
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
                // Set the tag to encode row and col information
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

        // Clear all buttons
        for (row in 0..2) {
            for (col in 0..2) {
                gameButtons[row][col].text = ""
                gameButtons[row][col].isEnabled = true
                gameButtons[row][col].setBackgroundColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
            }
        }

        updateStatusText()
    }

    // Handle cell clicks - simplified version
    private fun onCellClick(row: Int, col: Int) {
        if (!gameActive) return

        // Check if cell is already occupied
        if (gameBoard[row][col].isNotEmpty()) {
            Toast.makeText(requireContext(), "Cell already occupied!", Toast.LENGTH_SHORT).show()
            return
        }

        // Make move
        makeMove(row, col, gameButtons[row][col])
    }

    private fun makeMove(row: Int, col: Int, button: Button) {
        // Update game board and UI
        gameBoard[row][col] = currentPlayer
        button.text = currentPlayer
        button.isEnabled = false

        // Set color for X and O
        if (currentPlayer == "X") {
            button.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )
        } else {
            button.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
            )
        }

        // Check for win or draw
        when {
            checkWinner() -> {
                gameActive = false
                statusText.text = "Player $currentPlayer Wins!"
                updateScore()
                highlightWinningCells()
                showGameOverToast("Player $currentPlayer Wins!")
            }

            isBoardFull() -> {
                gameActive = false
                statusText.text = "It's a Draw!"
                showGameOverToast("It's a Draw!")
            }

            else -> {
                // Switch player
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                updateStatusText()
            }
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

    private fun isBoardFull(): Boolean {
        for (row in 0..2) {
            for (col in 0..2) {
                if (gameBoard[row][col].isEmpty()) {
                    return false
                }
            }
        }
        return true
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
            statusText.text = "Player $currentPlayer's Turn"
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

    // Handle reset button click
    private fun resetGame() {
        initializeGame()
        Toast.makeText(requireContext(), "Game Reset!", Toast.LENGTH_SHORT).show()
    }

    // Handle new game button click
    private fun newGame() {
        initializeGame()
        playerXScore = 0
        playerOScore = 0
        updateScoreDisplay()
        Toast.makeText(requireContext(), "New Game Started!", Toast.LENGTH_SHORT).show()
    }

}
package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.AbstractState.MOVE;
import model.State;
import eval.Evaluator;
import eval.ScoreEvaluator;

public class GreedyPlayerEval extends AbstractPlayer {
	
	private final Evaluator eval = new ScoreEvaluator();
	private final Random random = new Random();

	@Override
	public MOVE getMove(State game) {

		pause();
		
		List<MOVE> bestMoves = new ArrayList<MOVE>();
		
		double bestScore = Double.NEGATIVE_INFINITY;
		
		for(MOVE move : game.getMoves()) {
			
			game.move(move);
			double score = eval.evaluate(game);
			game.undo();
			
			if(score > bestScore) {
				bestMoves.clear();
				bestMoves.add(move);
				bestScore = score;
			} else if (score == bestScore) {
				bestMoves.add(move);
			}
			
		}
		
		return bestMoves.get(random.nextInt(bestMoves.size()));
	}

	@Override
	public int studentID() {
		return 201181111;
	}

	@Override
	public String studentName() {
		return "Phil Rodgers";
	}

}

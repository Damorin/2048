package ai;

import java.util.List;

import model.AbstractState.MOVE;
import model.State;
import eval.BonusEvaluator;
import eval.Evaluator;
import eval.FastEvaluator;
import eval.ScoreEvaluator;

public class BaselinePlayer extends AbstractPlayer {
	
	private final int depthLimit = 2;
	private final int iterations = 100;
	private Evaluator eval = new BonusEvaluator();

	@Override
	public MOVE getMove(State game) {
		pause();
		double bestScore = -1;
		List<MOVE> moves = game.getMoves();
		MOVE bestMove = moves.get(0);
		for(MOVE move : moves) {
			game.move(move);
			double score = 0;
			for(int i = 0 ; i < iterations ; i++) {
				score += dls(game, 0);
			}
			score /= iterations;
			if(score > bestScore) {
				bestScore = score;
				bestMove = move;
			}
			game.undo();
		}
		return bestMove;
	}
	
	private double dls(State game, int depth) {
		if(depth >= depthLimit) {
			return eval.evaluate(game);
		}
		double bestScore = -1;
		for(MOVE move : game.getMoves()) {
			game.move(move);
			bestScore = Math.max(dls(game, depth+1), bestScore);
			game.undo();
		}
		return bestScore;
	}

	@Override
	public int studentID() {
		return 201181111;
	}

	@Override
	public String studentName() {
		return "Baseline Player";
	}

}

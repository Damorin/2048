package ai;

import static model.AbstractState.MOVE.LEFT;
import static model.AbstractState.MOVE.RIGHT;
import static model.AbstractState.MOVE.UP;

import java.util.List;

import model.AbstractState.MOVE;
import model.State;

public class UpPlayer extends AbstractPlayer {

	private boolean left = true;
	private MOVE move = UP;
	
	@Override
	public MOVE getMove(State game) {
		
		pause();
		
		List<MOVE> moves = game.getMoves();
		if(moves.size()==1) {
			return moves.get(0);
		}
		if(moves.contains(UP)) {
			move = UP;
		} else {
			if(left) {
				if(moves.contains(LEFT)) {
					move = LEFT;
				} else {
					left = false;
					move = RIGHT;
				}
			} else {
				if(moves.contains(RIGHT)) {
					move = RIGHT;
				} else {
					left = true;
					move = LEFT;
				}
			}
		}
				
		return move;
	}

	@Override
	public int studentID() {
		return 201181111;
	}

	@Override
	public String studentName() {
		return "Up Player";
	}

}

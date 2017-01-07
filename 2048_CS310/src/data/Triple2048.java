package data;

public class Triple2048 {
	public final int score;
	public final int tile;
	public final int time;
	public Triple2048(int score, int tile, int time) {
		this.score = score;
		this.tile = tile;
		this.time = time;
	}
	public String toString() {
		return score + ", " + tile + "," + time;
	}
}

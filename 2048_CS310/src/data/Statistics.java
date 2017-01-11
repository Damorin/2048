package data;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.BaselinePlayer;
import ai.Player;
import model.AbstractState.MOVE;
import model.BinaryState;

public class Statistics {
	private final int nGames;
	private double totalScore = 0;
	private int highScore = 0;
	private int highTile = 0;
	private final int[] results;
	private final int[] highTiles = new int[15];
	private final Player player;
	private final BinaryState game = new BinaryState();
	private double mean;
	private double standardDeviation;

	public Statistics(int nGames, Player player) {
		this.nGames = nGames;
		results = new int[nGames];
		this.player = player;
	}

	public void reset() {
		highScore = 0;
		highTile = 0;
		standardDeviation = 0;
		mean = 0;
		for (int t = 0; t < 15; t++) {
			highTiles[t] = 0;
		}
		game.reset();
	}

	public class GamePlayer implements Callable<Point> {

		@Override
		public Point call() throws Exception {
			BinaryState game = new BinaryState();
			List<MOVE> moves = game.getMoves();
			while (!moves.isEmpty()) {
				MOVE move = player.getMove(game);
				game.move(move);
				moves = game.getMoves();
			}
			return new Point(game.getScore(), game.getHighestTileValue());
		}

	}

	public void begin() {
		long start = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		List<Future<Point>> list = new ArrayList<Future<Point>>();
		for (int i = 0; i < nGames; i++) {
			Callable<Point> worker = new GamePlayer();
			Future<Point> submit = executor.submit(worker);
			list.add(submit);
		}
		int i = 0;
		for (Future<Point> future : list) {
			try {
				Point result = future.get();
				results[i] = result.x;
				totalScore += result.x;
				highScore = Math.max(highScore, results[i]);
				int ht = result.y;
				highTile = Math.max(highTile, ht);
				highTiles[Integer.numberOfTrailingZeros(ht) - 1]++;
				System.out.println((i++) + " " + result);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		standardDeviation = calculateStandardDeviation();
		System.out.println(System.currentTimeMillis() - start);
	}

	private double calculateStandardDeviation() {
		mean = totalScore / nGames;
		double sumSqDiff = 0.0;
		for (int i = 0; i < nGames; i++) {
			double diff = results[i] - mean;
			sumSqDiff += diff * diff;
		}
		return Math.sqrt(sumSqDiff / nGames);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("100");
		sb.append("\nMean:               " + mean);
		sb.append("\nStandard deviation: " + standardDeviation);
		sb.append("\nHighest score:      " + highScore);
		sb.append("\nHighest tile:       " + highTile);
		sb.append("\nTile counts:        |");
		sb.append(
				"Calculating potential grade - Disclaimer: This is not the final grade and may be effected by machine performance");
		sb.append("\nGrade:              " + calculatePossibleGrade());
		for (int t : highTiles) {
			sb.append(t + "|");
		}
		return sb.toString();
	}

	private String calculatePossibleGrade() {
		double grade = 0;
		int minScore = 2000;
		int averageScore = 32000;
		if (mean < minScore) {
			return String.valueOf(grade);
		} else if (mean < averageScore) {
			grade = 40 + ((mean - minScore)/averageScore) * 30;
			return String.valueOf(grade);
		}
		grade = 70 + ((mean - averageScore) / mean) *30;
		return String.valueOf(grade);
	}

	public static void main(String[] args) {
		Statistics s = new Statistics(100, new BaselinePlayer());
		s.begin();
		System.out.println(s);
		// FileWriter results = null;
		// try {
		// results = new FileWriter(new File("1000000_nt.txt"), false);
		// results.write(s.toString());
		// results.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}

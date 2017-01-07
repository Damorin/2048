package data;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;

import model.AbstractState.MOVE;
import model.BinaryState;
import view.FancyPanel;
import ai.Player;
import controller.Controller;

public class MultiStatistics {
	private final int nGames;
	private double totalScore = 0;
	private int highScore = 0;
	private int highTile = 0;
	private final int[] results;
	private final int[] highTiles = new int[15];
	private final int[] tileValues = new int[]{0,2,4,8,16,32,64,128,256,512,1024,2048,4096,8192,16384,32768,65536};
	private final Player player;
	private final BinaryState game = new BinaryState();
	private double mean;
	private double standardDeviation;
	private final FancyPanel panel;
	private final JFrame frame;
	
	public MultiStatistics (int nGames, Player player) {
		this.nGames = nGames;
		results = new int[nGames];
		this.player = player;
		
		panel = new FancyPanel(game);
		panel.setPreferredSize(new Dimension(600, 600));

		frame = new JFrame("Score: 0");
		
		frame.getContentPane().add(panel);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.pack();
	}
	
	public void reset() {
		highScore = 0;
		highTile = 0;
		standardDeviation = 0;
		mean = 0;
		for(int t = 0 ; t < 15 ; t++) {
			highTiles[t] = 0;
		}
		game.reset();
	}
	public class GamePlayer implements Callable<Triple2048> {

		@Override
		public Triple2048 call() throws Exception {
			
			game.reset();
			
			List<MOVE> moves = game.getMoves();
			int badCount = 0;
			while(!moves.isEmpty()) {
				long t1 = System.currentTimeMillis();
				MOVE move = player.getMove(game.copy());
				
				if(moves.contains(move)) {
					game.updateTime((int) (System.currentTimeMillis() - t1));
					game.move(move);
					moves = game.getMoves();
					badCount = 0;
					frame.setTitle("Score: " + game.getScore());
					panel.repaint();
				} else {
					badCount++;
//					System.err.println(badCount);
					if(badCount == 10) {
						return new Triple2048(game.getScore(), game.getHighestTileValue(), game.getAvgTime());
					}
				}
			}
			return new Triple2048(game.getScore(), game.getHighestTileValue(), game.getAvgTime());
		}

		
	}
	
	public void begin() {
		long start = System.currentTimeMillis();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		List<Future<Triple2048>> list = new ArrayList<Future<Triple2048>>();
		for (int i = 0; i < nGames; i++) {
			Callable<Triple2048> worker = new GamePlayer();
			Future<Triple2048> submit = executor.submit(worker);
			list.add(submit);
		}
	    int i = 0;
	    FileWriter scores = null, tiles = null, times = null;
	    try {
	    	
	    	scores = new FileWriter(new File("res/scores.csv"), true);
			scores.append(player.studentName() + "," + player.studentID() + ",");
			
			tiles = new FileWriter(new File("res/tiles.csv"), true);
			tiles.append(player.studentName() + "," + player.studentID() + ",");
			
			times = new FileWriter(new File("res/times.csv"), true);
			times.append(player.studentName() + "," + player.studentID() + ",");
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (Future<Triple2048> future : list) {
			try {
				Triple2048 result = future.get();
				scores.append(result.score + ",");
				tiles.append(result.tile + ",");
				times.append(result.time + ",");
				results[i] = result.score;
				totalScore += result.score;
				highScore = Math.max(highScore, results[i]);
				int ht = result.tile;
				highTile = Math.max(highTile, ht);
				highTiles[Integer.numberOfTrailingZeros(ht)-1]++;
				System.out.println((i++) + " " + result);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			scores.append('\n');
			scores.close();
			tiles.append('\n');
			tiles.close();
			times.append('\n');
			times.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		standardDeviation = calculateStandardDeviation();
		System.out.println(System.currentTimeMillis() - start);
	}

	private double calculateStandardDeviation() {
		mean = totalScore / nGames;
		double sumSqDiff = 0.0;
		for(int i = 0 ; i < nGames ; i++) {
			double diff = results[i] - mean;
			sumSqDiff += diff * diff;
		}
		return Math.sqrt(sumSqDiff / nGames);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(  "100 games " + player.getClass().getSimpleName());
		sb.append(  "\nMean:               " + mean);
		sb.append("\nStandard deviation: " + standardDeviation);
		sb.append("\nHighest score:      " + highScore);
		sb.append("\nHighest tile:       " + highTile);
		sb.append("\nTile counts:        |");
		sb.append("\nGrade: " + calculateGrade());
		for(int t : highTiles) {
			sb.append(t + "|");
		}
		return sb.toString();
	}
	
	private String calculateGrade() {
		double grade = 0;
		if (mean < 2000) {
			return String.valueOf(grade);
		}
		else if (mean < 22000) {
			grade = (mean-2000)/286;
			return String.valueOf(grade);
		}
		grade = ((mean-22000)/1200)+70;
		return String.valueOf(grade);
	}

	public static void main (String[] args) {
		for(Player p : Controller.getAvailableInstances(Player.class)) {
//		for(Player p : new Player[]{new BaselinePlayer()}) {
			System.out.println(p.getClass().getSimpleName());
			MultiStatistics s = new MultiStatistics(100, p);
			s.begin();
			System.out.println(s);
		}
	}
}

package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;

public class AlphaBetaTurn {

	private static final int SEARCH_DEPTH = 1;
	
	private final Logger logger = LoggerFactory.getLogger(AlphaBetaTurn.class);
	private final IGameStateHeuristic heuristic = new WeightedHeuristic();
	private final DFBB search = new DFBB();
	
	private int leafCount;
	private int pruneCount;
	private int playerId;
	
	public Node search(GameContext state, int playerId, List<GameAction> actions) {
		leafCount = 0;
		pruneCount = 0;
		this.playerId = playerId;
		Node root = new Node(null, state, null, playerId);
		NBestQueue plans = planTurn(root);
		
		if(plans.isEmpty()) {
			logger.info("No plan available");
			return null;
		}
		
		Node best = plans.peekTail();
		double bestScore = Double.NEGATIVE_INFINITY;
		double worstScore = Double.POSITIVE_INFINITY;
		for(Node n : plans) {
			double score = alphaBeta(n, SEARCH_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
			n.score = score;
			if(score > bestScore) {
				bestScore = score;
				best = n;
			}
			if(score < worstScore) {
				worstScore = score;
			}
		}
		
		logger.info("Best: {}, Worst: {}, leafs: {}, pruned: {}", bestScore, worstScore, leafCount, pruneCount);
		return best;
	}
	
	/**
	 * Plan the next turn for the current player.
	 * Produce n candidate outcomes 
	 * @param node
	 * @return
	 */
	private NBestQueue planTurn(Node node) {
	
		int playerId = node.state.getActivePlayerId();
		if(node.state.getTurnState() == TurnState.TURN_ENDED) {
			node.state.startTurn(playerId);
		}
		
		List<GameAction> actions = node.state.getValidActions();
		NBestQueue outcomes = search.search(node.state, playerId, actions);
		
		return outcomes;
	}
	
	private double alphaBeta(Node node, int depth, double alpha, double beta, boolean maximize) {
		
		//Bottom of the search tree
		if(depth == 0 || node.state.gameDecided()) {
			
			double h = heuristic.getScore(node.state, this.playerId);
			node.score = h;
			
			//logger.info("Heuristic: {}", h);
			leafCount ++;
			return h;
		}
		
		NBestQueue outcomes = planTurn(node);
		
		double score;
		if(maximize) {
			score = Double.NEGATIVE_INFINITY;
			for(Node n : outcomes) {
				score = Math.max(score, alphaBeta(n, depth-1, alpha, beta, !maximize));
				alpha = Math.max(alpha, score);
				n.score = score;
				if(beta <= alpha) {
					//logger.info("Beta cutoff");
					pruneCount ++;
					break;
				}
			}
			//logger.info("Max: {}", score);
		} else {
			score = Double.POSITIVE_INFINITY;
			for(Node n : outcomes) {
				score = Math.min(score, alphaBeta(n, depth-1, alpha, beta, !maximize));
				beta = Math.min(beta, score);
				if(beta <= alpha) {
					//logger.info("Alpha cutoff");
					pruneCount ++;
					break;					
				}
			}
			//logger.info("Min: {}", score);
		}
		
		return score;
	}
}

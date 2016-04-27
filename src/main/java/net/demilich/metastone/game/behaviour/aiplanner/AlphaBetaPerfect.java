package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.IBehaviour;
import net.demilich.metastone.game.behaviour.TranspositionTable;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;
import net.demilich.metastone.game.cards.Card;

/**
 * Uses perfect knowledge to estimate the game tree and pick the best action
 * Random events can still mess with it
 * @author gudjon
 */
public class AlphaBetaPerfect extends Behaviour {

	private int assignedGC;
	private int nodeCounter;
	private int pruneCounter;
	private int rememberedCounter;
	private int playerId;
	
	private final Logger logger = LoggerFactory.getLogger(AlphaBetaPerfect.class);
	private final IGameStateHeuristic heuristic = new WeightedHeuristic();
	private final HashMap<ActionType, Integer> evaluatedActions = new HashMap<ActionType, Integer>();
	private final TranspositionTable table = new TranspositionTable();
	
	@Override
	public String getName() {
		return "Perfect Knowledge";
	}

	@Override
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		List<Card> discardedCards = new ArrayList<Card>();
		for (Card card : cards) {
			if (card.getBaseManaCost() >= 4) {
				discardedCards.add(card);
			}
		}
		return discardedCards;
	}

	@Override
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		if (validActions.size() == 1) {
			heuristic.onActionSelected(context, player.getId());
			return validActions.get(0);
		}

		// for now, do now evaluate battlecry actions
		if (validActions.get(0).getActionType() == ActionType.BATTLECRY) {
			return validActions.get(context.getLogic().random(validActions.size()));
		}

		if (assignedGC != 0 && assignedGC != context.hashCode()) {
			logger.warn("AI behaviour was used in another context!");
		}

		assignedGC = context.hashCode();
		evaluatedActions.clear();
		table.clear();
		playerId = player.getId();

		GameAction bestAction = validActions.get(0);
		double bestScore = Double.NEGATIVE_INFINITY;
		nodeCounter = 0;
		pruneCounter = 0;
		rememberedCounter = 0;
		
		for (GameAction gameAction : validActions) {
			//logger.info("********************* SIMULATION STARTS *********************");
			Node n = new Node(null, context, gameAction, player.getId());
			double score = alphaBeta(n, 2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
			if (score > bestScore) {
				bestAction = gameAction;
				bestScore = score;
			}
			//logger.info("********************* SIMULATION ENDS, Action {} achieves score {}", gameAction, score);
		}		

		//logger.debug("{} actions in total have been evaluated this turn", totalActionCount);
		//logger.debug("Selecting best action {} with score {}", bestAction, bestScore);
		heuristic.onActionSelected(context, player.getId());

		logger.info("Remembered: {}, Leafs: {}, Pruned: {}, Score {}, Action: {}, ", 
				rememberedCounter, 
				nodeCounter, 
				pruneCounter, 
				bestScore,
				bestAction);
		
		return bestAction;
	}
	
	@Override
	public IBehaviour clone() {
		return new AlphaBetaPerfect();
	}

	private double alphaBeta(Node node, int depth, double alpha, double beta, boolean maximize) {
		//Perform the given action
		GameContext simulation = node.state.clone();
		simulation.getLogic().performGameAction(node.player, node.action);
		
		int currentPlayerId = simulation.getActivePlayerId();
		
		//Check if the action changed the current player
		if(node.action.getActionType() == ActionType.END_TURN) {
			//Start the opponents turn
			simulation.startTurn(currentPlayerId);
			depth -= 1;
			maximize = !maximize;
		}
		
		if (table.known(simulation)) {
			rememberedCounter ++;
			double score = table.getScore(simulation);
			node.score = score;
			return score;
			// logger.info("GameState is known, has score of {}", score);
		}
		
		//logger.info("Depth: {}", depth);
		//Bottom of the search tree
		if(depth == 0 || simulation.gameDecided()) {
			nodeCounter ++;
			double h = heuristic.getScore(simulation, playerId);
			node.score = h;
			//logger.info("Heuristic: {}", h);
			return h;
		}

		//Recursively traverse the search tree
		List<GameAction> validActions = simulation.getLogic().getValidActions(currentPlayerId);
		if(validActions.size() == 0) {
			logger.info("No actions");
			return 0;
		}
		
		double score;
		if(maximize) {
			score = Double.NEGATIVE_INFINITY;
			for (GameAction gameAction : validActions) {
				Node n = new Node(node, simulation, gameAction, currentPlayerId);
				score = Math.max(score, alphaBeta(n, depth, alpha, beta, maximize));
				alpha = Math.max(alpha, score);
				if(beta <= alpha) {
					//logger.info("Beta cutoff");
					pruneCounter ++;
					break;
				}
			}
		} else {
			score = Double.POSITIVE_INFINITY;
			for (GameAction gameAction : validActions) {
				Node n = new Node(node, simulation, gameAction, currentPlayerId);
				score = Math.min(score, alphaBeta(n, depth, alpha, beta, maximize));
				beta = Math.min(beta, score);
				if(beta <= alpha) {
					//logger.info("Alpha cutoff");
					pruneCounter ++;
					break;					
				}
			}			
		}
		node.score = score;
		table.save(simulation, score);
		return score;
	}
	
}

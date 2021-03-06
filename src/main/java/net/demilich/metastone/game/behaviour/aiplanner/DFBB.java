package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;

public class DFBB {

	private static final int MAX_FRONTIER = 100;
	private static final int MAX_RESULTS = 100;
	
	private final Logger logger = LoggerFactory.getLogger(DFBB.class);
	private final IGameStateHeuristic heuristic = new WeightedHeuristic();
	//private final IGameStateHeuristic heuristic = new SimpleHeuristic();
	
	public NBestQueue search(GameContext state, int playerId, List<GameAction> actions) {
		Node root = new Node(null, state, null, playerId);
		if(state.getActivePlayerId() != playerId) {
			logger.info("Wrong player");
			return null;
		}
		
		NBestQueue leafs = new NBestQueue(MAX_RESULTS);
		NBestQueue frontier = new NBestQueue(MAX_FRONTIER);
		frontier.add(root);
		
		while(!frontier.isEmpty()) {
			Node node = frontier.popHead();
			
			try {
				List<Node> childNodes = expandNode(node);
				for(Node n : childNodes) {
					double h = heuristic.getScore(n.state, n.player);
					n.score = h;
					
					if(n.action.getActionType() == ActionType.END_TURN) {
						//Goal/Leaf
						leafs.add(n);
					} else {
						frontier.add(n);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return leafs;
	}
	
	private List<Node> expandNode(Node node, List<GameAction> validActions) {
		List<Node> childNodes = new ArrayList<Node>();
		
		for (GameAction gameAction : validActions) {
			if(gameAction.getActionType() == ActionType.DISCOVER) {
				logger.info("Discover: {}", gameAction);
			}
			
			GameContext s = node.state.clone();
			s.getLogic().performGameAction(node.player, gameAction);
			
			Node n = new Node(node, s, gameAction, node.player);
			childNodes.add(n);
		}
		
		return childNodes;		
	}
	private List<Node> expandNode(Node node) {
		List<GameAction> validActions = node.state.getValidActions();
		return expandNode(node, validActions);
	}
}

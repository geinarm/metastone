package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.behaviour.heuristic.WeightedHeuristic;

public class DepthFirst {

	private final IGameStateHeuristic heuristic = new WeightedHeuristic();
	private int leafCount;
	
	public Node search(GameContext state, int playerId, List<GameAction> actions) {
		leafCount = 0;
		
		Node bestNode = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for(GameAction a : actions) {
			Node n = new Node(null, state, a, playerId);
			Node bestLeaf = bestLeaf(n);
			if(bestLeaf.score > bestScore) {
				bestNode = bestLeaf;
				bestScore = bestLeaf.score;
			}
		}
		
		return bestNode;
	}
	
	public int getLeafCount() {
		return leafCount;
	}
	
	public Node bestLeaf(Node root) {
		GameContext state = root.state.clone();
		state.getLogic().performGameAction(root.player, root.action);
		
		if(root.action.getActionType() == ActionType.END_TURN) {
			//Leaf state
			leafCount ++;
			double h = heuristic.getScore(state, root.player);
			root.score = h;
			return root;
		}
		
		List<GameAction> actions = state.getValidActions();
		if(actions.isEmpty()) {
			return root;
		}
		
		Node bestNode = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for(GameAction a : actions) {
			Node n = new Node(root, state, a, root.player);
			Node bestChild = bestLeaf(n);
			if(bestChild.score > bestScore) {
				bestNode = bestChild;
				bestScore = bestChild.score;
			}
		}
		
		return bestNode;
	}
}

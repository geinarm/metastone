package net.demilich.metastone.game.behaviour.aiplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses perfect knowledge to plan future turns for each player
 * Random events can still mess with it
 * @author gudjon
 */
public class TurnPlanner extends Behaviour {

	private int assignedGC;
	private int playerId;
	private List<GameAction> currentPlan;
	private int currentPlanIndex;
	
	private final Logger logger = LoggerFactory.getLogger(TurnPlanner.class);
	private final IGameStateHeuristic heuristic = new WeightedHeuristic();
	private final HashMap<ActionType, Integer> evaluatedActions = new HashMap<ActionType, Integer>();
	private final TranspositionTable table = new TranspositionTable();
	
	@Override
	public String getName() {
		return "Turn Planner";
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
		if(currentPlan != null) {
			if(currentPlanIndex >= currentPlan.size()) {
				logger.info("What is this!");
				GameAction action = currentPlan.get(currentPlan.size()-1);
				currentPlan = null;
				return action;
			}
			GameAction planAction = currentPlan.get(currentPlanIndex);
			boolean valid = validActions.contains(planAction);
			logger.info("I have a plan: {}, {}", valid, planAction);
			if(planAction.getActionType() == ActionType.END_TURN) {
				currentPlan = null;
			}
			if(valid) {
				currentPlanIndex ++;
				return planAction;
			}
		}
		
		// for now, do now evaluate battlecry actions
		if (validActions.get(0).getActionType() == ActionType.BATTLECRY) {
			GameAction action = validActions.get(context.getLogic().random(validActions.size()));
			//logger.info("Battlecry {}", action);
			currentPlan = null;
			return action;
		}
		
		if (validActions.size() == 1) {
			heuristic.onActionSelected(context, player.getId());
			logger.info("Only option {}", validActions.get(0));
			currentPlan = null;
			return validActions.get(0);
		}

		if (assignedGC != 0 && assignedGC != context.hashCode()) {
			logger.warn("AI behaviour was used in another context!");
		}

		assignedGC = context.hashCode();
		evaluatedActions.clear();
		table.clear();
		playerId = player.getId();
		
		//DepthFirst search = new DepthFirst();
		DFBB search = new DFBB();
		Node goalNode = search.search(context, playerId, validActions).popHead();
		logger.info("New Plan, score {}:", goalNode.score);
		List<GameAction> plan = goalNode.getPlan();
		//logger.info("New Plan, leafs {}:", search.getLeafCount());
		for(GameAction a : plan) {
			logger.info("-{}", a);
		}
		
		currentPlan = plan;
		currentPlanIndex = 0;
		GameAction action = plan.get(currentPlanIndex);
		currentPlanIndex ++;
		
		boolean valid = validActions.contains(action);
		if(valid) {
			if(action.getActionType() == ActionType.END_TURN) {
				currentPlan = null;
			}
			return action;
		} else {
			currentPlan = null;
			return validActions.get(0);
		}
	}
	
	@Override
	public IBehaviour clone() {
		return new TurnPlanner();
	}
	
}

package net.demilich.metastone.game.behaviour.aiplanner;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.heuristic.IGameStateHeuristic;
import net.demilich.metastone.game.entities.minions.Minion;

public class SimpleHeuristic implements IGameStateHeuristic {

	@Override
	public double getScore(GameContext context, int playerId) {
		Player player  = context.getPlayer(playerId);
		Player opponent = context.getOpponent(player);
		int numMinion = context.getMinionCount(player);
		int numCards = player.getHand().getCount();
		int sumHealth = 0;
		for(Minion m : player.getMinions()) {
			sumHealth += m.getHp();
		}
		int sumOpHealth = 0;
		for(Minion m : opponent.getMinions()) {
			sumOpHealth += m.getHp();
		}
		int heroHp = player.getHero().getEffectiveHp();
		int opponentHp = opponent.getHero().getEffectiveHp();
		int mana = player.getMana();
		
		return numCards + (sumHealth - sumOpHealth) + (heroHp - opponentHp) - mana;
	}

	@Override
	public void onActionSelected(GameContext context, int playerId) {
	}

}

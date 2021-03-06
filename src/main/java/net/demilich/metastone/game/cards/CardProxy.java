package net.demilich.metastone.game.cards;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.GameNotification;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.nittygrittymvc.Proxy;

public class CardProxy extends Proxy<GameNotification> {

	public final static String NAME = "CardProxy";

	private static Logger logger = LoggerFactory.getLogger(CardProxy.class);

	public CardProxy() {
		super(NAME);
		try {
			loadCards();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void loadCards() throws FileNotFoundException {
		Map<String, CardDesc> cardDesc = new HashMap<String, CardDesc>();
		File folder = new File("./cards");
		if (!folder.exists()) {
			logger.error("/cards directory not found");
			return;
		}

		Collection<File> files = FileUtils.listFiles(folder, new String[] { "json" }, true);
		CardParser cardParser = new CardParser();
		for (File file : files) {
			try {
				CardDesc desc = cardParser.parseCard(file);
				if (cardDesc.containsKey(desc.id)) {
					logger.error("Card id {} is duplicated!", desc.id);
				}
				cardDesc.put(desc.id, desc);
			} catch (Exception e) {
				logger.error("Trouble reading " + file.getName());
				throw e;
			}
		}

		for (CardDesc desc : cardDesc.values()) {
			Card instance = desc.createInstance();
			CardCatalogue.add(instance);
			logger.debug("Adding {} to CardCatalogue", instance);
		}
	}

}

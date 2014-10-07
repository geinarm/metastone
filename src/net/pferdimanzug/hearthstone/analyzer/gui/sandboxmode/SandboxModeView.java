package net.pferdimanzug.hearthstone.analyzer.gui.sandboxmode;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import net.pferdimanzug.hearthstone.analyzer.ApplicationFacade;
import net.pferdimanzug.hearthstone.analyzer.GameNotification;
import net.pferdimanzug.hearthstone.analyzer.game.GameContext;
import net.pferdimanzug.hearthstone.analyzer.gui.playmode.GameBoardView;

public class SandboxModeView extends BorderPane implements EventHandler<ActionEvent> {

	@FXML
	private Button backButton;

	private GameBoardView boardView;
	private ToolboxView toolboxView;
	

	public SandboxModeView() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SandboxModeView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		backButton.setOnAction(this);

		boardView = new GameBoardView();
		setCenter(boardView);
		
		toolboxView = new ToolboxView();
		setRight(toolboxView);
	}

	public void updateSandbox(GameContext context) {
		boardView.updateGameState(context);
	}

	@Override
	public void handle(ActionEvent actionEvent) {
		if (actionEvent.getSource() == backButton) {
			ApplicationFacade.getInstance().sendNotification(GameNotification.MAIN_MENU);
		}
	}

}
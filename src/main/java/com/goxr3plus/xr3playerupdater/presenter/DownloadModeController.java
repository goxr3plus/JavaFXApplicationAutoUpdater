package main.java.com.goxr3plus.xr3playerupdater.presenter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import main.java.com.goxr3plus.xr3playerupdater.application.Main;
import main.java.com.goxr3plus.xr3playerupdater.tools.ActionTool;
import main.java.com.goxr3plus.xr3playerupdater.tools.InfoTool;

public class DownloadModeController extends BorderPane {
	
	//-----------------------------------------------------
	
	@FXML
	private Rectangle rectangle;
	
	@FXML
	private ProgressIndicator progressBar;
	
	@FXML
	private Label progressLabel;
	
	@FXML
	private StackPane failedStackPane;
	
	@FXML
	private Button tryAgainButton;
	
	@FXML
	private Button downloadManually;
	
	// -------------------------------------------------------------
	
	/** The logger. */
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * Constructor.
	 */
	public DownloadModeController() {
		
		// ------------------------------------FXMLLOADER ----------------------------------------
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.FXMLS + "DownloadModeController.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "", ex);
		}
		
	}
	
	/**
	 * Called as soon as .FXML is loaded from FXML Loader
	 */
	@FXML
	private void initialize() {
		
		//-- failedStackPane
		failedStackPane.setVisible(false);
		
		//-- tryAgainButton
		tryAgainButton.setOnAction(a -> {
			Main.restartApplication("XR3PlayerUpdater");
			tryAgainButton.setDisable(true);
		});
		
		//== Download Manually
		downloadManually.setOnAction(a -> ActionTool.openWebSite("https://sourceforge.net/projects/xr3player/"));
		
	}
	
	public ProgressIndicator getProgressBar() {
		return progressBar;
	}
	
	public Label getProgressLabel() {
		return progressLabel;
	}
	
	/**
	 * @return the failedStackPane
	 */
	public StackPane getFailedStackPane() {
		return failedStackPane;
	}
	
	/**
	 * @return the downloadManually
	 */
	public Button getDownloadManually() {
		return downloadManually;
	}
	
}

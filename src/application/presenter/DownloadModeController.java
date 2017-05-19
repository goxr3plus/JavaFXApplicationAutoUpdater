package application.presenter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.tools.InfoTool;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

public class DownloadModeController extends BorderPane {
	
	//-----------------------------------------------------
	
	@FXML
	private Rectangle rectangle;
	
	@FXML
	private ProgressIndicator progressBar;
	
	@FXML
	private Label progressLabel;
	
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
		
	}
	
	public ProgressIndicator getProgressBar() {
		return progressBar;
	}
	
	public Label getProgressLabel() {
		return progressLabel;
	}
	
}

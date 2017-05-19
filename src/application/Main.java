package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.presenter.DownloadModeController;
import application.services.DownloadService;
import application.services.ExportZipService;
import application.tools.ActionTool;
import application.tools.InfoTool;
import application.tools.NotificationType;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
	
	/** The Window of the Application */
	private static Stage window;
	/**
	 * The root of the Application is a StackPane [ Because in future updates i
	 * might add multiple screen layers]
	 */
	private static StackPane applicationStackPane = new StackPane();
	
	/**
	 * This is the folder where the update will take place [ obviously the
	 * parent folder of the application]
	 */
	File updateFolder = new File(InfoTool.getBasePathForClass(Main.class));
	
	/**
	 * The update will be downloaded as a zip folder , this is the prefix name
	 * of the zip folder
	 */
	String foldersNamePrefix;
	
	//The Update to download
	int update;
	
	/**
	 * The name of the application you want to update
	 */
	static String applicationName = "XR3Player";
	
	//--------------------
	
	//Create a change listener
	ChangeListener<? super Number> listener = (observable , oldValue , newValue) -> {
		if (newValue.intValue() == 1)
			exportUpdate();
	};
	//Create a change listener
	ChangeListener<? super Number> listener2 = (observable , oldValue , newValue) -> {
		if (newValue.intValue() == 1)
			packageUpdate();
	};
	
	/**
	 * The Main Mode of the Application
	 */
	public static DownloadModeController downloadMode = new DownloadModeController();
	
	//---------------------------------------------------------------------
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//Check the application parameters
		//I want one parameter -> for example [45] which is the update i want
		List<String> applicationParameters = super.getParameters().getRaw();
		if (!applicationParameters.isEmpty())
			update = Integer.valueOf(applicationParameters.get(0));
		else {
			System.out.println("No Parameters given... Application exiting..");
			System.exit(0);
		}
		
		//AppliationStackPane
		applicationStackPane.getChildren().add(downloadMode);
		
		// --------Window---------
		window = primaryStage;
		window.setTitle(applicationName + " Updater");
		window.setResizable(false);
		window.centerOnScreen();
		window.getIcons().add(InfoTool.getImageFromResourcesFolder("icon.png"));
		window.centerOnScreen();
		window.setOnCloseRequest(exit -> {
			exit.consume();
			System.exit(0);
		});
		
		// Scene
		Scene scene = new Scene(applicationStackPane);
		scene.getStylesheets().add(getClass().getResource(InfoTool.STYLES + InfoTool.APPLICATIONCSS).toExternalForm());
		window.setScene(scene);
		
		//Show
		window.show();
		
		//Update		
		foldersNamePrefix = updateFolder.getAbsolutePath() + File.separator + applicationName + " Update Package " + update;
		
		if (checkPermissions())
			downloadUpdate();
		else {
			
			//Update
			downloadMode.getProgressBar().setProgress(-1);
			downloadMode.getProgressLabel().setText("Please close the updater");
			
			//Show Message
			ActionTool.showNotification("Permission Denied[FATAL ERROR]",
					"Application has no permission to write inside this folder:\n [ " + updateFolder.getAbsolutePath()
							+ " ]\n -> I am working to find a solution for this error\n -> You can download " + applicationName + " manually :) ]",
					Duration.minutes(1), NotificationType.ERROR);
		}
	}
	
	/**
	 * In order to update this application must have READ,WRITE AND CREATE
	 * permissions on the current folder
	 */
	public boolean checkPermissions() {
		
		//Check for permission to Create
		try {
			File sample = new File(updateFolder.getAbsolutePath() + File.separator + "empty123123124122354345436.txt");
			/*
			 * Create and delete a dummy file in order to check file
			 * permissions. Maybe
			 * there is a safer way for this check.
			 */
			sample.createNewFile();
			sample.delete();
		} catch (IOException e) {
			//Error message shown to user. Operation is aborted
			return false;
		}
		
		//Also check for Read and Write Permissions
		return updateFolder.canRead() && updateFolder.canWrite();
	}
	
	/**
	 * Try to download the XR3Player Update
	 */
	private void downloadUpdate() {
		
		if (InfoTool.isReachableByPing("www.google.com")) {
			
			//Download it
			try {
				//Delete the ZIP Folder
				boolean zipDeleted = new File(foldersNamePrefix + ".zip").delete();
				
				//Create the downloadService
				DownloadService downloadService = new DownloadService();
				
				//Add Bindings
				downloadMode.getProgressBar().progressProperty().bind(downloadService.progressProperty());
				downloadMode.getProgressLabel().textProperty().bind(downloadService.messageProperty());
				downloadMode.getProgressBar().progressProperty().addListener(listener);
				
				//Start
				downloadService.startDownload(new URL("https://github.com/goxr3plus/XR3Player/releases/download/V3." + update + "/XR3Player.Update." + update + ".zip"),
						Paths.get(foldersNamePrefix + ".zip"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		} else {
			//Update
			downloadMode.getProgressBar().setProgress(-1);
			downloadMode.getProgressLabel().setText("No internet Connection");
		}
	}
	
	/**
	 * Exports the Update ZIP Folder
	 */
	private void exportUpdate() {
		
		//Create the ExportZipService
		ExportZipService exportZipService = new ExportZipService();
		
		//Remove Listeners
		downloadMode.getProgressBar().progressProperty().removeListener(listener);
		
		//Add Bindings		
		downloadMode.getProgressBar().progressProperty().bind(exportZipService.progressProperty());
		downloadMode.getProgressLabel().textProperty().bind(exportZipService.messageProperty());
		downloadMode.getProgressBar().progressProperty().addListener(listener2);
		
		//Start it
		exportZipService.exportZip(foldersNamePrefix + ".zip", updateFolder.getAbsolutePath());
		
	}
	
	/**
	 * After the exporting has been done i must delete the old update files and
	 * add the new ones
	 */
	private void packageUpdate() {
		
		//Delete the ZIP Folder
		boolean zipDeleted = new File(foldersNamePrefix + ".zip").delete();
		
		//Remove Listeners
		downloadMode.getProgressBar().progressProperty().removeListener(listener2);
		
		//Bindings
		downloadMode.getProgressBar().progressProperty().unbind();
		downloadMode.getProgressLabel().textProperty().unbind();
		
		//Packaging
		downloadMode.getProgressBar().setProgress(-1);
		downloadMode.getProgressLabel().setText("Starting " + applicationName + "...");
		
		//Start the XR3Player
		startTheApplication();
		
	}
	
	/**
	 * Calling this method to start the main Application which is XR3Player
	 * 
	 */
	public static void startTheApplication() {
		
		// Restart XR3Player
		new Thread(() -> {
			String path = InfoTool.getBasePathForClass(Main.class);
			String[] applicationPath = { new File(path + applicationName + ".jar").getAbsolutePath() };
			
			//Show message that application is restarting
			Platform.runLater(() -> ActionTool.showNotification("Starting " + applicationName,
					"Application Path:[ " + applicationPath[0] + " ]\n\tIf this takes more than 10 seconds either the computer is slow or it has failed....", Duration.seconds(25),
					NotificationType.INFORMATION));
			
			try {
				//------------Wait until XR3Player is created
				File XR3Player = new File(applicationPath[0]);
				while (!XR3Player.exists()) {
					Thread.sleep(50);
					System.out.println("Waiting " + applicationName + " Jar to be created...");
				}
				
				System.out.println(applicationName + " Path is : " + applicationPath[0]);
				
				//Create a process builder
				ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath[0]);
				builder.redirectErrorStream(true);
				Process process = builder.start();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				// Wait n seconds
				PauseTransition pause = new PauseTransition(Duration.seconds(10));
				pause.setOnFinished(f -> Platform.runLater(() -> ActionTool.showNotification("Starting " + applicationName + " failed",
						"\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));
				pause.play();
				
				// Continuously Read Output to check if the main application started
				String line;
				while (process.isAlive())
					while ( ( line = bufferedReader.readLine() ) != null) {
						if (line.isEmpty())
							break;
						if (line.contains(applicationName + " Application Started"))
							System.exit(0);
					}
				
			} catch (IOException | InterruptedException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.INFO, null, ex);
				
				// Show failed message
				Platform.runLater(() -> Platform.runLater(() -> ActionTool.showNotification("Starting " + applicationName + " failed",
						"\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));
				
			}
		}, "Start XR3Application Thread").start();
	}
	
	public static void main(String[] args) {
		
		launch(args);
	}
	
}

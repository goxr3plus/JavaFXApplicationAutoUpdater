package main.java.com.goxr3plus.xr3playerupdater.application;

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

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.java.com.goxr3plus.xr3playerupdater.presenter.DownloadModeController;
import main.java.com.goxr3plus.xr3playerupdater.services.DownloadService;
import main.java.com.goxr3plus.xr3playerupdater.services.ExportZipService;
import main.java.com.goxr3plus.xr3playerupdater.tools.ActionTool;
import main.java.com.goxr3plus.xr3playerupdater.tools.InfoTool;
import main.java.com.goxr3plus.xr3playerupdater.tools.NotificationType;

public class Main extends Application {
	
	//================Variables================
	
	/**
	 * This is the folder where the update will take place [ obviously the
	 * parent folder of the application]
	 */
	private File updateFolder = new File(InfoTool.getBasePathForClass(Main.class));
	
	/**
	 * Download update as a ZIP Folder , this is the prefix name of the ZIP
	 * folder
	 */
	private static String foldersNamePrefix;
	
	/** Update to download */
	private static int update;
	
	/** The name of the application you want to update */
	private String applicationName;
	
	//================Listeners================
	
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
	
	//================Services================
	
	DownloadService downloadService;
	ExportZipService exportZipService;
	
	//=============================================
	
	private Stage window;
	private static DownloadModeController downloadMode = new DownloadModeController();
	
	//---------------------------------------------------------------------
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		//Parse Arguments -> I want one parameter -> for example [45] which is the update i want
		List<String> applicationParameters = super.getParameters().getRaw();
		if (!applicationParameters.isEmpty())
			update = Integer.valueOf(applicationParameters.get(0));
		else {
			System.out.println("No arguments given...\nApplication exit.");
			System.exit(0);
		}
		
		//We need this in order to restart the update when it fails
		System.out.println("XR3PlayerUpdater Application Started");
		
		// --------Window---------
		window = primaryStage;
		window.setResizable(false);
		window.centerOnScreen();
		window.getIcons().add(InfoTool.getImageFromResourcesFolder("icon.png"));
		window.centerOnScreen();
		window.setOnCloseRequest(exit -> {
			
			//Check
			if (exportZipService != null && exportZipService.isRunning()) {
				ActionTool.showNotification("Message", "Can not exit right now cause it will corrupt the update", Duration.seconds(5), NotificationType.WARNING);
				exit.consume();
				return;
			}
			
			//Question
			if (!ActionTool.doQuestion("Soore you want to exit " + applicationName + " Updater?", window))
				exit.consume();
			else {
				
				//Delete the ZIP Folder
				deleteZipFolder();
				
				//Exit the application
				System.exit(0);
			}
			
		});
		
		// Scene
		Scene scene = new Scene(downloadMode);
		scene.getStylesheets().add(getClass().getResource(InfoTool.STYLES + InfoTool.APPLICATIONCSS).toExternalForm());
		window.setScene(scene);
		
		//Show
		window.show();
		
		//Start
		prepareForUpdate("XR3Player");
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Prepare for the Update
	 * 
	 * @param applicationName
	 */
	public void prepareForUpdate(String applicationName) {
		this.applicationName = applicationName;
		window.setTitle(applicationName + " Updater");
		
		//FoldersNamePrefix	
		foldersNamePrefix = updateFolder.getAbsolutePath() + File.separator + applicationName + " Update Package " + update;
		
		//Check the Permissions
		if (checkPermissions()) {
			downloadMode.getProgressLabel().setText("Checking permissions");
			downloadUpdate("https://github.com/goxr3plus/XR3Player/releases/download/V3." + update + "/XR3Player.Update." + update + ".zip");
		} else {
			
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
			 * permissions. Maybe there is a safer way for this check.
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
	
	/** Try to download the Update */
	private void downloadUpdate(String downloadURL) {
		
		if (InfoTool.isReachableByPing("www.google.com")) {
			
			//Download it
			try {
				//Delete the ZIP Folder
				deleteZipFolder();
				
				//Create the downloadService
				downloadService = new DownloadService();
				
				//Add Bindings
				downloadMode.getProgressBar().progressProperty().bind(downloadService.progressProperty());
				downloadMode.getProgressLabel().textProperty().bind(downloadService.messageProperty());
				downloadMode.getProgressLabel().textProperty().addListener((observable , oldValue , newValue) -> {
					//Give try again option to the user
					if (newValue.toLowerCase().contains("failed"))
						downloadMode.getFailedStackPane().setVisible(true);
				});
				downloadMode.getProgressBar().progressProperty().addListener(listener);
				window.setTitle("Downloading ( " + this.applicationName + " ) Update -> " + this.update);
				
				//Start
				downloadService.startDownload(new URL(downloadURL), Paths.get(foldersNamePrefix + ".zip"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		} else {
			//Update
			downloadMode.getProgressBar().setProgress(-1);
			downloadMode.getProgressLabel().setText("No internet Connection,please exit...");
			
			//Delete the ZIP Folder
			deleteZipFolder();
			
			//Give try again option to the user
			downloadMode.getFailedStackPane().setVisible(true);
		}
	}
	
	/** Exports the Update ZIP Folder */
	private void exportUpdate() {
		
		//Create the ExportZipService
		exportZipService = new ExportZipService();
		
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
		
		//Remove Listeners
		downloadMode.getProgressBar().progressProperty().removeListener(listener2);
		
		//Bindings
		downloadMode.getProgressBar().progressProperty().unbind();
		downloadMode.getProgressLabel().textProperty().unbind();
		
		//Packaging
		downloadMode.getProgressBar().setProgress(-1);
		downloadMode.getProgressLabel().setText("Starting " + applicationName + "...");
		
		//Delete the ZIP Folder
		deleteZipFolder();
		
		//Start XR3Player
		restartApplication(applicationName);
		
	}
	
	//---------------------------------------------------------------------------------------
	
	/** Calling this method to start the main Application which is XR3Player */
	public static void restartApplication(String appName) {
		
		// Restart XR3Player
		new Thread(() -> {
			String path = InfoTool.getBasePathForClass(Main.class);
			String[] applicationPath = { new File(path + appName + ".jar").getAbsolutePath() };
			
			//Show message that application is restarting
			Platform.runLater(() -> ActionTool.showNotification("Starting " + appName,
					"Application Path:[ " + applicationPath[0] + " ]\n\tIf this takes more than [20] seconds either the computer is slow or it has failed....", Duration.seconds(25),
					NotificationType.INFORMATION));
			
			try {
				
				//Delete the ZIP Folder
				deleteZipFolder();
				
				//------------Wait until Application is created
				File applicationFile = new File(applicationPath[0]);
				while (!applicationFile.exists()) {
					Thread.sleep(50);
					System.out.println("Waiting " + appName + " Jar to be created...");
				}
				
				System.out.println(appName + " Path is : " + applicationPath[0]);
				
				//Create a process builder
				ProcessBuilder builder = new ProcessBuilder("java", "-jar", applicationPath[0], !"XR3PlayerUpdater".equals(appName) ? "" : String.valueOf(update));
				builder.redirectErrorStream(true);
				Process process = builder.start();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				
				// Wait n seconds
				PauseTransition pause = new PauseTransition(Duration.seconds(10));
				pause.setOnFinished(f -> Platform.runLater(() -> ActionTool.showNotification("Starting " + appName + " failed",
						"\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));
				pause.play();
				
				// Continuously Read Output to check if the main application started
				String line;
				while (process.isAlive())
					while ( ( line = bufferedReader.readLine() ) != null) {
						if (line.isEmpty())
							break;
						//This line is being printed when XR3Player Starts 
						//So the AutoUpdater knows that it must exit
						else if (line.contains("XR3Player ready to rock!"))
							System.exit(0);
					}
				
			} catch (IOException | InterruptedException ex) {
				Logger.getLogger(Main.class.getName()).log(Level.INFO, null, ex);
				
				// Show failed message
				Platform.runLater(() -> Platform.runLater(() -> ActionTool.showNotification("Starting " + appName + " failed",
						"\nApplication Path: [ " + applicationPath[0] + " ]\n\tTry to do it manually...", Duration.seconds(10), NotificationType.ERROR)));
				
			}
		}, "Start Application Thread").start();
	}
	
	/**
	 * Delete the ZIP folder from the update
	 * 
	 * @return True if deleted , false if not
	 */
	public static boolean deleteZipFolder() {
		return new File(foldersNamePrefix + ".zip").delete();
	}
	
	public static void main(String[] args) {
		
		launch(args);
	}
	
}

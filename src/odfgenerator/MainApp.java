package odfgenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainApp extends Application {

    public final ODFTemplateProcessor odfTemplateProcessor = ODFTemplateProcessor.getInstance();
    public FileChooser templateChooser;
    public FileChooser dataODSChooser;
    public DirectoryChooser directoryChooser;

    private Stage primaryStage;
    private BorderPane rootLayout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        configureFileChoosers();

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Генератор документов");

        initRootLayout();

        showMainOverview();
    }

    public void initRootLayout() {
        //Отображение корневого элемента интерфейса
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMainOverview() {
        //Отображение главного интерфейса
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MainOverview.fxml"));
            AnchorPane mainOverview = (AnchorPane) loader.load();

            MainOverviewController controller = loader.getController();
            controller.setMainApp(this);
            rootLayout.setCenter(mainOverview);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void configureFileChoosers(){
        //Здесь создаются и конфигурируются окна выбора файлов
        this.templateChooser = new FileChooser();
        this.templateChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        this.templateChooser.getExtensionFilters().addAll(//
                new FileChooser.ExtensionFilter("Все файлы", "*.*"),
                new FileChooser.ExtensionFilter("Текстовые документы OpenDocument (ODT)", "*.odt"));

        this.dataODSChooser = new FileChooser();
        this.dataODSChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        this.dataODSChooser.getExtensionFilters().addAll(//
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Электронная таблица OpenDocument (ODS)", "*.ods"));

        this.directoryChooser = new DirectoryChooser();
        this.directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));


    }

    public Stage getPrimaryStage(){
        return this.primaryStage;
    }
}

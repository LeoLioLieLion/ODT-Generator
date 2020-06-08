package odfgenerator;

import com.independentsoft.office.odf.TextDocument;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class MainOverviewController {
    private MainApp mainApp;

    @FXML
    private TextField templatePathField;
    @FXML
    private TextField saveDirField;
    @FXML
    private TextField docNameField;
    @FXML
    private ChoiceBox<String> indexBox;
    @FXML
    private TableView<List<String>> dataTable;
    @FXML
    private CheckBox firstRowTitlesBox;

    private ObservableList<String> indexList;
    private Map<String, Integer> indexMap;

    public MainOverviewController() {
    }

    @FXML
    private void initialize() {
        dataTable.setPlaceholder(new Label("Данные не загружены"));
        resetIndexList();
        indexBox.setItems(indexList);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void searchTemplate() {
        //Открывается системное окно выбора файла
        //После выбора файла путь к нему записывается в соответствующее поле
        //Также после следующего открытия диалогового окна будет открыта последняя папка
        //Остальные методы с диалоговым окном работают так же
        File templateODT = mainApp.templateChooser.showOpenDialog(mainApp.getPrimaryStage());
        if (templateODT != null) {
            templatePathField.setText(templateODT.getPath());
            mainApp.templateChooser.setInitialDirectory(new File(templateODT.getParent()));
        }
    }

    @FXML
    private void searchSaveDir() {
        File saveDir = mainApp.directoryChooser.showDialog(mainApp.getPrimaryStage());
        if (saveDir != null) {
            saveDirField.setText(saveDir.getPath());
            mainApp.directoryChooser.setInitialDirectory(saveDir);
        }
    }

    @FXML
    private void readData() {
        try {
            File dataODS = mainApp.dataODSChooser.showOpenDialog(mainApp.getPrimaryStage());
            if (dataODS != null) {
                //Перед считыванием данных таблица данных очищается
                dataTable.getColumns().clear();
                boolean firstRowTitles = firstRowTitlesBox.selectedProperty().getValue();
                resetIndexList();
                mainApp.odfTemplateProcessor.readDataFromODS(dataODS.getPath(), firstRowTitles);

                ObservableList<List<String>> observableData = FXCollections.observableArrayList(mainApp.odfTemplateProcessor.data);
                //Если в первой строке таблицы ODS заголовки, они не должны заноситься в таблицу в окне приложения как данные
                if (firstRowTitles) {
                    observableData.remove(0);
                }

                int colCount = observableData.get(0).size();
                for (int i = 0; i < colCount; i++) {
                    String colTitle;
                    if (firstRowTitles) {
                        colTitle = mainApp.odfTemplateProcessor.data.get(0).get(i);
                    } else {
                        colTitle = "Колонка " + (i + 1);
                    }
                    TableColumn col = new TableColumn<>(colTitle);
                    final int colNo = i;

                    //Эта конструкция позволяет отображать данные из двухмерных списков в TableView
                    col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> p) {
                            return new SimpleStringProperty(p.getValue().get(colNo));
                        }
                    });
                    col.setPrefWidth(90);
                    dataTable.getColumns().add(col);
                    indexList.add(colTitle);
                    indexMap.put(colTitle, i);
                }

                dataTable.setItems(observableData);
                indexBox.setItems(indexList);
                mainApp.dataODSChooser.setInitialDirectory(new File(dataODS.getParent()));
            }
        } catch (IndexOutOfBoundsException e) {
            //При передаче некорректного файла пользователю выдается предупреждение
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setHeaderText("Загрузка данных отменена");
            alert.setContentText("Выбранный файл пуст или не является ODS");
            alert.showAndWait();
        }
    }

    @FXML
    private void generateDocuments() {
        Alert alert;
        mainApp.odfTemplateProcessor.setTemplate(templatePathField.getText());

        //Если поле директории для сохранения не указано, фалы будут сохранены в директорию шаблона
        String dirPath;
        if (saveDirField.getText().isBlank()) {
            dirPath = new File(templatePathField.getText()).getParent();
        } else {
            dirPath = saveDirField.getText();
        }
        mainApp.odfTemplateProcessor.setSaveDirectory(dirPath);

        //Если не указано имя для генерируемых шаблонов, будет использоваться имя файла шаблона
        if (docNameField.getText().isBlank()) {
            mainApp.odfTemplateProcessor.setSaveName(new File(templatePathField.getText()).getName().replace(".odt",""));
        } else {
            mainApp.odfTemplateProcessor.setSaveName(docNameField.getText());
        }

        //Выдать предупреждение пользователю в случае, если он не укажет способ индексации готовых документов
        if (indexBox.getValue() == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Способ индексации не выбран");
            alert.setContentText("Выберите нужный способ именования сгенерированных документов");
            alert.showAndWait();
            return;
        }

        try {
            mainApp.odfTemplateProcessor.generateDocuments(indexMap.get(indexBox.getValue()));

            //Уведомление пользователя в случае успешной генерации документов
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успех!");
            alert.setHeaderText("Генерация документов завершена");
            alert.setContentText("Сорхаренные документы сохранены в директории " + dirPath);
            alert.showAndWait();
        } catch (FileNotFoundException e) {
            //Предупредение при передаче некорректного файла шаблона
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Генерация документов отменена");
            alert.setContentText("Проверьте путь к шаблону");
            alert.showAndWait();
        } catch (NullPointerException e) {
            //Предупреждение в случае, если данные для генерации не были загружены
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Генерация документов отменена");
            alert.setContentText("Вы не загрузили данные");
            alert.showAndWait();
        }
    }

    //Метод для повторной генерации содержимого выпадающего списка
    private void resetIndexList() {
        indexList = FXCollections.observableArrayList();
        indexMap = new HashMap<>();
        indexList.add("Пронумеровать");
        indexMap.put("Пронумеровать", -1);
    }
}

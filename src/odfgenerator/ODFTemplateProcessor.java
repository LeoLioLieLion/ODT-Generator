package odfgenerator;

import com.independentsoft.office.odf.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ODFTemplateProcessor {

    public List<List<String>> data;
    private String odtTemplatePath;
    private String saveDirPath;
    private String saveName;
    private boolean firstRowTitles;

    public static class SingletonHolder {
        public static final ODFTemplateProcessor HOLDER_INSTANCE = new ODFTemplateProcessor();
    }

    public static ODFTemplateProcessor getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    //Метод для чтения данных из таблицы ODS
    //Метод получает путь к файлу с таблицей и уточнение, содержит ли первая строка заголовки для колонок
    //Если первая строка содержит заголовки, они в дальнейшем будут отображаться в таблице
    public void readDataFromODS(String path, boolean firstRowTitles) {
        this.data = new ArrayList<>();
        this.firstRowTitles = firstRowTitles;
        try {
            Spreadsheet spreadsheet = new Spreadsheet(path);
            Table sheet = spreadsheet.getTables().get(0);

            for (Row row : sheet.getRows()) {
                List<String> dataRow = new ArrayList<>();
                for (Cell c : row.getCells()) {
                    //В JODF странно работает метод getValue() - числа он считывает, но строки по какой-то причине превращаются в null
                    //Поэтому здесь используется toString(), который выводит XML код ячейки, от которого мы избавляемся
                    String cellValue = c.toString().replaceAll("<.*?>", "");
                    if (c.getType() != CellValueType.NONE) dataRow.add(cellValue);
                }
                if (dataRow.size() > 0) this.data.add(dataRow);
            }
        } catch (Exception e) {
        }
    }

    //Установка пути к файлу шаблона
    public void setTemplate(String templateFilePath) {
        this.odtTemplatePath = templateFilePath;
    }

    //Установка пути к директории для сохранения сгенерированных документов
    public void setSaveDirectory(String directoryPath) {
        this.saveDirPath = directoryPath;
    }

    //Указание общего имени генерируемых документов
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public void generateDocuments(Integer namingIndex) throws FileNotFoundException {
        String namePostfix;
        int rowCount = this.data.size();
        Integer i = 0;

        //Если в первой строке таблицы заголовки, они должны игнорироваться при генерации
        if (firstRowTitles) i++;
        for (; i < rowCount; i++) {
            List<String> dataRow = this.data.get(i);
            //namingIndex соответствует номеру колонки, по которой будут именоваться документы.
            //Если namingIndex равен -1, то готовые документы будут просто нумероваться
            if (namingIndex == -1)
                namePostfix = " - " + i.toString();
            else
                namePostfix = " - " + dataRow.get(namingIndex);
            this.generateDocument(dataRow, this.saveName + namePostfix);
        }
    }

    private void generateDocument(List<String> data, String saveName) throws FileNotFoundException {
        try {
            TextDocument doc = new TextDocument(this.odtTemplatePath);
            for (int i = 0; i < data.size(); i++) {
                //Замена меток по всему тексту документа
                doc.replace("{" + (i + 1) + "}", data.get(i));
            }
            doc.save(saveDirPath + "\\" + saveName + ".odt");
        } catch (Exception e) {
            //При ошибке считывания файла шаблона выбрасывается FileNotFoundException, которое будет перехватываться в контроллере
            throw new FileNotFoundException();
        }
    }
}

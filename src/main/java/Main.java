import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import employee.Employee;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "src/main/data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        Scanner scanner = new Scanner(System.in);


        // Сначала работаем с парсингом CSV-файла
        String json = listToJson(list);
        System.out.println("Введите имя файла для записи JSON-объекта, полученного из CSV-файла:");
        String outputFileName = scanner.nextLine();
        writeString(json, outputFileName);

        // Теперь делаем похожие вещи для парсинга XML-файла
        list = parseXML("data.xml");
        json = listToJson(list);
        System.out.println("Введите имя файла для записи JSON-объекта, полученного из XML-файла:");
        outputFileName = scanner.nextLine();
        writeString(json, outputFileName);

        // Теперь работаем с JSON-файлом
        System.out.println("Данные о сотрудниках, считанные из JSON-файла:");
        json = readString("src/main/data2.json");
        list = jsonToList(json);
        for(Employee emp: list) {
            System.out.println(emp);
        }
    }

    /**
     * Парсинг csv-файла с заданным набором колонок в список сотрудников (объектов типа Employee)
     * @param columnMapping - массив сторок с названиями для столбцов считываемого csv-файла
     * @param fileName - имя считываемого csv-файла
     * @return список объектов типа Employee с данными, считанными из csv-файла
     */
    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff = null;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            staff = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return staff;
    }

    /**
     * Парсинг xml-файла в список сотрудников (объектов типа Employee)
     * @param fileName - имя считываемого csv-файла
     * @return список объектов типа Employee с данными, считанными из xml-файла
     */
    private static List<Employee> parseXML(String fileName) {
        List<Employee> staff = new ArrayList<Employee>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File("src/main/data.xml"));
            Node root = doc.getDocumentElement();
            NodeList nodes = root.getChildNodes();
            Node node = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                if(nodes.item(i).getNodeName().equals("employee")) {
                    staff.add(getNewEmployeeFromNode(nodes.item(i)));
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла!" + e.getMessage());
        }  catch (ParserConfigurationException | SAXException e) {
            System.out.println("Ошибка при распарсивании файла!" + e.getMessage());
        }
        return staff;
    }

    /**
     * Создание нового сотрудника (объекта типа Employee) по переданному узлу xml-файла
     * @param node Узел xml-файла типа "employee"
     * @return Новый объект типа Employee
     */
    private static Employee getNewEmployeeFromNode(Node node) {
        long id = 0;
        String firstName = null;
        String lastName = null;
        String country = null;
        int age = 0;

        NodeList innerNodeChild = node.getChildNodes();
        for (int j = 0; j < innerNodeChild.getLength(); j++) {
            switch (innerNodeChild.item(j).getNodeName()) {
                case "id":
                    id = Integer.parseInt(innerNodeChild.item(j).getTextContent());
                    break;
                case "firstName":
                    firstName = innerNodeChild.item(j).getTextContent();
                    break;
                case "lastName":
                    lastName = innerNodeChild.item(j).getTextContent();
                    break;
                case "country":
                    country = innerNodeChild.item(j).getTextContent();
                    break;
                case "age":
                    age = Integer.parseInt(innerNodeChild.item(j).getTextContent());
                    break;
            }
        }
        return new Employee(id, firstName, lastName, country, age);
    }

    /**
     * Считывание данных из переданного файла в виде строки
     * @param fileName Имя файла для считывания
     * @return Строка со считанными из файла данными
     */
    private static String readString(String fileName) {
        StringBuilder resultString = new StringBuilder("");
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String str;
            while ((str = br.readLine()) != null) {
                resultString.append(str);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл с таким именем не найден!" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка чтения из файла!" + e.getMessage());;
        }
        return resultString.toString();
    }

    /**
     * Получение списка сотрудников (объектов типа Employee) из строки с данными в виде JSON
     * @param json Строка с данными в виде JSON
     * @return список объектов типа Employee с данными, считанными из строки с данными в виде JSON
     */
    private static List<Employee> jsonToList(String json) {
        List<Employee> staff = new ArrayList<Employee>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(json);
            JSONArray jsonArray = (JSONArray) obj;
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Employee employee;
            String jsonText;
            for (Object jsonObject: jsonArray) {
                jsonText = ((JSONObject) jsonObject).toJSONString();
                employee = gson.fromJson(jsonText, Employee.class);
                staff.add(employee);
            }
        } catch (ParseException e) {
            System.out.println("Ошибка распарсивания переданной строки!" + e.getMessage());
        }
        return staff;
    }

    /**
     * Преобразование списка объектов типа Employee в json-строку
     * @param employees - Преобразуемый список объектов типа Employee
     * @return Строка-представление в json-формате исходного списка объектов типа Employee
     */
    private static String listToJson(List<Employee> employees) {
        Type listType = new TypeToken<List<Employee>>() {}.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(employees, listType);
    }

    /**
     * Запись строки в файл
     * @param json Записываемая строка
     * @param outputFileName Имя файла для записи
     */
    private static void writeString(String json, String outputFileName) {
        try(BufferedWriter bfw = new BufferedWriter(new FileWriter(outputFileName))) {
            bfw.write(json);
            bfw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

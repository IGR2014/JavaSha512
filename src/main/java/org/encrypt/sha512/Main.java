package org.encrypt.sha512;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

public class Main extends Application {
    
    private TextArea textArea1 = new TextArea(); // Поле для введення/відображення текстового повідомлення
    private TextArea textArea2 = new TextArea(); // Поле для відображення хеш-функції
    
    private Spinner<Integer> bitSpinner = new Spinner<>(0, 0, 0, 1); // Спінбокс для вибору позиції біту для зміни
    
    private FileChooser fileChooser = new FileChooser(); // Діалог вибору файлу
    
    private Button openButton = new Button("Open File"); // Кнопка для відкриття файлу
    private Button changeBitButton = new Button("Change Bit"); // Кнопка для зміни біту
    
    private LineChart<Number, Number> bitChangeChart = createBitChangeChart(); // Графік для відображення залежності зміни бітів
    
    private byte[] fileBytes = new byte[] {}; // Байти файлу
    private byte[] savedFileBytes = new byte[] {}; // Збережені байти файлу
    
    private byte[] messageDigest = new byte[] {}; // Хеш-функція повідомлення
    private byte[] savedMessageDigest = new byte[] {}; // Збережена хеш-функція повідомлення

    public static void main(String[] args) {
	launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
	
	primaryStage.setTitle("SHA-512");
	
	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt")); // Встановлення фільтра файлів
	
	openButton.setOnAction(e -> {
	    File selectedFile = fileChooser.showOpenDialog(primaryStage); // Діалог відкриття файлу
	    if (selectedFile == null) {
		return;
	    }
	    try {
		fileBytes = Files.readAllBytes(selectedFile.toPath()); // Зчитування байтів файлу
		savedFileBytes = fileBytes.clone(); // Збереження копії байтів файлу
		textArea1.setText(new String(fileBytes)); // Відображення байтів у текстовому полі
		MessageDigest md = MessageDigest.getInstance("SHA-512"); // Створення об'єкта для обчислення хеш-функції SHA-512
		messageDigest = md.digest(fileBytes); // Обчислення хеш-функції
		savedMessageDigest = messageDigest.clone(); // Збереження копії хеш-функції
		StringBuilder hexString = new StringBuilder();
		for (byte b : messageDigest) {
		    hexString.append(String.format("%02X ", b)); // Перетворення хеш-функції у шістнадцятковий формат
		}
		textArea2.setText(hexString.toString()); // Відображення хеш-функції у текстовому полі
		bitSpinner.setValueFactory(
		    new SpinnerValueFactory.IntegerSpinnerValueFactory(
			0,
			fileBytes.length * 8 - 1
		    )
		); // Встановлення діапазону спінбоксу
		updateBitChangeChart(); // Оновлення графіка
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	});
	
	changeBitButton.setOnAction(e -> {
	    int selectedBitIndex = bitSpinner.getValue(); // Отримання вибраної позиції біту
	    if (selectedBitIndex >= 0 && selectedBitIndex < fileBytes.length * 8) {
		int charIndex = selectedBitIndex / 8; // Визначення індексу байту
		int bitOffset = selectedBitIndex % 8; // Визначення зсуву біта
		fileBytes[charIndex] ^= (1 << bitOffset); // Зміна біту в байті
		textArea1.setText(new String(fileBytes)); // Оновлення текстового поля
		try {
		    MessageDigest md = MessageDigest.getInstance("SHA-512"); // Повторне обчислення хеш-функції
		    messageDigest = md.digest(fileBytes);
		    StringBuilder hexString = new StringBuilder();
		    for (byte b : messageDigest) {
			hexString.append(String.format("%02X ", b)); // Перетворення хеш-функції у шістнадцятковий формат
		    }
		    textArea2.setText(hexString.toString()); // Оновлення текстового поля з хеш-функцією
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		updateBitChangeChart(); // Оновлення графіка
	    }
	});
	
	HBox buttonBox = new HBox(10, changeBitButton, bitSpinner); // Горизонтальний контейнер для кнопок
	VBox vbox = new VBox(10, openButton, textArea1, buttonBox, textArea2, bitChangeChart); // Вертикальний контейнер для всіх елементів і графіка
	
	primaryStage.setScene(new Scene(vbox, 800, 600)); // Встановлення сцени для відображення
	primaryStage.show(); // Показ головного вікна
    }
    
    private LineChart<Number, Number> createBitChangeChart() {
	
	NumberAxis xAxis = new NumberAxis();
	NumberAxis yAxis = new NumberAxis();
	xAxis.setLabel("Number of Changed Bits in textArea1"); // Підпис по горизонталі
	yAxis.setLabel("Number of Changed Bits in textArea2"); // Підпис по вертикалі
	
	LineChart<Number, Number> chart = new LineChart<Number, Number>(xAxis, yAxis);
	chart.setTitle("Bit Change Dependency Chart"); // Заголовок графіка
	
	XYChart.Series<Number, Number> series = new XYChart.Series<>();
	series.setName("Bit Change Dependency"); // Підпис для серії
	
	chart.getData().add(series); // Додавання серії до графіка
	
	return chart;
	
    }
    
    private void updateBitChangeChart() {
	
	int changedBits1 = countChangedBits(savedFileBytes, fileBytes); // Рахування кількості змінених бітів у текстовому полі 1
	int changedBits2 = countChangedBits(savedMessageDigest, messageDigest); // Рахування кількості змінених бітів у текстовому полі 2
	
	XYChart.Series<Number, Number> series = bitChangeChart.getData().get(0); // Отримання серії з графіка
	series.getData().add(new XYChart.Data<>(changedBits1, changedBits2)); // Додавання даних до серії
	
    }
    
    private int countChangedBits(byte[] array1, byte[] array2) {
	
	int changedBits = 0;
	if (array1.length == array2.length) {
	    for (int i = 0; i < array1.length; i++) {
		byte byte1 = array1[i];
		byte byte2 = array2[i];
		byte xorResult = (byte) (byte1 ^ byte2); // Використання побітового XOR для порівняння байтів
		for (int j = 0; j < 8; j++) {
		    if ((xorResult & (1 << j)) != 0) { // Порівняння кожного біта
			changedBits++;
		    }
		}
	    }
	}
	return changedBits;
	
    }
}

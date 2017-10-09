package com.mayur.excel.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mayur.excel.util.Splitter;
import com.mayur.pojo.Header;

public class ExcelToSqlMain {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		String fileName = "1420170814_query01_SIT";
		String inputExcel = fileName + ".xlsx";
		String outputFormat = "14outputFormat.txt";
		String outputFile = fileName + ".sql";
		long startTime = System.currentTimeMillis();
		FileInputStream inputExcelStream = new FileInputStream(new File(
				inputExcel));

		FileReader outputFormatStream = new FileReader(new File(outputFormat));

		Map<Header, ArrayList<String>> column = new HashMap<Header, ArrayList<String>>();

		List<Header> keyInHeader = new ArrayList<Header>();

		BufferedReader bufferedReader = new BufferedReader(outputFormatStream);

		int headerPosition = 0;
		String sCurrentLine;

		String finalOutputString = "";
		while ((sCurrentLine = bufferedReader.readLine()) != null) {
			finalOutputString = finalOutputString + sCurrentLine;
		}

		if(false){
		String newFinalOutputString = "";
		finalOutputString = finalOutputString.split("\\(")[0] + "($"
				+ finalOutputString.split("\\(")[1];
		finalOutputString = finalOutputString.split("\\)")[0] + "$)"
				+ finalOutputString.split("\\)")[1];
		for (String str : finalOutputString.split(",")) {
			newFinalOutputString = newFinalOutputString + str + "$,$";
		}
		newFinalOutputString = newFinalOutputString.substring(0,
				newFinalOutputString.length() - 3);

		finalOutputString = newFinalOutputString;
		}
		Workbook workbook = new XSSFWorkbook(inputExcelStream);
		workbook.setForceFormulaRecalculation(true);
		long loadTime = System.currentTimeMillis();
		long totalLoad = loadTime - startTime;
		System.out.println("File Load Time :"
				+ String.format(
						"%02d min, %02d sec",
						TimeUnit.MILLISECONDS.toMinutes(totalLoad),
						TimeUnit.MILLISECONDS.toSeconds(totalLoad)
								- TimeUnit.MINUTES
										.toSeconds(TimeUnit.MILLISECONDS
												.toMinutes(totalLoad))));
		Sheet firstSheet = workbook.getSheetAt(0);

		int noOfRows = firstSheet.getPhysicalNumberOfRows();
		// Now considering that first column is used to map header

		Row firstRow = firstSheet.getRow(0);

		Iterator<Cell> cellIterator = firstRow.cellIterator();

		cellIterator.forEachRemaining(nextCell -> {
			Header header = new Header(nextCell.getColumnIndex(), nextCell
					.getStringCellValue());
			keyInHeader.add(header);
			column.put(header, new ArrayList<String>());
		});

		Iterator<Row> iterator = firstSheet.iterator();
		while (headerPosition >= 0) {
			iterator.next();
			headerPosition--;
		}
		//new Splitter(inputExcel, 5000, "", fileName+"1");
		
		iterator.forEachRemaining(nextRow -> {
			Iterator<Cell> currentCellIterator = nextRow.cellIterator();
			currentCellIterator.forEachRemaining(currentCell -> {
				column.forEach((key, value) -> {
					if (currentCell.getColumnIndex() == key.getPosition()) {
						switch (currentCell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							column.get(key).add(
									currentCell.getStringCellValue());
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							column.get(key).add(
									String.valueOf(currentCell
											.getBooleanCellValue()));
							break;
						case Cell.CELL_TYPE_NUMERIC:
							Double doubleOfCurrentCell = new Double(currentCell
									.getNumericCellValue());
							// for now converting numeric(double etc.) to
							// integer and then to String
							column.get(key).add(
									String.valueOf(doubleOfCurrentCell
											.intValue()));
							break;

						}
					}
				});
			});
		});

		List<String> outputResult = new ArrayList<String>();
		for (int i = 2; i <= noOfRows; i++) {
			String finalOutputStringItem = new String(finalOutputString);

			final int representI = i;
			Optional<String> stream = column
					.entrySet()
					.stream()
					.map(item -> {
						String finalOutputStringItemInner = finalOutputStringItem;
						for (Header keyInHeaderItem : keyInHeader) {
							Pattern p = Pattern.compile("\\$"
									+ keyInHeaderItem.getHeaderName() + "\\$");
							Matcher m = p.matcher(finalOutputStringItemInner);
							while (m.find()) {
								if (column.get(keyInHeaderItem).size() > (representI - 2)) {
									boolean isDate = false;
									String ele = column.get(keyInHeaderItem)
											.get(representI - 2);
									if (ele.contains("-") || ele.contains("/")) {
										Map<Boolean, Date> returnEle = isThisDateValid(ele);
										Date dEle = returnEle.get(true);
										if (dEle != null) {
											ele = "TO_DATE('" + ele
													+ "', 'DD/MM/YYYY')";
											isDate = true;
										}

									}
									if (isDate)
										finalOutputStringItemInner = m
												.replaceAll("" + ele + "");
									else {
										try {
											finalOutputStringItemInner = m
													.replaceAll("'" + ele + "'");
										} catch (IndexOutOfBoundsException ex) {
											System.out
													.println(ele
															+ "|"
															+ finalOutputStringItemInner);
										}

									}
								} else
									finalOutputStringItemInner = m
											.replaceAll("'" + "'");
							}
						}
						return finalOutputStringItemInner;
					}).findFirst();

			outputResult.add(stream.get());
		}
		if (outputResult != null && !outputResult.isEmpty()) {
			FileWriter fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);

			outputResult.forEach(sqlQuery -> {
				for (String sqlPart : sqlQuery.split(";"))
					try {
						if (sqlPart.length() > 0)
							bw.write(sqlPart + ";" + "\n\n");
					} catch (Exception e) {
						System.out.println("Exception occured while writing:"
								+ e.getMessage());
					}
			});
			bw.close();
			fw.close();
		}
		long endTime = System.currentTimeMillis();
		long millis = endTime - startTime;
		long scriptTime = endTime - loadTime;
		String totalTime = String.format(
				"%02d min, %02d sec",
				TimeUnit.MILLISECONDS.toMinutes(millis),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(millis)));
		 replaceSpecialChar("Mayur & Kalekar # mayur"); 
		System.out.println("Script created in :"
				+ String.format(
						"%02d min, %02d sec",
						TimeUnit.MILLISECONDS.toMinutes(scriptTime),
						TimeUnit.MILLISECONDS.toSeconds(scriptTime)
								- TimeUnit.MINUTES
										.toSeconds(TimeUnit.MILLISECONDS
												.toMinutes(scriptTime))));
		System.out.println("Completed in : " + totalTime);
	}

	public static Map<Boolean, Date> isThisDateValid(String dateToValidate) {
		Map<Boolean, Date> returnObj = new HashMap<Boolean, Date>();
		if (dateToValidate == null) {
			returnObj.put(false, null);
			return returnObj;
		}
		String dateFormat = "dd/MM/YYYY";
		String dateFormat2 = "DD-MM-YYYY";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		sdf.setLenient(false);

		SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
		sdf2.setLenient(false);

		try {

			// if not valid, it will throw ParseException
			Date date = (Date) sdf.parse(dateToValidate);
			returnObj.put(true, date);

		} catch (ParseException e) {
			/*
			 * System.out.println("First Format failed[" + dateToValidate +
			 * "]");
			 */
			try {
				Date date = (Date) sdf2.parse(dateToValidate);
				returnObj.put(true, date);
			} catch (ParseException ex) {
				/*
				 * System.out.println("Both Format failed or not a date[" +
				 * dateToValidate + "]");
				 */
				returnObj.put(false, null);
				return returnObj;
			}
		}

		return returnObj;
	}

	public static String replaceSpecialChar(String str) {
		String specialChar[] = { "!", "@", "#", "$", "%", "^", "&", "*", "(",
				")", "-", "+" };
		for (String s : specialChar) {
			if (str.contains(s)) {
				str = str.replace(s, "chr(" + returnChrCode(s) + ")");
			}
		}

		return str;
	}

	public static Integer returnChrCode(String str) {
		Map<String, Integer> specialCha = new HashMap<String, Integer>();
		specialCha.put("!", 33);
		specialCha.put("@", 64);
		specialCha.put("#", 35);
		specialCha.put("$", 36);
		specialCha.put("%", 37);
		specialCha.put("^", 94);
		specialCha.put("&", 38);
		specialCha.put("*", 42);
		specialCha.put("(", 40);
		specialCha.put(")", 41);
		specialCha.put("_", 95);
		specialCha.put("+", 43);

		return specialCha.get(str);
	}

	public static boolean isSpecialChar(String str) {
		String specialChar[] = { "!", "@", "#", "$", "%", "^", "&", "*", "(",
				")", "_", "+" };
		for (String s : specialChar) {
			if (str.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public static String SpecialCharFormatter(int strCode) {

		return "'||chr(" + strCode + ")||'";
	}
}

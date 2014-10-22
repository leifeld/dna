package dna;
//Source: http://stackoverflow.com/questions/6100353/extract-dates-from-web-page

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DateExtractor {

	/**
	 * Extract date
	 * 
	 * @return Date object
	 * @throws ParseException 
	 */
	//TODO: Update Warning "date not extractable. Choose 'set date manually'-option"
	public Date extractDate(String text) throws ParseException {
		Date date = null;
		boolean dateFound = false;
		//boolean dateTodayFound = false;

		String year = null;
		String month = null;
		String monthName = null;
		String day = null;
		String hour = null;
		String minute = null;
		String second = null;
		//String ampm = null;

		String regexDelimiter = "[-:\\/.,]";
		String regexDay = "((?:[0-2]?\\d{1})|(?:[3][01]{1}))";
		String regexMonth = "(?:([0]?[1-9]|[1][012])|(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Sept|Oct(?:ober)?|Nov(?:ember)?|Dec(?:ember)?))";
		String regexYear = "((?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3}))";
		String regexHourMinuteSecond = "(?:(?:\\s)((?:[0-1][0-9])|(?:[2][0-3])|(?:[0-9])):([0-5][0-9])(?::([0-5][0-9]))?(?:\\s?(am|AM|pm|PM))?)?";
		String regexEndswith = "(?![\\d])";

		// today
		//String regexToday = "(heute)|(Heute)|(today)|(Today)";

		// DD/MM/YYYY
		String regexDateEuropean =
				regexDay + regexDelimiter + regexMonth + regexDelimiter + regexYear + regexHourMinuteSecond + regexEndswith;

		// MM/DD/YYYY
		String regexDateAmerican =
				regexMonth + regexDelimiter + regexDay + regexDelimiter + regexYear + regexHourMinuteSecond + regexEndswith;

		// YYYY/MM/DD
		String regexDateTechnical =
				regexYear + regexDelimiter + regexMonth + regexDelimiter + regexDay + regexHourMinuteSecond + regexEndswith;

		// see if there are any matches
		Matcher m = checkDatePattern(regexDateEuropean, text);
		if (m.find()) {
			day = m.group(1);
			month = m.group(2);
			monthName = m.group(3);
			year = m.group(4);
			hour = m.group(5);
			minute = m.group(6);
			second = m.group(7);
			//ampm = m.group(8);
			dateFound = true;
		}

		if(!dateFound) {
			m = checkDatePattern(regexDateAmerican, text);
			if (m.find()) {
				month = m.group(1);
				monthName = m.group(2);
				day = m.group(3);
				year = m.group(4);
				hour = m.group(5);
				minute = m.group(6);
				second = m.group(7);
				//ampm = m.group(8);
				dateFound = true;
			}
		}

		if(!dateFound) {
			m = checkDatePattern(regexDateTechnical, text);
			if (m.find()) {
				year = m.group(1);
				month = m.group(2);
				monthName = m.group(3);
				day = m.group(3);
				hour = m.group(5);
				minute = m.group(6);
				second = m.group(7);
				//ampm = m.group(8);
				dateFound = true;
			}
		}

		// construct date object if date was found
		if(dateFound) {
			String dateFormatPattern = "";
			String dayPattern = "";
			String dateString = "";

			if(day != null) {
				dayPattern = "d" + (day.length() == 2 ? "d" : "");
			}

			if(day != null && month != null && year != null) {
				dateFormatPattern = "yyyy MM " + dayPattern;
				dateString = year + " " + month + " " + day;
			} else if(monthName != null) {
				if(monthName.length() == 3) dateFormatPattern = "yyyy MMM " + dayPattern;
				else dateFormatPattern = "yyyy MMMM " + dayPattern;
				dateString = year + " " + monthName + " " + day;
			}

			if(hour != null && minute != null) {
				//TODO: figure out how to handle "am" "pm".
				dateFormatPattern += " hh:mm";
				dateString += " " + hour + ":" + minute;
				if(second != null) {
					dateFormatPattern += ":ss";
					dateString += ":" + second;
				}
			}

			if(!dateFormatPattern.equals("") && !dateString.equals("")) {
				//TODO: Support different locales
				SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern.trim(), Locale.US);
				date = dateFormat.parse(dateString.trim());
			}
		}else{
			//System.err.println("Date not extractable. Use 'set date manually'-option");
			//LB.Add:
			/*
	    	if(!dateFound) {
				m = checkDatePattern(regexToday, text);
				if (m.find()) {
					dateTodayFound = true;
				}
			}
			if(dateTodayFound){
				date = new Date();
			}
			 */
		}
		return date;
	}

	private Matcher checkDatePattern(String regex, String text) {
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		return p.matcher(text);
	}
}

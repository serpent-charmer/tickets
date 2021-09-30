package com.intellitag.tickets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.LongStream;

import com.google.gson.Gson;

public class App {

	public static long datediff(String dateStart, String dateEnd) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.ENGLISH);

		Date d = sdf.parse(dateStart);
		Date d1 = sdf.parse(dateEnd);

		long time = d1.getTime() - d.getTime();

		return time;
	}

	public static double percentile(double amt, long[] vals) {

		double x = amt * (vals.length - 1) + 1;

		int ioffset = (int) Math.floor(x) - 1;

		double rs = vals[ioffset] + (x % 1) * (vals[ioffset + 1] - vals[ioffset]);

		return rs;
	}

	public static void main(String[] args) throws IOException {
		Optional<String> strm = Files.lines(Paths.get("tickets.json")).reduce((v, v1) -> v + v1);

		String jstr = strm.get();
		Gson gson = new Gson();

		@SuppressWarnings("unchecked")
		Map<String, ArrayList<Map<String, Object>>> obj = gson.fromJson(jstr, Map.class);

		ArrayList<Map<String, Object>> lst = obj.get("tickets");

		long[] larr = lst.stream().map(s -> {
			try {
				return datediff((String) s.get("departure_time"), (String) s.get("arrival_time"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}).mapToLong(s -> s).sorted().toArray();
		
		double prc = percentile(0.9, larr);
		OptionalDouble avg_time = LongStream.of(larr).average();

		System.out.printf("AvgFlightTime :%11d ms\n90%% Percentile:%11d ms", (long)avg_time.getAsDouble(), (long)prc);
	}
}

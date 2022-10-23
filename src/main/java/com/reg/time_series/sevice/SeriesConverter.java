package com.reg.time_series.sevice;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.AttributeConverter;

/**
 * @author phars
 *
 */
public class SeriesConverter implements AttributeConverter<ArrayList<Long>, String> {

	@Override
	public String convertToDatabaseColumn(ArrayList<Long> attribute) {
		String str = attribute.stream().map(String::valueOf).collect(Collectors.joining(", "));
	 	return str;
	}

	@Override
	public ArrayList<Long> convertToEntityAttribute(String dbData) {
		ArrayList<Long> list = (ArrayList<Long>)Stream.of(dbData.split(","))
				.map(String::trim)
				.map(Long::parseLong)
				.collect(Collectors.toList());
		return list;
	}


}

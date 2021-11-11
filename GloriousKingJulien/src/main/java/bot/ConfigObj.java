package bot;

import java.util.ArrayList;
import java.util.Arrays;

public class ConfigObj {
	ConfigType type;
	String data;

	ConfigObj(ConfigType type, String data) {
		this.type = type;
		switch (type) {
		case ARRAY_STRING: {
			data = data.substring(data.indexOf('{') + 1, data.lastIndexOf('}'));
			break;
		}
		case ARRAY_LONG: {
			data = data.substring(data.indexOf('[') + 1, data.lastIndexOf(']'));
			break;
		}
		case STRING:
			data = data.substring(data.indexOf('"') + 1, data.lastIndexOf('"'));
			break;
		default:
			data = data.replace(" ", "");
			break;
		}
		this.data = data;
	}

	public ArrayList<String> readStringArray() {
		if (!type.equals(ConfigType.ARRAY_STRING)) {
			throw new IllegalArgumentException(
					"The type was " + type + " but it should have been " + ConfigType.ARRAY_STRING);
		}
		ArrayList<String> result = new ArrayList<>();
		int current_pointer = 0;
		int start_pointer = 0;
		boolean first = false;

		while (current_pointer < data.length()) {
			if (data.charAt(current_pointer) == '"') {
				if (!first) {
					first = true;
					start_pointer = current_pointer + 1;
				} else {
					first = false;
					result.add(data.substring(start_pointer, current_pointer));
				}
			}
			current_pointer++;
		}
		return result;
	}

	public long[] readLongArray() {
		if (!type.equals(ConfigType.ARRAY_LONG)) {
			throw new IllegalArgumentException(
					"The type was " + type + " but it should have been " + ConfigType.ARRAY_LONG);
		}
		String[] number_array = data.split(",");
		long[] result = new long[number_array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Long.parseLong(number_array[i].replace(" ", ""));
		}
		return result;
	}

	public String readString() {
		if (!type.equals(ConfigType.STRING)) {
			throw new IllegalArgumentException(
					"The type was " + type + " but it should have been " + ConfigType.STRING);
		}
		return data;
	}

	public long readLong() {
		if (!type.equals(ConfigType.LONG)) {
			throw new IllegalArgumentException("The type was " + type + " but it should have been " + ConfigType.LONG);
		}
		return Long.parseLong(data);
	}

	@Override
	public String toString() {
		switch (type) {
		case ARRAY_LONG: {
			return Arrays.toString(readLongArray());
		}
		case ARRAY_STRING: {
			String result = "{";
			for (String s : readStringArray()) {
				result += "\"" + s + "\",";
			}
			return result + "}";
		}
		case LONG:
			return "" + readLong();
		case STRING:
			return '"' + readString() + '"';
		}
		// TODO Auto-generated method stub
		return super.toString();
	}
}

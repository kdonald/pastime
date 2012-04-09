package com.pastime.util;

public final class Range {
	
	private final Integer min;
	
	private final Integer max;
	
	public Range(Integer min, Integer max) {
		this.min = min;
		this.max = max;
	}

	public boolean inRange(Integer number) {
		if (min != null) {
			if (number < min) {
				return false;
			}
		}
		if (max != null) {
			if (number > max) {
				return false;
			}
		}
		return true;
	}

	public static Range valueOf(Integer min, Integer max) {
		if (min == null && max == null) {
			return Range.ALWAYS_TRUE;
		}
		return new Range(min, max);
	}
	
	private static final Range ALWAYS_TRUE = new Range(null, null);
	
}
